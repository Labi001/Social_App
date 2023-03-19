package com.labinot.bajrami.social_app;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labinot.bajrami.social_app.Fragments.HomeFragment;
import com.labinot.bajrami.social_app.Fragments.NotificationFragment;
import com.labinot.bajrami.social_app.Fragments.ProfileFragment;
import com.labinot.bajrami.social_app.Fragments.SearchFragment;
import com.labinot.bajrami.social_app.Helper.Constants;
import com.labinot.bajrami.social_app.Helper.GlideEngine;
import com.labinot.bajrami.social_app.activities.CommentActivity;
import com.labinot.bajrami.social_app.activities.PostActivity;
import com.labinot.bajrami.social_app.models.User;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;
    private CircleImageView profile_img;
    private FirebaseUser firebaseUser;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        profile_img = findViewById(R.id.img_profile);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                if(!user.getImageurl().equals(Constants.IMAGE_DEFAULT_URL))
                    GlideEngine.createGlideEngine().loadImage(MainActivity.this,user.getImageurl(),profile_img);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new ProfileFragment())
                        .addToBackStack(HomeFragment.class.getSimpleName())
                        .commit();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.nav_home:
                selectorFragment = new HomeFragment();
                break;

                case R.id.nav_search:
                selectorFragment = new SearchFragment();
                break;

                case R.id.nav_add:
                selectorFragment = null;
                startActivity(new Intent(MainActivity.this, PostActivity.class));
                break;
                case R.id.nav_heart:
                selectorFragment = new NotificationFragment();
                break;

                }

                if (selectorFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectorFragment)
                            .addToBackStack(selectorFragment.getClass().getSimpleName())
                            .commit();
                }

                return true;
            }
        });


        Bundle inBundle = getIntent().getExtras();
        if (inBundle != null) {

            String profileId = inBundle.getString(Constants.PUBLISHER_ID);
            getSharedPreferences(Constants.PROFILE, MODE_PRIVATE).edit().putString(Constants.PROFILE_ID, profileId).apply();

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment())

                    .commit();

        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .addToBackStack(HomeFragment.class.getSimpleName())
                    .commit();
        }

    }
}