package com.example.freefood;

import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
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

    public PostsAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.allPosts = new ArrayList<>();
        this.filter = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemPostBinding.inflate(LayoutInflater.from(context), parent, false));
    }

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

    public HashSet<String> getFilter() {
        return filter;
    }

    public void filter(HashSet<String> filter) throws JSONException {
        List<Post> filteredPosts = new ArrayList<>();
        this.filter = filter;

        if (filter.size() == 0) {
            this.posts.clear();
            this.posts.addAll(allPosts);
            notifyDataSetChanged();
            return;
        }

        for (Post post: allPosts) {
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
                Log.i(TAG, post + " has no allergens");
            } else {
                Log.i(TAG, post + " has allergens");
            }
        }
        this.posts.clear();
        this.posts.addAll(filteredPosts);
        notifyDataSetChanged();
    }

    public void filter() throws JSONException {
        filter(this.filter);
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

            binding.tvLocation.setText(String.valueOf(post.getLocation()));
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
