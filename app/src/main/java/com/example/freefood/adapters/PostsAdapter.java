package com.example.freefood.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.freefood.R;
import com.example.freefood.utils.Utils;
import com.example.freefood.activities.PostDetailActivity;
import com.example.freefood.databinding.ItemPostBinding;
import com.example.freefood.models.Post;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static com.example.freefood.utils.Utils.containsJsontoString;
import static com.example.freefood.utils.Utils.getRelativeDistanceString;
import static com.example.freefood.utils.Utils.getRelativeTimeAgo;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private static final String TAG = "PostsAdapter";

    private List<Post> posts;
    private List<Post> allPosts;
    private Context context;
    private HashSet<String> allergens;
    private double maxDistance;
    private HashSet<String> tags;

    public PostsAdapter(Context context) {
        this.context = context;
        this.posts = new ArrayList<>();
        this.allPosts = new ArrayList<>();
        this.allergens = new HashSet<>();
        this.tags = new HashSet<>();
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

    public HashSet<String> getAllergens() {
        return this.allergens;
    }

    public double getMaxDistance() {
        return this.maxDistance;
    }


    public HashSet<String> getTags() { return this.tags; }

    public void filter(HashSet<String> allergens, double maxDistance, HashSet<String> tags) throws JSONException {
        List<Post> filteredPosts = new ArrayList<>();
        this.allergens = allergens;
        this.maxDistance = maxDistance;
        this.tags = tags;

        // no allergens, tags and no maximum distance set
        if (allergens.size() == 0 && maxDistance <= 0 && tags.size() == 0) {
            this.posts.clear();
            this.posts.addAll(allPosts);
            notifyDataSetChanged();
            return;
        }

        // loop through each post and check if it satisfies the requirements
        for (Post post : allPosts) {
            // if a max distance was set and the relative distance is greater than the max, don't add the post
            if (maxDistance > 0 && Utils.getRelativeDistance(post) > maxDistance) {
                continue;
            }

            // for all tags in this.tags, if post doesn't contain the tag, don't add the post
            JSONObject postTags = post.getTags();
            boolean satisfiesTags = true;
            for (String tag: tags) {
                if (postTags == null || !postTags.getBoolean(tag)) {
                    satisfiesTags = false;
                    break;
                }
            }

            // loop through the 8 allergens and checks if any are in the food and need to be filtered out
            boolean allergenFree = true;
            JSONObject postAllergens = post.getContains();
            Iterator<String> iterator = postAllergens.keys();
            while (iterator.hasNext() && allergenFree) {
                String allergen = iterator.next();
                if (postAllergens.getBoolean(allergen) && allergens.contains(allergen)) {
                    allergenFree = false;
                    break;
                }
            }

            if (allergenFree && satisfiesTags) {
                filteredPosts.add(post);
            }
        }
        this.posts.clear();
        this.posts.addAll(filteredPosts);
        notifyDataSetChanged();
    }

    // filter using the saved attributes
    public void filter() throws JSONException {
        filter(this.allergens, this.maxDistance, this.tags);
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemPostBinding binding;
        Post post;

        public ViewHolder(ItemPostBinding b) {
            super(b.getRoot());
            binding = b;
            itemView.setOnClickListener(this);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        public void bind(Post post) {
            this.post = post;
            if (post.getClaimed()) {
                binding.tvTitle.setText("CLAIMED - " + post.getTitle());
            } else {
                binding.tvTitle.setText(post.getTitle());
            }
//            binding.tvLocation.setText(Utils.shortenedReverseGeocode(context, post.getLocation()));
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

            try {
                setTagVisibility(post);
            } catch (JSONException e) {
                Log.e(TAG, "Error setting tag chip visibility", e);
            }
        }

        private void setTagVisibility(Post post) throws JSONException {
            JSONObject tags = post.getTags();
            if (tags == null) {
                for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                    Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                    chip.setVisibility(View.GONE);
                }
                return;
            }
            for (int i = 0; i < binding.chipGroup.getChildCount(); i++) {
                Chip chip = (Chip) binding.chipGroup.getChildAt(i);
                String tag = String.valueOf(chip.getText()).toLowerCase();
                if (tags.getBoolean(tag)) {
                    chip.setVisibility(View.VISIBLE);
                } else {
                    chip.setVisibility(View.GONE);
                }
            }
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

        public void markClaimed() {
            if (post.getAuthor().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                if (post.getClaimed()) {
                    Toast.makeText(context, "Post is already marked as claimed", Toast.LENGTH_SHORT).show();
                    notifyItemChanged(getAdapterPosition());
                } else {
                    post.setClaimed(true);
                    post.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "Error saving post after setting claimed to true", e);
                                return;
                            }
                            Log.i(TAG, "Successfully marked post as claimed and saved");
                            notifyItemChanged(getAdapterPosition());

                            final Snackbar snackbar = Snackbar.make(binding.getRoot(), "Post marked as claimed", Snackbar.LENGTH_SHORT);
                            snackbar.setAction("Undo", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                    undoMarkClaimed(view);
                                }
                            }).show();
                        }
                    });
                }

            } else {
                Toast.makeText(context, "You are not authorized to mark this post as claimed", Toast.LENGTH_SHORT).show();
            }
        }

        private void undoMarkClaimed(final View view) {
            post.setClaimed(false);
            post.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "Error saving post after setting claimed to false", e);
                        return;
                    }
                    Log.i(TAG, "Successfully marked post as not claimed and saved");
                    notifyDataSetChanged();
                    Snackbar.make(view, "Post marked as not claimed", Snackbar.LENGTH_SHORT).show();

                }
            });

        }
    }


}
