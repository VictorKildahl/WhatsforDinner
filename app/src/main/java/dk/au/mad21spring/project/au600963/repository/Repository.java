package dk.au.mad21spring.project.au600963.repository;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.project.au600963.constants.Constants;
import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.model.User;
import dk.au.mad21spring.project.au600963.model.randomrecipe.RandomRecipe;
import dk.au.mad21spring.project.au600963.model.recipe.Result;

public class Repository {

    public MutableLiveData<List<Recipe>> recipes;
    private MutableLiveData<Recipe> currentRecipe = new MutableLiveData<>();
    private MutableLiveData<Recipe> todaysRecipe = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private static Repository instance;
    private ExecutorService executor;           //for async processing
    private RequestQueue queue;
    private Context context;
    private String userId;
    private int random_int;

    public static Repository getInstance(Application application){
        if(instance == null){
            instance = new Repository(application);
        }
        return instance;
    }

    //Constuctor
    public Repository(Application application) {
        executor = Executors.newSingleThreadExecutor();
        context = application.getApplicationContext();
    }

    //Gets all recipes from the user that is logged in
    public MutableLiveData<List<Recipe>> getRecipes() {
        if(recipes == null){
            recipes = new MutableLiveData<List<Recipe>>();
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                    ArrayList<Recipe> updatedRecipes = new ArrayList<>();

                    if(snapshot != null && !snapshot.isEmpty()){
                        for(DocumentSnapshot doc : snapshot.getDocuments()){
                            Recipe r = doc.toObject(Recipe.class);

                            if(r != null){
                                updatedRecipes.add(r);
                            }
                        }
                    }

                    recipes.setValue(updatedRecipes);
                }
            });
        }

        return recipes;
    }

    //add a new recipe to database
    public void addRecipe(String recipeName){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                loadData(recipeName);
            }
        });
    }

    //Making URL to Recipe API
    private void loadData(String recipeName) {
        //If the api dosn't work try one of these API Keys:
        //kildahl 01: fa4d67d553e14a638d11145e3db60a61
        //kildahl 02: bd2e943f6c2f411586df06712425fce9
        recipeName = recipeName.replace(" ", "_");
        String dataUrl = "https://api.spoonacular.com/recipes/complexSearch?query=" + recipeName + "&apiKey=bd2e943f6c2f411586df06712425fce9&addRecipeInformation=true&number=1";
        sendRequest(dataUrl);
    }

    //Getting data from Recipe API
    private void sendRequest(String dataUrl) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if(queue == null){
                    queue = Volley.newRequestQueue(context);
                }

                StringRequest stringRequest = new StringRequest(Request.Method.GET, dataUrl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response){
                        Log.d(Constants.TAG, "Recipe information: " + response);
                        parseJson(response);
                    }
                }, new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        Log.e(Constants.TAG, "That did not work!", error);
                        Toast.makeText(context, "The recipe could not be found", Toast.LENGTH_SHORT).show();
                    }
                });

                queue.add(stringRequest);
            }
        });

    }

    //Putting the data from api into firebase database
    private void parseJson(String json) {
        Gson gson = new GsonBuilder().create();
        Result result = gson.fromJson(json, Result.class);

        if(result.getResults().size() == 0){
            Toast.makeText(context, "The recipe was not found.", Toast.LENGTH_SHORT).show();
        } else {
            String des = result.getResults().get(0).getSummary()
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .replaceAll("<a.*>", "")
                    .replaceAll("To use.*for similar recipes.", "")
                    .replaceAll("Try.*for similar recipes.", "");

            String instruction = "";
            String ingrediens = "";
            for (int i = 0; i < result.getResults().get(0).getAnalyzedInstructions().get(0).getSteps().size(); i++){
                instruction += "\n" + result.getResults().get(0).getAnalyzedInstructions().get(0).getSteps().get(i).getStep();

                for (int x = 0; x < result.getResults().get(0).getAnalyzedInstructions().get(0).getSteps().get(i).getIngredients().size(); x++){
                    ingrediens += "\n" + result.getResults().get(0).getAnalyzedInstructions().get(0).getSteps().get(i).getIngredients().get(x).getName();
                }
            }

            Recipe recipe = new Recipe(result.getResults().get(0).getTitle(), String.valueOf(result.getResults().get(0).getReadyInMinutes()), ingrediens, instruction, des, result.getResults().get(0).getImage());

            if(Exist(recipe)){
                Toast.makeText(context, "The recipe is already in the list", Toast.LENGTH_SHORT).show();
                return;
            } else {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes")
                        .add(recipe)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                addIdToRecipe(recipe, documentReference.getId());
                                Log.d(Constants.FIREBASE, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(Constants.FIREBASE, "Error adding document", e);
                            }
                        });

                Toast.makeText(context, "The recipe has been added to the list", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //Adds the ID from firebase to the specific Recipe
    private void addIdToRecipe(Recipe recipe, String id) {
        recipe.setUid(id);

        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("uid", id);
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes")
                .document(id)
                .update(recipeData);
    }

    //Checks if the recipe already exist in the list
    private boolean Exist(Recipe recipe) {
        List<Recipe> recipesExist = recipes.getValue();

        boolean found = false;
        int i = 0;

        while (!found && i < recipesExist.size()){
            if(recipesExist.get(i).getName().equals(recipe.getName())){
                found = true;
            }

            i++;
        }
        return found;
    }

    /////////////Random recipe start//////////////
    //Makes the url to get random recipe
    public void getRandomRecipe() {
        //If the api dosn't work try one of these API Keys:
        //kildahl 01: fa4d67d553e14a638d11145e3db60a61
        //kildahl 02: bd2e943f6c2f411586df06712425fce9
        String randomdataUrl = "https://api.spoonacular.com/recipes/random?apiKey=bd2e943f6c2f411586df06712425fce9&addRecipeInformation=true&number=1";
        randomSendRequest(randomdataUrl);
    }

    //Makes the request to the Recipe API, asking for random recipe
    private void randomSendRequest(String dataUrl) {
        if(queue == null){
            queue = Volley.newRequestQueue(context);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, dataUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response){
                Log.d(Constants.TAG, "Random Recipe information: " + response);
                randomParseJson(response);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){
                Log.e(Constants.TAG, "That did not work! Random", error);
                Toast.makeText(context, "The recipe could not be found random", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(stringRequest);
    }

    //Adds the new random recipe to firebase
    private void randomParseJson(String json) {
        Gson gson = new GsonBuilder().create();
        RandomRecipe randomresult = gson.fromJson(json, RandomRecipe.class);
        if(randomresult != null){

            String instruction = "";
            String ingrediens = "";
            for (int i = 0; i < randomresult.getRecipes().get(0).getAnalyzedInstructions().get(0).getSteps().size(); i++){
                instruction += "\n" + randomresult.getRecipes().get(0).getAnalyzedInstructions().get(0).getSteps().get(i).getStep();

                for (int x = 0; x < randomresult.getRecipes().get(0).getAnalyzedInstructions().get(0).getSteps().get(i).getIngredients().size(); x++){
                    ingrediens += "\n" + randomresult.getRecipes().get(0).getAnalyzedInstructions().get(0).getSteps().get(i).getIngredients().get(x).getName();
                }
            }

            String des = randomresult.getRecipes().get(0).getSummary()
                    .replaceAll("<b>", "")
                    .replaceAll("</b>", "")
                    .replaceAll("<a.*>", "")
                    .replaceAll("To use.*for similar recipes.", "")
                    .replaceAll("Try.*for similar recipes.", "");

            Recipe randomrecipe = new Recipe(randomresult.getRecipes().get(0).getTitle(), String.valueOf(randomresult.getRecipes().get(0).getReadyInMinutes()), ingrediens, instruction, des, randomresult.getRecipes().get(0).getImage());

            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes")
                    .add(randomrecipe)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            addIdToRecipe(randomrecipe, documentReference.getId());
                            Log.d(Constants.FIREBASE, "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(Constants.FIREBASE, "Error adding document", e);
                        }
                    });
        }
    }
    /////////////Random recipe end//////////////



    //Get clicked recipe
    public void getRecipe(String uid){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        Recipe temprecipe = new Recipe(
                                snapshot.get("name").toString(),
                                snapshot.get("time").toString(),
                                snapshot.get("ingrediens").toString(),
                                snapshot.get("instruction").toString(),
                                snapshot.get("description").toString(),
                                snapshot.get("imgUrl").toString());

                        temprecipe.setUid(snapshot.getId());
                        currentRecipe.setValue(temprecipe);
                        Log.d(Constants.FIREBASE, "DocumentSnapshot successfully fetched!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(Constants.FIREBASE, "Error deleting document", e);
                    }
                });
    }

    //Returns information of the clicked recipe
    public LiveData<Recipe> getCurrentRecipe(){
        return currentRecipe;
    }

    //Gets user from firebase
    public void getUser(){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if(snapshot.contains("timestamp") && snapshot.contains("todaysDinner")) {
                            User tempuser = new User(
                                    snapshot.get("todaysDinner").toString(),
                                    Long.valueOf(snapshot.get("timestamp").toString()));

                            tempuser.setUid(snapshot.getId());
                            currentUser.setValue(tempuser);
                            Log.d(Constants.FIREBASE, "DocumentSnapshot successfully fetched!");

                            checkIfNewDay();
                        } else {
                            addUser();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(Constants.FIREBASE, "Error deleting document", e);
                    }
                });
    }

    //checks if it is a new day
    public void checkIfNewDay() {
        long timestamp = currentUser.getValue().getTimestamp();
        long now = System.currentTimeMillis();
        long dif = now - timestamp;

        if(dif > 10*60*1000){
            getRandomRecipeFromList();
        } else {
            getTodaysRecipeFromUser(currentUser.getValue().getTodaysDinner());
        }
    }

    //Adds todaysdinner and timestamp to userid
    public void addUser(){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> newUser = new HashMap<>();
        newUser.put("todaysDinner", "");
        newUser.put("timestamp", 1);

        FirebaseFirestore.getInstance().collection("users").document(userId).set(newUser, SetOptions.merge());
    }

    //Get random recipe from list
    public void getRandomRecipeFromList(){
        MutableLiveData<List<Recipe>> allRecipes = new MutableLiveData<List<Recipe>>();

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                ArrayList<Recipe> updatedRecipes = new ArrayList<>();

                if(snapshot != null && !snapshot.isEmpty()){
                    for(DocumentSnapshot doc : snapshot.getDocuments()){
                        Recipe r = doc.toObject(Recipe.class);

                        if(r != null){
                            updatedRecipes.add(r);
                        }
                    }
                }

                allRecipes.setValue(updatedRecipes);
                if(updatedRecipes.size() != 0){
                    random_int = (int)(Math.random() * (allRecipes.getValue().size() - 0) + 0);

                    if((random_int-1) < 0) {
                        todaysRecipe.setValue(allRecipes.getValue().get(0));
                        updateUser(allRecipes.getValue().get(0).uid, System.currentTimeMillis());
                    } else  {
                        todaysRecipe.setValue(allRecipes.getValue().get(random_int-1));
                        updateUser(allRecipes.getValue().get(random_int-1).uid, System.currentTimeMillis());
                    }
                } else {
                    todaysRecipe.setValue(null);
                }
            }
        });
    }

    //Updates user when its time for new todaysdinner
    public void updateUser(String todaysDinner, long timestamp){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(todaysDinner != null){
            Map<String, Object> userData = new HashMap<>();
            userData.put("todaysDinner", todaysDinner);
            userData.put("timestamp", timestamp);

            FirebaseFirestore.getInstance().collection("users").document(userId).update(userData);
            getTodaysRecipeFromUser(todaysDinner);
        }
    }

    //Get clicked recipe
    public void getTodaysRecipeFromUser(String uid){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes").document(uid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot snapshot) {
                        if(snapshot.contains("name")){
                            Recipe temprecipe = new Recipe(
                                    snapshot.get("name").toString(),
                                    snapshot.get("time").toString(),
                                    snapshot.get("ingrediens").toString(),
                                    snapshot.get("instruction").toString(),
                                    snapshot.get("description").toString(),
                                    snapshot.get("imgUrl").toString());

                            temprecipe.setUid(snapshot.getId());
                            todaysRecipe.setValue(temprecipe);
                            Log.d(Constants.FIREBASE, "DocumentSnapshot successfully fetched!");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(Constants.FIREBASE, "Error deleting document", e);
                    }
                });
    }

    //gets todaysrecipe
    public LiveData<Recipe> getTodaysRecipe(){
        return todaysRecipe;
    }

    //Delete clicked Recipe
    public void delete(String uid){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes").document(uid).delete();
    }
}

