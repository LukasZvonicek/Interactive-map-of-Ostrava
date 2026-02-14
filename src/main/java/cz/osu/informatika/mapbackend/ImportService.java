package cz.osu.informatika.mapbackend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.geojson.GeoJsonReader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;

@Service
public class ImportService {

    @PersistenceContext
    private EntityManager entityManager;

    private final MapObjectRepository repository;

    public ImportService(MapObjectRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void importGeoJson(String filePath, String category) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        File file = new File(filePath);

        if (!file.exists()) {
            System.err.println("SOUBOR NENALEZEN: " + filePath);
            return;
        }

        JsonNode root = mapper.readTree(file);
        JsonNode features = root.get("features");

        GeoJsonReader reader = new GeoJsonReader();
        int count = 0;

        System.out.println("Start importu kategorie: " + category);

        for (JsonNode feature : features) {
            try {
                // 1. CHYTRÉ MAPOVÁNÍ NÁZVU
                JsonNode props = feature.get("properties");
                String name = "Neznámý objekt";
                String[] nameFields = {"NAME", "zast_jm", "INFO", "NAZEV", "TRASA"};
                String[] backupFields = {"ULICE"};

                for (String field : nameFields) {
                    if (props.has(field) && !props.get(field).asText().isEmpty()) {
                        name = props.get(field).asText();
                        break;
                    }
                }
                if(name.equals("Neznámý objekt")) {
                    for (String field : backupFields) {
                        if (props.has(field) && !props.get(field).asText().isEmpty()) {
                            name = props.get(field).asText();
                            break;
                        }
                    }
                }

                // 2. ČTENÍ GEOMETRIE
                String geometryJson = feature.get("geometry").toString();
                Geometry geom = reader.read(geometryJson);

                // 3. ODSTRANĚNÍ Z-SOUŘADNICE (zachováno)
                geom.apply(new org.locationtech.jts.geom.CoordinateSequenceFilter() {
                    @Override
                    public void filter(org.locationtech.jts.geom.CoordinateSequence seq, int i) {
                        seq.setOrdinate(i, org.locationtech.jts.geom.CoordinateSequence.Z, Double.NaN);
                    }
                    @Override public boolean isDone() { return false; }
                    @Override public boolean isGeometryChanged() { return true; }
                });

                // --- KLÍČOVÁ ZMĚNA: NATIVNÍ SQL INSERT S TRANSFORMACÍ ---
                // Místo entityManager.persist(obj) použijeme SQL, aby PostGIS mohl souřadnice přepočítat

                String sql;
                if (geom.getCoordinate().x > 5000) {
                    // Data jsou v Mercatoru (3395) -> transformujeme na 4326
                    sql = "INSERT INTO map_object (name, category, geometry) VALUES (:name, :category, ST_Transform(ST_SetSRID(ST_GeomFromText(:wkt), 3395), 4326))";
                } else {
                    // Data jsou již ve stupních -> jen nastavíme 4326
                    sql = "INSERT INTO map_object (name, category, geometry) VALUES (:name, :category, ST_SetSRID(ST_GeomFromText(:wkt), 4326))";
                }

                entityManager.createNativeQuery(sql)
                        .setParameter("name", name)
                        .setParameter("category", category)
                        .setParameter("wkt", geom.toText())
                        .executeUpdate();

                // 5. BATCHING
                if (++count % 500 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    System.out.println("...průběžně uloženo " + count + " prvků...");
                }

            } catch (Exception e) {
                System.err.println("Chyba u prvku v " + category + ": " + e.getMessage());
                // Vyhození výjimky zde by mohlo "otrávit" transakci,
                // ale NativeQuery je v tomto ohledu odolnější.
            }
        }

        entityManager.flush();
        entityManager.clear();
        System.out.println(">>> Import kategorie " + category + " (celkem " + count + ") DOKONČEN.");
    }
}