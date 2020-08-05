package com.example.freefood.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.freefood.databinding.ActivityLoginBinding;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // log in persistence
        if (ParseUser.getCurrentUser() != null) {
            goToMainActivity();
        }

        // Switch to SignupActivity
        binding.tvNoAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), SignupActivity.class);
                startActivity(i);
            }
        });

        // Handle click on log in button
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = String.valueOf(binding.etUsername.getText());
                String password = String.valueOf(binding.etPassword.getText());
                logInUser(username, password);
            }
        });
    }

    private void logInUser(String username, String password) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e == null) {
                    Log.i(TAG, "Successfully logged in");
                    goToMainActivity();
                }
                // error handling
                else if (e.getCode() == ParseException.USERNAME_MISSING) {
                    Toast.makeText(LoginActivity.this, "Username cannot be empty.", Toast.LENGTH_SHORT).show();
                } else if (e.getCode() == ParseException.PASSWORD_MISSING) {
                    Toast.makeText(LoginActivity.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
                } else if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                    Toast.makeText(LoginActivity.this, "Invalid username/password.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Error logging in. Check log.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error logging in" + e.getCode(), e);
                }
            }
        });
    }

    private void goToMainActivity() {
        Intent i = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}