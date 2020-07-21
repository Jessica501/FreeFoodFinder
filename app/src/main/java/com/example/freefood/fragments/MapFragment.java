package com.example.freefood.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.freefood.MainActivity;
import com.example.freefood.PostDetailActivity;
import com.example.freefood.PostInfoWindowAdapter;
import com.example.freefood.R;
import com.example.freefood.databinding.FragmentMapBinding;
import com.example.freefood.models.Post;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "MapFragment";
    FragmentMapBinding binding;

    private GoogleMap map;
    private List<Post> posts;


    public MapFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        posts = new ArrayList<>();
        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

//        addMarkers();
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setInfoWindowAdapter(new PostInfoWindowAdapter(getContext()));

        LatLng latLng = new LatLng(MainActivity.mLocation.getLatitude(), MainActivity.mLocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 20));

        queryMapPosts();

        map.setOnInfoWindowClickListener(this);
    }

    private void addMarker(Post post) {
        LatLng latLng = new LatLng(post.getLocation().getLatitude(), post.getLocation().getLongitude());
        Marker marker = map.addMarker(new MarkerOptions()
                .title(post.getTitle())
                .position(latLng));
        marker.setTag(post);
        marker.showInfoWindow();
    }

    // query all unclaimed posts and add markers to the map
    public void queryMapPosts() {
        map.clear();
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_AUTHOR);
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.whereEqualTo(Post.KEY_CLAIMED, false);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e("Utils.queryPosts", "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i("Utils.queryPosts", "Post: " + post.getTitle() + ", username: " + post.getAuthor().getUsername() + ", description: " + post.getDescription());
                    addMarker(post);
                }
            }
        });
    }

    // go to PostDetailActivity when info window is clicked
    @Override
    public void onInfoWindowClick(Marker marker) {
        Post post = (Post) marker.getTag();
        String postId = post.getObjectId();
        Intent i = new Intent(getContext(), PostDetailActivity.class);
        i.putExtra("post_id", postId);
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (map != null) {
            map.setInfoWindowAdapter(new PostInfoWindowAdapter(getContext()));
            queryMapPosts();
        }
    }
}