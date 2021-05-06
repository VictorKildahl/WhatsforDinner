
package dk.au.mad21spring.project.group13.model.randomrecipe;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Example {

    @SerializedName("recipes")
    @Expose
    private List<RandomRecipe> recipes = null;

    public List<RandomRecipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(List<RandomRecipe> recipes) {
        this.recipes = recipes;
    }

}
