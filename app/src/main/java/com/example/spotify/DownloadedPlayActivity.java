package com.example.spotify;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.view.WindowCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.spotify.databinding.ActivityDownloadedPlayBinding;

import com.example.spotify.R;

import java.io.File;
import java.io.IOException;

public class DownloadedPlayActivity extends AppCompatActivity {


    private TextView name,artist,playDur, realDur;
    private ImageView clickablePlayPause;

    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded_play);

        //set View for all clickable and data
        name = findViewById(R.id.song_name);
        artist = findViewById(R.id.song_artist);
        seekBar = findViewById(R.id.seekBar);
        playDur = findViewById(R.id.durationPlayed);
        realDur = findViewById(R.id.durationTotal);

        clickablePlayPause = findViewById(R.id.play_pause);

        mediaPlayer = new MediaPlayer();

        String[] data = new String[2];
        data = getIntent().getStringArrayExtra("data");
        if (data == null || data.length == 0) return;
        final String name1 = data[0];
        final String author1 = data[1];

        name.setText(name1);
        artist.setText(author1);

        final String finalDir = combineFileDir(name1, author1);
        long duration = 0;

        try {
            mediaPlayer.setDataSource(finalDir);
            mediaPlayer.prepare();
            duration = mediaPlayer.getDuration();
            realDur.setText(String.valueOf(duration/60000) + ":" + String.valueOf(duration%60000/1000));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        clickablePlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    clickablePlayPause.setImageResource(R.drawable.play_btn);
                }
                else if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                    clickablePlayPause.setImageResource(R.drawable.pause_btn);
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int duration = mediaPlayer.getDuration();
                    int newPosition = (progress * duration) / 100;
                    playDur.setText(String.valueOf(newPosition/60000)+":"+String.valueOf(newPosition%60000/1000));
                    mediaPlayer.seekTo(newPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int totalDuration = mediaPlayer.getDuration();

                    // Cập nhật giá trị của SeekBar dựa trên thời gian hiện tại của MediaPlayer
                    int progress = (currentPosition * 100) / totalDuration;
                    playDur.setText(String.valueOf(currentPosition/60000)+":"+String.valueOf(currentPosition%60000/1000));
                    seekBar.setProgress(progress);
                }

                // Lặp lại việc cập nhật sau một khoảng thời gian
                handler.postDelayed(this, 1000); // Cập nhật mỗi giây
            }
        }, 1000); // Bắt đầu cập nhật sau một giây
    }

    private String combineFileDir(String name, String author){
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC).toURI());
        String fileDir = file.getAbsolutePath();
        String tempString = fileDir+"/"+name+"-"+author+"."+"mp3";
        Log.d("tempString", tempString);
        return tempString;
    }
}