package dk.au.mad21spring.project.au600963.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.project.au600963.constants.Constants;
import dk.au.mad21spring.project.au600963.R;
import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.database.Repository;

public class ForegroundService extends Service {

    //Variables
    private ExecutorService execService;    //ExecutorService for running things off the main thread
    private boolean started = false;        //Indicating if Service is startet
    private Repository repository;
    private List<Recipe> recipeList;
    private Recipe notificationRecipe;
    private Context context;

    //Constructor
    public ForegroundService() {
    }

    //onCreate called before onStartCommand when Service first created
    @Override
    public void onCreate() {
        super.onCreate();
        repository = Repository.getInstance(this.getApplication());
        context = getApplicationContext();
    }

    //onStartCommand called when an Actvity starts the Service with Intent through calling startService(...)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Check for Android version - whether we need to create a notification channel (from Android 0 and up, API 26)
        if(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel channel = new NotificationChannel(Constants.SERVICE_CHANNEL, "Foreground Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
        }

        //Build the notification
        Notification notification = new NotificationCompat.Builder(this, Constants.SERVICE_CHANNEL)
                .setContentTitle("What's for Dinner?")
                .setContentText("Today's meal will be shown shortly...")
                .setSmallIcon(R.drawable.ic_recipeservice)
                .setTicker("Recipe")
                .build();

        //Call to startForeground will promote this Service to a foreground service (needs manifest permission)
        //Also require the notification to be set, so that user can always see that Service is running in the background
        startForeground(Constants.NOTIFICATION_ID, notification);

        //This method starts recursive background work
        //doBackgroundStuff();

        //Returning START_STICKY will make the Service restart again eventually if it gets killed off (e.g. due to resources)
        return START_STICKY;
    }


    //Initate the background work - only start if not already started
    private void doBackgroundStuff() {
        if(!started) {
            started = true;
            doRecursiveWork();
        }
    }

    //Method runs recursively (calls itself in the end) as long as started==true
    private void doRecursiveWork(){
        //Lazy creation of ExecutorService running as a single threaded executor
        //This executor will allow us to do work off the main thread
        if(execService == null) {
            execService = Executors.newSingleThreadExecutor();
        }

        //Submit a new Runnable (implement onRun() ) to the executor - code will run on other thread
        execService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);     //we can sleep, because this code is not run on main thread
                    Log.d(Constants.SERVICE, "60 seconds have passed");
                } catch (InterruptedException e) {
                    Log.e(Constants.SERVICE, "run: EROOR", e);
                }

                //Call to the repository that updates the cities in the list
                repository.serviceUpdate();

                //Finding random Recipe for notification
                recipeList = repository.recipes.getValue();
                int random_int = (int)(Math.random() * (recipeList.size() - 0) + 0);

                if((random_int-1) < 0) {
                    notificationRecipe = recipeList.get(0);
                } else  {
                    notificationRecipe = recipeList.get(random_int-1);
                }

                //Notification manager
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                //Notification
                Notification notification = new NotificationCompat.Builder(context, Constants.SERVICE_CHANNEL)
                        .setContentTitle("What's for Dinner?")
                        .setContentText("Today's meal: " + notificationRecipe.getName())
                        .setSmallIcon(R.drawable.ic_recipeservice)
                        .setTicker("Recipe")
                        .build();

                notificationManager.notify(Constants.NOTIFICATION_ID, notification);

                try {
                    Thread.sleep(86400000);     //we can sleep, because this code is not run on main thread
                    Log.d(Constants.SERVICE, "1 day have passed");
                } catch (InterruptedException e) {
                    Log.e(Constants.SERVICE, "run: EROOR", e);
                }

                //The recursive bit - if started still true, call self again
                if(started) {
                    doRecursiveWork();
                }
            }
        });
    }

    //This is not a bound service, so we return null
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //If Service is destroyed
    @Override
    public void onDestroy() {
        started = false;
        super.onDestroy();
    }
}
