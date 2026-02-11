package cz.osu.informatika.mapbackend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MapObjectRepository extends JpaRepository<MapObject, Long> {
    // Native query pro PostGIS: ST_DWithin najde body v okruhu metrů
    // ST_Transform a cast na geography zajistí, že radius 500 = 500 metrů
    @Query(value = "SELECT * FROM map_object m WHERE ST_DWithin(m.geometry::geography, ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography, :radius)",
            nativeQuery = true)
    List<MapObject> findInRadius(@Param("lat") Double lat, @Param("lon") Double lon, @Param("radius") Double radius);
}