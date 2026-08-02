package dev.discscout.mobile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

final class PhoneHelperServerTest {
  @Test
  void servesHelperPageWithSessionCode() throws Exception {
    try (var server = new PhoneHelperServer(update -> {})) {
      server.start();
      var response = HttpClient.newHttpClient().send(
          HttpRequest.newBuilder(URI.create(server.localUrl())).GET().build(),
          HttpResponse.BodyHandlers.ofString());

      assertEquals(200, response.statusCode());
      assertTrue(response.body().contains("sessionCodeText"));
      assertTrue(response.body().contains("/qr.png?code="));
      assertTrue(response.body().contains("Use My Location For Tee"));
      assertTrue(response.body().contains("Send Pasted GPS Coordinates"));
    }
  }

  @Test
  void rejectsHelperPageWithoutSessionCode() throws Exception {
    try (var server = new PhoneHelperServer(update -> {})) {
      server.start();
      var response = HttpClient.newHttpClient().send(
          HttpRequest.newBuilder(URI.create("http://localhost:%d/".formatted(server.port()))).GET().build(),
          HttpResponse.BodyHandlers.ofString());

      assertEquals(403, response.statusCode());
      assertTrue(response.body().contains("session link"));
    }
  }

  @Test
  void acceptsMatchingLocationPayload() throws Exception {
    var received = new AtomicReference<PhoneLocationUpdate>();
    try (var server = new PhoneHelperServer(received::set)) {
      server.start();
      var json = """
          {"sessionCode":"%s","latitude":39.7392,"longitude":-104.9903,"accuracyMeters":8.5,"label":"rear phone"}
          """.formatted(server.sessionCode());
      var response = HttpClient.newHttpClient().send(
          HttpRequest.newBuilder(URI.create("http://localhost:%d/api/location".formatted(server.port())))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(json))
              .build(),
          HttpResponse.BodyHandlers.ofString());

      assertEquals(200, response.statusCode());
      assertEquals(39.7392, received.get().coordinate().latitude(), 0.0);
      assertEquals("rear phone", received.get().label());
    }
  }

  @Test
  void rejectsWrongSessionCode() throws Exception {
    try (var server = new PhoneHelperServer(update -> {})) {
      server.start();
      var response = HttpClient.newHttpClient().send(
          HttpRequest.newBuilder(URI.create("http://localhost:%d/api/location".formatted(server.port())))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString("{\"sessionCode\":\"000000\",\"latitude\":1,\"longitude\":2}"))
              .build(),
          HttpResponse.BodyHandlers.ofString());

      assertEquals(403, response.statusCode());
    }
  }

  @Test
  void servesQrCodePngForHelperUrl() throws Exception {
    try (var server = new PhoneHelperServer(update -> {})) {
      server.start();
      var response = HttpClient.newHttpClient().send(
          HttpRequest.newBuilder(URI.create("http://localhost:%d/qr.png?code=%s".formatted(server.port(), server.sessionCode()))).GET().build(),
          HttpResponse.BodyHandlers.ofByteArray());

      assertEquals(200, response.statusCode());
      assertEquals("image/png", response.headers().firstValue("Content-Type").orElse(""));
      assertTrue(response.body().length > 100);
    }
  }

  @Test
  void rejectsQrCodeWithoutSessionCode() throws Exception {
    try (var server = new PhoneHelperServer(update -> {})) {
      server.start();
      var response = HttpClient.newHttpClient().send(
          HttpRequest.newBuilder(URI.create("http://localhost:%d/qr.png".formatted(server.port()))).GET().build(),
          HttpResponse.BodyHandlers.ofString());

      assertEquals(403, response.statusCode());
    }
  }
}
