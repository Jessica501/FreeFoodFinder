package com.example.freefood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.freefood.databinding.ActivityMainBinding;
import com.example.freefood.fragments.ComposeFragment;
import com.example.freefood.fragments.MapFragment;
import com.example.freefood.fragments.ProfileFragment;
import com.example.freefood.fragments.StreamFragment;
import com.example.freefood.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        final FragmentManager fragmentManager = getSupportFragmentManager();
        // handle navigation selection
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_stream:
                        fragment = new StreamFragment();
                        break;
                    case R.id.action_map:
                        fragment = new MapFragment();
                        break;
                    case R.id.action_compose:
                        fragment = new ComposeFragment();
                        break;
                    case R.id.action_profile:
                        fragment = new ProfileFragment();
                        break;
                    default:
                        fragment = new StreamFragment();
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });
        // default selected is stream
        binding.bottomNavigation.setSelectedItemId(R.id.action_stream);

    }
}