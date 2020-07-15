package com.example.freefood;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ItemPostBinding;
import com.example.freefood.models.Post;
import com.parse.ParseFile;

import org.json.JSONException;

import java.util.List;

import static com.example.freefood.Utils.containsJsontoString;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    public static final String TAG = "PostsAdapter";

    private List<Post> posts;
    private Context context;

    public PostsAdapter(Context context, List<Post> posts) {
        this.context = context;
        this.posts = posts;
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
        this.posts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Post> posts) {
        this.posts.addAll(posts);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ItemPostBinding binding;

        public ViewHolder(ItemPostBinding b) {
            super(b.getRoot());
            binding = b;
            itemView.setOnClickListener(this);
        }

        public void bind(Post post) {
            binding.tvTitle.setText(post.getTitle());
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

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                Intent i = new Intent(context, PostDetailActivity.class);
                Post post = posts.get(position);
                i.putExtra("post_id", post.getObjectId());
                context.startActivity(i);
            }
        }
    }
}
