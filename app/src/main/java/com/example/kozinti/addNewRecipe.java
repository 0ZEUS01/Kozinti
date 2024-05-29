package com.example.kozinti;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.kozinti.tables.Recipes;
import com.example.kozinti.tables.Users;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class addNewRecipe extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView imageViewRecipeImage;
    private Uri imageUri;
    private Button btnAddRecipe;
    private Button cancelButton;
    private TextInputEditText recipeNameEditText;
    private TextInputEditText recipePreparationTimeEditText;
    private TextInputEditText recipeCuisineEditText;
    private TextInputEditText recipeIngredientsEditText;
    private Spinner categorySpinner;
    private DatabaseReference databaseReference;
    private ImageView ProfileIcon;
    private ImageView homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_recipe);

        List<String> categories = fetchCategoriesFromDatabase();

        setStatusBarColor(getResources().getColor(R.color.black));

        imageViewRecipeImage = findViewById(R.id.imageViewRecipeImage);
        btnAddRecipe = findViewById(R.id.btnAddRecipe);
        cancelButton = findViewById(R.id.cancel_button);
        recipeNameEditText = findViewById(R.id.recipeName);
        recipePreparationTimeEditText = findViewById(R.id.RecipePreparationTime);
        recipeCuisineEditText = findViewById(R.id.RecipeCuisine);
        categorySpinner = findViewById(R.id.category);
        recipeIngredientsEditText = findViewById(R.id.RecipeIngredients);
        ProfileIcon = findViewById(R.id.profileIcon);
        homeIcon = findViewById(R.id.home);

        databaseReference = FirebaseDatabase.getInstance().getReference("Recipes");
        populateSpinner();
        imageViewRecipeImage.setOnClickListener(v -> openFileChooser());
        btnAddRecipe.setOnClickListener(v -> uploadRecipe());
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Spinner sp= findViewById(R.id.category);
        if (categories != null) {
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, categories);
            sp.setAdapter(categoryAdapter);
        }

        ProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(addNewRecipe.this, Profile.class));
            }
        });

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(addNewRecipe.this, MainActivity.class));
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadRecipe() {
        // Get other recipe details
        String recipeName = recipeNameEditText.getText().toString().trim();
        String recipePreparationTime = recipePreparationTimeEditText.getText().toString().trim();
        String recipeCuisine = recipeCuisineEditText.getText().toString().trim();
        String recipeCategory = categorySpinner.getSelectedItem().toString().trim();
        String recipeIngredients = recipeIngredientsEditText.getText().toString().trim();

        if (recipeName.isEmpty() || recipePreparationTime.isEmpty() || recipeCuisine.isEmpty() ||
                recipeCategory.isEmpty() || recipeIngredients.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("recipe_images");

            String imageFileName = UUID.randomUUID().toString();

            storageRef.child(imageFileName).putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {

                        storageRef.child(imageFileName).getDownloadUrl().addOnSuccessListener(uri -> {
                            String creatorId = Users.ConnectedUser;
                            Date currentDate = Calendar.getInstance().getTime();
                            Recipes recipe = new Recipes(UUID.randomUUID().toString(), recipeName, recipeIngredients,
                                    recipePreparationTime, recipeCuisine, recipeCategory, uri.toString(), creatorId, currentDate);

                            databaseReference.child(recipe.getRecipeId()).setValue(recipe)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(addNewRecipe.this, "Recipe added successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(addNewRecipe.this, "Failed to add recipe", Toast.LENGTH_SHORT).show());
                        });
                    })
                    .addOnFailureListener(e -> Toast.makeText(addNewRecipe.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageViewRecipeImage.setImageURI(imageUri);
        }
    }
    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
    private List<String> fetchCategoriesFromDatabase() {

        List<String> categories = new ArrayList<>();
        categories.add("Select a category");
        categories.add("Breakfast");
        categories.add("Lunch");
        categories.add("Dinner");
        categories.add("Appetizer");
        categories.add("Salad");
        categories.add("Side dish");
        categories.add("Dessert");
        categories.add("Snack");
        categories.add("Soup");
        categories.add("Vegetarian");
        categories.add("Other");
        return categories;
    }
    private void populateSpinner() {
        List<String> categories = fetchCategoriesFromDatabase();

        if (categories != null) {
            Spinner categorySpinner = findViewById(R.id.category);
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            categorySpinner.setAdapter(categoryAdapter);
            categorySpinner.setSelection(0, false);
        }
    }
}
