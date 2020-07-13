package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.freefood.databinding.ActivityPostDetailBinding;

public class PostDetailActivity extends AppCompatActivity {

    ActivityPostDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}