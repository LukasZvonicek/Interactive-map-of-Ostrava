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
                MapObject obj = new MapObject();
                obj.setCategory(category);

                // 1. CHYTRÉ MAPOVÁNÍ NÁZVU (včetně tvého 'zast_jm')
                JsonNode props = feature.get("properties");
                String name = "Neznámý objekt";

                String[] nameFields = {
                        "NAME", "zast_jm", "INFO", "NAZEV"
                };

                String[] backupFields = {
                        "ULICE"
                };

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
                obj.setName(name);

                // 2. ČTENÍ GEOMETRIE
                String geometryJson = feature.get("geometry").toString();
                Geometry geom = reader.read(geometryJson);

                // 3. ODSTRANĚNÍ Z-SOUŘADNICE (3D -> 2D)
                geom.apply(new org.locationtech.jts.geom.CoordinateSequenceFilter() {
                    @Override
                    public void filter(org.locationtech.jts.geom.CoordinateSequence seq, int i) {
                        seq.setOrdinate(i, org.locationtech.jts.geom.CoordinateSequence.Z, Double.NaN);
                    }
                    @Override public boolean isDone() { return false; }
                    @Override public boolean isGeometryChanged() { return true; }
                });

                geom.setSRID(4326); // Nastavení WGS84
                obj.setGeometry(geom);

                // 4. ULOŽENÍ (Persist je pro hromadné vkládání lepší než save)
                entityManager.persist(obj);

                // 5. BATCHING (Každých 500 prvků vyčistíme paměť)
                if (++count % 500 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                    System.out.println("...průběžně uloženo " + count + " prvků...");
                }

            } catch (Exception e) {
                System.err.println("Chyba u prvku v " + category + ": " + e.getMessage());
            }
        }

        // Finální vyprázdnění zbytku do DB
        entityManager.flush();
        entityManager.clear();
        System.out.println(">>> Import kategorie " + category + " (celkem " + count + ") DOKONČEN.");
    }
}