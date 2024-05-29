package com.example.kozinti;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kozinti.adapters.UserRecipeAdapter;
import com.example.kozinti.databinding.ActivityYourRecipesBinding;
import com.example.kozinti.tables.Recipes;
import com.example.kozinti.tables.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class YourRecipes extends AppCompatActivity {
    private ActivityYourRecipesBinding binding;
    private UserRecipeAdapter adapter;
    private List<Recipes> recipeList;
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;
    private View addRecipeIcon;
    private View ProfileIcon;
    private View homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYourRecipesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setStatusBarColor(getResources().getColor(R.color.black));

        binding.YourRecipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recipeList = new ArrayList<>();
        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference = FirebaseDatabase.getInstance().getReference("Recipes");
        adapter = new UserRecipeAdapter(recipeList, usersRef, databaseReference);
        binding.YourRecipeRecyclerView.setAdapter(adapter);
        addRecipeIcon = findViewById(R.id.addRecipe);
        ProfileIcon = findViewById(R.id.profileIcon);
        homeIcon = findViewById(R.id.home);

        addRecipeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(YourRecipes.this, addNewRecipe.class));
            }
        });

        ProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(YourRecipes.this, Profile.class));
            }
        });

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(YourRecipes.this, MainActivity.class));
            }
        });
        // Fetch user recipes
        fetchUserRecipes();
    }

    private void fetchUserRecipes() {
        String connectedUserId = Users.ConnectedUser;
        if (connectedUserId == null) {
            Toast.makeText(this, "No user is connected", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.orderByChild("creatorId").equalTo(connectedUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipes recipe = snapshot.getValue(Recipes.class);
                    if (recipe != null) {
                        recipeList.add(recipe);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(YourRecipes.this, "Failed to fetch recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
