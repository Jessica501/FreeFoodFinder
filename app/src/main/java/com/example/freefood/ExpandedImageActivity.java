package com.example.freefood;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.example.freefood.databinding.ActivityExpandedImageBinding;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

public class ExpandedImageActivity extends AppCompatActivity {

    ActivityExpandedImageBinding binding;
    View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityExpandedImageBinding.inflate(getLayoutInflater());
        view = binding.getRoot();

        Window window = getWindow();
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        view.setTransitionName("shared_element_container");

        setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());

        window.setSharedElementEnterTransition(new MaterialContainerTransform()
                .addTarget(view)
                .setDuration(250L));
        window.setSharedElementReturnTransition(new MaterialContainerTransform()
                .addTarget(view)
                .setDuration(250L));

        super.onCreate(savedInstanceState);
        setContentView(view);

        String imageUrl = getIntent().getStringExtra("imageUrl");
        Glide.with(this)
                .load(imageUrl)
                .into(binding.ivExpandedImage);

        binding.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAfterTransition();
            }
        });
    }
}