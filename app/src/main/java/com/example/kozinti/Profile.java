package com.example.kozinti;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kozinti.tables.Recipes;
import com.example.kozinti.tables.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Profile extends AppCompatActivity {

    private static final int EDIT_PROFILE_REQUEST = 1;

    private ImageView userImage;
    private TextView userFirstName, userLastName, userBirthdate, userEmail, joiningDate, sharedRecipes;
    private DatabaseReference userRef;
    private Context context;
    private DatabaseReference recipesRef;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        userImage = findViewById(R.id.recipe_image);
        userFirstName = findViewById(R.id.userFirstName);
        userLastName = findViewById(R.id.userLastName);
        userBirthdate = findViewById(R.id.userBirthdate);
        userEmail = findViewById(R.id.userEmail);
        joiningDate = findViewById(R.id.joiningDate);
        sharedRecipes = findViewById(R.id.sharedRecipes);

        setStatusBarColor(getResources().getColor(R.color.black));

        context = this;

        recipesRef = FirebaseDatabase.getInstance().getReference("Recipes");
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(Users.ConnectedUser);
        storageRef = FirebaseStorage.getInstance().getReference();

        findViewById(R.id.viewRecipes).setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, YourRecipes.class));
        });

        findViewById(R.id.editProfile).setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditProfile.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        findViewById(R.id.deleteProfile).setOnClickListener(v -> {
            showDeleteConfirmationDialog();
        });

        findViewById(R.id.logout).setOnClickListener(v -> {
            Users.ConnectedUser = null;
            startActivity(new Intent(Profile.this, Login.class));
            finish();
        });

        ImageView homeIcon = findViewById(R.id.home);
        homeIcon.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, MainActivity.class));
            finish();
        });

        ImageView addRecipeIcon = findViewById(R.id.addRecipe);
        addRecipeIcon.setOnClickListener(v -> {
            startActivity(new Intent(Profile.this, addNewRecipe.class));
            finish();
        });

        loadUserData();
    }

    private void loadUserData() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    if (user != null) {
                        userFirstName.setText("First Name: " + user.getfName());
                        userLastName.setText("Last Name: " + user.getlName());
                        userBirthdate.setText("Birthdate: " + user.getBirthdate());
                        userEmail.setText("Email: " + user.getEmail());

                        // Format and set the joining date
                        String formattedJoiningDate = formatDateTime(user.getJoiningDate());
                        joiningDate.setText("Joining Date: " + formattedJoiningDate);

                        // Update the sharedRecipes text with the actual count
                        getSharedRecipeCount(user.getId());

                        String imageUrl = user.getUserImg();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.placeholder_image)
                                    .error(R.drawable.error_image)
                                    .into(userImage);
                        }
                    }
                }
            } else {
                // Handle the case when the task is not successful
            }
        });
    }

    private String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat desiredFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        return desiredFormat.format(date);
    }

    private void getSharedRecipeCount(String userId) {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("Recipes");
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int recipeCount = 0;
                for (DataSnapshot recipeSnapshot: dataSnapshot.getChildren()) {
                    Recipes recipe = recipeSnapshot.getValue(Recipes.class);
                    if (recipe != null && recipe.getCreatorId().equals(userId)) {
                        recipeCount++;
                    }
                }
                sharedRecipes.setText("You shared: " + recipeCount + " Recipes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Log error or handle cancellation
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
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete your account?");
        builder.setPositiveButton("Yes, I'm sure", (dialog, which) -> {
            deleteUserAccount();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void deleteUserAccount() {
        userRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Account deleted successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Profile.this, Login.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(context, "Failed to delete account", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
