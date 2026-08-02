package dev.discscout.mobile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.discscout.domain.GeoPoint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class PhoneHelperServer implements AutoCloseable {
  private static final int MAX_LOCATION_BODY_BYTES = 8_192;
  private final ObjectMapper mapper = new ObjectMapper();
  private final String sessionCode;
  private final Consumer<PhoneLocationUpdate> locationConsumer;
  private HttpServer server;
  private ExecutorService executor;

  public PhoneHelperServer(Consumer<PhoneLocationUpdate> locationConsumer) {
    this.locationConsumer = locationConsumer;
    this.sessionCode = newSessionCode();
  }

  public void start() {
    if (server != null) {
      return;
    }
    try {
      server = HttpServer.create(new InetSocketAddress("0.0.0.0", 0), 0);
      server.createContext("/", this::handlePage);
      server.createContext("/api/location", this::handleLocation);
      server.createContext("/qr.png", this::handleQr);
      executor = Executors.newVirtualThreadPerTaskExecutor();
      server.setExecutor(executor);
      server.start();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not start phone helper server.", e);
    }
  }

  public String sessionCode() {
    return sessionCode;
  }

  public int port() {
    if (server == null) {
      return -1;
    }
    return server.getAddress().getPort();
  }

  public String localUrl() {
    return "http://localhost:%d/?code=%s".formatted(port(), sessionCode);
  }

  public String networkUrl() {
    try {
      return "http://%s:%d/?code=%s".formatted(InetAddress.getLocalHost().getHostAddress(), port(), sessionCode);
    } catch (IOException e) {
      return localUrl();
    }
  }

  @Override
  public void close() {
    if (server != null) {
      server.stop((int) Duration.ofSeconds(1).toSeconds());
      server = null;
    }
    if (executor != null) {
      executor.close();
      executor = null;
    }
  }

  private void handlePage(HttpExchange exchange) throws IOException {
    if (!"GET".equals(exchange.getRequestMethod())) {
      send(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
      return;
    }
    exchange.getResponseHeaders().set("Referrer-Policy", "no-referrer");
    exchange.getResponseHeaders().set("Permissions-Policy", "camera=(), microphone=()");
    send(exchange, 200, pageHtml(), "text/html; charset=utf-8");
  }

  private void handleQr(HttpExchange exchange) throws IOException {
    if (!"GET".equals(exchange.getRequestMethod())) {
      send(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
      return;
    }
    try {
      var matrix = new QRCodeWriter().encode(networkUrl(), BarcodeFormat.QR_CODE, 240, 240);
      var output = new ByteArrayOutputStream();
      MatrixToImageWriter.writeToStream(matrix, "PNG", output);
      var bytes = output.toByteArray();
      exchange.getResponseHeaders().set("Content-Type", "image/png");
      exchange.getResponseHeaders().set("Cache-Control", "no-store");
      exchange.sendResponseHeaders(200, bytes.length);
      try (var stream = exchange.getResponseBody()) {
        stream.write(bytes);
      }
    } catch (WriterException e) {
      send(exchange, 500, "Could not create QR code.", "text/plain; charset=utf-8");
    }
  }

  private void handleLocation(HttpExchange exchange) throws IOException {
    if (!"POST".equals(exchange.getRequestMethod())) {
      send(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
      return;
    }
    var body = exchange.getRequestBody().readNBytes(MAX_LOCATION_BODY_BYTES + 1);
    if (body.length > MAX_LOCATION_BODY_BYTES) {
      send(exchange, 413, "Location payload is too large.", "text/plain; charset=utf-8");
      return;
    }
    try {
      var node = mapper.readTree(body);
      if (!sessionCode.equals(node.path("sessionCode").asText())) {
        send(exchange, 403, "Session code did not match.", "text/plain; charset=utf-8");
        return;
      }
      var coordinate = new GeoPoint(node.path("latitude").asDouble(), node.path("longitude").asDouble());
      var accuracy = Math.max(0.0, node.path("accuracyMeters").asDouble(-1.0));
      var label = sanitize(node.path("label").asText("phone"));
      locationConsumer.accept(new PhoneLocationUpdate(sessionCode, coordinate, accuracy, label));
      send(exchange, 200, "{\"ok\":true}", "application/json; charset=utf-8");
    } catch (RuntimeException e) {
      send(exchange, 400, "Could not read location payload.", "text/plain; charset=utf-8");
    }
  }

  private String pageHtml() {
    return """
        <!doctype html>
        <html lang=\"en\">
        <head>
          <meta charset=\"utf-8\">
          <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">
          <title>DiscScout Phone Helper</title>
          <style>
            body { margin: 0; font-family: system-ui, sans-serif; background: #f7f8f2; color: #17231c; line-height: 1.45; }
            main { max-width: 36rem; margin: 0 auto; padding: 1rem; }
            .card { background: white; border: 1px solid #c7d6c5; border-radius: 8px; padding: 1rem; margin-block: 1rem; }
            .code { font-size: 2rem; font-weight: 800; letter-spacing: .08em; }
            .qr { display: block; inline-size: 240px; block-size: 240px; max-inline-size: 100%; margin-block: .75rem; border: 1px solid #c7d6c5; }
            .grid { display: grid; gap: .75rem; }
            button, input { font: inherit; min-height: 48px; border-radius: 6px; border: 1px solid #9db49f; padding: .7rem .9rem; box-sizing: border-box; }
            button { background: #2f6f4e; color: white; font-weight: 800; width: 100%; }
            button.secondary { background: #ffffff; color: #183326; }
            label { display: grid; gap: .35rem; font-weight: 700; }
            input { width: 100%; }
            .muted { color: #4b5c50; }
            #status { font-weight: 700; }
          </style>
        </head>
        <body>
          <main>
            <h1>DiscScout Phone Helper</h1>
            <section class=\"card\">
              <div class=\"muted\">Session code</div>
              <div class=\"code\">{{SESSION_CODE}}</div>
              <img class=\"qr\" src=\"/qr.png\" alt=\"QR code for this phone helper page\">
              <p>This page sends your current phone location to the DiscScout app running on this computer. It does not create an account or upload your location anywhere else.</p>
            </section>
            <section class=\"card\">
              <div class=\"grid\">
                <label>Phone label <input id=\"label\" value=\"tee phone\" maxlength=\"40\" autocomplete=\"off\"></label>
                <p class=\"muted\">Use this while standing at the tee/release point. Your browser will ask for location permission.</p>
                <button id=\"send\" type=\"button\">Use My Location For Tee</button>
              </div>
              <hr>
              <div class=\"grid\">
                <p class=\"muted\">If browser location is blocked, paste GPS coordinates from your phone map app.</p>
                <label>Latitude <input id=\"manualLat\" inputmode=\"decimal\" autocomplete=\"off\" placeholder=\"39.7392\"></label>
                <label>Longitude <input id=\"manualLon\" inputmode=\"decimal\" autocomplete=\"off\" placeholder=\"-104.9903\"></label>
                <button class=\"secondary\" id=\"manualSend\" type=\"button\">Send Pasted GPS Coordinates</button>
              </div>
              <p id=\"status\" role=\"status\" aria-live=\"polite\">Waiting.</p>
            </section>
          </main>
          <script>
            const sessionCode = '{{SESSION_CODE}}';
            const status = document.getElementById('status');
            async function sendPayload(payload) {
              const response = await fetch('/api/location', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(payload)
              });
              return response.ok;
            }

            document.getElementById('send').addEventListener('click', () => {
              if (!('geolocation' in navigator)) {
                status.textContent = 'This browser does not support location. Enter the tee manually in DiscScout.';
                return;
              }
              status.textContent = 'Asking for location permission...';
              navigator.geolocation.getCurrentPosition(async position => {
                status.textContent = 'Sending tee location to DiscScout...';
                const payload = {
                  sessionCode,
                  latitude: position.coords.latitude,
                  longitude: position.coords.longitude,
                  accuracyMeters: position.coords.accuracy ?? -1,
                  label: document.getElementById('label').value || 'phone'
                };
                const ok = await sendPayload(payload);
                status.textContent = ok ? 'Location sent. You can return to DiscScout.' : 'DiscScout rejected the location. Check the session code.';
              }, error => {
                status.textContent = 'Location was not shared. You can still enter the tee manually.';
              }, { enableHighAccuracy: true, timeout: 12000, maximumAge: 0 });
            });

            document.getElementById('manualSend').addEventListener('click', async () => {
              const latitude = Number.parseFloat(document.getElementById('manualLat').value);
              const longitude = Number.parseFloat(document.getElementById('manualLon').value);
              if (!Number.isFinite(latitude) || latitude < -90 || latitude > 90 || !Number.isFinite(longitude) || longitude < -180 || longitude > 180) {
                status.textContent = 'Enter a valid latitude and longitude.';
                return;
              }
              status.textContent = 'Sending pasted GPS coordinates to DiscScout...';
              const ok = await sendPayload({
                sessionCode,
                latitude,
                longitude,
                accuracyMeters: -1,
                label: document.getElementById('label').value || 'manual phone GPS'
              });
              status.textContent = ok ? 'Coordinates sent. You can return to DiscScout.' : 'DiscScout rejected the coordinates. Check the session code.';
            });
          </script>
        </body>
        </html>
        """.replace("{{SESSION_CODE}}", sessionCode);
  }

  private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
    var bytes = body.getBytes(StandardCharsets.UTF_8);
    exchange.getResponseHeaders().set("Content-Type", contentType);
    exchange.sendResponseHeaders(status, bytes.length);
    try (var stream = exchange.getResponseBody()) {
      stream.write(bytes);
    }
  }

  private static String sanitize(String value) {
    return value == null ? "phone" : value.replaceAll("[^a-zA-Z0-9 _-]", "").trim();
  }

  private static String newSessionCode() {
    var bytes = new byte[3];
    new SecureRandom().nextBytes(bytes);
    var number = Integer.parseInt(HexFormat.of().formatHex(bytes), 16) % 1_000_000;
    return "%06d".formatted(number);
  }
}