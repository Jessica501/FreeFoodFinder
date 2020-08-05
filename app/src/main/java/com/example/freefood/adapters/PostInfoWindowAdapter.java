package com.example.freefood.adapters;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.example.freefood.R;
import com.example.freefood.databinding.InfoWindowPostBinding;
import com.example.freefood.models.Post;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;

import static com.example.freefood.utils.Utils.containsJsontoString;

public class PostInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    InfoWindowPostBinding binding;
    private static final String TAG = "PostInfoWindowsAdapter";
    private Context context;
    private final View view;

    public PostInfoWindowAdapter(Context context) {
        this.context = context;
        binding = InfoWindowPostBinding.inflate(LayoutInflater.from(context));
        view = LayoutInflater.from(context).inflate(R.layout.info_window_post, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View getInfoContents(Marker marker) {

        Post post = (Post) marker.getTag();
        if (post.getClaimed()) {
            binding.tvTitle.setText("CLAIMED - " + post.getTitle());
        } else {
            binding.tvTitle.setText(post.getTitle());
        }
        if (post.getDescription().trim().isEmpty()) {
            binding.tvDescription.setVisibility(View.GONE);
        } else {
            binding.tvDescription.setVisibility(View.VISIBLE);
            binding.tvDescription.setText(post.getDescription());
        }
        try {
            binding.tvContains.setText(containsJsontoString(post.getContains()));
        } catch (JSONException e) {
            Log.e(TAG, "Error converting JSON to string");
        }
        if (post.getImage() != null) {
            Glide.with(context)
                    .load(post.getImage().getUrl())
                    .into(binding.ivImage);
        }
        return binding.getRoot();
    }
}
