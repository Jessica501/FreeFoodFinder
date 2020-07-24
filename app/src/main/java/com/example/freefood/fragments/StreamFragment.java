package com.example.freefood.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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

        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        binding.fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), FilterActivity.class);
                i.putExtra("currentFilter", adapter.getFilter());
                i.putExtra("currentMaxDistance", adapter.getMaxDistance());
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
                HashSet<String> filter = (HashSet<String>) data.getExtras().getSerializable("filter");
                double maxDistance = data.getExtras().getDouble("maxDistance");

                try {
                    adapter.filter(filter, maxDistance);
                } catch (JSONException e) {
                    Log.e(TAG, "error filtering posts", e);
                }
                toggleChipVisibility(filter);
            }
        }
    }

    private void toggleChipVisibility(HashSet<String> filter) {
        for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroup.getChildAt(i);
            String allergen = String.valueOf(chip.getText()).toLowerCase().substring(3);
            if (filter.contains(allergen)) {
                chip.setVisibility(View.VISIBLE);
                Log.i(TAG, allergen + " is visible");
            } else {
                chip.setVisibility(View.GONE);
                Log.i(TAG, allergen + " is not visible");
            }
        }
    }
}