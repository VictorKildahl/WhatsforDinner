package dk.au.mad21spring.project.au600963.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Button;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import dk.au.mad21spring.project.au600963.R;
import dk.au.mad21spring.project.au600963.constants.Constants;
import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.service.ForegroundService;
import dk.au.mad21spring.project.au600963.viewmodels.RecipeViewModel;
import dk.au.mad21spring.project.au600963.viewmodels.RecipeViewModelFactory;

public class ListActivity extends AppCompatActivity implements RecipeAdapter.IRecipeItemClickedListener{

    //Ui widgets and variables
    private RecyclerView rcvRecipe;
    private RecipeAdapter adapter;
    private EditText edtSearch;
    private Button btnAdd, btnRandom;
    private RecipeViewModel rvm;
    private String recipeName;
    private List<Recipe> recipeList;
    private Location userLocation;
    private int uid;

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

        //Start service
        Intent foregroundServiceIntent = new Intent(this, ForegroundService.class);
        startService(foregroundServiceIntent);

        //Handling what happens when clicking button "add"
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recipeName = edtSearch.getText().toString();
                rvm.addRecipe(recipeName);
            }
        });

        //Handling what happens when clicking button "Map"
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rvm.getRandomRecipe();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.recipes);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
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
        startActivityForResult(intent, Constants.REQUEST_CODE_DETAILS);
    }

    //Handling what happens when comming back from another activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Constants.REQUEST_CODE_DETAILS) {
            if(resultCode == RESULT_OK) { }
            if(resultCode == Constants.REQUEST_CODE_DELETE){
                uid = data.getIntExtra(Constants.UID, 0);
                Recipe deleteRecipe = rvm.getRecipe(uid);
                rvm.deleteRecipe(deleteRecipe);
                Toast.makeText(this, "The recipe has been removed", Toast.LENGTH_SHORT).show();
            } else { }
        }
    }
}