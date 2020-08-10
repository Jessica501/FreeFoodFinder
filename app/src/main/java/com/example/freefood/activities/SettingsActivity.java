package com.example.freefood.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.transition.Fade;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.freefood.MyFirebaseMessagingService;
import com.example.freefood.R;
import com.example.freefood.utils.Utils;
import com.example.freefood.databinding.ActivitySettingsBinding;
import com.example.freefood.models.User;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import permissions.dispatcher.NeedsPermission;

import static com.example.freefood.utils.Utils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.example.freefood.utils.Utils.PICK_PHOTO_CODE;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    ActivitySettingsBinding binding;

    public String photoFileName = "photo.jpg";
    public File photoFile;
    private ParseFile newProfileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initializeCurrentSettings();

        binding.btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto(view);
            }
        });

        binding.btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onLaunchCamera(view);
            }
        });

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
            }
        });
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    private void initializeCurrentSettings() {
        User user = (User) ParseUser.getCurrentUser();
        binding.tvName.setText(user.getName());
        binding.tvUsername.setText("@" + user.getUsername());
        Glide.with(SettingsActivity.this)
                .load(user.getProfileImage().getUrl())
                .circleCrop()
                .into(binding.ivProfile);

        float currentRadius = (float) MyFirebaseMessagingService.getNotificationsRadius();
        binding.tvDistance.setText(String.valueOf(currentRadius));
        binding.slider.setValue(currentRadius);

        binding.slider.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                binding.tvDistance.setText(String.valueOf(binding.slider.getValue()));
                return false;
            }
        });

        binding.etName.setText(user.getName());
        binding.etUsername.setText(user.getUsername());
    }

    private void saveSettings() {
        final String name = String.valueOf(binding.etName.getText());
        final String username = String.valueOf(binding.etUsername.getText());

        binding.pbLoading.setVisibility(View.VISIBLE);
        // check if username is unchanged
        if (username.equals(ParseUser.getCurrentUser().getUsername())) {
            saveUser(name, username);
            saveNotificationsRadius();
        }
        // if username is changed, check if username is taken
        else {
            ParseQuery<ParseUser> usernameQuery = ParseUser.getQuery();
            usernameQuery.whereEqualTo("username", username);
            usernameQuery.findInBackground(new FindCallback<ParseUser>() {
                @Override
                public void done(List<ParseUser> objects, ParseException e) {
                    if (objects.size() > 0) {
                        Toast.makeText(SettingsActivity.this, "Username taken. Try again.", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        saveUser(name, username);
                        saveNotificationsRadius();
                    }
                }
            });
        }
    }

    // save name, username, profile image to parse
    private void saveUser(final String name, final String username) {
        final User user = (User) ParseUser.getCurrentUser();
        user.setName(name);
        user.setUsername(username);
        if (newProfileImage != null) {
            user.setProfileImage(newProfileImage);
        }
        user.saveInBackground(new SaveCallback() {
            @SuppressLint("SetTextI18n")
            @Override
            public void done(ParseException e) {
                binding.pbLoading.setVisibility(View.INVISIBLE);
                if (e == null) {
                    Log.i(TAG, "Successfully saved user");
                    // load image into profile imageView
                    Glide.with(SettingsActivity.this)
                            .load(user.getProfileImage().getUrl())
                            .circleCrop()
                            .into(binding.ivProfile);
                    binding.ivPreview.setVisibility(View.INVISIBLE);
                    binding.tvPreview.setVisibility(View.INVISIBLE);

                    binding.tvName.setText(name);
                    binding.tvUsername.setText("@" + username);

                    binding.tilName.clearFocus();
                    binding.tilUsername.clearFocus();
                    Toast.makeText(SettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
                }
                // note: this should not happen
                else if (e.getCode() == ParseException.USERNAME_TAKEN) {
                    Toast.makeText(SettingsActivity.this, "Error: Username taken.", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.e(TAG, "Error saving user " + e.getCode(), e);
                }
            }
        });
    }

    private void saveNotificationsRadius() {
        MyFirebaseMessagingService.setNotificationsRadius(binding.slider.getValue());
    }

    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    public void onPickPhoto(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        if (intent.resolveActivity(this.getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result from gallery
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = Utils.loadFromUri(photoUri, SettingsActivity.this);

            // convert Bitmap to byte[], then create parseFile
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] photoBytes = stream.toByteArray();
            newProfileImage = new ParseFile(photoBytes);

            binding.ivPreview.setVisibility(View.VISIBLE);
            binding.tvPreview.setVisibility(View.VISIBLE);
            Glide.with(SettingsActivity.this)
                    .load(photoUri)
                    .circleCrop()
                    .into(binding.ivPreview);
        }
        // Result from camera
        else if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // by this point we have the camera photo on disk
                Bitmap takenImage = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                // create parseFile
                newProfileImage = new ParseFile(photoFile);
                // Load the taken image into a preview
                binding.ivPreview.setVisibility(View.VISIBLE);
                binding.tvPreview.setVisibility(View.VISIBLE);
                Glide.with(SettingsActivity.this)
                        .load(takenImage)
                        .circleCrop()
                        .into(binding.ivPreview);
            } else { // Result was a failure
                Toast.makeText(this, "Picture wasn't taken!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}