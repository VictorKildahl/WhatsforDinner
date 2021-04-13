package dk.au.mad21spring.project.au600963.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import dk.au.mad21spring.project.au600963.model.Recipe;

@Database(entities = {Recipe.class}, version = 12)
public abstract class RecipeDatabase extends RoomDatabase {

    public abstract RecipeDAO recipeDAO();  //Mandatory DAO getter

    private static RecipeDatabase instance; //Database instance for singleton

    //Single pattern used
    public static RecipeDatabase getDatabase(final Context context) {
        if (instance == null) {
            synchronized (RecipeDatabase.class) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        RecipeDatabase.class, "recipe_database")
                        .fallbackToDestructiveMigration()
                        .build();
            }
        }
        return instance;
    }
}
