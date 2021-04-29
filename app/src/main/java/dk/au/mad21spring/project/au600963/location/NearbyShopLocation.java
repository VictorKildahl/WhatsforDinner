package dk.au.mad21spring.project.au600963.location;

//Code from TheArnieExerciseFinder code demo, L9 Sensors, Location and Maps
public class NearbyShopLocation {
        private double latitude;
        private double longitude;
        private String name;
        private String description;

        public NearbyShopLocation(double lati, double longi, String nam, String desc){
            latitude = lati;
            longitude = longi;
            name = nam;
            description = desc;
        }

        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

}
