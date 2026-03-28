package cz.osu.informatika.mapbackend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class LocationController {

    private final MapObjectRepository repository;

    public LocationController(MapObjectRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/locations")
    public Map<String, Object> getLocationsInRadius(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radius) {

        List<MapObject> rawData = repository.findInRadius(lat, lon, radius);

        List<Map<String, Object>> mapData = new ArrayList<>();
        for (MapObject obj : rawData) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", obj.getName());
            m.put("category", obj.getCategory());
            m.put("geometry", obj.getGeometry().toString());
            m.put("lat", obj.getGeometry().getCentroid().getY());
            m.put("lon", obj.getGeometry().getCentroid().getX());
            mapData.add(m);
        }

        Map<String, Map<String, Object>> panelGroups = new HashMap<>();
        for (MapObject obj : rawData) {
            String key = obj.getName() + "|" + obj.getCategory();
            if (!panelGroups.containsKey(key)) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", obj.getName());
                m.put("category", obj.getCategory());
                m.put("lat", obj.getGeometry().getCentroid().getY());
                m.put("lon", obj.getGeometry().getCentroid().getX());
                panelGroups.put(key, m);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("mapData", mapData);
        response.put("panelData", new ArrayList<>(panelGroups.values()));

        return response;
    }

    @GetMapping("/api/search/objects")
    public List<MapObjectDTO> searchObjects(@RequestParam("query") String query) {
        return repository.findByNameContainingIgnoreCase(query).stream()
                .limit(5)
                .map(obj -> new MapObjectDTO(
                        obj.getName(),
                        obj.getCategory(),
                        obj.getGeometry().getCentroid().getY(),
                        obj.getGeometry().getCentroid().getX()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/api/search/address")
    public String searchAddress(@RequestParam("query") String query) {
        try {
            String cleanQuery = query.trim() + ", Ostrava";
            String encodedQuery = URLEncoder.encode(cleanQuery, StandardCharsets.UTF_8.toString());

            String urlString = "https://nominatim.openstreetmap.org/search?format=json&q="
                    + encodedQuery
                    + "&addressdetails=1&limit=5&countrycodes=cz";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Bakalarka-Ostrava-App)");

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }
}