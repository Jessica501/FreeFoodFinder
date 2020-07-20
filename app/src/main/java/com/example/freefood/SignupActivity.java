package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.freefood.databinding.ActivitySignupBinding;
import com.example.freefood.models.User;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    private static final String TAG = "SignupActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Switch to LoginActivity
        binding.tvHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            }
        });

        // handle click on sign up button
        binding.btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = String.valueOf(binding.etName.getText());
                String username = String.valueOf(binding.etUsername.getText());
                String password = String.valueOf(binding.etPassword.getText());
                String confirmPassword = String.valueOf(binding.etConfirmPassword.getText());
                if (validateUser(name, username, password, confirmPassword)) {
                    signUpUser(name, username, password);
                }
            }
        });
    }

    private boolean validateUser(String name, String username, String password, String confirmPassword) {
        if (name.trim().isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } if (username.trim().isEmpty()) {
            Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } if (password.trim().isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        } if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void signUpUser(String name, String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.i(TAG, "Successfully signed up");
                    Intent i = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
                // error handling
                else if (e.getCode() == ParseException.USERNAME_TAKEN) {
                    Toast.makeText(SignupActivity.this, "Username taken. Try again!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignupActivity.this, "Error signing up. Check log.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error signing up" + e.getCode(), e);
                }
            }
        });
    }
}