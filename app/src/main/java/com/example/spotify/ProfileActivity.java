package com.example.spotify;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity implements savedState {

    private Button save, cancel, changePassword;
    private EditText username;
    private ImageView ava;
    boolean isDarkMode = true;
    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    ActivityResultLauncher<Intent> launchGalleryActivity;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setView();
        setUsername(); //add

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                changeName();
                startActivity(intent);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ResetPasswordActivity.class);
                intent.putExtra("activity", "profile");
                startActivity(intent);
            }
        });

        launchGalleryActivity = registerForActivityResult(
                new ActivityResultContracts
                        .StartActivityForResult(),
                result -> {
                    if (result.getResultCode()
                            == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // do your operation from here....
                        if (data != null
                                && data.getData() != null) {
                            Uri selectedImageUri = data.getData();
                            Bitmap selectedImageBitmap = getBitmap(R.drawable.ava_1);
                            try {
                                selectedImageBitmap = MediaStore.Images.Media.getBitmap(
                                        this.getContentResolver(),
                                        selectedImageUri);
                                if (selectedImageBitmap != getBitmap(R.drawable.ava_1)) {
                                    StorageReference storage = FirebaseStorage.getInstance().getReference();

                                    String extension = selectedImageUri.toString().substring(selectedImageUri.toString().lastIndexOf("."));
                                    StorageReference uploadRef = storage.child("Avatars/" + thisUser.getUid() + ".jpg");
                                    UploadTask uploadTask = uploadRef.putFile(selectedImageUri);
                                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    Log.d("Checking", uploadRef.getDownloadUrl().toString());
                                                    firestore.collection("User").document(thisUser.getUid()).update("avatarDir", uploadRef.getDownloadUrl().toString());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.d("Failure read", e.toString());
                                                }
                                            });
                                }
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                            ava.setImageBitmap(selectedImageBitmap);
                        }
                    }
                });

        ava.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);

                launchGalleryActivity.launch(i);
            }
        });
        try {
            setAvatar();
        } catch (Exception e)
        {
            Bitmap selectedImageBitmap = getBitmap(R.drawable.ava_1);
            ava.setImageBitmap(selectedImageBitmap);
        }
    }
    private void setView(){
        save = findViewById(R.id.changeInfo);
        cancel = findViewById(R.id.cancel);
        changePassword = findViewById(R.id.changePwd);

        username = findViewById(R.id.EtUsername);

        ava = findViewById(R.id.avatar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateModeState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveModeStateData(isDarkMode);
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, 50,50);
        drawable.draw(canvas);

        return bitmap;
    }

    private void changeName() {
        String name = username.getText().toString();
        String UID = thisUser.getUid();
        firestore.collection("User").document(UID).update("name", name)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            username.setText(name);
                        }
                    }
                });
    }

    private void setUsername() {
        Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("User")
                .document(thisUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            String name = task.getResult().get("name").toString();
                            Log.d("Name", name);
                            username.setText(name);
                        }
                    }
                });
    }

    private void setAvatar() {
        Task<DocumentSnapshot> doc = firestore.collection("User").document(thisUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d("ava", "OnComplete");
                        if (task.isSuccessful()) {
                            Log.d("ava", "IsSuccess");
                            if (task.getResult().get("avatarDir") == null) {
                                Log.d("ava", "NotNull");
                                Bitmap selectedImageBitmap = getBitmap(R.drawable.ava_1);
                                ava.setImageBitmap(selectedImageBitmap);
                            } else {
                                String avatarDir = task.getResult().get("avatarDir").toString();
                                final long ONE_MEGABYTE = 1024 * 1024;
                                Log.d("ava", avatarDir);
                                storageReference.child("Avatars").child(thisUser.getUid() + ".jpg")
                                        .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Log.d("ava", avatarDir);
                                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                ava.setImageBitmap(bmp);
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    @Override
    public void saveModeStateData(boolean isDarkMode)
    {
        SharedPreferences saveModeContainer = getSharedPreferences("SaveModeState", Activity.MODE_PRIVATE);
        SharedPreferences.Editor saveModeContainerEditor = saveModeContainer.edit();
        String key = "mode";
        saveModeContainerEditor.putBoolean("mode", isDarkMode);
        saveModeContainerEditor.commit();
    }

    @Override
    public void updateModeState() {
        SharedPreferences saveModeContainer = getSharedPreferences("SaveModeState", Activity.MODE_PRIVATE);
        boolean defaultValue = isDarkMode;
        String key = "mode";
        if (( saveModeContainer != null ) && saveModeContainer.contains(key))
        {
            this.isDarkMode = saveModeContainer.getBoolean(key, defaultValue);
        }

        setMode();
    }

    private void setMode(){
        findViewById(R.id.profileAct).setBackgroundColor(getResources().getColor(R.color.lavender_200));

        save.setBackgroundColor(getResources().getColor(R.color.lavender_200));
        save.setTextColor(getResources().getColor(R.color.dark_200));
        cancel.setTextColor(getResources().getColor(R.color.dark_200));
        cancel.setBackgroundColor(getResources().getColor(R.color.lavender_200));

        changePassword.setTextColor(getResources().getColor(R.color.dark_200));
        changePassword.setBackgroundColor(getResources().getColor(R.color.lavender_200));
        if (isDarkMode)
        {
            findViewById(R.id.profileAct).setBackgroundColor(getResources().getColor(R.color.dark_200));
            save.setBackgroundColor(getResources().getColor(R.color.dark_200));
            save.setTextColor(getResources().getColor(R.color.cream_200));
            cancel.setTextColor(getResources().getColor(R.color.cream_200));
            cancel.setBackgroundColor(getResources().getColor(R.color.dark_200));

            changePassword.setTextColor(getResources().getColor(R.color.cream_200));
            changePassword.setBackgroundColor(getResources().getColor(R.color.dark_200));
        }
    }
}