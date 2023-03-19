package com.labinot.bajrami.social_app.activities;

import static com.labinot.bajrami.social_app.Helper.Utils.getFileExtension;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;

import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.labinot.bajrami.social_app.Helper.Constants;
import com.labinot.bajrami.social_app.Helper.GlideEngine;
import com.labinot.bajrami.social_app.Helper.Utils;
import com.labinot.bajrami.social_app.MainActivity;
import com.labinot.bajrami.social_app.R;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.listener.OnResultCallbackListener;


import java.io.File;
import java.util.HashMap;
import java.util.List;

public class PostActivity extends AppCompatActivity {


    private ContentValues contentValues;
    private Uri imageUri;
    private String imageUrl;
    private SocialAutoCompleteTextView description;
    private ImageView close;
    private TextView post;
    private ImageView imageAdded;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        description = findViewById(R.id.description);
        close = findViewById(R.id.close);
        post = findViewById(R.id.post);
        imageAdded = findViewById(R.id.image_added);

        description.setMentionEnabled(false);
        description.setHashtagColor(getApplicationContext().getColor(R.color.primary));

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                uploadToServer();


            }
        });
        
        uploadPhotos();

    }

    private void uploadPhotos() {

        // Take Photo from Camera Snippet
//        contentValues = new ContentValues();
//        contentValues.put(MediaStore.Images.Media.TITLE, "MyPicture");
//        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());
//        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
//
//        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//        startActivityForResult(takePicture, 0);

        // Get Photo from Gallery Snippet
        //Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // startActivityForResult(pickPhoto, 1);

        InsGallery.openGallery(PostActivity.this, GlideEngine.createGlideEngine(), new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {

                for(LocalMedia media: result){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        imageUri = Uri.fromFile(new File(media.getAndroidQToPath()));
                     else
                        imageUri = Uri.fromFile(new File(media.getPath()));

                     imageAdded.setImageURI(imageUri);

                }

            }

            @Override
            public void onCancel() {

                finish();
            }
        });


    }

    private void uploadToServer() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        if(imageUri != null){

            StorageReference filePath = FirebaseStorage.getInstance().getReference(Constants.POSTS).child(System.currentTimeMillis() + "." + getFileExtension(PostActivity.this,imageUri));

            StorageTask uploadTask = filePath.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {

                    if(!task.isSuccessful())
                        throw task.getException();

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    Uri downloadUri = task.getResult();
                    imageUrl = downloadUri.toString();

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Constants.POSTS);
                    String postId = reference.push().getKey();

                    HashMap<String, Object> map = new HashMap<>();
                    map.put(Constants.POSTID,postId);
                    map.put(Constants.IMAGEURL,imageUrl);
                    map.put(Constants.DESCRIPTION,description.getText().toString());
                    map.put(Constants.PUBLISHER, FirebaseAuth.getInstance().getCurrentUser().getUid());

                    reference.child(postId).setValue(map);

                    DatabaseReference mHashTagRef = FirebaseDatabase.getInstance().getReference().child(Constants.HASH_TAGS);
                    List<String> hashTags = description.getHashtags();

                    if(!hashTags.isEmpty()){

                        for(String tag : hashTags){

                            map.clear();

                            map.put(Constants.HASHTAG,tag.toLowerCase());
                            map.put(Constants.POSTID,postId);

                            mHashTagRef.child(tag.toLowerCase()).child(postId).setValue(map);

                        }
                    }

                    progressDialog.dismiss();
                    finish();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                   progressDialog.dismiss();
                    Utils.showMessageSnackBar(PostActivity.this, findViewById(R.id.register_layout), e.getMessage());

                }
            });



        }else{
            Utils.showMessageSnackBar(PostActivity.this, findViewById(R.id.register_layout), getString(R.string.no_image_selected));
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        ArrayAdapter<Hashtag> hashtagArrayAdapter = new HashtagArrayAdapter<>(getApplicationContext());

        FirebaseDatabase.getInstance().getReference().child(Constants.HASH_TAGS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot dataSnapshot: snapshot.getChildren()){

                    hashtagArrayAdapter.add(new Hashtag(dataSnapshot.getKey(),(int)dataSnapshot.getChildrenCount()));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

          description.setHashtagAdapter(hashtagArrayAdapter);

    }
}