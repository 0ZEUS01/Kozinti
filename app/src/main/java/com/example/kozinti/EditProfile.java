package com.example.kozinti;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kozinti.tables.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class EditProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView userImage;
    private TextInputEditText fName, lName, email, password, newPassword, confirmPassword;
    private TextView updatePassword;
    private Button btnEdit;
    private DatabaseReference userRef;
    private Context context;
    private StorageReference storageRef;
    private Users currentUser;
    private Spinner daySpinner, monthSpinner, yearSpinner;
    private Uri imageUri;
    private ImageView ProfileIcon, homeIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        setStatusBarColor(getResources().getColor(R.color.black));

        userImage = findViewById(R.id.imageViewUserImage);
        fName = findViewById(R.id.fName);
        lName = findViewById(R.id.lName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        newPassword = findViewById(R.id.newPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        updatePassword = findViewById(R.id.updatePassword);
        btnEdit = findViewById(R.id.btnEdit);
        ProfileIcon = findViewById(R.id.profileIcon);
        homeIcon = findViewById(R.id.home);

        daySpinner = findViewById(R.id.daySpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);

        context = this;

        userRef = FirebaseDatabase.getInstance().getReference("Users");
        if (Users.ConnectedUser != null) {
            userRef = userRef.child(Users.ConnectedUser);
        } else {
            // Log the error message instead of showing a toast
            Log.e("EditProfile", "!!!!!!!!!!!!!!!!!!!User is not connected!!!!!!!!!!!!!!!!!");
            finish(); // Finish the activity if user is not connected
        }


        storageRef = FirebaseStorage.getInstance().getReference();

        loadUserData();

        initializeSpinners();

        updatePassword.setOnClickListener(v -> {
            newPassword.setVisibility(View.VISIBLE);
            confirmPassword.setVisibility(View.VISIBLE);
        });

        btnEdit.setOnClickListener(v -> {
            String oldPassword = password.getText().toString().trim();
            if (currentUser.getPassword().equals(oldPassword)) {
                showEditConfirmationDialog();
            } else {
                Toast.makeText(context, "Your password is wrong", Toast.LENGTH_SHORT).show();
            }
        });

        userImage.setOnClickListener(v -> openFileChooser());

        ProfileIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfile.this, Profile.class));
            }
        });

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(EditProfile.this, MainActivity.class));
            }
        });

        setupPasswordVisibilityToggle(password);
        setupPasswordVisibilityToggle(newPassword);
        setupPasswordVisibilityToggle(confirmPassword);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupPasswordVisibilityToggle(TextInputEditText passwordField) {
        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_off, 0);
        passwordField.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int right = passwordField.getRight() - passwordField.getCompoundDrawables()[2].getBounds().width();
                if (event.getRawX() >= right) {
                    if (passwordField.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye, 0);
                    } else {
                        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        passwordField.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.eye_off, 0);
                    }
                    passwordField.setSelection(passwordField.length());
                    return true;
                }
            }
            return false;
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                userImage.setImageBitmap(bitmap);
                uploadImageToFirebase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            StorageReference fileReference = storageRef.child("profile_pictures/" + Users.ConnectedUser + ".jpg");

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        updateUserProfilePicture(downloadUrl);
                    }))
                    .addOnFailureListener(e -> Toast.makeText(context, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(context, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUserProfilePicture(String downloadUrl) {
        currentUser.setUserImg(downloadUrl);
        userRef.setValue(currentUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Profile picture updated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot snapshot = task.getResult();
                if (snapshot.exists()) {
                    currentUser = snapshot.getValue(Users.class);
                    if (currentUser != null) {
                        fName.setText(currentUser.getfName());
                        lName.setText(currentUser.getlName());
                        email.setText(currentUser.getEmail());

                        String[] birthdateParts = currentUser.getBirthdate().split("-");
                        if (birthdateParts.length == 3) {
                            daySpinner.setSelection(((ArrayAdapter) daySpinner.getAdapter()).getPosition(birthdateParts[0]));
                            monthSpinner.setSelection(((ArrayAdapter) monthSpinner.getAdapter()).getPosition(birthdateParts[1]));
                            yearSpinner.setSelection(((ArrayAdapter) yearSpinner.getAdapter()).getPosition(birthdateParts[2]));
                        }

                        String imageUrl = currentUser.getUserImg();
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(context)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.upload_picture)
                                    .error(R.drawable.error_image)
                                    .into(userImage);
                        }
                    }
                }
            } else {
                // Handle errors
            }
        });
    }

    private void initializeSpinners() {
        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);

        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.valueOf(i));
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);

        List<String> years = new ArrayList<>();
        for (int i = 1900; i <= 2024; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
    }

    private void showEditConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Confirm Edit?");
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            updateUserProfile();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void updateUserProfile() {
        String updatedFName = fName.getText().toString().trim();
        String updatedLName = lName.getText().toString().trim();
        String updatedEmail = email.getText().toString().trim();
        String updatedPassword = currentUser.getPassword();

        String updatedBirthdate = daySpinner.getSelectedItem().toString() + "-" +
                monthSpinner.getSelectedItem().toString() + "-" +
                yearSpinner.getSelectedItem().toString();

        if (newPassword.getVisibility() == View.VISIBLE && confirmPassword.getVisibility() == View.VISIBLE) {
            String newPasswordText = newPassword.getText().toString().trim();
            String confirmPasswordText = confirmPassword.getText().toString().trim();

            if (!newPasswordText.isEmpty() && newPasswordText.equals(confirmPasswordText)) {
                updatedPassword = newPasswordText;
            } else {
                Toast.makeText(context, "New password and confirm password do not match", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        currentUser.setfName(updatedFName);
        currentUser.setlName(updatedLName);
        currentUser.setBirthdate(updatedBirthdate);
        currentUser.setEmail(updatedEmail);
        currentUser.setPassword(updatedPassword);

        userRef.setValue(currentUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(EditProfile.this, Profile.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(context, "Profile update failed", Toast.LENGTH_SHORT).show();
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
