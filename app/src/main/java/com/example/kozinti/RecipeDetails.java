package com.example.kozinti;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.kozinti.tables.Recipes;
import com.example.kozinti.tables.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class RecipeDetails extends AppCompatActivity {
    private DatabaseReference databaseReference;
    private ImageView recipeImageView;
    private TextView recipeNameTextView;
    private TextView cuisineAndTimeTextView;
    private TextView categoryTextView;
    private TextView ingredientsTextView;
    private ImageView addRecipeIcon;
    private ImageView profileIcon;
    private ImageView homeIcon;
    private TextView creatorTextView;
    private TextView creationTimeTextView;
    private ImageView shareIcon;
    private Recipes currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
        setStatusBarColor(getResources().getColor(R.color.black));

        recipeImageView = findViewById(R.id.recipe_image_D);
        recipeNameTextView = findViewById(R.id.recipe_name_D);
        cuisineAndTimeTextView = findViewById(R.id.RecipeCuisine_RecipePreparationTime_D);
        categoryTextView = findViewById(R.id.RecipeCategory_D);
        ingredientsTextView = findViewById(R.id.RecipeIngredients_D);
        addRecipeIcon = findViewById(R.id.addRecipe_D);
        profileIcon = findViewById(R.id.profileIcon_D);
        creatorTextView = findViewById(R.id.RecipeCreator);
        homeIcon = findViewById(R.id.home);
        creationTimeTextView = findViewById(R.id.RecipeCreationTime);
        shareIcon = findViewById(R.id.Share_D);

        String recipeId = getIntent().getStringExtra("recipeId");

        databaseReference = FirebaseDatabase.getInstance().getReference("Recipes").child(recipeId);

        addRecipeIcon.setOnClickListener(v -> startActivity(new Intent(RecipeDetails.this, addNewRecipe.class)));

        profileIcon.setOnClickListener(v -> startActivity(new Intent(RecipeDetails.this, Profile.class)));

        homeIcon.setOnClickListener(v -> startActivity(new Intent(RecipeDetails.this, MainActivity.class)));

        shareIcon.setOnClickListener(v -> {
            if (currentRecipe != null) {
                shareRecipe(currentRecipe);
            }
        });

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Recipes recipe = dataSnapshot.getValue(Recipes.class);
                if (recipe != null) {
                    currentRecipe = recipe;
                    populateRecipeDetails(recipe);
                } else {
                    Toast.makeText(RecipeDetails.this, "Recipe not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(RecipeDetails.this, "Failed to load recipe details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateRecipeDetails(Recipes recipe) {
        recipeNameTextView.setText(recipe.getRecipeName());
        cuisineAndTimeTextView.setText(recipe.getRecipeCuisine() + ", " + recipe.getRecipePreparationTime() + " Minutes");
        categoryTextView.setText(recipe.getRecipeCategory());
        ingredientsTextView.setText(recipe.getRecipeIngredients());

        DatabaseReference creatorRef = FirebaseDatabase.getInstance().getReference("Users").child(recipe.getCreatorId());
        creatorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users creator = dataSnapshot.getValue(Users.class);
                if (creator != null) {
                    String creatorName = creator.getfName() + " " + creator.getlName();
                    creatorTextView.setText("Created by " + creatorName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        String creationTime = sdf.format(recipe.getCreationTime());
        creationTimeTextView.setText(creationTime);

        String imageUrl = recipe.getRecipeImageURL();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(RecipeDetails.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(recipeImageView);
        }
    }

    private void shareRecipe(Recipes recipe) {
        String shareText = "Check out this recipe: " + recipe.getRecipeName() + "\n" +
                "Cuisine: " + recipe.getRecipeCuisine() + "\n" +
                "Preparation Time: " + recipe.getRecipePreparationTime() + " minutes.";

        String imageUrl = recipe.getRecipeImageURL();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            try {
                                File cachePath = new File(getExternalCacheDir(), "shared_images");
                                cachePath.mkdirs();
                                File file = new File(cachePath, "recipe_image.png");
                                FileOutputStream stream = new FileOutputStream(file);
                                resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                stream.close();

                                Uri imageUri = FileProvider.getUriForFile(RecipeDetails.this, getPackageName() + ".fileprovider", file);

                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/*");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                startActivity(Intent.createChooser(shareIntent, "Share recipe via"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                        }
                    });
        } else {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "Share recipe via"));
        }
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }
}
