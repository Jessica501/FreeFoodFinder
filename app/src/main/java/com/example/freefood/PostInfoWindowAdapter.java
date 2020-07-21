package com.example.freefood;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.FragmentProfileBinding;
import com.example.freefood.databinding.InfoWindowPostBinding;
import com.example.freefood.models.Post;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import org.json.JSONException;
import org.parceler.Parcels;

import static com.example.freefood.Utils.containsJsontoString;

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

    @Override
    public View getInfoContents(Marker marker) {

        Post post = (Post) marker.getTag();
        binding.tvTitle.setText(post.getTitle());
        binding.tvDescription.setText(post.getDescription());
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
