package com.example.freefood.fragments;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.transition.Fade;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.freefood.activities.LoginActivity;
import com.example.freefood.activities.SignupActivity;
import com.example.freefood.adapters.PostsAdapter;
import com.example.freefood.R;
import com.example.freefood.activities.SettingsActivity;
import com.example.freefood.databinding.FragmentProfileBinding;
import com.example.freefood.models.User;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseUser;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

import static com.example.freefood.utils.Utils.queryPosts;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    protected PostsAdapter adapter;
    User user;

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
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle();
                startActivity(i,bundle);
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
        loadUserFields();

        // set adapter and layout manager for recycler view
        adapter = new PostsAdapter(getContext());
        binding.rvPosts.setAdapter(adapter);
        binding.rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        new ItemTouchHelper(itemTouchHelpeerCallback).attachToRecyclerView(binding.rvPosts);
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

        binding.swipeContainer.setColorSchemeResources(R.color.secondaryColor,
                R.color.secondaryDarkColor,
                R.color.secondaryLightColor);
    }

    private void loadUserFields() {
        user = (User) ParseUser.getCurrentUser();
        binding.tvName.setText(user.getName());
        binding.tvUsername.setText("@" + user.getUsername());
        Glide.with(getContext())
                .load(user.getProfileImage().getUrl())
                .circleCrop()
                .into(binding.ivProfile);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserFields();
        queryPosts(adapter, ParseUser.getCurrentUser());
    }

    ItemTouchHelper.SimpleCallback itemTouchHelpeerCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            ((PostsAdapter.ViewHolder)viewHolder).markClaimed();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(getContext(), R.color.secondaryColor))
                    .addActionIcon(R.drawable.ic_baseline_check_24)
                    .create()
                    .decorate();
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}