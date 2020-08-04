package com.example.freefood.fragments;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.freefood.MainActivity;
import com.example.freefood.PostDetailActivity;
import com.example.freefood.R;
import com.example.freefood.Utils;
import com.example.freefood.databinding.FragmentComposeBinding;
import com.example.freefood.models.Post;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.GetCallback;
import com.parse.Parse;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;
import static com.example.freefood.Utils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.example.freefood.Utils.PICK_PHOTO_CODE;

@RuntimePermissions
public class ComposeFragment extends Fragment {

    private static final String TAG = "ComposeFragment";

    FragmentComposeBinding binding;
    public String photoFileName = "photo.jpg";
    public File photoFile;
    public byte[] photoBytes;
    public ParseFile parseFile;

    private Place place;
    private ParseGeoPoint parseGeoPoint;
    private boolean edit;
    private Post editPost;
    private AutocompleteSupportFragment autocompleteFragment;


    public ComposeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentComposeBinding.inflate(getLayoutInflater(), container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLaunchCamera(view);
            }
        });

        binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto(view);
            }
        });

//        edit = getIntent().getBooleanExtra("edit", false);
//        if (edit) {
//            editPost = getIntent().getExtras().getParcelable("post");
//            try {
//                loadInitialFields(editPost);
//            } catch (JSONException e) {
//                Log.e(TAG, "Error loading contains json information into checkboxes", e);
//            }
//            binding.btnClaimed.setVisibility(View.VISIBLE);
//        } else {
//            image = (ParseFile) Parcels.unwrap(getIntent().getParcelableExtra("image"));
//            Glide.with(CreateDetailActivity.this)
//                    .load(image.getUrl())
//                    .into(binding.ivImage);
//            binding.btnClaimed.setVisibility(View.GONE);
//        }

        PlacesClient placesClient = Places.createClient(getContext());

        // Initialize the AutocompleteSupportFragment.
        autocompleteFragment = (AutocompleteSupportFragment)
                this.getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
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
                // error handling: make sure title, location aren't empty
                if (String.valueOf(binding.etTitle.getText()).trim().isEmpty()) {
                    Toast.makeText(getContext(), "Title cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (binding.cbCurrentLocation.isChecked() && MainActivity.mLocation == null) {
                    Log.e(TAG, "mLocation is null, but current location is checked");
                }
                if (!binding.cbCurrentLocation.isChecked() && parseGeoPoint == null) {
                    Toast.makeText(getContext(), "Location cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
//                if (edit) {
//                    updatePost();
//                } else {
                    savePost(null);
//                }
            }
        });

        binding.btnClaimed.setVisibility(View.GONE);

//        binding.btnClaimed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                editPost.setClaimed(true);
//                editPost.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e != null) {
//                            Log.e(TAG, "Error saving post after setting claimed to false");
//                        }
//                        finish();
//                    }
//                });
//            }
//        });

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

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = Utils.getPhotoFileUri(photoFileName, getContext());

        // wrap File object into a content provider
        // required for API >= 24
        // See https://guides.codepath.com/android/Sharing-Content-with-Intents#sharing-files-with-api-24-or-higher
        Uri fileProvider = FileProvider.getUriForFile(getContext(), "com.codepath.fileprovider.FreeFoodFinder", photoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Start the image capture intent to take photo
            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result from camera
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // create parseFile
                parseFile = new ParseFile(photoFile);
                // Load the taken image into a preview
                binding.ivImage.setImageBitmap(takenImage);
            } else { // Result was a failure
                Toast.makeText(getContext(), "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
        // Result from gallery
        else if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = Utils.loadFromUri(photoUri, getContext());

            // convert Bitmap to byte[], then create parseFile
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            photoBytes = stream.toByteArray();
            parseFile = new ParseFile(photoBytes);

            // Load the selected image into a preview
            binding.ivImage.setImageBitmap(selectedImage);
        }
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
        if (parseFile != null) {
            post.setImage(parseFile);
        }
        post.setAuthor(ParseUser.getCurrentUser());
        post.setTitle(String.valueOf(binding.etTitle.getText()));
        if (binding.cbCurrentLocation.isChecked()) {
            Location location = MainActivity.mLocation;
            if (location != null) {
                post.setLocation(new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
            } else {
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


        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSONObject for allergen information", e);
        }

        try {
            post.setTags(createTagsJson());
            Log.i(TAG, "Successfully created JSONObject for tags information");
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSONObject for tags information", e);
        }

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
                    i = new Intent(getContext(), PostDetailActivity.class);
                    i.putExtra("post_id", editPost.getObjectId());
                } else {
                    i = new Intent(getContext(), MainActivity.class);
                }
                startActivity(i);
            }
        });
    }

    private JSONObject createTagsJson() throws JSONException {
        JSONObject tags = new JSONObject();
        tags.put("vegetarian", binding.cbVegetarian.isChecked());
        tags.put("vegan", binding.cbVegan.isChecked());
        tags.put("kosher", binding.cbKosher.isChecked());
        tags.put("halal", binding.cbHalal.isChecked());
        return tags;
    }

//    private void updatePost() {
//        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
//        query.getInBackground(editPost.getObjectId(), new GetCallback<Post>() {
//            @Override
//            public void done(Post object, ParseException e) {
//                savePost(object);
//            }
//        });
//    }

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

//    // load initial fields if post is being edited
//    private void loadInitialFields(Post post) throws JSONException {
//        parseFile = post.getImage();
//        Glide.with(this)
//                .load(post.getImage().getUrl())
//                .into(binding.ivImage);
//        binding.etTitle.setText(post.getTitle());
//        String address = Utils.reverseGeocode(getContext(), post.getLocation());
//        autocompleteFragment.setText(address);
//        this.parseGeoPoint = post.getLocation();
//        binding.etDescription.setText(post.getDescription());
//        JSONObject jsonObject = post.getContains();
//
//        CheckBox[] checkBoxes = new CheckBox[]{binding.cbMilk, binding.cbEggs, binding.cbPeanuts, binding.cbTreeNuts, binding.cbSoy, binding.cbWheat, binding.cbFish, binding.cbShellfish};
//        for (int i = 0; i < 8; i++) {
//            CheckBox checkBox = checkBoxes[i];
//            String allergen = String.valueOf(checkBox.getText()).toLowerCase();
//            if (jsonObject.getBoolean(allergen)) {
//                checkBox.setChecked(true);
//            }
//        }
//    }
}