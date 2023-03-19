package com.labinot.bajrami.social_app.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.labinot.bajrami.social_app.Helper.Constants;
import com.labinot.bajrami.social_app.Helper.GlideEngine;
import com.labinot.bajrami.social_app.Helper.Utils;
import com.labinot.bajrami.social_app.R;
import com.labinot.bajrami.social_app.adapters.CommentAdapter;
import com.labinot.bajrami.social_app.models.Comment;
import com.labinot.bajrami.social_app.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentActivity extends AppCompatActivity {

    private String postID;
    private RecyclerView recyclerView;
    private List<Comment> commentList;
    private CommentAdapter commentAdapter;
    private FirebaseUser firebaseUser;
    private EditText add_comment;
    private TextView post_txt;
    private CircleImageView imageProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.comment_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Intent intent = getIntent();
        postID = intent.getStringExtra(Constants.POSTID);


        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(CommentActivity.this,postID,commentList);
        recyclerView.setAdapter(commentAdapter);

        imageProfile = findViewById(R.id.image_profile);
        add_comment = findViewById(R.id.add_comment);
        post_txt = findViewById(R.id.post);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        add_comment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getUserImage();


        post_txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(add_comment.getText().toString()))
                    return;
                    //Utils.showMessageSnackBar(CommentActivity.this, findViewById(R.id.commentMainLayout), getString(R.string.no_comment_added));
                else
                    putComment();


            }
        });

        getComment();
    }

    private void checkText(String text) {

        if(!TextUtils.isEmpty(text))
            post_txt.setTextColor(getApplicationContext().getColor(R.color.primary));
        else
            post_txt.setTextColor(getApplicationContext().getColor(R.color.primary_light));

    }

    private void getComment() {

        FirebaseDatabase.getInstance().getReference().child(Constants.COMMENTS).child(postID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentList.clear();

                for(DataSnapshot snap: snapshot.getChildren()){

                    Comment comment = snap.getValue(Comment.class);
                    commentList.add(comment);

                }

               commentAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void putComment() {

        HashMap<String,Object> map = new HashMap<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child(Constants.COMMENTS).child(postID);

        String id = reference.push().getKey();

        map.put(Constants.ID, id);
        map.put(Constants.COMMENT, add_comment.getText().toString());
        map.put(Constants.PUBLISHER,firebaseUser.getUid());

        add_comment.setText("");

        reference.child(id).setValue(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful())
                    Utils.showMessageSnackBar(CommentActivity.this, findViewById(R.id.commentMainLayout), getString(R.string.comment_added));
                 else
                    Utils.showMessageSnackBar(CommentActivity.this, findViewById(R.id.commentMainLayout), task.getException().getMessage());


            }
        });

    }


    private void getUserImage() {

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                if(!user.getImageurl().equals(Constants.IMAGE_DEFAULT_URL))
                    GlideEngine.createGlideEngine().loadImage(CommentActivity.this, user.getImageurl(), imageProfile);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}