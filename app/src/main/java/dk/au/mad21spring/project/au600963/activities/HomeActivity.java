package dk.au.mad21spring.project.au600963.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.au.mad21spring.project.au600963.R;
import dk.au.mad21spring.project.au600963.model.Recipe;
import dk.au.mad21spring.project.au600963.viewmodels.DetailViewModel;
import dk.au.mad21spring.project.au600963.viewmodels.HomeViewModel;

public class HomeActivity extends AppCompatActivity {

    private FirebaseUser user;
    private ImageView imgAvatar, imgRecipe;
    private TextView txtUsername, txtHeader, txtWelcome, txtRecipe;
    private Button btnLogout;
    private Intent logindata;
    private FirebaseAuth auth;
    private List<Recipe> recipeList;
    private HomeViewModel hvm;
    private Recipe todaysRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        logindata = getIntent();
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        hvm = new ViewModelProvider(this).get(HomeViewModel.class);
        hvm.getRecipeList().observe(this, new Observer<List<Recipe>>() {
            @Override
            public void onChanged(List<Recipe> recipes) {
                recipeList = recipes;
            }
        });

        imgAvatar = findViewById(R.id.imgAvatar);
        imgRecipe = findViewById(R.id.imgRecipe);
        txtUsername = findViewById(R.id.txtUsername);
        txtHeader = findViewById(R.id.txtHeader);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtRecipe = findViewById(R.id.txtRecipe);
        btnLogout = findViewById(R.id.btnLogout);

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

        getTodaysRecipe();
        updateUI();


        //Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.home);
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

    private void getTodaysRecipe() {
        //todaysRecipe = recipeList.get(0);
        /*int random_int = (int)(Math.random() * (recipeList.size() - 0) + 0);

        if((random_int-1) < 0) {
            todaysRecipe = recipeList.get(0);
        } else  {
            todaysRecipe = recipeList.get(random_int-1);
        }*/
    }

    private void updateUI() {
        Glide.with(imgAvatar.getContext()).load(user.getPhotoUrl()).into(imgAvatar);
        //Glide.with(imgRecipe.getContext()).load(todaysRecipe.getImgUrl()).into(imgRecipe);
        txtWelcome.setText("Welcome!");
        txtUsername.setText(user.getEmail());
        txtHeader.setText("What's for dinner?");
        //txtRecipe.setText(todaysRecipe.getName());
    }

    private void logout() {
        if(auth == null){
            //Firebase Auth
            auth = FirebaseAuth.getInstance();
        }

        auth.signOut();
        finish();
    }
}