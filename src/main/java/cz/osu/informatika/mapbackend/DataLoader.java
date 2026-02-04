package cz.osu.informatika.mapbackend;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner initDatabae(EnviromentalPointRepository repository){
        return args -> {
            if (repository.count()==0){
                GeometryFactory gf = new GeometryFactory();

                EnviromentalPoint p1 = new EnviromentalPoint();
                p1.setName("Nová radnice");
                p1.setQualityIndex(85);
                p1.setLocation(gf.createPoint(new Coordinate(18.291,49.841)));

                repository.save(p1);
                System.out.println("--- Testovací data uložena do Docker DB ---");
            }
        };
    }
}
