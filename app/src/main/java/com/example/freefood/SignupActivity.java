package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.freefood.databinding.ActivitySignupBinding;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class SignupActivity extends AppCompatActivity {

    ActivitySignupBinding binding;
    public static final String TAG = "SignupActivity";

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
                String username = String.valueOf(binding.etUsername.getText());
                String password = String.valueOf(binding.etPassword.getText());
                String name = String.valueOf(binding.etName.getText());
                signUpUser(username, password, name);
            }
        });
    }

    private void signUpUser(String username, String password, String name) {
        ParseUser parseUser = new ParseUser();
        parseUser.setUsername(username);
        parseUser.setPassword(password);
        parseUser.put("name", name);
        parseUser.signUpInBackground(new SignUpCallback() {
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