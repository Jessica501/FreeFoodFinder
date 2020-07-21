package com.example.freefood.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.freefood.LoginActivity;
import com.example.freefood.PostsAdapter;
import com.example.freefood.R;
import com.example.freefood.SettingsActivity;
import com.example.freefood.databinding.FragmentProfileBinding;
import com.example.freefood.models.Post;
import com.example.freefood.models.User;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

import static com.example.freefood.Utils.queryPosts;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    protected PostsAdapter adapter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // go to settings
        binding.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), SettingsActivity.class);
                startActivity(i);
            }
        });

        // log out user and switch to LoginActivity
        binding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseUser.logOut();
                Intent i = new Intent(getContext(), LoginActivity.class);
                startActivity(i);
            }
        });
        final User user = (User) ParseUser.getCurrentUser();
        binding.tvName.setText(user.getName());
        binding.tvUsername.setText("@" + user.getUsername());
        Glide.with(getContext())
                .load(user.getProfileImage().getUrl())
                .circleCrop()
                .into(binding.ivProfile);

        // set adapter and layout manager for recycler view
        adapter = new PostsAdapter(getContext());
        binding.rvPosts.setAdapter(adapter);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        queryPosts(adapter, ParseUser.getCurrentUser());

        // add refresh listener to swipe container
        binding.swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // query posts again
                queryPosts(adapter, ParseUser.getCurrentUser());
                // reload profile image
                Glide.with(getContext())
                        .load(user.getProfileImage().getUrl())
                        .circleCrop()
                        .into(binding.ivProfile);
                binding.swipeContainer.setRefreshing(false);
            }
        });

        binding.swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    @Override
    public void onResume() {
        super.onResume();
        queryPosts(adapter, ParseUser.getCurrentUser());
    }
}