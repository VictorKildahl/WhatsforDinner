package dk.au.mad21spring.project.au600963.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.database.Repository;

public class RecipeViewModel extends AndroidViewModel {
    private Repository repository;

    public RecipeViewModel(Application application){
        super(application);
        repository = Repository.getInstance(application);
    }

    public Recipe getRecipe(int uid) {
        return repository.getRecipe(uid);
    }

    public void getRandomRecipe() {
        repository.getRandomRecipe();
    }

    public LiveData<List<Recipe>> getRecipeList() {
        return repository.getRecipes();
    }

    public void addRecipe(String recipeName){
        repository.addRecipe(recipeName);
    }

    public void deleteRecipe(Recipe recipe){
        repository.delete(recipe);
    }
}
