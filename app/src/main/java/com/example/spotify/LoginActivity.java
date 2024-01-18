package com.example.spotify;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.spotify.Object.Users;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookActivity;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements savedState {

    private Button login, signup, forgot;
    private EditText pwd, email;
    private RelativeLayout gmailLogin;
    private ImageView gmailIcon;
    private Slide slide;

    boolean isDarkMode = true;

    //new var
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onPause() {
        super.onPause();
        saveModeStateData(isDarkMode);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateModeState();

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

        if (!connected)
        {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("connectivity", false);

            startActivity(intent);

            return;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            if(currentUser.isEmailVerified()) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(getApplicationContext(), "Your email has not been verified yet", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //some change
        firebaseAuth = FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);


        login = (Button)findViewById(R.id.btnLogin);
        signup = (Button)findViewById(R.id.btnSignUp);
        forgot = (Button)findViewById(R.id.btnForgotPassword);

        pwd = (EditText)findViewById(R.id.EtPwdLogin);
        email = (EditText)findViewById(R.id.EtEmailLogin);

        gmailLogin = (RelativeLayout)findViewById(R.id.gmailLogin);
        gmailIcon = (ImageView)findViewById(R.id.gmail_svg);

        gmailLogin.setClickable(true);
        gmailLogin.setFocusable(true);
        gmailLogin.setFocusableInTouchMode(false);


        getWindow().setAllowEnterTransitionOverlap(false);

        slide = new Slide(Gravity.BOTTOM);
        slide.setDuration(1);
        getWindow().setExitTransition(slide);

        //reset pwd
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resetPwd = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                resetPwd.putExtra("activity", "login");
                LoginActivity.this.startActivity(resetPwd);
            }
        });

        //onetap gmail setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(LoginActivity.this, gso);

        gmailLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gmailOneTap();  //only method for gmail one tap

            }
        });


        //onetap facebook setup
        CallbackManager callbackManager = CallbackManager.Factory.create();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login.setBackground(getResources().getDrawable(R.drawable.round_btn_selected));
                String inputEmail = email.getText().toString();
                String password = pwd.getText().toString();
                if (inputEmail.contains(" "))
                {
                    Toast.makeText(LoginActivity.this, "Invalid Email", Toast.LENGTH_SHORT).show();
                    login.setBackground(getResources().getDrawable(R.drawable.round_btn));
                    return;
                }

                firebaseAuth.signInWithEmailAndPassword(inputEmail, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                                            if (currentUser != null) {
                                                if (currentUser.isEmailVerified()){
                                                    Toast.makeText(getApplicationContext(), "Login successfully.", Toast.LENGTH_SHORT).show();
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                                else {
                                                    Toast.makeText(getApplicationContext(), "Your email has not been verified yet", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        else Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                    }
                                });


                login.setBackground(getResources().getDrawable(R.drawable.round_btn));
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(myIntent);
            }
        });
    }

    enum ALL_ONE_TAP {
        GMAIL_REQUEST,
        FACEBOOK_REQUEST
    }

    private void facebookOneTap() {
        CallbackManager callbackManager;
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Intent intent = new Intent(LoginActivity.this, FacebookActivity.class);
                        startActivityForResult(intent, ALL_ONE_TAP.FACEBOOK_REQUEST.ordinal());
                        handleFacebookAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.w("Facebook canceled", "Facebook one tap has been canceled");
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Log.e("Facebook exception", String.valueOf(exception));
                    }
                });

    }

    CallbackManager callbackManager;
    private void gmailOneTap() {
        Intent intent = googleSignInClient.getSignInIntent();
        startActivityForResult(intent, ALL_ONE_TAP.GMAIL_REQUEST.ordinal());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ALL_ONE_TAP.GMAIL_REQUEST.ordinal()){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount gsa = task.getResult(ApiException.class);
                settingFirebaseAuth(gsa.getIdToken());
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }

        if (requestCode == ALL_ONE_TAP.FACEBOOK_REQUEST.ordinal()) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            Log.d("check continuous operation", "Good");
        }
    }

    //for gmail
    private void settingFirebaseAuth (String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            addUserToFirestore(user.getEmail(), firebaseAuth);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("token flag", "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("task login check", "signInWithCredential:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("task login check", "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
    }


    private void addUserToFirestore(String email, FirebaseAuth firebaseAuth) {

        String uid = firebaseAuth.getCurrentUser().getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        String userName = "User_" + uid;

        FirestoreDuplicateFlag duplicateFlag = new FirestoreDuplicateFlag(uid);
        duplicateFlag.checkDuplicatedInFirestore(new FirestoreDuplicateFlag.FlagCallback() {
            @Override
            public void isDuplicatedInFirestore(boolean flag) {
                if (flag) return; //handling data existed.
                Log.w("Exists or not", userName);
                String provider = firebaseAuth.getCurrentUser().getProviderId();
                Users newUser = new Users(userName, email, "user", convertedProvider(provider));
                firestore.collection("User").document(firebaseAuth.getCurrentUser().getUid()).set(newUser);
            }
        });


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
        findViewById(R.id.loginAct).setBackgroundColor(getResources().getColor(R.color.lavender_200));

        if (isDarkMode)
        {
            findViewById(R.id.loginAct).setBackgroundColor(getResources().getColor(R.color.dark_200));

        }
    }
}

class FirestoreDuplicateFlag{
    private final String uid;
    public FirestoreDuplicateFlag(String uid) {
        this.uid = uid;
    }
    public interface FlagCallback {
        void isDuplicatedInFirestore(boolean flag);
    }
    public void checkDuplicatedInFirestore(FlagCallback callback){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> documentReference = firestore.collection("User").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {
                        callback.isDuplicatedInFirestore(true);
                    }
                    else {
                        callback.isDuplicatedInFirestore(false);
                    }
                }
            }
        });
    }
}




