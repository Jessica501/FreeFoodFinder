package com.example.freefood;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.freefood.fragments.MapFragment;
import com.example.freefood.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import permissions.dispatcher.PermissionUtils;


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

    public static void queryPosts(final PostsAdapter adapter, ParseUser parseUser) {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_AUTHOR);
        query.setLimit(20);
        if (parseUser != null) {
            query.whereEqualTo(Post.KEY_AUTHOR, parseUser);
        }
        query.addDescendingOrder(Post.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e("Utils.queryPosts", "Issue with getting posts", e);
                    return;
                }
                for (Post post: posts) {
                    Log.i("Utils.queryPosts", "Post: " + post.getTitle() + ", username: " + post.getAuthor().getUsername() + ", description: " + post.getDescription());
                }
                adapter.clear();
                adapter.addAll(posts);
            }
        });
    }

    public static void queryPosts(PostsAdapter adapter) {
        queryPosts(adapter, null);
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


}
