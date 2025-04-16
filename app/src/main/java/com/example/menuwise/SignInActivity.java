package com.example.menuwise;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.menuwise.databinding.ActivitySignInBinding;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth auth;
    private AlertDialog loadingDialog;

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

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Login Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .setCancelable(false);
        builder.create().show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.emailEditText.getText()).toString();
            String password = Objects.requireNonNull(binding.passwordEditText.getText()).toString();

            binding.emailInputLayout.setError(null);
            binding.passwordInputLayout.setError(null);

            if (email.isEmpty() || password.isEmpty()) {
                if (email.isEmpty()) {
                    binding.emailInputLayout.setError("Email required");
                }
                if (password.isEmpty()) {
                    binding.passwordInputLayout.setError("Password required");
                }
                return;
            }
            if (!isValidEmail(email)) {
                binding.emailInputLayout.setError("Invalid email");
                return;
            }
            showLoadingDialog();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        hideLoadingDialog();

                        if (task.isSuccessful()) {
                            startActivity(new Intent(this, FoodMenuActivity.class));
                            finish();
                        } else {
                            Exception exception = task.getException();

                            assert exception != null;
                            showErrorDialog(exception.getMessage() != null ? exception.getMessage() : "Login failed");
                        }
                    });
        });

        binding.signupButton.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }

    private boolean isValidEmail(String email) {
        return Pattern.matches("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",email);
    }
}
