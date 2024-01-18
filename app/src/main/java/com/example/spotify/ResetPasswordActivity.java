package com.example.spotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class ResetPasswordActivity extends AppCompatActivity implements savedState{

    private Button sendCode, confirm;
    private EditText email;
    private FirebaseAuth firebaseAuth;

    private boolean isDarkMode = true;
    private int counter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        setView();

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Add sendPasswordResetEmail
                sendPasswordResetEmail();

                counter = 60;
                sendCode.setClickable(false);
                new CountDownTimer(60000, 1000){
                    public void onTick(long millisUntilFinished){
                        sendCode.setText("TRY AGAIN AFTER " + String.valueOf(counter) + "s");
                        counter--;
                        sendCode.setBackgroundColor(getResources().getColor(R.color.black_300));
                        sendCode.setElevation(0);
                    }
                    public  void onFinish(){
                        sendCode.setText("SEND CONFIRMATION CODE");
                        sendCode.setClickable(true);
                        sendCode.setBackground(getResources().getDrawable(R.drawable.round_btn));
                        sendCode.setElevation(10);
                    }
                }.start();
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String activity = getIntent().getStringExtra("activity");
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                if (activity.contains("profile")) {
                    intent = new Intent(v.getContext(), ProfileActivity.class);
                }
                if (activity.contains("login")) {
                    intent = new Intent(v.getContext(), LoginActivity.class);
                }
                v.getContext().startActivity(intent);
            }
        });
    }




    private void sendPasswordResetEmail() {
        String userEmail = String.valueOf(email.getText());

        AvailableEmailFlag availableEmailFlag = new AvailableEmailFlag(userEmail);
        availableEmailFlag.checkLegitEmail(new AvailableEmailFlag.FlagCallback() {
            @Override
            public void isLegitEmail(boolean checker) {
                if (checker) {
                    Log.d("Email legit", "True");
                    actionSend(userEmail);
                }
                if (!checker) {
                    Log.d("Email not legit", "False");
                    Toast.makeText(ResetPasswordActivity.this, "Can not find email in database, please try again ~~", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void actionSend(String email) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(ResetPasswordActivity.this, "Email sent ~~ ", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Email sending Error", e.toString());
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
        findViewById(R.id.passwordAct).setBackgroundColor(getResources().getColor(R.color.lavender_200));

        if (isDarkMode)
        {
            findViewById(R.id.passwordAct).setBackgroundColor(getResources().getColor(R.color.dark_200));

        }
    }

    private void setView()
    {
        sendCode = findViewById(R.id.btnSendCode);
        confirm = findViewById(R.id.reset_btn);
        email = findViewById(R.id.EtEmailReset);
    }

}

class AvailableEmailFlag {
    private final String email;

    public AvailableEmailFlag(String email) {
        this.email = email;
    }

    public interface FlagCallback {
        public void isLegitEmail(boolean checker);
    }

    public void checkLegitEmail(FlagCallback flagCallback) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Task<QuerySnapshot> querySnapshotTask = firestore.collection("User")
                .whereEqualTo("email", this.email)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()){
                            flagCallback.isLegitEmail(true);
                        }
                        else flagCallback.isLegitEmail(false);
                    }
                });
    }


}


