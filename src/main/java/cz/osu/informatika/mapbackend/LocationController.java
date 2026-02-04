package cz.osu.informatika.mapbackend;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class LocationController {

    private final EnviromentalPointRepository repository;

    public LocationController(EnviromentalPointRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/locations")
    public List<Map<String, Object>> getAllLocations() {
        return repository.findAll().stream().map(point -> {
            // Vytvoříme mapu explicitně jako Map<String, Object>
            Map<String, Object> map = Map.of(
                    "id", point.getId(),
                    "name", point.getName(),
                    "value", point.getQualityIndex(),
                    "lat", point.getLocation().getY(),
                    "lon", point.getLocation().getX()
            );
            return map;
        }).collect(Collectors.toList());
    }
}
