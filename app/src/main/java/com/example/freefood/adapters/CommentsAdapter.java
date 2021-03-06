package com.example.freefood.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.freefood.utils.Utils;
import com.example.freefood.activities.ExpandedImageActivity;
import com.example.freefood.databinding.ItemCommentBinding;
import com.example.freefood.models.Comment;
import com.example.freefood.models.User;
import com.parse.ParseFile;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private static final String TAG = "CommentsAdapter";

    private List<Comment> comments;
    private Context context;

    public CommentsAdapter(Context context) {
        this.context = context;
        this.comments = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentsAdapter.ViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(context), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void clear() {
        comments.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<Comment> comments) {
        this.comments.addAll(comments);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ItemCommentBinding binding;

        public ViewHolder(ItemCommentBinding b) {
            super(b.getRoot());
            binding = b;
        }

        public void bind(Comment comment) {
            User author = (User) comment.getAuthor();
            final ParseFile image = comment.getImage();

            binding.tvUsername.setText("@" + author.getUsername());
            if (comment.getDescrption().trim().isEmpty()) {
                binding.tvDescription.setVisibility(View.GONE);
            } else {
                binding.tvDescription.setVisibility(View.VISIBLE);
                binding.tvDescription.setText(comment.getDescrption());
            }
            binding.tvRelativeTime.setText(Utils.getRelativeTimeAgo(comment.getCreatedAt()));
            Glide.with(context)
                    .load(author.getProfileImage().getUrl())
                    .circleCrop()
                    .into(binding.ivProfile);
            if (image == null) {
                binding.ivImage.setVisibility(View.GONE);
            } else {
                binding.ivImage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(image.getUrl())
                        .into(binding.ivImage);
            }

            binding.ivImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, ExpandedImageActivity.class);
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                            (Activity)context,
                            binding.ivImage,
                            "shared_element_container");
                    i.putExtra("imageUrl", image.getUrl());
                    context.startActivity(i, options.toBundle());
                }
            });
        }
    }
}
