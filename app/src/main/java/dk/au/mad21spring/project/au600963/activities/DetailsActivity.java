package dk.au.mad21spring.project.au600963.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import dk.au.mad21spring.project.au600963.R;
import dk.au.mad21spring.project.au600963.constants.Constants;
import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.viewmodels.DetailViewModel;

public class DetailsActivity extends AppCompatActivity {

    //Ui widgets and variables
    private DetailViewModel dvm;
    private Recipe currentRecipe;
    private ImageView imgFlag;
    private TextView txtRecipe, txtTime, txtIngrediens, txtDescription;
    private Button btnBack, btnDelete;
    private int uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        //Setting up UI elements
        imgFlag = findViewById(R.id.imgRecipe);
        txtRecipe = findViewById(R.id.txtRecipe);
        txtTime = findViewById(R.id.txtTime);
        txtIngrediens = findViewById(R.id.txtIngrediens);
        txtDescription = findViewById(R.id.txtDescription);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);

        //Viewmodel
        Intent recipeData = getIntent();
        uid = recipeData.getIntExtra(Constants.UID, 0);
        dvm = new ViewModelProvider(this).get(DetailViewModel.class);
        dvm.getRecipeList().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                currentRecipe = dvm.getRecipe(uid);

                if(currentRecipe != null) {
                    updateUI(currentRecipe);
                }
            }
        });

        //Handling what happens when clicking button "Back"
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        //Handling what happens when clicking button "Delete"
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete();
            }
        });

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
                        startActivity(new Intent(getApplicationContext(), ListActivity.class));
                        overridePendingTransition(0,0);
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

    private void delete() {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra(Constants.UID, uid);
        setResult(Constants.REQUEST_CODE_DELETE, intent);
        finish();
    }

    //Handling what happens when clicking button "back"
    private void goBack() {
        setResult(RESULT_OK);
        finish();
    }

    //Setting data from Recipe that is clicked
    public void updateUI(Recipe recipe){
        currentRecipe = recipe;
        Glide.with(imgFlag.getContext()).load(currentRecipe.getImgUrl()).into(imgFlag);
        txtRecipe.setText(currentRecipe.getName());
        txtTime.setText(getResources().getString(R.string.txtTime) + " " + String.valueOf(currentRecipe.getTime()) + " min");
        txtIngrediens.setText(getResources().getString(R.string.txtIngrediens) + " " + String.valueOf(currentRecipe.getIngrediens()) + "%");;
        txtDescription.setText(getResources().getString(R.string.txtDescription) + " " + currentRecipe.getDescription());;
    }
}