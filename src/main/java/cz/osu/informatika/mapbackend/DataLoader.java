package cz.osu.informatika.mapbackend;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner initDatabae(ImportService importservice, MapObjectRepository repo){
        return args -> {
            if (repo.count()==0){
                System.out.println("---Databáze je prázdná, zahajuji import dat z GeoJSON souborů ---");

                try{
                    importservice.importGeoJson("src/main/resources/data/cycle/cycle.geojson", "CYKLOSTEZKA");
                    importservice.importGeoJson("src/main/resources/data/parking/parking.geojson", "PARKOVÁNÍ");
                    importservice.importGeoJson("src/main/resources/data/parking_zones/parking_zones.geojson", "PARKOVACÍ ZÓNY");
                    importservice.importGeoJson("src/main/resources/data/public_transport_stops/public_transport_stops.geojson", "ZASTÁVKY MHD");
                    importservice.importGeoJson("src/main/resources/data/schools/schools.geojson", "ŠKOLSKÁ ZAŘÍZENÍ");
                    importservice.importGeoJson("src/main/resources/data/sport_fields/sport_fields.geojson", "SPORTOVIŠTĚ");
                }  catch (IOException e){
                    System.err.println("CHYBA: Nepodařilo se najít nebo přečíst soubor: " + e.getMessage());
                } catch (Exception e){
                    System.err.println("CHYBA: Neočekávaný problém při importu " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("--- Databáze již obsahuje data (počet záznamů: " + repo.count() + "), import přeskočen ---");
            }
        };
    }
}
