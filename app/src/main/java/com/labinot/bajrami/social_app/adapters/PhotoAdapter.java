package com.labinot.bajrami.social_app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.labinot.bajrami.social_app.Fragments.PostDetailFragment;
import com.labinot.bajrami.social_app.Helper.Constants;
import com.labinot.bajrami.social_app.Helper.GlideEngine;
import com.labinot.bajrami.social_app.R;
import com.labinot.bajrami.social_app.models.Post;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private Context context;
    private List<Post> mPosts;

    public PhotoAdapter(Context context, List<Post> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    @NonNull
    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.photo_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoAdapter.ViewHolder holder, int position) {

        Post post = mPosts.get(position);

        GlideEngine.createGlideEngine().loadImage(context, post.getImageurl(), R.drawable.ic_placeholder, holder.postImage);

        holder.postImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE).edit().putString(Constants.POSTID, post.getPostid()).apply();

                ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostDetailFragment())
                        .addToBackStack(PostDetailFragment.class.getSimpleName()).commit();


            }
        });

    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView postImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            postImage = itemView.findViewById(R.id.post_image);
        }
    }
}
