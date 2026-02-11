package cz.osu.informatika.mapbackend;

import jakarta.persistence.*;
import lombok.Data;
import org.locationtech.jts.geom.Geometry;

@Entity
@Data
public class MapObject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String category;

    @Column(columnDefinition = "geometry(Geometry, 4326)")
    private Geometry geometry;
}
