package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.freefood.databinding.ActivityCreateDetailBinding;

public class CreateDetailActivity extends AppCompatActivity {

    ActivityCreateDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}