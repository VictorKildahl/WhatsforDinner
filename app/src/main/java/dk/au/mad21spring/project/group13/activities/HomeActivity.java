package dk.au.mad21spring.project.group13.activities;

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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import dk.au.mad21spring.project.group13.R;
import dk.au.mad21spring.project.group13.model.Recipe;
import dk.au.mad21spring.project.group13.service.ForegroundService;
import dk.au.mad21spring.project.group13.viewmodels.HomeViewModel;

public class HomeActivity extends AppCompatActivity {

    //Ui widgets and variables
    private FirebaseUser user;
    private ImageView imgAvatar, imgRecipe;
    private TextView txtUsername, txtHeader, txtWelcome, txtRecipe;
    private Button btnLogout;
    private FirebaseAuth auth;
    private HomeViewModel hvm;
    private Recipe todaysRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Setting up UI elements
        imgAvatar = findViewById(R.id.imgAvatar);
        imgRecipe = findViewById(R.id.imgRecipe);
        txtUsername = findViewById(R.id.txtUsername);
        txtHeader = findViewById(R.id.txtHeader);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtRecipe = findViewById(R.id.txtRecipe);
        btnLogout = findViewById(R.id.btnLogout);

        //Start service
        Intent foregroundServiceIntent = new Intent(this, ForegroundService.class);
        startService(foregroundServiceIntent);

        //Firebase
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        //Viewmodel and update UI
        hvm = new ViewModelProvider(this).get(HomeViewModel.class);
        hvm.getTodaysRecipe().observe(this, new Observer<Recipe>() {
            @Override
            public void onChanged(Recipe recipe) {
                todaysRecipe = recipe;

                if(todaysRecipe == null){
                    txtRecipe.setText(getResources().getString(R.string.txtRecipe1) + "\n" + getResources().getString(R.string.txtRecipe2));
                    Glide.with(imgRecipe.getContext()).load(R.drawable.nodinner).into(imgRecipe);
                } else {
                    txtRecipe.setText(todaysRecipe.getName());
                    Glide.with(imgRecipe.getContext()).load(todaysRecipe.getImgUrl()).into(imgRecipe);
                }
            }
        });

        hvm.getUser();
        updateUI();

        //Handling what happens when clicking button "Logout"
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout();
            }
        });

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

    //Set information from google login
    private void updateUI() {
        if(todaysRecipe == null){
            txtRecipe.setText(getResources().getString(R.string.txtRecipe1) + "\n" + getResources().getString(R.string.txtRecipe2));
            Glide.with(imgRecipe.getContext()).load(R.drawable.nodinner).into(imgRecipe);
        }

        Glide.with(imgAvatar.getContext()).load(user.getPhotoUrl()).into(imgAvatar);
        txtWelcome.setText(getResources().getString(R.string.txtWelcome));
        txtUsername.setText(user.getEmail());
        txtHeader.setText(getResources().getString(R.string.txtHeader));
    }

    //Log out from google
    private void logout() {
        if(auth == null){
            //Firebase Auth
            auth = FirebaseAuth.getInstance();
        }

        auth.signOut();
        finish();
    }
}