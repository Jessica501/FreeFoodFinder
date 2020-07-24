package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.freefood.databinding.ActivityFilterBinding;
import com.google.android.material.chip.Chip;

import org.json.JSONException;

import java.util.HashSet;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "FilterActivity";
    ActivityFilterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        HashSet<String> currentFilter = (HashSet<String>) getIntent().getExtras().getSerializable("currentFilter");
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            String allergen = String.valueOf(chip.getText()).toLowerCase();
            if (currentFilter.contains(allergen)) {
                chip.setChecked(true);
            }
        }

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashSet<String> filter = new HashSet<>();
                for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                    if (chip.isChecked()) {
                        filter.add(String.valueOf(chip.getText()).toLowerCase());
                    }
                }
                Log.i(TAG, String.valueOf(filter));
                Intent data = new Intent();
                data.putExtra("filter", filter);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }
}