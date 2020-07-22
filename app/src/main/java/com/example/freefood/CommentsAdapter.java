package com.example.freefood;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
            ParseFile image = comment.getImage();

            binding.tvUsername.setText("@" + author.getUsername());
            binding.tvDescription.setText(comment.getDescrption());
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
        }
    }
}
