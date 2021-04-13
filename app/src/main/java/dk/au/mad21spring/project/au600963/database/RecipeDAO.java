package dk.au.mad21spring.project.au600963.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dk.au.mad21spring.project.au600963.model.Recipe;

@Dao
public interface RecipeDAO {

    @Query("SELECT COUNT(*) FROM Recipe")
    int getDatabaseCount();

    @Query("SELECT * FROM Recipe")
    LiveData<List<Recipe>> getAll();

    @Query("SELECT * FROM Recipe WHERE uid LIKE :uid")
    Recipe findRecipe(int uid);

    @Query("SELECT * FROM Recipe WHERE name LIKE :name LIKE :name LIMIT 1")
    Recipe findRecipe(String name);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addRecipe(Recipe recipe);

    @Update
    void updateRecipe(Recipe recipe);

    @Delete
    void delete(Recipe recipe);
}
