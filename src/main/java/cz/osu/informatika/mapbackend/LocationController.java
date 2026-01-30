package cz.osu.informatika.mapbackend;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class LocationController {
    @GetMapping("/api/test-data")
    public List<Map<String, Object>> getTestData() {
        return List.of(
                Map.of("name", "Centrum", "lat", 49.835, "lon", 18.292, "value", 75),
                Map.of("name", "Ostrava-Jih", "lat", 49.777, "lon", 18.248, "value", 50)
        );
    }
}
