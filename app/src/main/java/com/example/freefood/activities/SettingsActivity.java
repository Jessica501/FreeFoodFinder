package com.example.freefood.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
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
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

import permissions.dispatcher.NeedsPermission;

import static com.example.freefood.utils.Utils.PICK_PHOTO_CODE;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    ActivitySettingsBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        User user = (User) ParseUser.getCurrentUser();
        binding.tvName.setText(user.getName());
        binding.tvUsername.setText("@"+user.getUsername());
        Glide.with(SettingsActivity.this)
                .load(user.getProfileImage().getUrl())
                .circleCrop()
                .into(binding.ivProfile);

        binding.btnUpdateProfilePhoro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto(view);
            }
        });

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

        binding.btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyFirebaseMessagingService.setNotificationsRadius(binding.slider.getValue());
                Toast.makeText(SettingsActivity.this, "Notifications radius set to " + binding.slider.getValue() + " miles", Toast.LENGTH_SHORT).show();
            }
        });
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
            final ParseFile parseFile = new ParseFile(photoBytes);

            // save profile image to Parse
            final User user = (User) ParseUser.getCurrentUser();
            user.setProfileImage(parseFile);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error saving profile image", e);
                    } else {
                        Log.i(TAG, "Successfully saved profile image");
                        // load image into profile imageView
                        Glide.with(SettingsActivity.this)
                                .load(user.getProfileImage().getUrl())
                                .circleCrop()
                                .into(binding.ivProfile);
                    }
                }
            });

        }
    }
}