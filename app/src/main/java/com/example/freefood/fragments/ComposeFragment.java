package com.example.freefood.fragments;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.example.freefood.CreateDetailActivity;
import com.example.freefood.R;
import com.example.freefood.Utils;
import com.example.freefood.databinding.FragmentComposeBinding;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static android.app.Activity.RESULT_OK;
import static com.example.freefood.Utils.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE;
import static com.example.freefood.Utils.PICK_PHOTO_CODE;

@RuntimePermissions
public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";

    FragmentComposeBinding binding;
    public String photoFileName = "photo.jpg";
    public File photoFile;
    public byte[] photoBytes;
    public ParseFile parseFile;


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

        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                parseFile.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "Error saving image file to Parse", e);
                            return;
                        }
                        // go to PostDetailActivity with the parseFile
                        Intent i = new Intent(getContext(), CreateDetailActivity.class);
                        i.putExtra("image", Parcels.wrap(parseFile));
                        startActivity(i);
                    }
                });
            }
        });
    }

    public void onLaunchCamera(View view) {
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Create a File reference for future access
        photoFile = getPhotoFileUri(photoFileName);

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


    // Returns the File for a photo stored on disk given the fileName (for using camera)
    private File getPhotoFileUri(String fileName) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), TAG);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d(TAG, "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
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
                binding.btnContinue.setVisibility(View.VISIBLE);
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
            binding.btnContinue.setVisibility(View.VISIBLE);
        }
    }
}