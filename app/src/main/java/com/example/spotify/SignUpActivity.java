package com.example.spotify;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotify.Object.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignUpActivity extends AppCompatActivity implements savedState {

    private Button signup;
    private EditText pwd, email, confirmPassword;

    private FirebaseAuth firebaseAuth;
    private Slide slide;

    private boolean isDarkMode = false;

    @Override
    public void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }

        updateModeState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveModeStateData(isDarkMode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();
        //signing up process
        //UI SETUP
        email = (EditText) findViewById(R.id.EtEmailLogin);
        pwd = (EditText) findViewById(R.id.EtPwdRegister);
        confirmPassword = (EditText) findViewById(R.id.EtPwdRegisterRetype);
        signup = (Button)findViewById(R.id.btnSignUpActivity);

        //clicked signup button (confirm)
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(intent);
                String e_data = String.valueOf(email.getText());
                String p_data = String.valueOf(pwd.getText());
                String rp_data = String.valueOf(confirmPassword.getText());
                Log.w("pass and retype", p_data + " on " + rp_data);
                if (!p_data.equals(rp_data)) {
                    Toast.makeText(getApplicationContext(), "Incorrect data inserted, try do it again", Toast.LENGTH_SHORT).show();
                    return;
                }
                firebaseAuth.createUserWithEmailAndPassword(e_data, p_data)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                    if (currentUser != null) {
                                        currentUser.sendEmailVerification();
                                        Toast.makeText(getApplicationContext(),"Confirm this account in your email", Toast.LENGTH_SHORT).show();
                                        addUserToFirestore(e_data, firebaseAuth);

                                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "Register failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                slide = new Slide(Gravity.TOP);
                slide.setDuration(1);
                getWindow().setEnterTransition(slide);
            }
        });
    }


    private void addUserToFirestore(String email, FirebaseAuth firebaseAuth) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userName = "User_" + firebaseAuth.getCurrentUser().getUid();
        String provider = firebaseAuth.getCurrentUser().getProviderId();
        Users newUser = new Users(userName, email, "user", convertedProvider(provider));
        firestore.collection("User").document(firebaseAuth.getCurrentUser().getUid()).set(newUser);
    }

    private String convertedProvider(String providerID){
        switch (providerID){
            case "firebase": return "Firebase";
            case "phone": return "Phone";
            case "google.com": return "Gmail";
            case "facebook.com": return "Facebook";
            case "twitter.com": return "Twitter";
            case "github.com": return "Github";
            case "apple.com": return "Apple";
            case "yahoo.com": return "Yahoo";
            case "hotmail.com": return "Microsoft";
            default: return null;
        }
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
        findViewById(R.id.passwordSignUp).setBackgroundColor(getResources().getColor(R.color.lavender_200));

        if (isDarkMode)
        {
            findViewById(R.id.passwordSignUp).setBackgroundColor(getResources().getColor(R.color.dark_200));
        }
    }
}

