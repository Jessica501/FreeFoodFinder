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

        HashSet<String> initialFilter = (HashSet<String>) getIntent().getExtras().getSerializable("currentFilter");
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            String allergen = String.valueOf(chip.getText()).toLowerCase();
            if (initialFilter.contains(allergen)) {
                chip.setChecked(true);
            }
        }

        double initialMaxDistance = getIntent().getExtras().getDouble("currentMaxDistance");
        binding.slider.setValue((float) initialMaxDistance);
        toggleSliderHighlight();
        toggleChipHighlight();

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

                double maxDistance = binding.slider.getValue();

                Intent data = new Intent();
                data.putExtra("filter", filter);
                data.putExtra("maxDistance",maxDistance);
                setResult(RESULT_OK, data);
                finish();
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
                for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                    chip.setChecked(false);
                }
                toggleChipHighlight();

                binding.slider.setValue(0);
                toggleSliderHighlight();
            }
        });
        View.OnClickListener chipClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleChipHighlight();
            }
        };
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            chip.setOnClickListener(chipClickListener);
        }
        binding.chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                Toast.makeText(FilterActivity.this, "woo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleChipHighlight() {
        List<Integer> checkedChipIds = binding.chipGroup.getCheckedChipIds();

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
}