package com.labinot.bajrami.social_app.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labinot.bajrami.social_app.Helper.Constants;
import com.labinot.bajrami.social_app.Helper.GlideEngine;
import com.labinot.bajrami.social_app.R;
import com.labinot.bajrami.social_app.activities.EditProfileActivity;
import com.labinot.bajrami.social_app.activities.FollowersActivity;
import com.labinot.bajrami.social_app.activities.OptionsActivity;
import com.labinot.bajrami.social_app.adapters.PhotoAdapter;
import com.labinot.bajrami.social_app.models.Notification;
import com.labinot.bajrami.social_app.models.Post;
import com.labinot.bajrami.social_app.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private String profileId;
    private RecyclerView recycler_view_saved;
    private PhotoAdapter photoAdapter;
    private List<Post> myPhotoList;

    private RecyclerView recycler_view_pictures;
    private List<Post> mySavedPosts;
    private PhotoAdapter postAdapterSaves;

    private TabLayout tabLayout;

    private TextView username;
    private TextView fullname;
    private TextView bio;

    private CircleImageView image_profile;
    private TextView posts;
    private TextView followers;
    private TextView following;

    private LinearLayout following_layout;
    private LinearLayout followers_layout;

    private Button edit_profile;

    private ImageView options;
    private FirebaseUser firebaseUser;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        saveProfile();

        image_profile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);

        followers_layout = view.findViewById(R.id.followers_layout);
        following_layout = view.findViewById(R.id.following_layout);

        posts = view.findViewById(R.id.posts);
        fullname = view.findViewById(R.id.fullname);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);

        tabLayout = view.findViewById(R.id.tabLayout);
        edit_profile = view.findViewById(R.id.btn_followF);


        recycler_view_pictures = view.findViewById(R.id.recycler_view_pictures);
        recycler_view_pictures.setHasFixedSize(true);
        recycler_view_pictures.setLayoutManager(new GridLayoutManager(getContext(), 3));
        myPhotoList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), myPhotoList);
        recycler_view_pictures.setAdapter(photoAdapter);

        recycler_view_saved = view.findViewById(R.id.recycler_view_saved);
        recycler_view_saved.setHasFixedSize(true);
        recycler_view_saved.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mySavedPosts = new ArrayList<>();
        postAdapterSaves = new PhotoAdapter(getContext(), mySavedPosts);
        recycler_view_saved.setAdapter(postAdapterSaves);


        userInfo();
        getFollowersAndFollowingCount();
        getPostCount();
        myPhotos();
        getSavedPost();
        setupTabLayout();


        if(profileId.equals(firebaseUser.getUid()))
            edit_profile.setText(getString(R.string.edit_profile));
        else
            checkFollowingStatus();




        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String btn_text = edit_profile.getText().toString();

                if (btn_text.equals(getString(R.string.edit_profile))) {
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else {

                if (btn_text.equals("follow")) {

                    FirebaseDatabase.getInstance().getReference()
                            .child(Constants.FOLLOW)
                            .child(firebaseUser.getUid())
                            .child(Constants.FOLLOWING)
                            .child(profileId).setValue(true);

                    FirebaseDatabase.getInstance().getReference()
                            .child(Constants.FOLLOW)
                            .child(profileId)
                            .child(Constants.FOLLOWERS)
                            .child(firebaseUser.getUid()).setValue(true);

                    addNotification(profileId,true);

                } else {

                    FirebaseDatabase.getInstance().getReference()
                            .child(Constants.FOLLOW)
                            .child(firebaseUser.getUid())
                            .child(Constants.FOLLOWING)
                            .child(profileId).removeValue();

                    FirebaseDatabase.getInstance().getReference()
                            .child(Constants.FOLLOW)
                            .child(profileId)
                            .child(Constants.FOLLOWERS)
                            .child(firebaseUser.getUid()).removeValue();

                    addNotification(profileId,false);

                }

            }


            }
        });

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(getContext(), OptionsActivity.class));
            }
        });

        following_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra(Constants.ID, profileId);
                intent.putExtra(Constants.TITLE, Constants.FOLLOWINGS_HELPER);
                startActivity(intent);
            }
        });

        followers_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra(Constants.ID, profileId);
                intent.putExtra(Constants.TITLE, Constants.FOLLOWERS_HELPER);
                startActivity(intent);
            }
        });
        

        return view;
    }

    private void addNotification(String profileId, boolean follow) {

        if (follow) {

            HashMap<String, Object> map = new HashMap<>();

            map.put(Notification.USERID, firebaseUser.getUid());
            map.put(Notification.TEXT, "started following you.");
            map.put(Notification.POSTID, "");
            map.put(Notification.IS_POST, false);

            FirebaseDatabase.getInstance().getReference().child(Constants.NOTIFICATIONS).child(profileId).push().setValue(map);

        } else {
            FirebaseDatabase.getInstance().getReference().child(Constants.NOTIFICATIONS).child(profileId).removeValue();
        }

    }

    private void checkFollowingStatus() {

        FirebaseDatabase.getInstance().getReference().child(Constants.FOLLOW).child(firebaseUser.getUid()).child(Constants.FOLLOWING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.child(profileId).exists())

                    edit_profile.setText("following");
                else
                    edit_profile.setText("follow");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }

    private void userInfo() {

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(profileId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                if(!user.getImageurl().equals(Constants.IMAGE_DEFAULT_URL) && !getActivity().isFinishing())
                    GlideEngine.createGlideEngine().loadImage(getContext(), user.getImageurl(), image_profile);

                username.setText(user.getUsername());
                fullname.setText(user.getName());
                bio.setText(user.getBio());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getFollowersAndFollowingCount() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.FOLLOW).child(profileId);

        reference.child(Constants.FOLLOWING).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                following.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        reference.child(Constants.FOLLOWERS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                 followers.setText("" + snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void getPostCount() {

        FirebaseDatabase.getInstance().getReference().child(Constants.POSTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                int counter = 0;

                for(DataSnapshot snap: snapshot.getChildren()){

                    Post post = snap.getValue(Post.class);

                    if(post.getPublisher().equals(profileId))
                        counter++;

                }

                posts.setText(String.valueOf(counter));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void myPhotos() {

        FirebaseDatabase.getInstance().getReference().child(Constants.POSTS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                myPhotoList.clear();

                for (DataSnapshot snap : snapshot.getChildren()) {

                    Post post = snap.getValue(Post.class);

                    if (post.getPublisher().equals(profileId)) {

                        myPhotoList.add(post);

                    }

                }

                Collections.reverse(myPhotoList);
                photoAdapter.notifyDataSetChanged();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSavedPost() {

        List<String> savedIds = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference().child(Constants.SAVES).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot snap:snapshot.getChildren()){

                    savedIds.add(snap.getKey());
                }

                FirebaseDatabase.getInstance().getReference().child(Constants.POSTS).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        mySavedPosts.clear();

                        for(DataSnapshot snap1: snapshot.getChildren()){

                           Post post = snap1.getValue(Post.class);

                           for(String id:savedIds){

                               if(post.getPostid().equals(id))
                                   mySavedPosts.add(post);

                           }

                        }
                       postAdapterSaves.notifyDataSetChanged();
                    }



                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    private void setupTabLayout() {


        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_grid_icon));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.ic_save_icon));

        tabLayout.setTabIconTint(new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{android.R.attr.state_enabled}
                },
                new int[]{
//                        is Deprecated
//                        getResources().getColor(R.color.primary),
//                        getResources().getColor(R.color.accent),

                        getContext().getColor(R.color.primary),
                        getContext().getColor(R.color.accent),

                }
        ));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        if(edit_profile.getText().toString().equals("follow"))
                            recycler_view_pictures.setVisibility(View.GONE);

                        break;
                    case 1:


                        recycler_view_pictures.setVisibility(View.GONE);
                        recycler_view_saved.setVisibility(View.VISIBLE);
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void saveProfile() {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        String data = getContext().getSharedPreferences(Constants.PROFILE, Context.MODE_PRIVATE).getString(Constants.PROFILE_ID, Constants.NONE);

        if (data.equals(Constants.NONE)) {

            profileId = firebaseUser.getUid();

        } else {

            profileId = data;

            getContext().getSharedPreferences(Constants.PROFILE, Context.MODE_PRIVATE).edit().clear().apply();

        }

    }
}