package com.example.freefood.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.example.freefood.FilterActivity;
import com.example.freefood.PostsAdapter;
import com.example.freefood.R;
import com.example.freefood.databinding.FragmentStreamBinding;
import com.example.freefood.models.Post;
import com.google.android.material.chip.Chip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.freefood.Utils.queryPosts;


public class StreamFragment extends Fragment {

    private static final String TAG = "StreamFragment";
    private static final int FILTER_REQUEST_CODE = 609;
    FragmentStreamBinding binding;

    protected PostsAdapter adapter;

    public StreamFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStreamBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set adapter and layout manager for recycler view
        adapter = new PostsAdapter(getContext());
        binding.rvPosts.setAdapter(adapter);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        queryPosts(adapter);

        // add refresh listener to swipe container
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPosts(adapter);
                binding.swipeContainer.setRefreshing(false);
            }
        });

        binding.swipeContainer.setColorSchemeResources(R.color.secondaryColor,
                R.color.secondaryDarkColor,
                R.color.secondaryLightColor);

        binding.fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), FilterActivity.class);
                i.putExtra("currentAllergens", adapter.getAllergens());
                i.putExtra("currentMaxDistance", adapter.getMaxDistance());
                i.putExtra("currentTags", adapter.getTags());
                startActivityForResult(i, FILTER_REQUEST_CODE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        queryPosts(adapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                HashSet<String> allergens = (HashSet<String>) data.getExtras().getSerializable("allergens");
                double maxDistance = data.getExtras().getDouble("maxDistance");
                HashSet<String> tags = (HashSet<String>) data.getExtras().getSerializable("tags");


                try {
                    adapter.filter(allergens, maxDistance, tags);
                } catch (JSONException e) {
                    Log.e(TAG, "error filtering posts", e);
                }
                toggleChipVisibility(allergens, maxDistance, tags);
            }
        }
    }

    private void toggleChipVisibility(HashSet<String> allergens, double maxDistance, HashSet<String> tags) {
        Log.i(TAG, String.valueOf(maxDistance));
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            String chipText = String.valueOf(chip.getText()).toLowerCase();
            // allergen chip
            if (chipText.startsWith("no")) {
                String allergen = chipText.substring(3);
                if (allergens.contains(allergen)) {
                    chip.setVisibility(View.VISIBLE);
                    Log.i(TAG, allergen + " is visible");
                } else {
                    chip.setVisibility(View.GONE);
                    Log.i(TAG, allergen + " is not visible");
                }
            }
            // distance chip
            else if (chipText.startsWith("within")){
                if (maxDistance > 0) {
                    chip.setVisibility(View.VISIBLE);
                    double roundedMaxDistance = Math.round(maxDistance * 10.0) / 10.0;
                    chip.setText("Within " + roundedMaxDistance + " miles");
                } else {
                    chip.setVisibility(View.GONE);
                }
            }

            // tag chip
            else {
                if (tags.contains(chipText)) {
                    chip.setVisibility(View.VISIBLE);
                } else {
                    chip.setVisibility(View.GONE);
                }
            }
        }
    }
}