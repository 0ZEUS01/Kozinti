package com.example.kozinti.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.kozinti.EditRecipe;
import com.example.kozinti.R;
import com.example.kozinti.RecipeDetails;
import com.example.kozinti.tables.Recipes;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class UserRecipeAdapter extends RecyclerView.Adapter<UserRecipeAdapter.RecipeViewHolder> {
    private List<Recipes> recipeList;
    private Context context;
    private DatabaseReference usersRef;
    private DatabaseReference recipesRef;

    public UserRecipeAdapter(List<Recipes> recipeList, DatabaseReference usersRef, DatabaseReference recipesRef) {
        this.recipeList = recipeList;
        this.usersRef = usersRef;
        this.recipesRef = recipesRef;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.your_recipe_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipes recipes = recipeList.get(position);
        holder.bind(recipes);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private TextView recipeNameTextView;
        private TextView cuisineAndTimeTextView;
        private ImageView recipeImageView;
        private ImageView shareIcon;
        private ImageView detailsIcon;
        private ImageView editIcon;
        private ImageView deleteIcon;
        private TextView creatorTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.recipe_name);
            cuisineAndTimeTextView = itemView.findViewById(R.id.RecipeCuisine_RecipePreparationTime);
            recipeImageView = itemView.findViewById(R.id.recipe_image);
            shareIcon = itemView.findViewById(R.id.Share);
            detailsIcon = itemView.findViewById(R.id.details);
            editIcon = itemView.findViewById(R.id.edit);
            deleteIcon = itemView.findViewById(R.id.delete);
            creatorTextView = itemView.findViewById(R.id.RecipeCreator);

            shareIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipes recipe = recipeList.get(position);
                    shareRecipe(recipe);
                }
            });

            detailsIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipes recipe = recipeList.get(position);
                    String recipeId = recipe.getRecipeId();
                    Intent intent = new Intent(context, RecipeDetails.class);
                    intent.putExtra("recipeId", recipeId);
                    context.startActivity(intent);
                }
            });

            editIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipes recipe = recipeList.get(position);
                    String recipeId = recipe.getRecipeId();
                    if (recipeId != null) {
                        Intent intent = new Intent(context, EditRecipe.class);
                        intent.putExtra("recipeId", recipeId);
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Recipe ID is null", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            deleteIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipes recipe = recipeList.get(position);
                    showDeleteConfirmationDialog(recipe);
                }
            });
        }

        public void bind(Recipes recipes) {
            recipeNameTextView.setText(recipes.getRecipeName());
            String cuisineAndTime = recipes.getRecipeCuisine() + ", " + recipes.getRecipePreparationTime() + " Minutes.";
            cuisineAndTimeTextView.setText(cuisineAndTime);

            String imageUrl = recipes.getRecipeImageURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(recipeImageView);
            }
            usersRef.child(recipes.getCreatorId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String creatorName = dataSnapshot.child("fName").getValue(String.class);
                        if (creatorName != null) {
                            creatorTextView.setText("Created by " + creatorName);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }

        private void shareRecipe(Recipes recipe) {
            String shareText = "Check out this recipe: " + recipe.getRecipeName() + "\n" +
                    "Cuisine: " + recipe.getRecipeCuisine() + "\n" +
                    "Preparation Time: " + recipe.getRecipePreparationTime() + " minutes.";

            String imageUrl = recipe.getRecipeImageURL();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .asBitmap()
                        .load(imageUrl)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                try {
                                    File cachePath = new File(context.getExternalCacheDir(), "shared_images");
                                    cachePath.mkdirs();
                                    File file = new File(cachePath, "recipe_image.png");
                                    FileOutputStream stream = new FileOutputStream(file);
                                    resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                                    stream.close();

                                    Uri imageUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);

                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.setType("image/*");
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                                    context.startActivity(Intent.createChooser(shareIntent, "Share recipe via"));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            } else {
                // If no image, share only text
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                context.startActivity(Intent.createChooser(shareIntent, "Share recipe via"));
            }
        }

        private void showDeleteConfirmationDialog(Recipes recipe) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Confirm Deletion");
            builder.setMessage("Are you sure you want to delete this recipe?");
            builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteRecipe(recipe);
                }
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }

        private void deleteRecipe(Recipes recipe) {
            String recipeId = recipe.getRecipeId();
            if (recipeId == null) {
                Toast.makeText(context, "Recipe ID is null", Toast.LENGTH_SHORT).show();
                return;
            }
            DatabaseReference recipeRef = recipesRef.child(recipeId);
            recipeRef.removeValue().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    int position = getAdapterPosition();
                    recipeList.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
