package com.example.kozinti.adapters;

import android.content.Context;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
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
import java.util.ArrayList;
import java.util.List;


public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private List<Recipes> recipeList;
    private List<Recipes> filteredList;
    private Context context;
    private boolean isSearchActive;
    private DatabaseReference usersRef;

    private static final String TAG = "RecipeAdapter";

    public RecipeAdapter(List<Recipes> recipeList, DatabaseReference usersRef) {
        this.recipeList = recipeList;
        this.filteredList = new ArrayList<>(recipeList);
        this.isSearchActive = false;
        this.usersRef = usersRef;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipes recipes = isSearchActive ? filteredList.get(position) : recipeList.get(position);
        holder.bind(recipes);
    }

    @Override
    public int getItemCount() {
        return isSearchActive ? filteredList.size() : recipeList.size();
    }

    public void updateList(List<Recipes> newList) {
        this.recipeList = newList;
        this.filteredList = new ArrayList<>(newList);
        isSearchActive = false;
        notifyDataSetChanged();
    }

    public void filterList(List<Recipes> filteredList) {
        this.filteredList = filteredList;
        isSearchActive = true;
        notifyDataSetChanged();
    }

    public void resetSearch() {
        isSearchActive = false;
        notifyDataSetChanged();
    }

    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        private TextView recipeNameTextView;
        private TextView cuisineAndTimeTextView;
        private ImageView recipeImageView;
        private ImageView shareIcon;
        private ImageView detailsIcon;
        private TextView creatorTextView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeNameTextView = itemView.findViewById(R.id.recipe_name);
            cuisineAndTimeTextView = itemView.findViewById(R.id.RecipeCuisine_RecipePreparationTime);
            recipeImageView = itemView.findViewById(R.id.recipe_image);
            shareIcon = itemView.findViewById(R.id.Share);
            detailsIcon = itemView.findViewById(R.id.details);
            creatorTextView = itemView.findViewById(R.id.RecipeCreator);

            shareIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipes recipe = isSearchActive ? filteredList.get(position) : recipeList.get(position);
                    shareRecipe(recipe);
                }
            });

            detailsIcon.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Recipes recipe = isSearchActive ? filteredList.get(position) : recipeList.get(position);
                    String recipeId = recipe.getRecipeId();
                    Intent intent = new Intent(context, RecipeDetails.class);
                    intent.putExtra("recipeId", recipeId);
                    context.startActivity(intent);
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
                    Log.e(TAG, "onCancelled: " + databaseError.getMessage());
                }
            });
        }

        private void shareRecipe(Recipes recipe) {
            String shareText = "Check out this recipe: " + recipe.getRecipeName() + "\n" +
                    "Cuisine: " + recipe.getRecipeCuisine() + "\n" +
                    "Preparation Time: " + recipe.getRecipePreparationTime() + " minutes.";

            // Share image
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
                                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(Intent.createChooser(shareIntent, "Share recipe via"));
                                } catch (IOException e) {
                                    Log.e(TAG, "shareRecipe: Error writing bitmap", e);
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

    }
}
