package dev.discscout.app;

import dev.discscout.course.DiscGolfCourse;
import dev.discscout.course.DiscGolfTee;
import dev.discscout.course.OverpassDiscGolfClient;
import dev.discscout.domain.DiscProfile;
import dev.discscout.domain.DiscWeightClass;
import dev.discscout.domain.GeoPoint;
import dev.discscout.domain.Handedness;
import dev.discscout.domain.MeasurementUncertainty;
import dev.discscout.domain.ThrowInput;
import dev.discscout.domain.ThrowType;
import dev.discscout.domain.TrackingObservation;
import dev.discscout.domain.Wind;
import dev.discscout.domain.WindSource;
import dev.discscout.export.ExportService;
import dev.discscout.persistence.DiscScoutProject;
import dev.discscout.persistence.ProjectStore;
import dev.discscout.physics.FlightSimulator;
import dev.discscout.physics.SimplifiedAerodynamicModel;
import dev.discscout.search.SearchRoute;
import dev.discscout.search.SearchRouteGenerator;
import dev.discscout.simulation.SimulationOutcome;
import dev.discscout.simulation.MonteCarloSimulator;
import dev.discscout.video.VideoMetadataReader;
import dev.discscout.weather.OpenMeteoWindClient;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public final class DiscScoutApplication extends Application {
  private static final String[] STEPS = {
      "Setup", "Video", "Mark Disc", "Wind", "Estimate", "Search"
  };

  private final ProjectStore projectStore = new ProjectStore();
  private final ExportService exportService = new ExportService();
  private final SearchRouteGenerator routeGenerator = new SearchRouteGenerator();
  private final VideoMetadataReader videoMetadataReader = new VideoMetadataReader();
  private final OpenMeteoWindClient windClient = new OpenMeteoWindClient();
  private final OverpassDiscGolfClient courseClient = new OverpassDiscGolfClient();
  private StartupContext startupContext;
  private DiscScoutProject project = sampleProject();
  private Path projectDir = Path.of("projects", "sample");
  private SimulationOutcome.Success lastOutcome;
  private SearchRoute lastRoute;

  private TextArea status;
  private ListView<String> stepList;
  private StackPane stepContent;
  private Label resultSummary;
  private Label videoEmpty;
  private Label windSummary;
  private Label courseSummary;
  private boolean attemptedWindFetch;
  private WebEngine mapEngine;
  private TextField latitude;
  private TextField longitude;
  private TextField bearing;
  private Slider speed;
  private Slider launch;
  private Slider hyzer;
  private TextField windSpeed;
  private TextField windDirection;
  private TextField windGust;
  private ComboBox<DiscGolfCourse> nearbyCourses;
  private ComboBox<DiscGolfTee> nearbyTees;
  private ComboBox<DiscProfile> disc;
  private ComboBox<DiscWeightClass> discWeight;
  private ComboBox<ThrowType> throwType;
  private ComboBox<Handedness> handedness;
  private final TableView<TrackingObservation.ManualPoint> trackingTable = new TableView<>();
  private MediaPlayer mediaPlayer;
  private StackPane videoPane;
  private Pane trackingOverlay;
  private Label markSummary;

  @Override
  public void start(Stage stage) throws Exception {
    startupContext = new StartupLoader().load();
    initializeInputs();
    var root = new BorderPane();
    root.setTop(header());
    root.setCenter(stepper(stage));
    status = new TextArea("What happened\nReady. Open the sample project for the fastest walkthrough, or start at Setup for your own throw.\n" + startupContext.config().message() + "\nJava runtime: " + startupContext.javaVersion());
    status.setEditable(false);
    status.setWrapText(true);
    status.setPrefRowCount(4);
    root.setBottom(status);
    var scene = new Scene(root, 1220, 820);
    scene.getStylesheets().add(getClass().getResource("/dev/discscout/app/discscout.css").toExternalForm());
    stage.setTitle("DiscScout");
    stage.setScene(scene);
    stage.show();
    refreshMap();
  }

  public static void main(String[] args) {
    launch(args);
  }

  private HBox header() {
    var title = new Label("DiscScout");
    title.getStyleClass().add("app-title");
    var subtitle = new Label("guided lost-disc search from phone video");
    subtitle.getStyleClass().add("screen-caption");
    var box = new HBox(12, title, subtitle);
    box.setPadding(new Insets(14));
    box.setAlignment(Pos.CENTER_LEFT);
    return box;
  }

  private BorderPane stepper(Stage stage) {
    stepList = new ListView<>(FXCollections.observableArrayList(STEPS));
    stepList.getStyleClass().add("step-list");
    stepList.setPrefWidth(190);
    stepContent = new StackPane();
    stepContent.getStyleClass().add("step-content");
    var panes = new Node[] {
        recordPane(), importPane(stage), markPane(), windPane(), simulatePane(), searchPane()
    };
    stepList.getSelectionModel().selectedIndexProperty().addListener((obs, old, index) -> {
      stepContent.getChildren().setAll(panes[index.intValue()]);
      if (index.intValue() == 3 && !attemptedWindFetch) fetchWindForCurrentTee();
      if (index.intValue() == 5) refreshMap();
    });
    var shell = new BorderPane(stepContent);
    shell.setLeft(stepList);
    stepList.getSelectionModel().select(0);
    return shell;
  }

  private void showStep(int index) {
    if (stepList != null) {
      stepList.getSelectionModel().select(Math.max(0, Math.min(STEPS.length - 1, index)));
    }
  }

  private Node recordPane() {
    var sample = actionCard("Open Sample Project", "Instant guided demo", "Runs 500 trajectories and jumps to the search map.");
    sample.getStyleClass().add("primary-card");
    sample.setOnAction(e -> {
      project = sampleProject();
      fillInputsFromProject();
      log("Sample walkthrough: loaded synthetic throw data, ran the simulation, and opened Search.");
      runSimulation(false);
      showStep(5);
    });

    var solo = actionCard("Start Solo Search", "One phone is enough", "Record with the normal camera app, then mark what you can see.");
    solo.setOnAction(e -> {
      log("Solo Search started. Record landscape video from 12-18 feet behind the tee, then import it on the Video step.");
      showStep(1);
    });

    var precision = actionCard("Precision Mode Preview", "Use two phones later", "Side-view triangulation is optional and falls back to Solo Mode when geometry is weak.");
    precision.setOnAction(e -> log("Precision Mode remains optional. Complete Solo Search first; it is the submission-safe path."));

    var how = actionCard("Recording Setup", "Stable phone, early flight visible", "Landscape and 60 FPS help, but manual marking keeps the app usable.");
    how.setOnAction(e -> log(recordingInstructions()));

    var title = new Label("Find the search zone, not a fake exact dot");
    title.getStyleClass().add("screen-title");
    var caption = new Label("Follow the steps on the left. The sample path is the fastest way to see the finished experience.");
    caption.getStyleClass().add("screen-caption");
    var cards = new GridPane();
    cards.getStyleClass().add("action-grid");
    cards.add(sample, 0, 0);
    cards.add(solo, 1, 0);
    cards.add(how, 0, 1);
    cards.add(precision, 1, 1);
    var body = new VBox(18, cards, courseFinderPane());
    return page(missionCard("Step 1 of 6", "Choose how you want to start.", "The sample is safest for a quick demo. Solo Search is the simplest real course workflow.", "Open Sample Project, Start Solo Search, or use public OSM course data."), title, caption, body);
  }


  private Node courseFinderPane() {
    courseSummary = new Label("Optional: find public OpenStreetMap course and tee data near the current release coordinate. Device location permission will be added with the phone helper page; this desktop step does not store your location.");
    courseSummary.getStyleClass().add("screen-caption");
    var find = new Button("Find Nearby Courses");
    find.getStyleClass().add("primary-button");
    find.setOnAction(e -> findNearbyCourses());
    var useTee = new Button("Use Selected Tee");
    useTee.setOnAction(e -> useSelectedTee());
    var controls = new HBox(8, find, nearbyCourses, nearbyTees, useTee);
    controls.getStyleClass().add("command-row");
    var box = new VBox(8, screenTitle("Public course lookup"), courseSummary, controls);
    box.getStyleClass().add("lookup-card");
    return box;
  }

  private void findNearbyCourses() {
    final GeoPoint point;
    try {
      point = new GeoPoint(Double.parseDouble(latitude.getText()), Double.parseDouble(longitude.getText()));
    } catch (RuntimeException ex) {
      courseSummary.setText("Open Advanced model details and enter a valid latitude/longitude, or use the sample project first.");
      return;
    }
    courseSummary.setText("Searching public OpenStreetMap disc-golf data near the current coordinate...");
    CompletableFuture
        .supplyAsync(() -> courseClient.nearbyCourses(point, 5_000))
        .thenAccept(courses -> Platform.runLater(() -> {
          nearbyCourses.setItems(FXCollections.observableArrayList(courses));
          if (!courses.isEmpty()) {
            nearbyCourses.getSelectionModel().selectFirst();
            populateTeesForSelectedCourse();
            courseSummary.setText("Found %d public OSM course result(s). Pick the course and tee, then choose Use Selected Tee. OSM data may be incomplete, so you can still correct it manually.".formatted(courses.size()));
          } else {
            nearbyTees.setItems(FXCollections.observableArrayList());
            courseSummary.setText("No mapped public OSM disc-golf tees found nearby. Use map/manual release placement for this course.");
          }
        }))
        .exceptionally(ex -> {
          Platform.runLater(() -> courseSummary.setText("Course lookup failed. Use manual release placement; the rest of DiscScout still works."));
          return null;
        });
  }

  private void populateTeesForSelectedCourse() {
    var selected = nearbyCourses.getSelectionModel().getSelectedItem();
    if (selected == null) {
      nearbyTees.setItems(FXCollections.observableArrayList());
      return;
    }
    nearbyTees.setItems(FXCollections.observableArrayList(selected.tees()));
    if (!selected.tees().isEmpty()) {
      nearbyTees.getSelectionModel().selectFirst();
    }
  }

  private void useSelectedTee() {
    var course = nearbyCourses.getSelectionModel().getSelectedItem();
    var tee = nearbyTees.getSelectionModel().getSelectedItem();
    if (course == null || tee == null) {
      courseSummary.setText("Pick a course and tee first. If none are listed, use manual release placement.");
      return;
    }
    latitude.setText("%.6f".formatted(tee.coordinate().latitude()));
    longitude.setText("%.6f".formatted(tee.coordinate().longitude()));
    var suggestedBearing = course.suggestedBearingFor(tee);
    suggestedBearing.ifPresent(value -> bearing.setText("%.1f".formatted(value)));
    attemptedWindFetch = false;
    refreshMap();
    courseSummary.setText(suggestedBearing.isPresent()
        ? "Using %s at %s. Throw direction filled from matching/nearest basket. You can still correct it in Estimate.".formatted(tee, course.name())
        : "Using %s at %s. Basket direction was not mapped, so keep or edit the throw direction in Estimate.".formatted(tee, course.name()));
  }
  private Node importPane(Stage stage) {
    videoEmpty = new Label("No video yet. Import an MP4/MOV from your phone, or use Open Sample Project from Setup to try DiscScout without your own footage.");
    videoEmpty.getStyleClass().add("empty-title");
    videoPane = new StackPane(videoEmpty);
    videoPane.getStyleClass().add("video-pane");
    configureTrackingOverlay();
    var importVideo = new Button("Import Video");
    importVideo.setOnAction(e -> importVideo(stage));
    var play = new Button("Play");
    play.setOnAction(e -> { if (mediaPlayer != null) mediaPlayer.play(); });
    var pause = new Button("Pause");
    pause.setOnAction(e -> { if (mediaPlayer != null) mediaPlayer.pause(); });
    var next = new Button("Continue To Mark Disc");
    next.getStyleClass().add("primary-button");
    next.setOnAction(e -> showStep(2));
    var tools = new HBox(8, importVideo, play, pause, next);
    tools.getStyleClass().add("command-row");
    var box = new VBox(12,
        missionCard("Step 2 of 6", "Load the phone video.", "The video helps DiscScout estimate speed and uncertainty, but the workflow can continue when footage is imperfect.", "Import Video, then continue to Mark Disc."),
        screenTitle("Load the throw video"),
        screenCaption("Native phone camera recordings are expected. If decoding fails, the project still works with manual observations."),
        screenCaption("After import, DiscScout opens Mark Disc so you can click the visible disc in the video frame."),
        tools);
    box.setPadding(new Insets(18));
    VBox.setVgrow(videoPane, Priority.ALWAYS);
    return box;
  }

  private Node markPane() {
    trackingTable.setItems(FXCollections.observableArrayList(project.trackingPoints));
    if (trackingTable.getColumns().isEmpty()) {
      trackingTable.getColumns().add(column("Frame", p -> Integer.toString(p.frame())));
      trackingTable.getColumns().add(column("X", p -> "%.1f".formatted(p.x())));
      trackingTable.getColumns().add(column("Y", p -> "%.1f".formatted(p.y())));
      trackingTable.getColumns().add(column("Confidence", p -> "%.2f".formatted(p.confidence())));
    }
    var playMark = new Button("Play");
    playMark.setOnAction(e -> { if (mediaPlayer != null) mediaPlayer.play(); });
    var pauseMark = new Button("Pause");
    pauseMark.setOnAction(e -> { if (mediaPlayer != null) mediaPlayer.pause(); });
    var addPoint = new Button("Use Sample Marks");
    addPoint.setOnAction(e -> addTrackingPoint());
    var undo = new Button("Undo Mark");
    undo.setOnAction(e -> undoLastTrackingPoint());
    var delete = new Button("Delete Selected");
    delete.setOnAction(e -> deleteSelectedTrackingPoint());
    var next = new Button("Continue To Wind");
    next.setOnAction(e -> showStep(3));
    var tools = new HBox(8, playMark, pauseMark, addPoint, undo, delete, next);
    tools.getStyleClass().add("command-row");
    markSummary = new Label(markSummaryText());
    markSummary.getStyleClass().add("screen-caption");
    var box = new VBox(12,
        missionCard("Step 3 of 6", "Click the disc in a few frames.", "DiscScout uses these observations to widen or shrink the search area honestly.", "Pause the video, click the disc, then continue to Wind."),
        screenTitle("Mark what you can see"),
        markSummary,
        videoPane,
        trackingTable,
        tools);
    box.setPadding(new Insets(18));
    VBox.setVgrow(trackingTable, Priority.ALWAYS);
    return box;
  }

  private Node windPane() {
    windSummary = new Label("DiscScout will get nearby model wind from the tee coordinate automatically. If weather is unavailable, the estimate still runs with a wider search zone.");
    windSummary.getStyleClass().add("result-summary");

    var windGrid = new GridPane();
    windGrid.getStyleClass().add("input-grid");
    windGrid.setHgap(12);
    windGrid.setVgap(10);
    windGrid.setPadding(new Insets(18));
    addRow(windGrid, 0, "Wind speed m/s", windSpeed);
    addRow(windGrid, 1, "Wind from degrees", windDirection);
    addRow(windGrid, 2, "Gust m/s", windGust);
    var advanced = new TitledPane("Advanced wind override", windGrid);
    advanced.getStyleClass().add("advanced-pane");
    advanced.setExpanded(false);

    var onlineWind = new Button("Refresh Wind Automatically");
    onlineWind.getStyleClass().add("primary-button");
    onlineWind.setOnAction(e -> fetchWindForCurrentTee());
    var noWind = new Button("Use Wider Zone");
    noWind.setOnAction(e -> {
      windSpeed.setText("0.0");
      windGust.setText("0.0");
      windSummary.setText("Weather unavailable or skipped. DiscScout will continue with a calm-wind assumption and a conservative search zone.");
      log("Continuing without weather data. The result should be treated as wider and less certain.");
    });
    var next = new Button("Continue To Estimate");
    next.setOnAction(e -> showStep(4));
    var tools = new HBox(8, onlineWind, noWind, next);
    tools.getStyleClass().add("command-row");
    return page(
        missionCard("Step 4 of 6", "DiscScout gets wind automatically.", "Most players will not know wind speed or direction. DiscScout uses the tee coordinate and clearly labels the source.", "Wait for the automatic lookup, refresh it, or use a wider zone if weather is unavailable."),
        screenTitle("Wind near the tee"),
        screenCaption("Nearby model wind is useful, but not exact fairway wind. Trees, hills, and gusts can still widen the result."),
        new VBox(12, windSummary, advanced, tools));
  }

  private Node simulatePane() {
    var simpleGrid = new GridPane();
    simpleGrid.getStyleClass().add("input-grid");
    simpleGrid.setHgap(12);
    simpleGrid.setVgap(10);
    simpleGrid.setPadding(new Insets(18));
    var s = 0;
    addRow(simpleGrid, s++, "Disc type", disc);
    addRow(simpleGrid, s++, "Disc weight", discWeight);
    addRow(simpleGrid, s++, "Throw style", throwType);
    addRow(simpleGrid, s++, "Handedness", handedness);
    addRow(simpleGrid, s++, "Throw direction", bearing);

    var advancedGrid = new GridPane();
    advancedGrid.getStyleClass().add("input-grid");
    advancedGrid.setHgap(12);
    advancedGrid.setVgap(10);
    advancedGrid.setPadding(new Insets(18));
    var r = 0;
    addRow(advancedGrid, r++, "Release latitude", latitude);
    addRow(advancedGrid, r++, "Release longitude", longitude);
    addRow(advancedGrid, r++, "Release speed m/s", labeledSlider(speed));
    addRow(advancedGrid, r++, "Launch angle degrees", labeledSlider(launch));
    addRow(advancedGrid, r++, "Hyzer/anhyzer degrees", labeledSlider(hyzer));
    var advanced = new TitledPane("Advanced model details", advancedGrid);
    advanced.getStyleClass().add("advanced-pane");
    advanced.setExpanded(false);

    var run = new Button("Estimate Landing Zone");
    run.getStyleClass().add("primary-button");
    run.setOnAction(e -> runSimulation(true));
    var save = new Button("Save Project");
    save.setOnAction(e -> saveProject());
    var tools = new HBox(8, run, save);
    tools.getStyleClass().add("command-row");
    return page(
        missionCard("Step 5 of 6", "Estimate the landing zone.", "DiscScout runs many plausible throws and shows the area to search first, not a guaranteed point.", "Most players only need the simple choices. Open Advanced model details if you want exact numbers."),
        screenTitle("Estimate the landing zone"),
        screenCaption("Choose the disc and throw basics first. Advanced values stay editable, but they are hidden until you need them."),
        new VBox(12, screenCaption("Simple mode"), simpleGrid, advanced, tools));
  }

  private Node searchPane() {
    resultSummary = new Label("Simulation not run yet. Open the sample project or run 500 trajectories to draw the search zone.");
    resultSummary.getStyleClass().add("result-summary");
    var map = new WebView();
    mapEngine = map.getEngine();
    mapEngine.load(getClass().getResource("/dev/discscout/mapping/map.html").toExternalForm());
    var run = new Button("Estimate Again");
    run.setOnAction(e -> runSimulation(false));
    var export = new Button("Export Search Plan");
    export.getStyleClass().add("primary-button");
    export.setOnAction(e -> export());
    var dark = new CheckBox("Dark map");
    dark.setOnAction(e -> mapEngine.executeScript("document.body.classList.toggle('dark', " + dark.isSelected() + ")"));
    var tools = new HBox(8, run, export, dark);
    tools.getStyleClass().add("command-row");
    var box = new VBox(10,
        missionCard("Step 6 of 6", "Search the most likely area first.", "Start with the 80 percent route, but respect hazards and private property.", "Use the route on the map or export a search plan."),
        resultSummary,
        tools,
        map);
    box.setPadding(new Insets(14));
    VBox.setVgrow(map, Priority.ALWAYS);
    return box;
  }

  private void initializeInputs() {
    latitude = field("39.7392");
    longitude = field("-104.9903");
    bearing = field("45");
    speed = slider(4, 35, 22);
    launch = slider(-5, 30, 8);
    hyzer = slider(-25, 25, 0);
    nearbyCourses = new ComboBox<>();
    nearbyCourses.setPromptText("Find courses first");
    nearbyCourses.setPrefWidth(360);
    nearbyCourses.setOnAction(e -> populateTeesForSelectedCourse());
    nearbyTees = new ComboBox<>();
    nearbyTees.setPromptText("Pick a tee");
    nearbyTees.setPrefWidth(220);
    windSpeed = field("2.5");
    windDirection = field("270");
    windGust = field("4.0");
    disc = new ComboBox<>(FXCollections.observableArrayList(DiscProfile.builtIns()));
    disc.getSelectionModel().select(2);
    discWeight = new ComboBox<>(FXCollections.observableArrayList(DiscWeightClass.values()));
    discWeight.getSelectionModel().select(DiscWeightClass.NORMAL);
    throwType = new ComboBox<>(FXCollections.observableArrayList(ThrowType.values()));
    throwType.getSelectionModel().select(ThrowType.BACKHAND);
    handedness = new ComboBox<>(FXCollections.observableArrayList(Handedness.values()));
    handedness.getSelectionModel().select(Handedness.RIGHT);
  }

  private void runSimulation(boolean jumpToSearch) {
    try {
      var input = input();
      var simulator = new MonteCarloSimulator(new FlightSimulator(new SimplifiedAerodynamicModel()));
      var outcome = simulator.run(input, 500, project.simulationSeed);
      if (outcome instanceof SimulationOutcome.Success success) {
        lastOutcome = success;
        lastRoute = routeGenerator.lawnMower(input.releasePoint(), success.probability80(), SearchRouteGenerator.Vegetation.LIGHT_BRUSH.spacingMeters());
        updateSummary(success, lastRoute);
        log("Generated %d valid trajectories. Median search anchor: %.6f, %.6f. Confidence: %s."
            .formatted(success.validTrajectories(), success.medianCoordinate().latitude(), success.medianCoordinate().longitude(), success.confidenceLabel()));
        saveProject();
        refreshMap();
        if (jumpToSearch) showStep(5);
      } else if (outcome instanceof SimulationOutcome.TooFewValidTrajectories failed) {
        log("Simulation failed: " + failed.reason());
      }
    } catch (RuntimeException ex) {
      log("Could not run simulation: " + ex.getMessage());
    }
  }

  private ThrowInput input() {
    return new ThrowInput(
        new GeoPoint(Double.parseDouble(latitude.getText()), Double.parseDouble(longitude.getText())),
        Double.parseDouble(bearing.getText()),
        speed.getValue(),
        launch.getValue(),
        hyzer.getValue(),
        selectedDiscProfile(),
        throwType.getSelectionModel().getSelectedItem(),
        handedness.getSelectionModel().getSelectedItem(),
        new Wind(Double.parseDouble(windSpeed.getText()), Double.parseDouble(windDirection.getText()), Double.parseDouble(windGust.getText()), new WindSource.Manual("player editable input")),
        uncertaintyFromTrackingMarks());
  }



  private MeasurementUncertainty uncertaintyFromTrackingMarks() {
    var marks = trackingTable.getItems().size();
    if (marks < 2) {
      return new MeasurementUncertainty(3.8, 15.0, 7.0, 10.0, 2.5, 25.0, 0.30, 22.0);
    }
    if (marks < 4) {
      return MeasurementUncertainty.soloDefault();
    }
    return new MeasurementUncertainty(1.8, 6.0, 3.5, 5.0, 1.8, 18.0, 0.22, 7.0);
  }
  private DiscProfile selectedDiscProfile() {
    var selected = disc.getSelectionModel().getSelectedItem();
    var weight = discWeight.getSelectionModel().getSelectedItem();
    return (weight == null ? DiscWeightClass.NORMAL : weight).applyTo(selected);
  }

  private void fetchWindForCurrentTee() {
    final GeoPoint point;
    try {
      point = new GeoPoint(Double.parseDouble(latitude.getText()), Double.parseDouble(longitude.getText()));
    } catch (RuntimeException ex) {
      log("Enter a valid release latitude and longitude before getting wind near the tee.");
      return;
    }
    attemptedWindFetch = true;
    if (windSummary != null) {
      windSummary.setText("Getting nearby model wind for the tee...");
    }
    log("Getting nearby model wind for %.5f, %.5f...".formatted(point.latitude(), point.longitude()));
    CompletableFuture
        .supplyAsync(() -> windClient.currentWind(point))
        .thenAccept(result -> Platform.runLater(() -> {
          windSpeed.setText("%.1f".formatted(result.wind().speedMps()));
          windDirection.setText("%.0f".formatted(result.wind().directionFromDegrees()));
          windGust.setText("%.1f".formatted(result.wind().gustMps()));
          var summary = "Nearby wind loaded: %.1f m/s from %.0f degrees, gusting %.1f m/s. Source: %s.".formatted(result.wind().speedMps(), result.wind().directionFromDegrees(), result.wind().gustMps(), result.wind().source().label());
          if (windSummary != null) windSummary.setText(summary);
          log(result.message() + " Source: " + result.wind().source().label());
        }))
        .exceptionally(ex -> {
          Platform.runLater(() -> {
            if (windSummary != null) windSummary.setText("Weather retrieval failed. DiscScout can still estimate with a wider zone, or you can open Advanced wind override.");
            log("Weather retrieval failed. DiscScout can still estimate with a wider zone, or you can open Advanced wind override.");
          });
          return null;
        });
  }
  private void refreshMap() {
    if (mapEngine == null) return;
    var input = input();
    var median = lastOutcome == null ? input.releasePoint() : lastOutcome.medianCoordinate();
    var release = input.releasePoint();
    var script = "window.discScoutSetData && window.discScoutSetData(%s);".formatted(mapJson(release, median, lastOutcome));
    Platform.runLater(() -> {
      try {
        mapEngine.executeScript(script);
      } catch (RuntimeException ignored) {
        mapEngine.getLoadWorker().stateProperty().addListener((obs, old, state) -> {
          if (state == javafx.concurrent.Worker.State.SUCCEEDED) mapEngine.executeScript(script);
        });
      }
    });
  }

  private String mapJson(GeoPoint release, GeoPoint median, SimulationOutcome.Success outcome) {
    var fifty = outcome == null ? 20 : outcome.probability50().majorAxisMeters();
    var eighty = outcome == null ? 40 : outcome.probability80().majorAxisMeters();
    var ninetyFive = outcome == null ? 70 : outcome.probability95().majorAxisMeters();
    var orientation = outcome == null ? 0.0 : outcome.probability80().orientationDegrees();
    var confidence = outcome == null ? "not run" : outcome.confidenceLabel();
    var provider = startupContext.config().mapProvider();
    return """
        {"release":{"lat":%.8f,"lon":%.8f},"median":{"lat":%.8f,"lon":%.8f},"ellipses":[%.2f,%.2f,%.2f],"orientation":%.2f,"confidence":"%s","tileUrl":"%s","attribution":"%s","message":"%s","route":%s}
        """.formatted(release.latitude(), release.longitude(), median.latitude(), median.longitude(), fifty, eighty, ninetyFive, orientation,
        json(confidence), json(provider.tileUrl()), json(provider.attribution()), json(startupContext.config().message()), routeJson());
  }

  private String routeJson() {
    if (lastRoute == null) return "[]";
    var sb = new StringBuilder("[");
    for (var i = 0; i < lastRoute.waypoints().size(); i++) {
      if (i > 0) sb.append(',');
      var point = lastRoute.waypoints().get(i);
      sb.append("{\"lat\":").append(point.latitude()).append(",\"lon\":").append(point.longitude()).append('}');
    }
    return sb.append(']').toString();
  }

  private void updateSummary(SimulationOutcome.Success success, SearchRoute route) {
    if (resultSummary == null) return;
    resultSummary.setText("Search this zone first: median anchor %.5f, %.5f. 80%% route: %s with %d waypoints. Confidence: %s. This is an estimate, not a guaranteed landing point."
        .formatted(success.medianCoordinate().latitude(), success.medianCoordinate().longitude(), route.name(), route.waypoints().size(), success.confidenceLabel()));
  }

  private void export() {
    if (lastOutcome == null || lastRoute == null) {
      log("Run a simulation before exporting.");
      return;
    }
    try {
      exportService.exportGeoJson(projectDir.resolve("exports/search-plan.geojson"), lastOutcome, lastRoute);
      exportService.exportCsv(projectDir.resolve("exports/search-route.csv"), lastRoute);
      exportService.exportPrintableSummary(projectDir.resolve("exports/printable-summary.md"), lastOutcome, lastRoute);
      log("Exports written to " + projectDir.resolve("exports").toAbsolutePath());
    } catch (Exception ex) {
      log("Export failed: " + ex.getMessage());
    }
  }

  private void saveProject() {
    try {
      project.releasePoint = new GeoPoint(Double.parseDouble(latitude.getText()), Double.parseDouble(longitude.getText()));
      project.bearingDegrees = Double.parseDouble(bearing.getText());
      project.trackingPoints = new ArrayList<>(trackingTable.getItems());
      if (Files.notExists(projectDir.resolve("project.json"))) {
        projectDir = projectStore.createProjectDirectory(Path.of("projects"), project.name);
      }
      projectStore.save(projectDir, project);
      log("Project saved to " + projectDir.toAbsolutePath());
    } catch (Exception ex) {
      log("Autosave failed: " + ex.getMessage());
    }
  }


  private void configureTrackingOverlay() {
    trackingOverlay = new Pane();
    trackingOverlay.getStyleClass().add("tracking-overlay");
    trackingOverlay.setPickOnBounds(true);
    trackingOverlay.prefWidthProperty().bind(videoPane.widthProperty());
    trackingOverlay.prefHeightProperty().bind(videoPane.heightProperty());
    trackingOverlay.setOnMouseClicked(event -> addTrackingPointFromClick(event.getX(), event.getY()));
    videoPane.getChildren().add(trackingOverlay);
    videoPane.widthProperty().addListener((obs, old, value) -> renderTrackingOverlay());
    videoPane.heightProperty().addListener((obs, old, value) -> renderTrackingOverlay());
  }

  private void renderTrackingOverlay() {
    if (trackingOverlay == null) return;
    trackingOverlay.getChildren().clear();
    var points = trackingTable.getItems();
    for (var i = 1; i < points.size(); i++) {
      var previous = points.get(i - 1);
      var current = points.get(i);
      var trail = new Line(previous.x(), previous.y(), current.x(), current.y());
      trail.getStyleClass().add("tracking-trail");
      trackingOverlay.getChildren().add(trail);
    }
    for (var i = 0; i < points.size(); i++) {
      var point = points.get(i);
      var marker = new Circle(point.x(), point.y(), 7);
      marker.getStyleClass().add("tracking-marker");
      marker.setMouseTransparent(true);
      trackingOverlay.getChildren().add(marker);
    }
  }

  private void updateMarkSummary() {
    if (markSummary != null) {
      markSummary.setText(markSummaryText());
    }
  }

  private String markSummaryText() {
    var count = trackingTable.getItems().size();
    if (count == 0) {
      return "No disc marks yet. Import a video, pause on a visible frame, and click the disc. Three marks is enough to continue.";
    }
    if (count < 3) {
      return "%d disc mark(s). Add %d more visible mark(s) for a basic estimate.".formatted(count, 3 - count);
    }
    return "%d disc marks. Good enough to estimate; extra marks may improve confidence.".formatted(count);
  }
  private void importVideo(Stage stage) {
    var chooser = new FileChooser();
    chooser.setTitle("Import phone video");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video files", "*.mp4", "*.mov", "*.m4v", "*.avi"));
    File file = chooser.showOpenDialog(stage);
    if (file == null) return;
    try {
      if (mediaPlayer != null) mediaPlayer.dispose();
      mediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
      var mediaView = new MediaView(mediaPlayer);
      mediaView.setPreserveRatio(true);
      mediaView.fitWidthProperty().bind(videoPane.widthProperty());
      mediaView.fitHeightProperty().bind(videoPane.heightProperty());
      videoPane.getChildren().setAll(mediaView, trackingOverlay);
      project.primaryVideo = videoMetadataReader.read(file.toPath());
      renderTrackingOverlay();
      log("Video loaded: %.2f FPS, %dx%d, %.1f seconds. Pause on visible frames and click the disc to add marks."
          .formatted(project.primaryVideo.frameRate(), project.primaryVideo.width(), project.primaryVideo.height(), project.primaryVideo.durationSeconds()));
      showStep(2);
    } catch (RuntimeException ex) {
      log("Video could not be decoded: " + ex.getMessage() + ". Try MP4/H.264 or continue with manual observations.");
    }
  }

  private void addTrackingPoint() {
    var next = trackingTable.getItems().size();
    trackingTable.getItems().add(new TrackingObservation.ManualPoint(next * 5, 320 + next * 18, 220 - next * 8, 0.70));
    updateMarkSummary();
    renderTrackingOverlay();
    log("Sample mark added. Click the video frame for real marks when a video is loaded.");
  }

  private void addTrackingPointFromClick(double x, double y) {
    if (mediaPlayer == null || project.primaryVideo == null) {
      log("Import a video before clicking to mark the disc.");
      return;
    }
    var frame = Math.max(0, (int) Math.round(mediaPlayer.getCurrentTime().toSeconds() * project.primaryVideo.frameRate()));
    trackingTable.getItems().add(new TrackingObservation.ManualPoint(frame, x, y, 0.95));
    updateMarkSummary();
    renderTrackingOverlay();
    log("Marked disc at frame %d. %s".formatted(frame, markSummaryText()));
  }

  private void undoLastTrackingPoint() {
    var items = trackingTable.getItems();
    if (items.isEmpty()) return;
    items.remove(items.size() - 1);
    updateMarkSummary();
    renderTrackingOverlay();
    log("Removed the last disc mark. " + markSummaryText());
  }

  private void deleteSelectedTrackingPoint() {
    var selected = trackingTable.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    trackingTable.getItems().remove(selected);
    updateMarkSummary();
    renderTrackingOverlay();
    log("Deleted the selected disc mark. " + markSummaryText());
  }

  private void fillInputsFromProject() {
    latitude.setText(Double.toString(project.releasePoint.latitude()));
    longitude.setText(Double.toString(project.releasePoint.longitude()));
    bearing.setText(Double.toString(project.bearingDegrees));
    trackingTable.setItems(FXCollections.observableArrayList(project.trackingPoints));
    updateMarkSummary();
    renderTrackingOverlay();
  }

  private DiscScoutProject sampleProject() {
    var p = new DiscScoutProject();
    p.name = "DiscScout Sample Project";
    p.releasePoint = new GeoPoint(39.7392, -104.9903);
    p.bearingDegrees = 42.0;
    p.trackingPoints.add(new TrackingObservation.ManualPoint(105, 301, 260, 0.9));
    p.trackingPoints.add(new TrackingObservation.ManualPoint(110, 344, 238, 0.85));
    p.trackingPoints.add(new TrackingObservation.ManualPoint(115, 389, 221, 0.8));
    return p;
  }

  private Node page(Node mission, Label title, Label caption, Node body) {
    var box = new VBox(14, mission, title, caption, body);
    box.setPadding(new Insets(24));
    var scroll = new ScrollPane(box);
    scroll.setFitToWidth(true);
    scroll.getStyleClass().add("page-scroll");
    return scroll;
  }


  private Label screenTitle(String text) {
    var label = new Label(text);
    label.getStyleClass().add("screen-title");
    return label;
  }

  private Label screenCaption(String text) {
    var label = new Label(text);
    label.getStyleClass().add("screen-caption");
    return label;
  }

  private Node missionCard(String step, String task, String why, String action) {
    var stepLabel = new Label(step);
    stepLabel.getStyleClass().add("mission-step");
    var taskLabel = new Label(task);
    taskLabel.getStyleClass().add("mission-task");
    var whyLabel = new Label(why);
    whyLabel.getStyleClass().add("mission-copy");
    var actionLabel = new Label(action);
    actionLabel.getStyleClass().add("mission-action");
    var card = new VBox(6, stepLabel, taskLabel, whyLabel, actionLabel);
    card.getStyleClass().add("mission-card");
    return card;
  }
  private Button actionCard(String title, String subtitle, String detail) {
    var button = new Button(title + "\n" + subtitle + "\n" + detail);
    button.getStyleClass().add("action-card");
    button.setMaxWidth(Double.MAX_VALUE);
    button.setAlignment(Pos.CENTER_LEFT);
    button.setWrapText(true);
    return button;
  }

  private String json(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private void addRow(GridPane grid, int row, String label, Node node) {
    grid.add(new Label(label), 0, row);
    grid.add(node, 1, row);
  }

  private TextField field(String value) {
    var f = new TextField(value);
    f.setMaxWidth(220);
    return f;
  }

  private Slider slider(double min, double max, double value) {
    var s = new Slider(min, max, value);
    s.setShowTickLabels(true);
    s.setShowTickMarks(true);
    s.setMajorTickUnit((max - min) / 5.0);
    return s;
  }

  private HBox labeledSlider(Slider slider) {
    var value = new Label();
    value.textProperty().bind(slider.valueProperty().asString("%.1f"));
    return new HBox(10, slider, value);
  }

  private TableColumn<TrackingObservation.ManualPoint, String> column(String name, java.util.function.Function<TrackingObservation.ManualPoint, String> value) {
    var c = new TableColumn<TrackingObservation.ManualPoint, String>(name);
    c.setCellValueFactory(data -> new SimpleStringProperty(value.apply(data.getValue())));
    return c;
  }

  private void log(String message) {
    if (status != null) status.appendText("\n" + message);
  }

  private String recordingInstructions() {
    return """
        Place one phone 12-18 feet behind the tee and slightly toward the throwing-arm side.
        Use a tripod, bag, bench, or stable object.
        Record with the ordinary camera app in landscape orientation.
        Use 60 FPS when available and keep release plus early flight visible.
        Precision Mode adds a side-view phone, a six-digit/QR upload flow, and calibration marker guidance after Solo Mode is working.
        """;
  }

  private String privacyText() {
    return """
        DiscScout processes videos and exact coordinates locally by default.
        The predicted area is an estimate, not a guarantee.
        Trees, skips, rolls, water, terrain, and changing wind may move the actual disc.
        Do not enter roads, water, cliffs, private property, or restricted areas.
        Do not use DiscScout as a navigation or emergency-location system.
        Get consent before analyzing footage containing another identifiable person.
        """;
  }
}
