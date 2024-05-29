package com.example.kozinti;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kozinti.adapters.RecipeAdapter;
import com.example.kozinti.databinding.ActivityMainBinding;
import com.example.kozinti.tables.Recipes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity_Guest extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseReference databaseReference;
    private DatabaseReference usersRef;
    private List<Recipes> recipeList;
    private RecipeAdapter nameAdapter;
    private RecipeAdapter categoryAdapter;
    private RecipeAdapter cuisineAdapter;
    private TextWatcher currentTextWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setStatusBarColor(getResources().getColor(R.color.black));

        databaseReference = FirebaseDatabase.getInstance().getReference("Recipes");
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        recipeList = new ArrayList<>();
        nameAdapter = new RecipeAdapter(new ArrayList<>(), usersRef);
        categoryAdapter = new RecipeAdapter(new ArrayList<>(), usersRef);
        cuisineAdapter = new RecipeAdapter(new ArrayList<>(), usersRef);

        binding.RecipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.RecipeRecyclerView.setAdapter(nameAdapter);

        fetchRecipes();

        binding.addRecipe.setOnClickListener(v -> showSignInPromptRecipe());
        binding.profileIcon.setOnClickListener(v -> showSignInPromptProfile());

        binding.searchRecipes.setOnClickListener(v -> {
            binding.searchLayout.setVisibility(View.VISIBLE);
            binding.filterLayout.setVisibility(View.VISIBLE);
            binding.cancelSearch.setVisibility(View.VISIBLE);
        });

        binding.cancelSearch.setOnClickListener(v -> {
            binding.searchLayout.setVisibility(View.GONE);
            binding.filterLayout.setVisibility(View.GONE);
            binding.cancelSearch.setVisibility(View.GONE);
            binding.searchLayout.getEditText().setText("");
        });

        binding.byRecipeName.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.RecipeRecyclerView.setAdapter(nameAdapter);
                setupTextWatcher("name");
            }
        });

        binding.byCategory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.RecipeRecyclerView.setAdapter(categoryAdapter);
                setupTextWatcher("category");
            }
        });

        binding.byCuisine.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.RecipeRecyclerView.setAdapter(cuisineAdapter);
                setupTextWatcher("cuisine");
            }
        });
    }

    private void fetchRecipes() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Recipes recipes = snapshot.getValue(Recipes.class);
                    if (recipes != null) {
                        recipeList.add(recipes);
                    }
                }
                updateAdapters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity_Guest.this, "Failed to fetch recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateAdapters() {
        nameAdapter.updateList(recipeList);
        categoryAdapter.updateList(recipeList);
        cuisineAdapter.updateList(recipeList);
    }

    private void setupTextWatcher(final String filterType) {
        if (currentTextWatcher != null) {
            binding.searchLayout.getEditText().removeTextChangedListener(currentTextWatcher);
        }

        currentTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed here
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchRecipes(s.toString().trim(), filterType);
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchRecipes(s.toString().trim(), filterType);
            }
        };

        binding.searchLayout.getEditText().addTextChangedListener(currentTextWatcher);
    }

    private void searchRecipes(String searchText, String filterType) {
        final String searchTextLower = searchText.toLowerCase();

        switch (filterType) {
            case "name":
                databaseReference.orderByChild("recipeName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Recipes> filteredList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Recipes recipe = snapshot.getValue(Recipes.class);
                            if (recipe != null && recipe.getRecipeName().toLowerCase().contains(searchTextLower)) {
                                filteredList.add(recipe);
                            }
                        }
                        nameAdapter.updateList(filteredList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity_Guest.this, "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case "category":
                databaseReference.orderByChild("recipeCategory").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Recipes> filteredList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Recipes recipe = snapshot.getValue(Recipes.class);
                            if (recipe != null && recipe.getRecipeCategory().toLowerCase().contains(searchTextLower)) {
                                filteredList.add(recipe);
                            }
                        }
                        categoryAdapter.updateList(filteredList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity_Guest.this, "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case "cuisine":
                databaseReference.orderByChild("recipeCuisine").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Recipes> filteredList = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Recipes recipe = snapshot.getValue(Recipes.class);
                            if (recipe != null && recipe.getRecipeCuisine().toLowerCase().contains(searchTextLower)) {
                                filteredList.add(recipe);
                            }
                        }
                        cuisineAdapter.updateList(filteredList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity_Guest.this, "Search failed", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    private void showSignInPromptRecipe() {
        new AlertDialog.Builder(this)
                .setTitle("Sign In Required")
                .setMessage("You need to sign in to add a recipe. Do you want to sign in now?")
                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity_Guest.this, Login.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    private void showSignInPromptProfile() {
        new AlertDialog.Builder(this)
                .setTitle("Sign In Required")
                .setMessage("You need to sign in to view your profile. Do you want to sign in now?")
                .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity_Guest.this, Login.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
