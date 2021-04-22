package dk.au.mad21spring.project.au600963;

import android.app.Activity;
import android.util.Log;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import dk.au.mad21spring.project.au600963.NearbyShopLocation;


/**
 * Created by kasper on 06/05/15.
 */
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
                        temp = new NearbyShopLocation(Double.parseDouble(items[1]), Double.parseDouble(items[0]), items[2], "All it takes is all you've got!");
                        locations.add(temp);
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
