package com.example.freefood;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ActivityPostDetailBinding;
import com.example.freefood.models.Post;
import com.example.freefood.models.User;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.freefood.Utils.containsJsontoString;
import static com.example.freefood.Utils.getRelativeDistanceString;
import static com.example.freefood.Utils.getRelativeTimeAgo;

public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";
    ActivityPostDetailBinding binding;
    Post post;
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String object_id = getIntent().getStringExtra("post_id");
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_AUTHOR);
        query.getInBackground(object_id, new GetCallback<Post>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void done(final Post queriedPost, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error querying post", e);
                    return;
                }
                post = queriedPost;
                setFields();
            }
        });
        binding.btnClaimed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                post.setClaimed(true);
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error saving post after setting claimed to false");
                        }
                        finish();
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setFields() {
        // only show the claimed button if the post was by the current user
        if (ParseUser.getCurrentUser().getObjectId().equals(post.getAuthor().getObjectId()) && !post.getClaimed()) {
            binding.btnClaimed.setVisibility(View.VISIBLE);
        } else {
            binding.btnClaimed.setVisibility(View.GONE);
        }

        if (post.getClaimed()) {
            binding.tvTitle.setText("CLAIMED - " + post.getTitle());
        } else {
            binding.tvTitle.setText(post.getTitle());
        }
        binding.tvRelativeDistance.setText(getRelativeDistanceString(post));
        binding.tvLocation.setText(String.valueOf(post.getLocation()));
        binding.tvUsername.setText("@" + post.getAuthor().getUsername());
        String description = post.getDescription().trim();
        if (description.isEmpty()) {
            binding.tvDescription.setVisibility(View.GONE);
        } else {
            binding.tvDescription.setVisibility(View.VISIBLE);
            binding.tvDescription.setText(post.getDescription());
        }
        try {
            binding.tvAllergens.setText(containsJsontoString(post.getContains()));
        } catch (JSONException ex) {
            Log.e(TAG, "Error converting contains JSONObject to String", ex);
        }
        String relativeTime = getRelativeTimeAgo(post.getCreatedAt());
        binding.tvRelatieTime.setText(relativeTime + " ago");
        if (post.getImage() != null) {
            Glide.with(PostDetailActivity.this)
                    .load(post.getImage().getUrl())
                    .into(binding.ivImage);
        } else {
            binding.ivImage.setPadding(64, 64, 64, 64);
        }
        ParseFile profileImage = ((User) post.getAuthor()).getProfileImage();
        Glide.with(PostDetailActivity.this)
                .load(profileImage.getUrl())
                .circleCrop()
                .into(binding.ivProfile);

        SupportMapFragment mapFragment;
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
                // move map camera to location of food and add marker to that location
                LatLng latLng = new LatLng(post.getLocation().getLatitude(), post.getLocation().getLongitude());
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(latLng));
            }
        });
    }


}