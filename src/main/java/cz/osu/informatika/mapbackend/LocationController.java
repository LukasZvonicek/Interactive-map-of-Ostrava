package cz.osu.informatika.mapbackend;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
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

        // 1. Získání surových dat z databáze
        List<MapObject> rawData = repository.findInRadius(lat, lon, radius);

        // 2. Příprava dat pro MAPU (všechny body, všechny úseky čar)
        List<Map<String, Object>> mapData = new ArrayList<>();
        for (MapObject obj : rawData) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", obj.getName());
            m.put("category", obj.getCategory());
            // Posíláme celou geometrii ve formátu WKT (např. LINESTRING nebo POINT)
            m.put("geometry", obj.getGeometry().toString());
            // Centroid pro případné popisky
            m.put("lat", obj.getGeometry().getCentroid().getY());
            m.put("lon", obj.getGeometry().getCentroid().getX());
            mapData.add(m);
        }

        // 3. Příprava dat pro PANEL (seskupení podle jména a kategorie)
        // Klíčem je kombinace jména a kategorie, aby se nepletly např. zastávka "Škola" a škola "Škola"
        Map<String, Map<String, Object>> panelGroups = new HashMap<>();
        for (MapObject obj : rawData) {
            String key = obj.getName() + "|" + obj.getCategory();

            if (!panelGroups.containsKey(key)) {
                Map<String, Object> m = new HashMap<>();
                m.put("name", obj.getName());
                m.put("category", obj.getCategory());
                // Pro panel stačí souřadnice prvního nalezeného pro vycentrování mapy
                m.put("lat", obj.getGeometry().getCentroid().getY());
                m.put("lon", obj.getGeometry().getCentroid().getX());
                panelGroups.put(key, m);
            }
        }

        // 4. Sestavení finální odpovědi
        Map<String, Object> response = new HashMap<>();
        response.put("mapData", mapData);
        response.put("panelData", new ArrayList<>(panelGroups.values()));

        return response;
    }
}