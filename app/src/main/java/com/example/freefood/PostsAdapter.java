package com.example.freefood;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ItemPostBinding;
import com.example.freefood.models.Post;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.example.freefood.Utils.containsJsontoString;
import static com.example.freefood.Utils.getRelativeDistanceString;
import static com.example.freefood.Utils.getRelativeTimeAgo;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private static final String TAG = "PostsAdapter";

    private List<Post> posts;
    private List<Post> allPosts;
    private Context context;
    private HashSet<String> filter;
    private double maxDistance;

    public PostsAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.allPosts = new ArrayList<>();
        this.filter = new HashSet<>();
        maxDistance = 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPostBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }


    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void clear() {
        this.allPosts.clear();
        this.posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> posts) {
        this.allPosts.addAll(posts);
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    public void sort() {
        Collections.sort(this.posts);
        Collections.sort(this.allPosts);
    }

    public HashSet<String> getFilter() {
        return filter;
    }

    public double getMaxDistance() {
        return this.maxDistance;
    }

    public void filter(HashSet<String> filter, double maxDistance) throws JSONException {
        List<Post> filteredPosts = new ArrayList<>();
        this.filter = filter;
        this.maxDistance = maxDistance;

        // no allergens and no maximum distance set
        if (filter.size() == 0 && maxDistance <= 0) {
            this.posts.clear();
            this.posts.addAll(allPosts);
            notifyDataSetChanged();
            return;
        }

        // loop through each post and check if it satisfies the requirements
        for (Post post: allPosts) {
            // if a max distance was set and the relative distance is greater than the max, don't add the post
            if (maxDistance > 0 && Utils.getRelativeDistance(post) > maxDistance) {
                continue;
            }

            // loop through the 8 allergens and checks if any are in the food and need to be filtered out
            boolean allergenFree = true;
            JSONObject jsonObject = post.getContains();
            Iterator<String> allergens = jsonObject.keys();
            while (allergens.hasNext() && allergenFree) {
                String allergen = allergens.next();
                if (jsonObject.getBoolean(allergen) && filter.contains(allergen)) {
                    allergenFree = false;
                    break;
                }
            }
            if (allergenFree) {
                filteredPosts.add(post);
            }
        }
        this.posts.clear();
        this.posts.addAll(filteredPosts);
        notifyDataSetChanged();
    }

    // filter using the saved attributes
    public void filter() throws JSONException {
        filter(this.filter, this.maxDistance);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemPostBinding binding;

        public ViewHolder(ItemPostBinding b) {
            super(b.getRoot());
            binding = b;
            itemView.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bind(Post post) {
            if (post.getClaimed()) {
                binding.tvTitle.setText("CLAIMED - " + post.getTitle());
            } else {
                binding.tvTitle.setText(post.getTitle());
            }
            binding.tvLocation.setText(Utils.reverseGeocode(context, post.getLocation()));
            String description = post.getDescription().trim();
            if (description.isEmpty()) {
                binding.tvDescription.setVisibility(View.GONE);
            } else {
                binding.tvDescription.setVisibility(View.VISIBLE);
                binding.tvDescription.setText(post.getDescription());
            }
            try {
                binding.tvContains.setText(containsJsontoString(post.getContains()));
            } catch (JSONException e) {
                Log.e(TAG, "Error converting contains JSONObject to String", e);
            }

            ParseFile image = post.getImage();
            if (image != null) {
                binding.ivImage.setPadding(0, 0, 0, 0);
                Glide.with(context)
                        .load(image.getUrl())
                        .into(binding.ivImage);
            } else {
                binding.ivImage.setImageResource(R.drawable.ic_baseline_fastfood_24);
                binding.ivImage.setPadding(64, 64, 64, 64);
            }
            binding.tvDistance.setText(getRelativeDistanceString(post));
            binding.tvRelativeTime.setText(getRelativeTimeAgo(post.getCreatedAt()));
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Intent i = new Intent(context, PostDetailActivity.class);
                Post post = posts.get(position);
                i.putExtra("post_id", post.getObjectId());
                Log.i(TAG, context.toString());
                context.startActivity(i);
            }
        }
    }
}
