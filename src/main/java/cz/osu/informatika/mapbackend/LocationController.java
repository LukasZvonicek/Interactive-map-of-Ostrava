package cz.osu.informatika.mapbackend;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class LocationController {

    private final MapObjectRepository repository;

    public LocationController(MapObjectRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/locations")
    public List<Map<String, Object>> getLocationRadius(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam Double radius) {
        return repository.findInRadius(lat, lon, radius)
                .stream()
                .map(obj -> {
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("id", obj.getId());
                    map.put("name",  obj.getName());
                    map.put("category",  obj.getCategory());
                    map.put("lat", obj.getGeometry().getCoordinate().y);
                    map.put("lon", obj.getGeometry().getCoordinate().x);
                    return map;
                }).collect(Collectors.toList());
    }
}
