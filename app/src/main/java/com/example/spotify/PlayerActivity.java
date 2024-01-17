package com.example.spotify;

import static com.example.spotify.AlbumDetailsAdapter.albumFiles;
import static com.example.spotify.ApplicationClass.ACTION_PLAY;
import static com.example.spotify.ApplicationClass.ACTION_PREVIOUS;
import static com.example.spotify.ApplicationClass.CHANNEL_ID_2;
import static com.example.spotify.MainActivity.albumArtMini;
import static com.example.spotify.MainActivity.artistMini;
import static com.example.spotify.MainActivity.musicFiles;
import static com.example.spotify.MainActivity.playPauseBtnMini;
import static com.example.spotify.MainActivity.repeatBoolean;
import static com.example.spotify.MainActivity.shuffleBoolean;
import static com.example.spotify.MainActivity.songNameMini;
import static com.example.spotify.ServiceMusic.musicService;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import androidx.appcompat.widget.Toolbar;


public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, ActionPlaying, NavigationView.OnNavigationItemSelectedListener, ServiceConnection {
    TextView song_name, artist_name, duration_played, duration_total;
    ImageView cover_art, nextBtn, prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    ScrollView lyricsScroll;
    SeekBar seekBar;
    int position = -1;
    int currentPosition = 0;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
//    MusicService musicService;
    MediaSessionCompat mediaSessionCompat;
    TextView lyrics;
    boolean isDarkMode = true;
    int scrollX = 0;
    int scrollY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();

        Toolbar toolbar = (Toolbar) findViewById(R.id.menu_btn); //Ignore red line errors
        setSupportActionBar(toolbar);

        lyrics = findViewById(R.id.lyrics);
        lyricsScroll = findViewById(R.id.lyricsScroll);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout_player);
        NavigationView navigationView = findViewById(R.id.nav_player_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

//        mediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My audio");
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (musicService != null && fromUser)
                {
                    musicService.seekTo(progress * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (musicService != null) {
                    int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    duration_played.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shuffleBoolean)
                {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.baseline_shuffle_off);
                }
                else
                {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.baseline_shuffle_on_24);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeatBoolean)
                {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.baseline_repeat_off);
                }
                else
                {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.baseline_repeat_on_24);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.putExtra("activity", "player");
                intent.putExtra("position", position);
                v.getContext().startActivity(intent);
            }
        });
    }
    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        prevThreadBtn();
        nextThreadBtn();
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();
    }
    public void prevBtnClicked() {
        if (musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
                }
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            songNameMini.setText(listSongs.get(position).getTitle());
            artistMini.setText(listSongs.get(position).getArtist());
            final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
            storageReference.child("Thumbnails/" + listSongs.get(position).getthumbnailName())
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            albumArtMini.setImageBitmap(bmp);
                        }
                    });
            seekBar.setMax(musicService.getDuration() / 1000);
            setSongLyric(listSongs.get(position).title);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.baseline_pause_24);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_pause_24);
            musicService.start();
        }
        else
        {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position - 1));
                }
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            songNameMini.setText(listSongs.get(position).getTitle());
            artistMini.setText(listSongs.get(position).getArtist());
            final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
            storageReference.child("Thumbnails/" + listSongs.get(position).getthumbnailName())
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            albumArtMini.setImageBitmap(bmp);
                        }
                    });
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.baseline_play_arrow_24);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24);
            //mediaPlayer.start();
        }
    }
    public void nextBtnClicked() {
        if (musicService.isPlaying())
        {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position + 1) % listSongs.size());
                }
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            songNameMini.setText(listSongs.get(position).getTitle());
            artistMini.setText(listSongs.get(position).getArtist());
            final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
            storageReference.child("Thumbnails/" + listSongs.get(position).getthumbnailName())
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            albumArtMini.setImageBitmap(bmp);
                        }
                    });
            seekBar.setMax(musicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.baseline_pause_24);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_pause_24);
            musicService.start();
        }
        else
        {
            musicService.stop();
            musicService.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size() - 1);
            }
            else
            {
                if (!shuffleBoolean && !repeatBoolean)
                {
                    position = ((position + 1) % listSongs.size());
                }
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            musicService.createMediaPlayer(position);
            metaData(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtist());
            songNameMini.setText(listSongs.get(position).getTitle());
            artistMini.setText(listSongs.get(position).getArtist());
            final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
            storageReference.child("Thumbnails/" + listSongs.get(position).getthumbnailName())
                    .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            albumArtMini.setImageBitmap(bmp);
                        }
                    });
            seekBar.setMax(musicService.getDuration() / 1000);

            setSongLyric(listSongs.get(position).title);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            musicService.onCompleted();
            musicService.showNotification(R.drawable.baseline_play_arrow_24);
            playPauseBtn.setBackgroundResource(R.drawable.baseline_play_arrow_24);
            //mediaPlayer.stop();
        }
    }

    private int getRandom(int i) {
        Random random = new Random();
        return random.nextInt(i + 1);
    }
    private void nextThreadBtn() {
        nextThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }
    private void playThreadBtn() {
        playThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playPauseBtnClicked();
                    }
                });
            }
        };
        playThread.start();
    }
    public void playPauseBtnClicked() {
        if (musicService.isPlaying()) {
            playPauseBtn.setImageResource(R.drawable.baseline_play_arrow_24);
            musicService.showNotification(R.drawable.baseline_play_arrow_24);
            playPauseBtnMini.setImageResource(R.drawable.baseline_play_arrow_24);
            musicService.pause();
            seekBar.setMax(musicService.getDuration() / 1000);
            setSongLyric(listSongs.get(position).title);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
        else
        {
            playPauseBtn.setImageResource(R.drawable.baseline_pause_24);
            musicService.showNotification(R.drawable.baseline_pause_24);
            playPauseBtnMini.setImageResource(R.drawable.baseline_pause_24);
            musicService.start();
            seekBar.setMax(musicService.getDuration() / 1000);
            setSongLyric(listSongs.get(position).title);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (musicService != null) {
                        int mCurrentPosition = musicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }
    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":"+"0"+seconds;
        if (seconds.length() == 1)
        {
            return totalNew;
        }
        return totalout;
    }
    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);

        currentPosition = getIntent().getIntExtra("currentPositionFromMain", 0);
        listSongs = musicFiles;

        String sender = getIntent().getStringExtra("sender");
        if (sender!=null && sender.equals("albumDetails"))
        {
            listSongs = albumFiles;
        } else{
            listSongs = musicFiles  ;
        }

        Log.e("Size: ", String.valueOf(musicFiles.size()));
        if (listSongs != null)
        {
            playPauseBtn.setImageResource(R.drawable.baseline_pause_24);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("servicePosition", position);
        intent.putExtra("serviceCurrentPosition", currentPosition);
        startService(intent);

    }
    private void initViews() {
        song_name = findViewById(R.id.song_name);
        artist_name = findViewById(R.id.song_artist);
        duration_played = findViewById(R.id.durationPlayed);
        duration_total = findViewById(R.id.durationTotal);
        cover_art = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
    }
    private  void metaData(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art = null;
        Bitmap bmp = null;
        setSongLyric(listSongs.get(position).title);

        final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
        storageReference.child("Thumbnails/" + listSongs.get(position).getthumbnailName())
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        ImageAnimation(PlayerActivity.this, cover_art, bitmap);
                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(@Nullable Palette palette) {
                                Palette.Swatch swatch = palette.getDominantSwatch();
                                if (swatch != null)
                                {
                                    ImageView gradient = findViewById(R.id.imageViewGradient);
                                    RelativeLayout mContainer = findViewById(R.id.mContainer);
                                    gradient.setBackgroundResource(R.drawable.gradient_bg);
                                    mContainer.setBackgroundResource(R.drawable.main_bg);
                                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{swatch.getRgb(), 0x00000000});
                                    gradient.setBackground(gradientDrawable);
                                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{swatch.getRgb(), swatch.getRgb()});
                                    mContainer.setBackground(gradientDrawableBg);
                                    song_name.setTextColor(swatch.getTitleTextColor());
                                    artist_name.setTextColor(swatch.getBodyTextColor());
                                }
                                else
                                {
                                    ImageView gradient = findViewById(R.id.imageViewGradient);
                                    RelativeLayout mContainer = findViewById(R.id.mContainer);
                                    gradient.setBackgroundResource(R.drawable.gradient_bg);
                                    mContainer.setBackgroundResource(R.drawable.main_bg);
                                    GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{0xff000000, 0x00000000});
                                    gradient.setBackground(gradientDrawable);
                                    GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                            new int[]{0xff000000, 0xff000000});
                                    mContainer.setBackground(gradientDrawableBg);
                                    song_name.setTextColor(Color.WHITE);
                                    artist_name.setTextColor(Color.DKGRAY);
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Glide.with(PlayerActivity.this)
                                .asBitmap()
                                .load(R.drawable.anh)
                                .into(cover_art);
                        song_name.setTextColor(Color.WHITE);
                        artist_name.setTextColor(Color.DKGRAY);
                    }
                });
    }
    public void ImageAnimation(Context context, ImageView imageView, Bitmap bitmap) {
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        nextBtnClicked();
        if (musicService != null)
        {
            musicService.createMediaPlayer(position);
            musicService.start();
            musicService.onCompleted();
            setSongLyric(listSongs.get(position).title);
        }

    }
    //all method support showing notification and progress bar
    final private String CHANNEL_ID = "1";
    private void downloadProcessing(StorageReference reference, File file){
        notificationProgressBarInit();
        reference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                //success
                Log.d("SuccessNotif", file.toString());
                notificationFinishInit();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Download Error", e.toString());
            }
        });
    }


    private void notificationProgressBarInit() {
        createNotificationChannel();
        int id = 1;
        Log.w("Method check", "reach here");
        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        mBuilder.setChannelId(CHANNEL_ID);
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setContentTitle("My app") ;
        mBuilder.setContentText("Download in progress");
        mBuilder.setSmallIcon(R.drawable.ic_launcher_foreground);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setAutoCancel(true);
        mBuilder.setProgress(0, 100, true);

        // Issues the notification
        mNotifyManager.notify(id, mBuilder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
    private void notificationFinishInit(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);

        builder.setContentTitle("My app").setContentText("Finished");
        builder.setSmallIcon(R.drawable.ic_launcher_background);
        notificationManager.notify(1, builder.build());
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        MusicFiles playing  = listSongs.get(position);
        String songID = listSongs.get(position).getId();

        if (id == R.id.download){
            StorageReference fileRef =  FirebaseStorage.getInstance().getReferenceFromUrl(playing.getPath());
            File localFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), songID + ".mp3");
            downloadProcessing(fileRef, localFile);
        }

        if (id == R.id.like){
            FirebaseFirestore.getInstance().collection("Music").whereEqualTo("id", songID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        //Got the document
                        for (QueryDocumentSnapshot doc: task.getResult()){
                            ArrayList<String> userList = (ArrayList<String>) doc.get("likeList");
                            if (userList == null || userList.isEmpty()){
                                userList = new ArrayList<String>();
                            }
                            if (userList.indexOf(FirebaseAuth.getInstance().getCurrentUser().getUid())==-1)
                            {
                                userList.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            }
                            else {
                                userList.remove(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            }
                            FirebaseFirestore.getInstance().collection("Music").document(doc.getId()).update("likeList", userList);
                        }
                    }
                    else {
                        Log.e("Error", task.getException().toString());
                    }
                }
            });
        }
        if (id == R.id.nav_logout)
        {
            FirebaseAuth.getInstance().signOut();

            GoogleSignIn.getClient(
                    this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut();

            Intent intent = new Intent(PlayerActivity.this, LoginActivity.class);
            PlayerActivity.this.startActivity(intent);
            musicService.stop();
            return true;
        }


        if (id == R.id.profile)
        {
            Intent intent = new Intent(PlayerActivity.this, ProfileActivity.class);
            intent.putExtra("activity_profile", "player");
            PlayerActivity.this.startActivity(intent);
            return true;
        }

        if (id == R.id.mode)
        {
            autoSlide();
            isDarkMode = !isDarkMode;
            item.setIcon(R.drawable.light_mode);
            if (isDarkMode) {
                item.setIcon(R.drawable.night_mode);
            }

            item.setTitle("Light mode");
            if (isDarkMode)
            {
                item.setTitle("Night mode");
            }

            return true;
        }

        if  (id == R.id.addToPlaylist) {
            final String currentId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final CollectionReference albumRef = FirebaseFirestore.getInstance().collection("Albums");
            final ArrayList<String>[] docList = new ArrayList[1];

            FirebaseFirestore.getInstance().collection("Albums").document(currentId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Log.d("Error", "Reach");
                        //Document exists
                        if(task.getResult().exists()){
                            //Bai hat dang o tren man hinh
                            //List of all recent song on the firebase
                            docList[0] = (ArrayList<String>) task.getResult().get("songList");
                            docList[0].add(listSongs.get(position).getId());
                            Log.d("Position showing id",listSongs.get(position).getId());
                            albumRef.document(currentId).update("songList", docList[0]);
                        }
                        //Not exists, create a new one
                        else {
                            docList[0] = new ArrayList<String>();
                            docList[0].add(listSongs.get(position).getId());
                            Log.d("Position new", listSongs.get(position).getId());
                            albumRef.document(currentId).set(new HashMap<String, Object>(){{
                                put("id", currentId);
                                put("songList", docList[0]);
                            }});
                        }
                    }
                    else {
                        Log.e("Error", task.getException().toString());
                    }
                }
            });
        }
        return false;
    }
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public void autoSlide(){

        scrollX = lyricsScroll.getScrollX();

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            int count = 0;
            @Override
            public void run() {
                scrollY = lyricsScroll.getScrollY() + 1;

                if (scrollY > lyricsScroll.getBottom())
                {
                    lyricsScroll.scrollTo(scrollX, lyricsScroll.getBottom());
                    return;
                }

                lyricsScroll.scrollTo(scrollX, scrollY);
                handler.postDelayed(this, 100);
                }
        };
        handler.postDelayed(runnable, 100);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) service;
        musicService = myBinder.getService();
        musicService.setCallBack(this);
//        Toast.makeText(this, "Connected" + musicService, Toast.LENGTH_SHORT).show();
        seekBar.setMax(musicService.getDuration() / 1000);
        metaData(uri);
        song_name.setText(listSongs.get(position).getTitle());
        artist_name.setText(listSongs.get(position).getArtist());
        musicService.onCompleted();
        musicService.showNotification(R.drawable.baseline_pause_24);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        musicService = null;
    }

    private void setSongLyric(String songName) {
        //Note: Tên bài hát là tên của file txt nên hãy lấy songName của nó đi :)
        Log.d("Song", "setSongLyric: " + songName);
        StorageReference ref = FirebaseStorage.getInstance().getReference();
        try {
            File localFile = File.createTempFile("temp", "txt");
            ref.child("Lyric").child(songName+".txt").getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        Log.d("result",task.getResult().toString());
                        try {
                            String data = "";
                            FileInputStream fileInputStream = new FileInputStream(localFile);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream));
                            String line = "";
                            while ((line = reader.readLine())!= null) {
                                data+= line + "\n";
                                lyrics.setText(data);
                            }
                            reader.close();
                            Log.d("String res", data.toString());

                            // Biến data chứa dữ liệu lyric của bài hát, có thể setText() tại đây
                        } catch (FileNotFoundException e) {
                            throw new RuntimeException(e);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    }
                }
            });
        } catch (IOException e) {
            Log.d("result", "setSongLyric: failed");
            throw new RuntimeException(e);
        }
    }
}