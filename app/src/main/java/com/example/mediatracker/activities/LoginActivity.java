package com.example.mediatracker.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.example.mediatracker.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends BaseActivity {
    private EditText etEmail, etPassword;
    private View btnLogin;
    private TextView tvSignUp;
    private SharedPreferences prefs;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize SharedPreferences
        prefs = getSharedPreferences("MediaTrackerPrefs", Context.MODE_PRIVATE);

        // Check if user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToHome();
            return;
        }

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(v -> loginUser());
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show loading state
        btnLogin.setEnabled(false);

        // Sign in with Firebase Auth
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        btnLogin.setEnabled(true);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Get user name from Firebase Realtime Database
                                DatabaseReference userRef = FirebaseDatabase.getInstance()
                                        .getReference("users")
                                        .child(user.getUid())
                                        .child("name");
                                
                                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String userName = snapshot.getValue(String.class);
                                        if (userName == null) {
                                            userName = user.getDisplayName() != null ? 
                                                    user.getDisplayName() : user.getEmail();
                                        }

                                        // Save to SharedPreferences
                                        prefs.edit()
                                                .putString("user_id", user.getUid())
                                                .putString("user_email", user.getEmail())
                                                .putString("user_name", userName)
                                                .putBoolean("is_logged_in", true)
                                                .apply();
                                        
                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        navigateToHome();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // If we can't get name, use email
                                        prefs.edit()
                                                .putString("user_id", user.getUid())
                                                .putString("user_email", user.getEmail())
                                                .putString("user_name", user.getEmail())
                                                .putBoolean("is_logged_in", true)
                                                .apply();
                                        
                                        Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                        navigateToHome();
                                    }
                                });
                            }
                        } else {
                            String errorMessage = "Login failed";
                            if (task.getException() != null) {
                                errorMessage = task.getException().getMessage();
                            }
                            Toast.makeText(LoginActivity.this, errorMessage != null ? 
                                    errorMessage : "Invalid email or password", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

