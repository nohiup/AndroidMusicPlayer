package com.example.spotify;

import static com.example.spotify.ApplicationClass.ACTION_NEXT;
import static com.example.spotify.ApplicationClass.ACTION_PLAY;
import static com.example.spotify.ApplicationClass.ACTION_PREVIOUS;
import static com.example.spotify.ApplicationClass.CHANNEL_ID_2;
import static com.example.spotify.PlayerActivity.listSongs;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {

    IBinder mBinder = new MyBinder();
    MediaPlayer mediaPlayer;
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    Uri uri;
    int position = -1;
    ActionPlaying actionPlaying;
    MediaSessionCompat mediaSessionCompat;
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE ="STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String SONG_NAME = "SONG_NAME";

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My audio");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
            showNotification(R.drawable.baseline_pause_24);
        }
        createMediaPlayer(position);
        mediaPlayer.start();
        onCompleted();
        //btn_nextClicked();

    }

    public class MyBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        int myCurrentPosition = intent.getIntExtra("serviceCurrentPosition", 0);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1) {
            playMedia(myPosition, myCurrentPosition);
        }
        if (actionName != null) {
            switch (actionName) {
                case "playPause":
//                    Toast.makeText(this, "PlayPause", Toast.LENGTH_SHORT).show();
                    playPauseBtnClicked();
                    break;
                case "next":
//                    Toast.makeText(this, "next", Toast.LENGTH_SHORT).show();
                    nextBtnClicked();
                    break;
                case "previous":
//                    Toast.makeText(this, "previous", Toast.LENGTH_SHORT).show();
                    prevBtnClicked();
                    break;
            }
        }
        return START_STICKY;
    }

    private void playMedia(int StartPosition, int Current) {
        musicFiles = listSongs;
        position = StartPosition;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (musicFiles != null) {
                createMediaPlayer(position);
                seekTo(Current);
                mediaPlayer.start();
            }
        }
        else
        {
            createMediaPlayer(position);
            seekTo(Current);
            mediaPlayer.start();
        }
    }

    void start() {
        mediaPlayer.start();
    }
    boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }
    void stop() {
        mediaPlayer.stop();
    }
    void release() {
        mediaPlayer.release();
    }
    int getDuration() {
        return mediaPlayer.getDuration();
    }
    void seekTo(int position) {
        mediaPlayer.seekTo(position);
    }
    int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    void createMediaPlayer(int positionInner) {
        position = positionInner;
        uri = Uri.parse(musicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE).edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.apply();
        editor.putString(ARTIST_NAME, musicFiles.get(position).getArtist());
        editor.apply();
        editor.putString(SONG_NAME, musicFiles.get(position).getTitle());
        editor.apply();
        mediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }
    void pause() {
        mediaPlayer.pause();
    }
    void onCompleted() {
        mediaPlayer.setOnCompletionListener(this);
    }
    void setCallBack(ActionPlaying actionPlaying) {
        this.actionPlaying = actionPlaying;
    }

    @SuppressLint("NotificationId0")
    void showNotification (int playPauseBtn) {
        Intent intent = new Intent(this, PlayerActivity.class);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent prevPending = PendingIntent
                .getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent pausePending = PendingIntent
                .getBroadcast(this, 0, pauseIntent, PendingIntent.FLAG_IMMUTABLE);
        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        @SuppressLint("UnspecifiedImmutableFlag")
        PendingIntent nextPending = PendingIntent
                .getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_IMMUTABLE);
        byte[] picture = null;
//        picture = getAlbumArt(musicFiles.get(position).getPath());

        Bitmap thumb = getBitmap(R.drawable.anh);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(R.drawable.anh)
                .setLargeIcon(thumb)
                .setContentTitle(musicFiles.get(position).getTitle())
                .setContentText(musicFiles.get(position).getArtist())
                .addAction(R.drawable.baseline_skip_previous_24, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.baseline_skip_next_24, "Next", nextPending)
//                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1 /* #1: pause button \*/)
                        .setMediaSession(mediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, notification);
//        startForeground(0, notification);
    }

    void playPauseBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.playPauseBtnClicked();
        }
    }
    void prevBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.prevBtnClicked();
        }
    }

    void nextBtnClicked() {
        if (actionPlaying != null) {
            actionPlaying.nextBtnClicked();
        }
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
}
