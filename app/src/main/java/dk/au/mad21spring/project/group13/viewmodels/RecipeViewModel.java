package dk.au.mad21spring.project.group13.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import dk.au.mad21spring.project.group13.model.Recipe;
import dk.au.mad21spring.project.group13.repository.Repository;

public class RecipeViewModel extends AndroidViewModel {
    private Repository repository;

    public RecipeViewModel(Application application){
        super(application);
        repository = Repository.getInstance(application);
    }

    //Gets a specific recipe
    public void getRecipe(String uid) {
        repository.getRecipe(uid);
    }

    //Gets a random recipe
    public void getRandomRecipe() {
        repository.getRandomRecipe();
    }

    //Gets a list of all recipes
    public LiveData<List<Recipe>> getRecipeList() {
        return repository.getRecipes();
    }

    //Adds a specific recipe
    public void addRecipe(String recipeName){
        repository.addRecipe(recipeName);
    }

    //Deletes then chosen recipe
    public void deleteRecipe(String uid){
        repository.delete(uid);
    }
}
