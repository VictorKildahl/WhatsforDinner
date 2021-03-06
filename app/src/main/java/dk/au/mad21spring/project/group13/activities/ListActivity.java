package dk.au.mad21spring.project.group13.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import dk.au.mad21spring.project.group13.R;
import dk.au.mad21spring.project.group13.constants.Constants;
import dk.au.mad21spring.project.group13.model.Recipe;
import dk.au.mad21spring.project.group13.viewmodels.RecipeViewModel;
import dk.au.mad21spring.project.group13.viewmodels.RecipeViewModelFactory;

public class ListActivity extends AppCompatActivity implements RecipeAdapter.IRecipeItemClickedListener{

    //Ui widgets and variables
    private RecyclerView rcvRecipe;
    private RecipeAdapter adapter;
    private EditText edtSearch;
    private Button btnAdd, btnRandom;
    private RecipeViewModel rvm;
    private String recipeName;
    private List<Recipe> recipeList;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //Setting up UI elements
        edtSearch = findViewById(R.id.edtSearch);
        rcvRecipe = findViewById(R.id.rcvRecipe);
        btnAdd = findViewById(R.id.btnAdd);
        btnRandom = findViewById(R.id.btnRandom);

        //Setup recyclerview with adapter and layout manager
        adapter = new RecipeAdapter(this);
        rcvRecipe.setLayoutManager(new LinearLayoutManager(this));
        rcvRecipe.setAdapter(adapter);

        //Viewmodel
        rvm = new ViewModelProvider(this, new RecipeViewModelFactory(getApplication())).get(RecipeViewModel.class);
        rvm.getRecipeList().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                recipeList = recipes;
                adapter.updateRecipeList(recipes);
            }
        });

        //Handling what happens when clicking button "add"
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipeName = edtSearch.getText().toString();

                if(!recipeName.isEmpty()){
                    rvm.addRecipe(recipeName);
                }
            }
        });

        //Handling what happens when clicking button "Random"
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvm.getRandomRecipe();
            }
        });

        //Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.recipes);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.recipes:
                        return true;
                    case R.id.map:
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    //Handling what happens when a Recipe is clicked
    @Override
    public void onRecipeClicked(int index){
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(Constants.UID, recipeList.get(index).getUid());

        rvm.getRecipe(recipeList.get(index).getUid());
        startActivityForResult(intent, Constants.REQUEST_CODE_DETAILS);
    }

    //Handling what happens when comming back from another activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.REQUEST_CODE_DETAILS) {
            if(resultCode == RESULT_OK) { }
            if(resultCode == Constants.REQUEST_CODE_DELETE){
                uid = data.getStringExtra(Constants.UID);
                rvm.deleteRecipe(uid);
                Toast.makeText(this, "The recipe has been removed", Toast.LENGTH_SHORT).show();
            } else { }
        }
    }
}