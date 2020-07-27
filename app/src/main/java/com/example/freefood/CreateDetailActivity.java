package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ActivityCreateDetailBinding;
import com.example.freefood.models.Post;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.Arrays;

public class CreateDetailActivity extends AppCompatActivity{

    private static final String TAG = "CreateDetailActivity";

    private Place place;
    private ParseGeoPoint parseGeoPoint;
    private boolean edit;
    private Post editPost;
    private AutocompleteSupportFragment autocompleteFragment;
    private ParseFile image;

    ActivityCreateDetailBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        edit = getIntent().getBooleanExtra("edit", false);
        if (edit) {
            editPost = getIntent().getExtras().getParcelable("post");
            try {
                loadInitialFields(editPost);
            } catch (JSONException e) {
                Log.e(TAG, "Error loading contains json information into checkboxes", e);
            }
            binding.btnClaimed.setVisibility(View.VISIBLE);
        } else {
            image = (ParseFile) Parcels.unwrap(getIntent().getParcelableExtra("image"));
            Glide.with(CreateDetailActivity.this)
                    .load(image.getUrl())
                    .into(binding.ivImage);
            binding.btnClaimed.setVisibility(View.GONE);
        }

        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("Location");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                setPlace(place);
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng());
            }
            @Override
            public void onError(@NotNull Status status) {
                Log.e(TAG, "An error occurred: " + status);
            }
        });

        // save post to parse when submit is clicked
        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // error handling: make sure title isn't empty
                if (String.valueOf(binding.etTitle.getText()).trim().isEmpty()) {
                    Toast.makeText(CreateDetailActivity.this, "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (binding.cbCurrentLocation.isChecked() && MainActivity.mLocation == null) {
                    Log.e(TAG, "mLocation is null, but current location is checked");
                }
                if (!binding.cbCurrentLocation.isChecked() && parseGeoPoint == null) {
                    Toast.makeText(CreateDetailActivity.this, "Location cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (edit) {
                    updatePost();
                } else {
                    savePost(null);
                }
            }
        });

        binding.btnClaimed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editPost.setClaimed(true);
                editPost.saveInBackground(new SaveCallback() {
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

        binding.cbCurrentLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    autocompleteFragment.getView().setEnabled(false);
                    autocompleteFragment.setText("");
                    parseGeoPoint = null;

                } else {
                    autocompleteFragment.getView().setEnabled(true);
                }
            }
        });
    }

    private void setPlace(Place place) {
        this.place = place;
        this.parseGeoPoint = new ParseGeoPoint(place.getLatLng().latitude, place.getLatLng().longitude);
    }

    // save the post to parse using the entered data
    private void savePost(Post post) {
        if (post == null) {
            post = new Post();
        }
        post.setImage(image);
        post.setAuthor(ParseUser.getCurrentUser());
        post.setTitle(String.valueOf(binding.etTitle.getText()));
        if (binding.cbCurrentLocation.isChecked()) {
            Location location = MainActivity.mLocation;
            if (location != null) {
                post.setLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            }
            else {
                post.setLocation(new ParseGeoPoint(30, -120));
                Log.e(TAG, "Error: current location is checked, but the location is null");
            }
        } else {
            post.setLocation(this.parseGeoPoint);
        }
        post.setDescription(String.valueOf(binding.etDescription.getText()));
        try {
            post.setContains(createContainsJson());
            Log.i(TAG, "Successfully created JSONObject for allergen information");

            post.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error while saving post", e);
                        return;
                    }
                    Log.i(TAG, "Post save was successful");
                    Intent i;
                    if (edit) {
                        i = new Intent(CreateDetailActivity.this, PostDetailActivity.class);
                        i.putExtra("post_id", editPost.getObjectId());
                    } else {
                        i = new Intent(CreateDetailActivity.this, MainActivity.class);
                    }
                    startActivity(i);
                    finish();
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSONObject for allergen information", e);
        }
    }

    private void updatePost() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.getInBackground(editPost.getObjectId(), new GetCallback<Post>() {
            @Override
            public void done(Post object, ParseException e) {
                savePost(object);
            }
        });
    }

    // creates a contains JSONObject using the checkboxes for the various allergens
    private JSONObject createContainsJson() throws JSONException {
        JSONObject contains = new JSONObject();
        contains.put("milk", binding.cbMilk.isChecked());
        contains.put("eggs", binding.cbEggs.isChecked());
        contains.put("tree nuts", binding.cbTreeNuts.isChecked());
        contains.put("peanuts", binding.cbPeanuts.isChecked());
        contains.put("soy", binding.cbSoy.isChecked());
        contains.put("wheat", binding.cbWheat.isChecked());
        contains.put("shellfish", binding.cbShellfish.isChecked());
        contains.put("fish", binding.cbFish.isChecked());
        return contains;
    }

    // load initial fields if post is being edited
    private void loadInitialFields(Post post) throws JSONException {
        image = post.getImage();
        Glide.with(this)
                .load(post.getImage().getUrl())
                .into(binding.ivImage);
        binding.etTitle.setText(post.getTitle());
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setText(String.valueOf(post.getLocation()));
        this.parseGeoPoint = post.getLocation();
        binding.etDescription.setText(post.getDescription());
        JSONObject jsonObject = post.getContains();

        CheckBox[] checkBoxes = new CheckBox[] {binding.cbMilk, binding.cbEggs, binding.cbPeanuts, binding.cbTreeNuts, binding.cbSoy, binding.cbWheat, binding.cbFish, binding.cbShellfish};
        for (int i = 0; i < 8; i++) {
            CheckBox checkBox = checkBoxes[i];
            String allergen = String.valueOf(checkBox.getText()).toLowerCase();
            if (jsonObject.getBoolean(allergen)) {
                checkBox.setChecked(true);
            }
        }
    }
}