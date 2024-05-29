package com.example.kozinti;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kozinti.databinding.ActivityRegisterBinding;
import com.example.kozinti.tables.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity {
    private static final String TAG = "Register";

    ActivityRegisterBinding binding;
    String fName, lName, birthdate, email, confirmEmail, password, confirmPassword;
    FirebaseDatabase db;
    DatabaseReference reference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private ImageView imageViewUserImage;
    private String userImageUrl;

    private Spinner daySpinner, monthSpinner, yearSpinner;
    private boolean isRegistering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setStatusBarColor(getResources().getColor(R.color.black));

        daySpinner = findViewById(R.id.daySpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        yearSpinner = findViewById(R.id.yearSpinner);
        imageViewUserImage = findViewById(R.id.imageViewUserImage);

        initializeSpinners();

        imageViewUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        db = FirebaseDatabase.getInstance();
        reference = db.getReference("Users");

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRegistering) return; // Prevent multiple clicks
                isRegistering = true;
                binding.btnRegister.setEnabled(false); // Disable the button

                fName = binding.fName.getText().toString();
                lName = binding.lName.getText().toString();
                birthdate = getSelectedDate();
                email = binding.email.getText().toString().trim();
                confirmEmail = binding.confirmEmail.getText().toString().trim();
                password = binding.password.getText().toString();
                confirmPassword = binding.confirmPassword.getText().toString();

                if (!fName.isEmpty() && !lName.isEmpty() && !email.isEmpty() && !confirmEmail.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
                    if (isValidEmail(email)) {
                        if (email.equals(confirmEmail)) {
                            if (isStrongPassword(password)) {
                                if (isPasswordMatching(password, confirmPassword)) {
                                    if (imageUri != null) {
                                        uploadImageAndRegisterUser();
                                    } else {
                                        Toast.makeText(Register.this, "Please select an image", Toast.LENGTH_SHORT).show();
                                        resetRegisterButton();
                                    }
                                } else {
                                    Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                                    resetRegisterButton();
                                }
                            } else {
                                Toast.makeText(Register.this, "Use a stronger Password", Toast.LENGTH_SHORT).show();
                                resetRegisterButton();
                            }
                        } else {
                            Toast.makeText(Register.this, "Emails are not identical", Toast.LENGTH_SHORT).show();
                            resetRegisterButton();
                        }
                    } else {
                        Toast.makeText(Register.this, "Invalid email", Toast.LENGTH_SHORT).show();
                        resetRegisterButton();
                    }
                } else {
                    Toast.makeText(Register.this, "Please enter all values", Toast.LENGTH_SHORT).show();
                    resetRegisterButton();
                }
            }
        });

        binding.loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        setupPasswordVisibilityToggle(binding.password);
        setupPasswordVisibilityToggle(binding.confirmPassword);
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

    public static boolean isStrongPassword(String password) {
        String regexPattern = "^(?=.*[A-Z]).{8,}$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean isValidEmail(String email) {
        String regexPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public static boolean isPasswordMatching(String password, String confirmPassword) {
        return password.equals(confirmPassword);
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
            imageViewUserImage.setImageURI(imageUri);
        }
    }

    private void uploadImageAndRegisterUser() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("user_images");
        String imageFileName = UUID.randomUUID().toString();
        storageRef.child(imageFileName).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    storageRef.child(imageFileName).getDownloadUrl().addOnSuccessListener(uri -> {
                        userImageUrl = uri.toString();
                        registerUser();
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Register.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    resetRegisterButton();
                });
    }

    private void registerUser() {
        String userId = reference.push().getKey();
        Date joiningDate = Calendar.getInstance().getTime();
        Users user = new Users(userId, fName, lName, birthdate, email, password, userImageUrl, joiningDate);

        reference.child(userId).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(Register.this, "Successfully Added", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Register.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
                resetRegisterButton();
            }
        });
    }

    private void resetRegisterButton() {
        new CountDownTimer(2000, 1000) {
            public void onTick(long millisUntilFinished) {
                // You can use this to display remaining time if needed
            }

            public void onFinish() {
                isRegistering = false;
                binding.btnRegister.setEnabled(true);
            }
        }.start();
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(color);
        }
    }

    private void initializeSpinners() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        List<String> days = new ArrayList<>();
        for (int i = 1; i <= 31; i++) {
            days.add(String.valueOf(i));
        }
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setSelection(currentDay - 1);

        List<String> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(String.valueOf(i));
        }
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(currentMonth - 1);

        List<String> years = new ArrayList<>();
        for (int i = 1900; i <= currentYear; i++) {
            years.add(String.valueOf(i));
        }
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setSelection(yearAdapter.getPosition(String.valueOf(currentYear)));
    }

    private String getSelectedDate() {
        String day = daySpinner.getSelectedItem().toString();
        String month = monthSpinner.getSelectedItem().toString();
        String year = yearSpinner.getSelectedItem().toString();
        return day + "-" + month + "-" + year;
    }
}
