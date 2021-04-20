package dk.au.mad21spring.project.au600963.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.repository.Repository;

public class DetailViewModel extends AndroidViewModel {

    private Repository repository;

    public DetailViewModel(Application application){
        super(application);
        repository = Repository.getInstance(application);
    }

    //Gets information about the clicked recipe
    public LiveData<Recipe> getCurrentRecipe() {
        return repository.getCurrentRecipe();
    }
}
