package dk.au.mad21spring.project.au600963.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import dk.au.mad21spring.project.au600963.model.User;
import dk.au.mad21spring.project.au600963.repository.Repository;
import dk.au.mad21spring.project.au600963.model.Recipe;

public class HomeViewModel extends AndroidViewModel {

    private Repository repository;

    public HomeViewModel(Application application){
        super(application);
        repository = Repository.getInstance(application);
    }

    //Gets a random recipe that is already in the list
    public void checkIfNewDay() {
         repository.checkIfNewDay();
    }

    //Gets a list of all recipes
    public LiveData<List<Recipe>> getRecipeList() {
        return repository.getRecipes();
    }

    public LiveData<Recipe> getTodaysRecipe() {
        return repository.getTodaysRecipe();
    }

    public void getUser() {
        repository.getUser();
    }


    //Gets information about the todays recipe
    public LiveData<Recipe> getCurrentRecipe() {
        return repository.getCurrentRecipe();
    }
}
