package com.example.freefood;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.freefood.models.Post;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;


public class Utils {


    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 11;
    public static final int PICK_PHOTO_CODE = 21;


    // converts the contains JSONObject to a String
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected static String containsJsontoString(JSONObject jsonObject) throws JSONException {
        if (jsonObject == null) {
            return "NO MAJOR ALLERGENS";
        }
        List<String> contains = new ArrayList<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (jsonObject.getBoolean(key)) {
                contains.add(key);
            }
        }
        if (contains.size() == 0) {
            return "NO MAJOR ALLERGENS";
        } else {
            return "CONTAINS: " + String.join(", ", contains).toUpperCase();
        }
    }

    // query the first 20 posts by parseUser and add to adapter. Doesn't query claimed posts if ignoreClaimed is true.
    public static void queryPosts(final PostsAdapter adapter, ParseUser parseUser, boolean ignoreClaimed) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_AUTHOR);
        query.setLimit(20);
        if (parseUser != null) {
            query.whereEqualTo(Post.KEY_AUTHOR, parseUser);
        }
        if (ignoreClaimed) {
            query.whereEqualTo(Post.KEY_CLAIMED, false);
        }
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e("Utils.queryPosts", "Issue with getting posts", e);
                    return;
                }
                for (Post post : posts) {
                    Log.i("Utils.queryPosts", "Post: " + post.getTitle() + ", username: " + post.getAuthor().getUsername() + ", description: " + post.getDescription());
                }
                adapter.clear();
                adapter.addAll(posts);
            }
        });
    }

    // query the first 20 posts by parseUser and add to adapter. Doesn't ignore claimed by default
    public static void queryPosts(final PostsAdapter adapter, ParseUser parseUser) {
        queryPosts(adapter, parseUser, false);
    }

    // query the first 20 posts and add to adapter. Ignores claimed by default
    public static void queryPosts(PostsAdapter adapter) {
        queryPosts(adapter, null, true);
    }


    // Returns the File for a photo stored on disk given the fileName (for using camera)
    public static File getPhotoFileUri(String fileName, Context context) {
        // Get safe storage directory for photos
        // Use `getExternalFilesDir` on Context to access package-specific directories.
        // This way, we don't need to request external read/write runtime permissions.
        File mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "Utils.getPhotoFileUri");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            Log.d("Utils.getPhotoFileUri", "failed to create directory");
        }

        // Return the file target for the photo based on filename
        File file = new File(mediaStorageDir.getPath() + File.separator + fileName);

        return file;
    }

    // returns Bitmap from Uri for image selected from gallery
    public static Bitmap loadFromUri(Uri photoUri, Context context) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if (Build.VERSION.SDK_INT > 27) {
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            Log.e("Utils.loadFromUri", "Error loading image", e);
        }
        return image;
    }

    public static String getRelativeDistanceString(Post post) {
        ParseGeoPoint currentLocation = new ParseGeoPoint(MainActivity.mLocation.getLatitude(), MainActivity.mLocation.getLongitude());
        double relativeDistance = post.getLocation().distanceInMilesTo(currentLocation);
        double roundedRelativeDistance = Math.round(relativeDistance * 10.0) / 10.0;

        if (roundedRelativeDistance == 0) {
            return ("0 mi");
        } else {
            return (roundedRelativeDistance + " mi");
        }
    }

    public static String getRelativeTimeAgo(Date date) {
        long dateMillis = date.getTime();
        String relativeDate = "";
        relativeDate = reformatRelativeTime(String.valueOf(DateUtils.getRelativeTimeSpanString(dateMillis,
                System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS)));
        return relativeDate;
    }

    private static String reformatRelativeTime(String s) {
        int spaceIndex = s.indexOf(" ");
        return s.substring(0, spaceIndex) + s.substring(spaceIndex + 1, spaceIndex + 2);
    }
}
