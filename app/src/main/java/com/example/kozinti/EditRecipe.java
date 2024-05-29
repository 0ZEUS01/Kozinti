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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kozinti.tables.Recipes;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EditRecipe extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private TextInputEditText recipeNameEditText;
    private TextInputEditText recipePreparationTimeEditText;
    private TextInputEditText recipeCuisineEditText;
    private Spinner categorySpinner;
    private TextInputEditText recipeIngredientsEditText;
    private ImageView recipeImageView;
    private Button editButton;
    private Button cancelButton;

    private DatabaseReference recipesRef;
    private String recipeId;
    private Recipes currentRecipe;
    private Uri imageUri;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);
        setStatusBarColor(getResources().getColor(R.color.black));
        recipesRef = FirebaseDatabase.getInstance().getReference("Recipes");

        recipeNameEditText = findViewById(R.id.recipeName);
        recipePreparationTimeEditText = findViewById(R.id.RecipePreparationTime);
        recipeCuisineEditText = findViewById(R.id.RecipeCuisine);
        categorySpinner = findViewById(R.id.category);
        recipeIngredientsEditText = findViewById(R.id.RecipeIngredients);
        recipeImageView = findViewById(R.id.imageViewRecipeImage);
        editButton = findViewById(R.id.btnEditRecipe);
        cancelButton = findViewById(R.id.cancel_button);

        recipeId = getIntent().getStringExtra("recipeId");

        if (recipeId != null) {
            loadRecipeDetails(recipeId);

            recipeImageView.setOnClickListener(v -> openFileChooser());

            editButton.setOnClickListener(v -> showEditConfirmationDialog());

            cancelButton.setOnClickListener(v -> finish());

            populateCategorySpinner();
        } else {
            Toast.makeText(this, "Recipe ID is null", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadRecipeDetails(String recipeId) {
        recipesRef.child(recipeId).get().addOnSuccessListener(dataSnapshot -> {
            currentRecipe = dataSnapshot.getValue(Recipes.class);
            if (currentRecipe != null) {
                recipeNameEditText.setText(currentRecipe.getRecipeName());
                recipePreparationTimeEditText.setText(currentRecipe.getRecipePreparationTime());
                recipeCuisineEditText.setText(currentRecipe.getRecipeCuisine());
                recipeIngredientsEditText.setText(currentRecipe.getRecipeIngredients());

                Glide.with(this)
                        .load(currentRecipe.getRecipeImageURL())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(recipeImageView);

                // Set selected category
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) categorySpinner.getAdapter();
                int spinnerPosition = adapter.getPosition(currentRecipe.getRecipeCategory());
                categorySpinner.setSelection(spinnerPosition);
            }
        }).addOnFailureListener(e -> Toast.makeText(EditRecipe.this, "Failed to load recipe details", Toast.LENGTH_SHORT).show());
    }

    private void showEditConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Edit")
                .setMessage("Are you sure you want to edit this recipe?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (imageUri != null) {
                        uploadImageAndEditRecipe();
                    } else {
                        editRecipe();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            recipeImageView.setImageURI(imageUri);
        }
    }

    private void uploadImageAndEditRecipe() {
        if (imageUri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("recipe_images");
            String imageFileName = UUID.randomUUID().toString();
            storageRef.child(imageFileName).putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.child(imageFileName).getDownloadUrl().addOnSuccessListener(uri -> {
                        imageUrl = uri.toString();
                        editRecipe();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(EditRecipe.this, "Failed to upload image", Toast.LENGTH_SHORT).show());
        }
    }

    private void editRecipe() {
        String updatedName = recipeNameEditText.getText().toString();
        String updatedPreparationTime = recipePreparationTimeEditText.getText().toString();
        String updatedCuisine = recipeCuisineEditText.getText().toString();
        String updatedCategory = categorySpinner.getSelectedItem().toString();
        String updatedIngredients = recipeIngredientsEditText.getText().toString();

        currentRecipe.setRecipeName(updatedName);
        currentRecipe.setRecipePreparationTime(updatedPreparationTime);
        currentRecipe.setRecipeCuisine(updatedCuisine);
        currentRecipe.setRecipeCategory(updatedCategory);
        currentRecipe.setRecipeIngredients(updatedIngredients);

        if (imageUrl != null) {
            currentRecipe.setRecipeImageURL(imageUrl);
        }

        recipesRef.child(recipeId).setValue(currentRecipe)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditRecipe.this, "Recipe updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditRecipe.this, "Failed to update recipe", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void populateCategorySpinner() {
        List<String> categories = fetchCategoriesFromDatabase();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
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

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
