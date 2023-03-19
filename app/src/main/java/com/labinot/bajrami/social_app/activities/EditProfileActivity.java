package com.labinot.bajrami.social_app.activities;

import static com.labinot.bajrami.social_app.Helper.Utils.getFileExtension;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.labinot.bajrami.social_app.Helper.Constants;
import com.labinot.bajrami.social_app.Helper.GlideEngine;
import com.labinot.bajrami.social_app.Helper.Utils;
import com.labinot.bajrami.social_app.R;
import com.labinot.bajrami.social_app.models.User;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.instagram.InsGallery;
import com.luck.picture.lib.listener.OnResultCallbackListener;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private static String LOG_TAG = "EditProfileLog";
    private ImageView close;
    private CircleImageView imageProfile;
    private TextView save;
    private TextView changePhoto;
    private TextInputEditText fullname;
    private TextInputEditText username;
    private TextInputEditText bio;

    private FirebaseUser firebaseUser;
    private StorageReference storageReference;
    private Uri mImageUri;
    private StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        close = findViewById(R.id.close);
        imageProfile = findViewById(R.id.image_profile);
        save = findViewById(R.id.save);
        changePhoto = findViewById(R.id.change_photo);
        fullname = findViewById(R.id.fullname);
        username = findViewById(R.id.username);
        bio = findViewById(R.id.bio);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child(Constants.UPLOADS);

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user = snapshot.getValue(User.class);

                fullname.setText(user.getName());
                username.setText(user.getUsername());
                bio.setText(user.getBio());

                if (!user.getImageurl().equals(Constants.IMAGE_DEFAULT_URL) && !isFinishing()) {
                    GlideEngine.createGlideEngine()
                            .loadImage(
                                    EditProfileActivity.this,
                                    user.getImageurl(),
                                    imageProfile);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        
        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                uploadPhoto();
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadPhoto();
            }
        });
        
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
                updateProfile();
                finish();
            }
        });


    }

    private void updateProfile() {

        HashMap<String, Object> map = new HashMap<>();
        map.put(Constants.NAME,fullname.getText().toString());
        map.put(Constants.USERNAME,username.getText().toString());
        map.put(Constants.BIO,bio.getText().toString());

        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(firebaseUser.getUid()).updateChildren(map);

    }

    private void uploadPhoto() {

        InsGallery.openGallery(EditProfileActivity.this, GlideEngine.createGlideEngine(), new OnResultCallbackListener<LocalMedia>() {
            @Override
            public void onResult(List<LocalMedia> result) {

                for(LocalMedia media : result){

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
                        mImageUri = Uri.fromFile(new File(media.getAndroidQToPath()));
                     else
                        mImageUri = Uri.fromFile(new File(media.getPath()));

                     uploadToServer();

                }

            }

            @Override
            public void onCancel() {

            }
        });

    }

    private void uploadToServer() {

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.uploading));

        if(mImageUri != null){

            progressDialog.show();

            StorageReference filePath = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(EditProfileActivity.this, mImageUri));

            uploadTask = filePath.putFile(mImageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();
                        FirebaseDatabase.getInstance().getReference().child(Constants.USERS).child(firebaseUser.getUid()).child(Constants.IMAGEURL).setValue(url);
                    } else {
                        Utils.showMessageSnackBar(EditProfileActivity.this, findViewById(R.id.edit_profile_main_layout), getString(R.string.upload_failed));
                    }

                    progressDialog.dismiss();

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Log.e(LOG_TAG, "onFailure - ", e);
                    Utils.showMessageSnackBar(EditProfileActivity.this, findViewById(R.id.edit_profile_main_layout), e.getMessage());
                }
            });


        }else{

            Utils.showMessageSnackBar(EditProfileActivity.this, findViewById(R.id.edit_profile_main_layout), getString(R.string.no_image_selected));
        }

    }
}