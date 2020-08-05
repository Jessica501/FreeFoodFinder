package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.example.freefood.databinding.ActivityFilterBinding;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONException;

import java.util.HashSet;
import java.util.List;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = "FilterActivity";
    ActivityFilterBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFilterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeFilters();

        binding.btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passFiltersToActivityResult();
            }
        });

        binding.slider.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                toggleSliderHighlight();
                return false;
            }
        });

        binding.btnClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearFilters();
            }
        });

        View.OnClickListener allergenChipClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAllergenChipHighlight();
            }
        };
        for (int i = 0; i < binding.cgAllergens.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgAllergens.getChildAt(i);
            chip.setOnClickListener(allergenChipClickListener);
        }

        View.OnClickListener tagChipClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleTagChipHighlight();
            }
        };
        for (int i = 0; i < binding.cgTags.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgTags.getChildAt(i);
            chip.setOnClickListener(tagChipClickListener);
        }
    }

    private void clearFilters() {
        for (int i = 0; i < binding.cgAllergens.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgAllergens.getChildAt(i);
            chip.setChecked(false);
        }
        toggleAllergenChipHighlight();

        binding.slider.setValue(0);
        toggleSliderHighlight();

        for (int i = 0; i < binding.cgTags.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgTags.getChildAt(i);
            chip.setChecked(false);
        }
        toggleTagChipHighlight();
    }

    private void passFiltersToActivityResult() {
        HashSet<String> allergens = new HashSet<>();
        for (int i = 0; i < binding.cgAllergens.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgAllergens.getChildAt(i);
            if (chip.isChecked()) {
                allergens.add(String.valueOf(chip.getText()).toLowerCase());
            }
        }
        Log.i(TAG, String.valueOf(allergens));

        double maxDistance = binding.slider.getValue();

        HashSet<String> tags = new HashSet<>();
        for (int i = 0; i < binding.cgTags.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgTags.getChildAt(i);
            if (chip.isChecked()) {
                tags.add(String.valueOf(chip.getText()).toLowerCase());
            }
        }

        Intent data = new Intent();
        data.putExtra("allergens", allergens);
        data.putExtra("maxDistance",maxDistance);
        data.putExtra("tags", tags);
        setResult(RESULT_OK, data);
        finish();
    }

    private void initializeFilters() {
        HashSet<String> initialAllergenFilter = (HashSet<String>) getIntent().getExtras().getSerializable("currentAllergens");
        for (int i = 0; i < binding.cgAllergens.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgAllergens.getChildAt(i);
            String allergen = String.valueOf(chip.getText()).toLowerCase();
            if (initialAllergenFilter.contains(allergen)) {
                chip.setChecked(true);
            }
        }

        double initialMaxDistance = getIntent().getExtras().getDouble("currentMaxDistance");
        binding.slider.setValue((float) initialMaxDistance);
        toggleSliderHighlight();
        toggleAllergenChipHighlight();
        toggleTagChipHighlight();

        HashSet<String> initialTagFilter = (HashSet<String>) getIntent().getExtras().getSerializable("currentTags");
        for (int i = 0; i < binding.cgTags.getChildCount(); i++) {
            Chip chip = (Chip) binding.cgTags.getChildAt(i);
            String tag = String.valueOf(chip.getText()).toLowerCase();
            if (initialTagFilter.contains(tag)) {
                chip.setChecked(true);
            }
        }
    }

    private void toggleAllergenChipHighlight() {
        List<Integer> checkedChipIds = binding.cgAllergens.getCheckedChipIds();

        if (checkedChipIds.size() > 0) {
            binding.tvAllergens.setTextColor(getResources().getColor(R.color.primaryTextColor));
        } else {
            binding.tvAllergens.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    private void toggleSliderHighlight() {
        if (binding.slider.getValue() != 0) {
            binding.tvDistance.setTextColor(getResources().getColor(R.color.primaryTextColor));
        } else {
            binding.tvDistance.setTextColor(getResources().getColor(R.color.gray));
        }
    }

    private void toggleTagChipHighlight() {
        List<Integer> checkedChipIds = binding.cgTags.getCheckedChipIds();

        if (checkedChipIds.size() > 0) {
            binding.tvTags.setTextColor(getResources().getColor(R.color.primaryTextColor));
        } else {
            binding.tvTags.setTextColor(getResources().getColor(R.color.gray));
        }
    }
}