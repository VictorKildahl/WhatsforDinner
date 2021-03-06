package dk.au.mad21spring.project.group13.location;

import android.app.Activity;
import android.util.Log;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


//Code from TheArnieExerciseFinder code demo, L9 Sensors, Location and Maps
public class NearbyShopLocationLoader {
    Activity activity;

    public NearbyShopLocationLoader(Activity a){
        activity = a;
    }

    public ArrayList<NearbyShopLocation> getNearbyShopLocationList(){

        ArrayList<NearbyShopLocation> locations = new ArrayList<NearbyShopLocation>();
        NearbyShopLocation temp;
        InputStream is = activity.getResources().openRawResource(activity.getResources().getIdentifier("raw/rema1000",
                "raw", activity.getPackageName()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");
                if(items.length==3){
                    try {
                        temp = new NearbyShopLocation(Double.parseDouble(items[1]), Double.parseDouble(items[0]), items[2], "Rema1000");
                        locations.add(temp);
                        Log.d("asd", "LOCATIONS: " + locations);
                    } catch (NumberFormatException ex) {
                        Log.e("ERROR", "Bad format of number, ex");
                    } catch (NullPointerException ex) {
                        Log.e("ERROR", "Null value", ex);
                    } catch (Exception ex){
                        Log.e("ERROR", "Something crazy happened", ex);
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("ERROR", "Something wrong during CSV file read", ex);
        }

        return locations;
    }
}
