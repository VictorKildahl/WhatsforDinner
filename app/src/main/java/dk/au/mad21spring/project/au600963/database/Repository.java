package dk.au.mad21spring.project.au600963.database;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dk.au.mad21spring.project.au600963.constants.Constants;
import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.model.randomrecipe.RandomRecipe;
import dk.au.mad21spring.project.au600963.model.recipe.Result;

public class Repository {

    private ExecutorService executor;           //for async processing
    public MutableLiveData<List<Recipe>> recipes;
    private RequestQueue queue;
    private Context context;
    private static Repository instance;
    private String userId;
    private MutableLiveData<Recipe> currentRecipe = new MutableLiveData<>();
    private static final String TAG = "Repository";

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

    //Making URL to Weather API
    private void loadData(String recipeName) {
        //kildahl 01: fa4d67d553e14a638d11145e3db60a61
        //kildahl 02: bd2e943f6c2f411586df06712425fce9
        recipeName = recipeName.replace(" ", "_");
        String dataUrl = "https://api.spoonacular.com/recipes/complexSearch?query=" + recipeName + "&apiKey=bd2e943f6c2f411586df06712425fce9&addRecipeInformation=true&number=1";
        sendRequest(dataUrl);
    }

    //Getting data from Weather API
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

    //Putting the data from api into arraylist
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
                                Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });

                Toast.makeText(context, "The recipe has been added to the list", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
    //Makes the url to update the recipes in the list
    public void getRandomRecipe() {
        //kildahl 01: fa4d67d553e14a638d11145e3db60a61
        //kildahl 02: bd2e943f6c2f411586df06712425fce9
        String randomdataUrl = "https://api.spoonacular.com/recipes/random?apiKey=bd2e943f6c2f411586df06712425fce9&addRecipeInformation=true&number=1";
        randomSendRequest(randomdataUrl);
    }

    //Makes the request to the Recipe API for the current cities
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

    //Adds the new random recipe
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

            Recipe recipe = new Recipe(randomresult.getRecipes().get(0).getTitle(), String.valueOf(randomresult.getRecipes().get(0).getReadyInMinutes()), ingrediens, instruction, des, randomresult.getRecipes().get(0).getImage());

            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes")
                    .add(recipe)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error adding document", e);
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
                        Log.d(TAG, "DocumentSnapshot successfully fetched!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }

    public LiveData<Recipe> getCurrentRecipe(){
        return currentRecipe;
    }

    //Delete clicked Recipe
    public void delete(String uid){
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("users/" + userId + "/recipes").document(uid).delete();
    }



    ///////SERVICE METHODS START/////////
    //Gets the recipes from the db and calls serviceLoadData
    /*public void serviceUpdate(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                List<Recipe> recipesUpdate = recipes.getValue();

                for (int i = 0; i < recipesUpdate.size(); i++){
                    serviceLoadData(recipesUpdate.get(i).getName());
                }
            }
        });

    }

    //Makes the url to update the recipes in the list
    private void serviceLoadData(String recipeName) {
        String dataUrl = "https://api.spoonacular.com/recipes/complexSearch?query=" + recipeName + "&apiKey=fa4d67d553e14a638d11145e3db60a61&addRecipeInformation=true&number=1";
        serviceSendRequest(dataUrl);
    }

    //Makes the request to the Recipe API for the current cities
    private void serviceSendRequest(String dataUrl) {
        if(queue == null){
            queue = Volley.newRequestQueue(context);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, dataUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response){
                Log.d(Constants.TAG, "Recipe information: " + response);
                serviceParseJson(response);
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

    //Updates the current cities with the new data
    private void serviceParseJson(String json) {
        Gson gson = new GsonBuilder().create();
        Result result = gson.fromJson(json, Result.class);
        if(result != null){
            Recipe recipe = new Recipe(result.getTitle(), String.valueOf(result.getReadyInMinutes()), "ingrediens", "test", result.getSummary(), result.getImage());

            Log.d(TAG, "updated recipe: " + recipe.getName());
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    db.recipeDAO().updateRecipe(recipe);
                }
            });
        }
    }*/
    ///////SERVICE METHODS END/////////

}
