package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ActivityCreateDetailBinding;
import com.example.freefood.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

public class CreateDetailActivity extends AppCompatActivity{

    private static final String TAG = "CreateDetailActivity";

    ActivityCreateDetailBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        final ParseFile image = (ParseFile) Parcels.unwrap(getIntent().getParcelableExtra("image"));
        Glide.with(CreateDetailActivity.this)
                .load(image.getUrl())
                .into(binding.ivImage);

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
                // TODO: add a check for location
                savePost(image);
            }
        });


        binding.cbCurrentLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    binding.etLocation.setText("");
                    binding.etLocation.setEnabled(false);
                } else {
                    binding.etLocation.setEnabled(true);
                }
            }
        });
    }

    // save the post to parse using the entered data
    private void savePost(ParseFile image) {
        Post post = new Post();
        post.setImage(image);
        post.setAuthor(ParseUser.getCurrentUser());
        post.setTitle(String.valueOf(binding.etTitle.getText()));
        // TODO: allow user to type in location
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
            post.setLocation(new ParseGeoPoint(30, -120));
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
                    Intent i = new Intent(CreateDetailActivity.this, MainActivity.class);
                    startActivity(i);
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSONObject for allergen information", e);
        }

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
}