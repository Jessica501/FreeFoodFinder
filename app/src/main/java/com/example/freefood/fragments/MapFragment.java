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

import com.example.freefood.activities.FilterActivity;
import com.example.freefood.activities.MainActivity;
import com.example.freefood.activities.PostDetailActivity;
import com.example.freefood.adapters.PostInfoWindowAdapter;
import com.example.freefood.R;
import com.example.freefood.utils.Utils;
import com.example.freefood.databinding.FragmentMapBinding;
import com.example.freefood.models.Post;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.chip.Chip;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    private static final String TAG = "MapFragment";
    private int FILTER_REQUEST_CODE = 617;
    FragmentMapBinding binding;

    private GoogleMap map;
    private HashSet<String> allergens;
    private double maxDistance;
    private HashSet<String> tags;


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


        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        allergens = new HashSet<>();
        maxDistance = 0;
        tags = new HashSet<>();
        binding.fabFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), FilterActivity.class);
                i.putExtra("currentAllergens", allergens);
                i.putExtra("currentMaxDistance", maxDistance);
                i.putExtra("currentTags", tags);
                startActivityForResult(i, FILTER_REQUEST_CODE);
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady");

        map = googleMap;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);
        map.getUiSettings().setZoomGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setPadding(0, 0, 0, binding.fabFilter.getHeight() + 16);
        map.setInfoWindowAdapter(new PostInfoWindowAdapter(getContext()));

        LatLng latLng = new LatLng(MainActivity.mLocation.getLatitude(), MainActivity.mLocation.getLongitude());
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));

        queryMapPosts();

        map.setOnInfoWindowClickListener(this);

        PlacesClient placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                this.getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                map.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());
            }

            @Override
            public void onError(@NotNull Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });

    }


    // query all unclaimed posts and add markers to the map
    public void queryMapPosts() {
        Log.i(TAG, "querying map posts");
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
                    try {
                        filter(post);
                    } catch (JSONException ex) {
                        Log.e(TAG, "Error filtering posts", e);
                    }
                }
            }
        });
    }

    // checks whether one post fulfills the requirements. If so, add marker. Else, return (don't add marker)
    private void filter(Post post) throws JSONException {
        if (maxDistance > 0 && Utils.getRelativeDistance(post) > maxDistance) {
            return;
        }

        if (allergens.size() > 0) {
            // loop through the 8 allergens and checks if any are in the food and need to be filtered out
            JSONObject jsonObject = post.getContains();
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String allergen = iterator.next();
                if (jsonObject.getBoolean(allergen) && allergens.contains(allergen)) {
                    return;
                }
            }
        }

        // loop through the tags and check whether the post has each tag. If not, return.
        JSONObject postTags = post.getTags();
        for (String tag : tags) {
            if (postTags == null || !postTags.getBoolean(tag)) {
                return;
            }
        }

        // if not yet returned (passed through filter), then add it to the map
        addMarker(post);
    }

    private void addMarker(Post post) {
        LatLng latLng = new LatLng(post.getLocation().getLatitude(), post.getLocation().getLongitude());
        Marker marker = map.addMarker(new MarkerOptions()
                .title(post.getTitle())
                .position(latLng));
        marker.setTag(post);
        marker.showInfoWindow();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILTER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                HashSet<String> allergens = (HashSet<String>) data.getExtras().getSerializable("allergens");
                double maxDistance = data.getExtras().getDouble("maxDistance");
                HashSet<String> tags = (HashSet<String>) data.getExtras().getSerializable("tags");
                this.allergens = allergens;
                this.maxDistance = maxDistance;
                this.tags = tags;
                queryMapPosts();

                toggleChipVisibility();
            }
        }
    }

    private void toggleChipVisibility() {
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