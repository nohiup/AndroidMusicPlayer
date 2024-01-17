package com.example.spotify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ChangePassword extends AppCompatActivity {

    private Button confirm;
    private EditText pwd, retype, oldPwd;
    private LinearLayout codeInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        setView();
    }
    private void setView()
    {
        confirm = findViewById(R.id.reset_btn);
        pwd = findViewById(R.id.EtPwdReset);
        retype = findViewById(R.id.EtRetype);
        codeInput.setVisibility(View.GONE);
    }
}