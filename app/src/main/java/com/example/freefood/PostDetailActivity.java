package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ActivityPostDetailBinding;
import com.example.freefood.models.Post;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.example.freefood.Utils.containsJsontoString;

public class PostDetailActivity extends AppCompatActivity {

    public static final String TAG = "PostDetailActivity";
    ActivityPostDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String object_id = getIntent().getStringExtra("post_id");
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_AUTHOR);
        query.getInBackground(object_id, new GetCallback<Post>() {
            @Override
            public void done(Post post, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Error querying post", e);
                    return;
                }
                if (!ParseUser.getCurrentUser().getObjectId().equals(post.getAuthor().getObjectId())) {
                    binding.btnClaimed.setVisibility(View.INVISIBLE);
                } else {
                    binding.btnClaimed.setVisibility(View.VISIBLE);
                }

                binding.tvTitle.setText(post.getTitle());
                binding.tvLocation.setText(String.valueOf(post.getLocation()));
                binding.tvUsername.setText(post.getAuthor().getUsername());
                String description = post.getDescription().trim();
                if (description.isEmpty()) {
                    binding.tvDescription.setVisibility(View.GONE);
                } else {
                    binding.tvDescription.setVisibility(View.VISIBLE);
                    binding.tvDescription.setText(post.getDescription());
                }
                try {
                    binding.tvAllergens.setText(containsJsontoString(post.getContains()));
                } catch (JSONException ex) {
                    Log.e(TAG, "Error converting contains JSONObject to String", ex);
                }
                if (post.getImage() != null) {
                    Glide.with(PostDetailActivity.this)
                            .load(post.getImage().getUrl())
                            .into(binding.ivImage);
                } else {
                    binding.ivImage.setPadding(64, 64, 64, 64);
                }
                ParseFile profileImage = post.getAuthor().getParseFile("profileImage");
                Glide.with(PostDetailActivity.this)
                        .load(profileImage.getUrl())
                        .circleCrop()
                        .into(binding.ivProfile);

            }
        });
    }


}