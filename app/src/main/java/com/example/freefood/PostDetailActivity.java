package com.example.freefood;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ActivityPostDetailBinding;
import com.example.freefood.models.Comment;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.freefood.Utils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.example.freefood.Utils.containsJsontoString;
import static com.example.freefood.Utils.getRelativeDistanceString;
import static com.example.freefood.Utils.getRelativeTimeAgo;
import static com.example.freefood.Utils.queryPosts;

public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";
    ActivityPostDetailBinding binding;
    Post post;
    ParseRelation relation;
    private GoogleMap map;
    protected CommentsAdapter adapter;
    private File photoFile;
    private String photoFileName = "photo.jpg";
    public ParseFile parseFile;


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
                queryComments();
            }
        });

        adapter = new CommentsAdapter(PostDetailActivity.this);
        binding.rvComments.setAdapter(adapter);
        binding.rvComments.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this));

        binding.ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLaunchCamera(view);
            }
        });

        binding.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = String.valueOf(binding.etComment.getText());
                if (description.trim().isEmpty() && parseFile == null) {
                    Toast.makeText(PostDetailActivity.this, "Cannot submit empty comment", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveComment(description);
            }
        });

        binding.ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(PostDetailActivity.this, CreateDetailActivity.class);
                i.putExtra("edit", true);
                i.putExtra("post", post);
                startActivity(i);
                finish();
            }
        });
        
        binding.ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deletePost();
            }
        });
    }

    private void deletePost() {
        post.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                finish();
            }
        });
    }

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = Utils.getPhotoFileUri(photoFileName, this);

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(this, "com.codepath.fileprovider.FreeFoodFinder", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    private void saveComment(String description) {
        final Comment comment = new Comment();
        comment.setAuthor(ParseUser.getCurrentUser());
        comment.setPost(post);
        comment.setDescription(description);
        if (parseFile != null) {
            comment.setImage(parseFile);
        }
        comment.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error saving comment", e);
                    return;
                }
                if (relation == null) {
                    relation = post.getComments();
                }
                relation.add(comment);
                post.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error saving comment", e);
                            return;
                        }

                        // hide keyboard and clear edit text and image after submitting comment
                        InputMethodManager inputManager = (InputMethodManager)
                                getSystemService(INPUT_METHOD_SERVICE);
                        inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                        binding.etComment.setText("");
                        binding.etComment.clearFocus();
                        binding.ivCamera.setImageResource(R.drawable.ic_baseline_photo_camera_24);
                        parseFile = null;
                        queryComments();
                    }
                });
            }
        });
    }

    private void queryComments() {
        relation = post.getComments();
        ParseQuery<Comment> query = relation.getQuery();
        query.include(Comment.KEY_AUTHOR);
        query.setLimit(20);
        query.addDescendingOrder(Comment.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> comments, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue with querying comments", e);
                    return;
                }
                for (Comment comment : comments) {
                    Log.i(TAG, "Comment: " + comment.getDescrption() + ", username: " + comment.getAuthor().getUsername());
                }
                adapter.clear();
                adapter.addAll(comments);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setFields() {
        // check if author of post is the current user
        if (ParseUser.getCurrentUser().getObjectId().equals(post.getAuthor().getObjectId())) {
            // if post has been claimed, make ivEdit gone
            if (post.getClaimed()) {
                binding.ivEdit.setVisibility(View.GONE);
            }
            // if post hasn't been claimed, make both buttons visible
            else {
                binding.ivEdit.setVisibility(View.VISIBLE);
            }
            binding.ivDelete.setVisibility(View.VISIBLE);

        }
        // if post author isn't current user, set both buttons gone
        else {
            binding.ivEdit.setVisibility(View.GONE);
            binding.ivDelete.setVisibility(View.GONE);
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
        binding.tvRelativeTime.setText(relativeTime);
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result from camera
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // create parseFile
                parseFile = new ParseFile(photoFile);
                // Load the taken image into a preview
                binding.ivCamera.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}