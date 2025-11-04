package com.example.pruebassistemadesastres.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.CoordinateLine;
import com.sothawo.mapjfx.Extent;
import com.sothawo.mapjfx.MapView;
import javafx.application.Platform;
import javafx.scene.paint.Color;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ServicioRutas {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    // Mantén una referencia a la última línea trazada
    private static CoordinateLine rutaActual;

    /** Llama a OSRM y dibuja la ruta siguiendo carreteras. */
    public static void dibujarRutaCarretera(MapView mapView,
                                            List<Coordinate> waypoints,
                                            boolean limpiarPrevias) {
        if (mapView == null || waypoints == null || waypoints.size() < 2) return;

        // OSRM espera lon,lat;lon,lat;...
        String coordsPath = waypoints.stream()
                .map(c -> c.getLongitude() + "," + c.getLatitude())
                .collect(Collectors.joining(";"));

        String urlGeoJson = "https://router.project-osrm.org/route/v1/driving/" + coordsPath
                + "?overview=full&geometries=geojson";

        HttpRequest req = HttpRequest.newBuilder(URI.create(urlGeoJson))
                .header("Accept", "application/json")
                .GET()
                .build();

        HTTP.sendAsync(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenCompose(resp -> {
                    String body = resp.body();
                    List<Coordinate> coords = parsearCoordenadasGeoJson(body);
                    if (coords.size() >= 3) {
                        return java.util.concurrent.CompletableFuture.completedFuture(coords);
                    }
                    // Fallback: probar con polyline6
                    String urlPolyline6 = "https://router.project-osrm.org/route/v1/driving/" + coordsPath
                            + "?overview=full&geometries=polyline6";
                    System.out.println("[OSRM] GeoJSON devolvió " + coords.size() + " puntos. Probando polyline6...");
                    HttpRequest req2 = HttpRequest.newBuilder(URI.create(urlPolyline6))
                            .header("Accept", "application/json")
                            .GET()
                            .build();
                    return HTTP.sendAsync(req2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                            .thenApply(HttpResponse::body)
                            .thenApply(ServicioRutas::parsearCoordenadasPolyline6);
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return List.of();
                })
                .thenAccept(coords -> {
                    System.out.println("[OSRM] puntos recibidos: " + coords.size());
                    if (coords.size() < 2) return;

                    CoordinateLine line = new CoordinateLine(coords)
                            .setVisible(true)
                            .setClosed(false)
                            .setColor(Color.DODGERBLUE)
                            .setWidth(4);

                    Platform.runLater(() -> {
                        if (limpiarPrevias && rutaActual != null) {
                            mapView.removeCoordinateLine(rutaActual);
                            rutaActual = null;
                        }
                        rutaActual = line;
                        mapView.addCoordinateLine(line);

                        Extent ext = calcularExtent(coords);
                        if (ext != null) {
                            mapView.setExtent(ext);
                        }
                    });
                });
    }

    /** Parsea GeoJSON: routes[0].geometry.coordinates -> [[lon,lat], ...] */
    private static List<Coordinate> parsearCoordenadasGeoJson(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            // Asegúrate de que no hubo error de OSRM
            String code = root.path("code").asText("");
            if (!code.isEmpty() && !"Ok".equalsIgnoreCase(code)) {
                System.out.println("[OSRM] code=" + code);
            }
            JsonNode coords = root.path("routes").path(0).path("geometry").path("coordinates");
            List<Coordinate> out = new ArrayList<>();
            if (coords.isArray()) {
                for (JsonNode p : coords) {
                    double lon = p.get(0).asDouble();
                    double lat = p.get(1).asDouble();
                    out.add(new Coordinate(lat, lon));
                }
            }
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** Parsea polyline6: routes[0].geometry -> String y lo decodifica. */
    private static List<Coordinate> parsearCoordenadasPolyline6(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            String code = root.path("code").asText("");
            if (!code.isEmpty() && !"Ok".equalsIgnoreCase(code)) {
                System.out.println("[OSRM] code=" + code);
            }
            String encoded = root.path("routes").path(0).path("geometry").asText("");
            if (encoded.isEmpty()) return List.of();
            return decodePolyline6(encoded);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** Decodificador polyline precision 6 (Mapbox/OSRM). */
    private static List<Coordinate> decodePolyline6(String polyline) {
        List<Coordinate> coords = new ArrayList<>();
        int index = 0, len = polyline.length();
        long lat = 0, lon = 0;
        final int precision = 6;
        final double factor = Math.pow(10, precision);

        while (index < len) {
            long result = 0, shift = 0;
            long b;
            do {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            long dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            result = 0; shift = 0;
            do {
                b = polyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            long dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lon += dlng;

            coords.add(new Coordinate(lat / factor, lon / factor));
        }
        return coords;
    }

    /** Calcula un Extent a partir de la lista de coordenadas. */
    private static Extent calcularExtent(List<Coordinate> coords) {
        if (coords == null || coords.isEmpty()) return null;

        double minLat = coords.stream().map(Coordinate::getLatitude).min(Comparator.naturalOrder()).orElse(0.0);
        double maxLat = coords.stream().map(Coordinate::getLatitude).max(Comparator.naturalOrder()).orElse(0.0);
        double minLon = coords.stream().map(Coordinate::getLongitude).min(Comparator.naturalOrder()).orElse(0.0);
        double maxLon = coords.stream().map(Coordinate::getLongitude).max(Comparator.naturalOrder()).orElse(0.0);

        double padLat = Math.max(0.0005, (maxLat - minLat) * 0.10);
        double padLon = Math.max(0.0005, (maxLon - minLon) * 0.10);

        Coordinate southWest = new Coordinate(minLat - padLat, minLon - padLon);
        Coordinate northEast = new Coordinate(maxLat + padLat, maxLon + padLon);

        return Extent.forCoordinates(southWest, northEast);
    }
}
