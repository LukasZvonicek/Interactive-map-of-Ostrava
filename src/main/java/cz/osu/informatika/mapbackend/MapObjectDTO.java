package cz.osu.informatika.mapbackend;

public class MapObjectDTO {
        private String name;
        private String category;
        private double lat;
        private double lon;

        public MapObjectDTO(String name, String category, double lat, double lon) {
            this.name = name;
            this.category = category;
            this.lat = lat;
            this.lon = lon;
        }

        // Gettery
        public String getName() { return name; }
        public String getCategory() { return category; }
        public double getLat() { return lat; }
        public double getLon() { return lon; }
    }
