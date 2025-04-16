package com.example.menuwise;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.menuwise.databinding.ActivitySignUpBinding;
import com.example.menuwise.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    private ActivitySignUpBinding binding;
    private AlertDialog loadingDialog;
    private final FirebaseAuth auth=FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.signupButton.setOnClickListener(v -> {
            String fullName = Objects.requireNonNull(binding.nameEditText.getText()).toString().trim();
            String email = Objects.requireNonNull(binding.emailEditText.getText()).toString().trim();
            String phone = Objects.requireNonNull(binding.phoneEditText.getText()).toString().trim();
            String password = Objects.requireNonNull(binding.passwordEditText.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(binding.confirmPasswordEditText.getText()).toString().trim();
            binding.nameInputLayout.setError(null);
            binding.emailInputLayout.setError(null);
            binding.phoneInputLayout.setError(null);
            binding.passwordInputLayout.setError(null);
            binding.confirmPasswordInputLayout.setError(null);

            if (fullName.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                if (fullName.isEmpty()) {
                    binding.nameInputLayout.setError("Full Name is required");
                }
                if (email.isEmpty()) {
                    binding.emailInputLayout.setError("Email is required");
                }
                if (phone.isEmpty()) {
                    binding.phoneInputLayout.setError("Phone is required");
                }
                if (password.isEmpty()) {
                    binding.passwordInputLayout.setError("Password is required");
                }
                if (confirmPassword.isEmpty()) {
                    binding.confirmPasswordInputLayout.setError("Confirm Password is required");
                }
                return;
            }

            if (!isValidEmail(email)) {
                binding.emailInputLayout.setError("Invalid email format");
                return;
            }

            if (!isValidPhone(phone)) {
                binding.phoneInputLayout.setError("Invalid phone number");
                return;
            }

            if (!password.equals(confirmPassword)) {
                binding.confirmPasswordInputLayout.setError("Passwords do not match");
                return;
            }
            showLoadingDialog();
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                User userProfile = new User(fullName, phone, email);
                                FirebaseFirestore.getInstance().collection("users")
                                        .document(user.getUid())
                                        .set(userProfile)
                                        .addOnSuccessListener(aVoid -> {
                                            hideLoadingDialog();
                                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(this, FoodMenuActivity.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e ->{
                                                    hideLoadingDialog() ;
                                                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                                                }
                                        );
                            }
                        } else {
                            Toast.makeText(this, "Signup failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        binding.loginButton.setOnClickListener(v -> startActivity(new Intent(this, SignInActivity.class)));

    }
    private boolean isValidEmail(String email) {
        return Pattern.matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",email);
    }
    private boolean isValidPhone(String phone) {
        return Pattern.matches( "^[+]?[0-9]{10,13}$", phone);
    }
    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.loading, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

}