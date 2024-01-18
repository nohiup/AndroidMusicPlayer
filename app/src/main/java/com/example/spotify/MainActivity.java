package com.example.spotify;

import static com.example.spotify.ServiceMusic.musicService;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class ServiceMusic {
    public static MusicService musicService;
}

public class MainActivity extends AppCompatActivity implements MainCallback, NavigationView.OnNavigationItemSelectedListener, savedState{
    static public ArrayList<MusicFiles> musicFiles;
    static String currentPlaylist;

    ArrayList<MusicFiles> mainMs;
    public static ArrayList<MusicFiles> supposedFavoriteList;
    static boolean shuffleBoolean = false, repeatBoolean = false;
    static boolean isPlaying = false;
    boolean isDarkMode = true;
    BottomNavigationView bottomNavigationView;
    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    ConstraintLayout mainAct;
    FragmentTransaction ft;
    DownloadedFragment downloadedFragment;
    HomeFragment homeFragment;
    LeaderboardFragment ldFragment;
    MyLibraryFragment mlFragment;
    LinearLayout topNav;
    SearchFragment searchFragment;
    PlaylistFragment playlistFragment;
    static ArrayList<MusicFiles> albums;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    DrawerLayout drawerLayout;
    AlbumFragment albumFragment;

    NavigationView navigationView;

    FrameLayout frag_bottom_player;
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE ="STORED_MUSIC";
    public static boolean SHOW_MINI_PLAYER = false;
    public static String PATH_TO_FRAG = null;
    public static String ARTIST_TO_FRAG = null;
    public static String SONG_TO_FRAG = null;
    public static final String ARTIST_NAME = "ARTIST_NAME";
    public static final String SONG_NAME = "SONG_NAME";
    public static ImageView nextBtnMini, albumArtMini, preBtnMini;
    public static TextView artistMini, songNameMini;
    public static FloatingActionButton playPauseBtnMini;
    FrameLayout miniPlayer;
    ActionPlaying actionPlaying;
    private ArrayList<MusicFiles> musicList;
    private final ArrayList<HashMap<String, Object>> indexSortList[] = new ArrayList[1];

    static String current_fragment = "home";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar); //Ignore red line errors
            setSupportActionBar(toolbar);

            miniPlayer = findViewById(R.id.miniplayer);
            artistMini = findViewById(R.id.song_artist_miniPlayer);
            songNameMini = findViewById(R.id.song_name_miniPlayer);
            albumArtMini = findViewById(R.id.album_art_mini);
            nextBtnMini = findViewById(R.id.skip_next_bottom);
            playPauseBtnMini = findViewById(R.id.play_pause_miniPlayer);
            preBtnMini = findViewById(R.id.skip_previous_bottom);

            downloadedFragment = downloadedFragment.newInstance("ld-fragment", "test");
            albumFragment = albumFragment.newInstance("album-Fragment");
            drawerLayout = (DrawerLayout)findViewById(R.id.main_act_drawer);
            ldFragment = ldFragment.newInstance("ld-fragment", "test");
            mlFragment = mlFragment.newInstance("ml-fragment", "test-2");
            mainAct = findViewById(R.id.main_act);
            topNav = findViewById(R.id.top_nav);
            navigationView = findViewById(R.id.home_nav);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.bringToFront();
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav,
                    R.string.close_nav);
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            artistMini.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = musicService.position;
                    Intent intent = new Intent(v.getContext(), PlayerActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("currentPositionFromMain", musicService.getCurrentPosition());
                    v.getContext().startActivity(intent);
                }
            });

            songNameMini.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (songNameMini.getText().toString().equals("Chưa chọn bài hát"))
                    {
                        return;
                    }
                    int position = musicService.position;

                    Intent intent = new Intent(v.getContext(), PlayerActivity.class);
                    intent.putExtra("position", position);
                    intent.putExtra("currentPositionFromMain", musicService.getCurrentPosition());
                    v.getContext().startActivity(intent);
                }
            });

            if (musicService != null) {
                int position = musicService.position;
                artistMini.setText(musicService.musicFiles.get(position).getArtist());
                songNameMini.setText(musicService.musicFiles.get(position).getTitle());
                playPauseBtnMini.setEnabled(true);
                nextBtnMini.setEnabled(true);
                preBtnMini.setEnabled(true);
                if (musicService.isPlaying()) {
                    playPauseBtnMini.setImageResource(R.drawable.baseline_pause_24);
//                    Toast.makeText(this, "pause mini", Toast.LENGTH_SHORT).show();
                } else {
                    playPauseBtnMini.setImageResource(R.drawable.baseline_play_arrow_24);
//                    Toast.makeText(this, "play mini", Toast.LENGTH_SHORT).show();
                }

                setArt(position);

                playPauseBtnMini.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent serviceIntent = new Intent(v.getContext(), MusicService.class);
                        serviceIntent.putExtra("ActionName", "playPause");
                        v.getContext().startService(serviceIntent);
                    }
                });

                nextBtnMini.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent serviceIntent = new Intent(v.getContext(), MusicService.class);
                        serviceIntent.putExtra("ActionName", "next");
                        v.getContext().startService(serviceIntent);
                    }
                });

                preBtnMini.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent serviceIntent = new Intent(v.getContext(), MusicService.class);
                        serviceIntent.putExtra("ActionName", "previous");
                        v.getContext().startService(serviceIntent);
                    }
                });

            } else {
                artistMini.setText("Chưa chọn bài hát");
                songNameMini.setText("Chưa chọn bài hát");
                playPauseBtnMini.setEnabled(false);
                nextBtnMini.setEnabled(false);
                preBtnMini.setEnabled(false);
            }

            bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottomNavigationView);
            musicFiles = new ArrayList<>();
            albums = new ArrayList<>();
            homeFragment = homeFragment.newInstance("home-fragment", this);
            searchFragment = searchFragment.newInstance("search-fragment");
            playlistFragment = playlistFragment.newInstance("playlist-fragment");
            ArrayList<String> duplicated = new ArrayList<>();
            db = FirebaseFirestore.getInstance();


            db.collection("Music").get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            // after getting the data we are calling on success method
                            // and inside this method we are checking if the received
                            // query snapshot is empty or not.
                            if (!queryDocumentSnapshots.isEmpty()) {
                                // if the snapshot is not empty we are
                                // hiding our progress bar and adding
                                // our data in a list.
                                //                            loadingPB.setVisibility(View.GONE);
                                List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();
                                for (DocumentSnapshot d : list) {
                                    // after getting this list we are passing
                                    // that list to our object class.

                                    //                                MusicFiles c = d.toObject(MusicFiles.class);

                                    String title = d.getString("name");
                                    String artist = d.getString("singer");
                                    //                                String path = d.getString("source");
                                    String album = d.getString("album");
                                    String id = d.getString("id");
                                    String duration = "";
                                    String path = d.getString("source");
                                    String genre = d.getString("genre");
                                    String language = d.getString("language");
                                    String releaseDate = d.getString("releaseDate");
                                    String thumbnailName = d.getString("thumbnailName");

                                    MusicFiles c = new MusicFiles(path, title, artist, album, duration, id, genre, language, releaseDate, thumbnailName);
                                    //                                Log.e("Duration: ", c.getDuration());

                                    // and we will pass this object class
                                    // inside our arraylist which we have
                                    // created for recycler view.
                                    musicFiles.add(c);
                                    homeFragment.onMessageFromMainToFrag("MAIN", c);
                                    searchFragment.onMessageFromMainToFrag("MAIN", c);
                                    if (!duplicated.contains(album)){
                                        albums.add(c);
                                        duplicated.add(album);
                                        albumFragment.onMessageFromMainToFrag("MAIN", c);
                                    }
                                }
                                // after adding the data to recycler view.
                                // we are calling recycler view notifyDataSetChanged
                                // method to notify that data has been changed in recycler view.
                            } else {
                                // if the snapshot is empty we are displaying a toast message.
                                Toast.makeText(MainActivity.this, "Empty", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // if we do not get any data or any error we are displaying
                            // a toast message that we do not get any data
                            Toast.makeText(MainActivity.this, "Fail", Toast.LENGTH_SHORT).show();
                        }
                    });

            bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();

                    ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
                    NetworkInfo nInfo = cm.getActiveNetworkInfo();
                    boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

                    if (!connected)
                    {
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrameContainer, downloadedFragment);
                        ft.commit();

                        return true;
                    }

                    if (id == R.id.Home)
                    {
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrameContainer, homeFragment);
                        ft.commit();

                        current_fragment = "home";
                        return true;
                    }
                    if (id == R.id.Search)
                    {
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrameContainer, searchFragment);
                        ft.commit();

                        current_fragment = "search";
                        return true;
                    }
                    if (id == R.id.Playlist)
                    {
                        ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.mainFrameContainer, albumFragment);
                        ft.commit();

                        current_fragment = "playlist";
                        return true;
                    }

                    homeFragment.onMessageFromMainToFrag("main", isDarkMode);
                    return false;
                }
            });

            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, homeFragment);
            ft.commit();


        } catch (Exception e)
        {
            Log.d("myTag", e.toString());
            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        saveModeStateData(isDarkMode);

        SharedPreferences preferences = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE);
        String path = preferences.getString(MUSIC_FILE, null);
        String artist = preferences.getString(ARTIST_NAME, null);
        String song_name = preferences.getString(SONG_NAME, null);
        if (path != null) {
            SHOW_MINI_PLAYER = true;
            PATH_TO_FRAG = path;
            ARTIST_TO_FRAG = artist;
            SONG_TO_FRAG = song_name;

        } else {
            SHOW_MINI_PLAYER = false;
            PATH_TO_FRAG = null;
            ARTIST_TO_FRAG = null;
            SONG_TO_FRAG = null;
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        boolean connectivity = getIntent().getBooleanExtra("connectivity", true);

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

        if (!connectivity && connected)
        {
            startActivity(new Intent(this, LoginActivity.class));

            return;
        }

        if (!connectivity)
        {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, downloadedFragment);
            ft.commit();

            return;
        }

        setAvatar();
        setUsername();
        try
        {
            updateModeState();
        }
        catch (Exception e)
        {
            saveModeStateData(isDarkMode);
            updateModeState();
        }


        if (current_fragment.equals("home"))
        {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, homeFragment);
            ft.commit();

            bottomNavigationView.setSelectedItemId(R.id.Home);
            homeFragment.onMessageFromMainToFrag("main", isDarkMode);

            return;
        }
        if (current_fragment.equals("search"))
        {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, searchFragment);
            ft.commit();

            bottomNavigationView.setSelectedItemId(R.id.Search);

            return;
        }
        if (current_fragment.equals("playlist"))
        {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, albumFragment);
            ft.commit();

            bottomNavigationView.setSelectedItemId(R.id.Playlist);
            return;
        }

        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainFrameContainer, homeFragment);
        ft.commit();

        bottomNavigationView.setSelectedItemId(R.id.Home);
        current_fragment = "home";
        homeFragment.onMessageFromMainToFrag("main", isDarkMode);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onMessageFromFragToMain(String sender, String request)
    {
        if (sender.equals("HOME-FRAG") && request.equals("mode"))
        {
            homeFragment.onMessageFromMainToFrag("main", isDarkMode);
        }

        if (sender.equals("SEARCH-FRAG") && request.equals("mode"))
        {
            searchFragment.onMessageFromMainToFrag("main", isDarkMode);
        }

        if (sender.equals("leaderBoard") && request.equals("mode"))
        {
            ldFragment.onMessageFromMainToFrag("main", isDarkMode);
        }

        if (sender.equals("library") && request.equals("mode"))
        {
            mlFragment.onMessageFromMainToFrag("main", isDarkMode);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        ConnectivityManager cm = (ConnectivityManager)getApplicationContext().getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();
        boolean connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();

        if (!connected)
        {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, downloadedFragment);
            ft.commit();

            return true;
        }

        if (id == R.id.logout_home)
        {
            FirebaseAuth.getInstance().signOut();

            GoogleSignIn.getClient(
                    this,
                    new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            ).signOut();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(intent);
            return true;
        }


        if (id == R.id.profile)
        {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            intent.putExtra("activity_profile", "main");
            MainActivity.this.startActivity(intent);
            return true;
        }

        if (id == R.id.mode)
        {
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

            item.setChecked(false);

            setMode();
            return true;
        }

        if (id == R.id.downloaded)
        {
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, downloadedFragment);
            ft.commit();

            current_fragment = "downloaded";

            return true;
        }

        if (id == R.id.leaderBoard)
        {
            fetchDataFromFirestore();
            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, ldFragment);
            ft.commit();

            current_fragment = "leaderBoard";

            return true;
        }

        if (id == R.id.myLibrary)
        {

            ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFrameContainer, mlFragment);
            ft.commit();
            fetchDataForUserPlaylist();

            current_fragment = "library";

            return true;
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


    private void setMode()
    {
        findViewById(R.id.card_bottom_player).setBackgroundColor(getResources().getColor(R.color.lavender_200));
        TextView songArtist = (TextView)findViewById(R.id.song_artist_miniPlayer);
        TextView song = (TextView)findViewById(R.id.song_name_miniPlayer);

        song.setTextColor(getResources().getColor(R.color.cream_200));
        songArtist.setTextColor(getResources().getColor(R.color.cream_200));
        mainAct.setBackgroundColor(getResources().getColor(R.color.cream_200));
        bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.lavender_200));
        topNav.setBackgroundColor(getResources().getColor(R.color.lavender_200));
        bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.bottom_nav_color_light_mode));
        bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.bottom_nav_color_light_mode));
        navigationView.setBackgroundColor(getResources().getColor(R.color.cream_200));
        navigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.black_300)));
        navigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.black_300)));
        View headerView = navigationView.getHeaderView(0);

        LinearLayout navView = headerView.findViewById(R.id.nav_player_view);
        navView.setBackgroundColor(getResources().getColor(R.color.lavender_200));
        TextView navTextView = headerView.findViewById(R.id.name_holder);

        navTextView.setTextColor(getResources().getColor(R.color.cream_200));

        Menu menu = navigationView.getMenu();
        MenuItem item = menu.findItem(R.id.mode);

        item.setIcon(R.drawable.night_mode);
        item.setTitle("Night mode");

        if (current_fragment == "home")
            homeFragment.onMessageFromMainToFrag("main", isDarkMode);
        if (current_fragment.equals("search"))
            searchFragment.onMessageFromMainToFrag("main", isDarkMode);
        if (current_fragment == "library")
            mlFragment.onMessageFromMainToFrag("MAIN", isDarkMode);
        if (current_fragment == "leaderBoard")
            ldFragment.onMessageFromMainToFrag("MAIN", isDarkMode);

        if (isDarkMode)
        {
            item.setIcon(R.drawable.light_mode);
            item.setTitle("Light mode");
            song.setTextColor(getResources().getColor(R.color.cream_200));
            songArtist.setTextColor(getResources().getColor(R.color.cream_200));
            findViewById(R.id.card_bottom_player).setBackgroundColor(getResources().getColor(R.color.dark_200));
            navigationView.setBackgroundColor(getResources().getColor(R.color.dark_200));
            mainAct.setBackgroundColor(getResources().getColor(R.color.dark_gray));
            bottomNavigationView.setBackgroundColor(getResources().getColor(R.color.dark_200));
            topNav.setBackgroundColor(getResources().getColor(R.color.dark_200));
            bottomNavigationView.setItemTextColor(getResources().getColorStateList(R.color.bottom_nav_color));
            bottomNavigationView.setItemIconTintList(getResources().getColorStateList(R.color.bottom_nav_color));
            navigationView.setItemTextColor(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            navigationView.setItemIconTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
            navView.setBackgroundColor(getResources().getColor(R.color.dark_200));
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

    private void setUsername() {
        Task<DocumentSnapshot> documentSnapshotTask = FirebaseFirestore.getInstance().collection("User")
                .document(thisUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        View headerView = navigationView.getHeaderView(0);
                        TextView username = headerView.findViewById(R.id.name_holder);
                        if (task.isSuccessful()){
                            String name = task.getResult().get("name").toString();
                            Log.d("Name", name);
                            username.setText(name);
                        }
                    }
                });
    }

    private void setAvatar() {
        Task<DocumentSnapshot> doc = db.collection("User").document(thisUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        Log.d("ava", "OnComplete");
                        if (task.isSuccessful()) {
                            View headerView = navigationView.getHeaderView(0);
                            ImageView ava = headerView.findViewById(R.id.ava);
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

    public void setArt(int position) {

        final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
        storageReference.child("Thumbnails/" + musicService.musicFiles.get(position).getthumbnailName())
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        albumArtMini.setImageBitmap(bmp);
                    }
                });

    }

    private void fetchDataFromFirestore(){
        musicList = new ArrayList<>();
        indexSortList[0] = new ArrayList<>();
        FirebaseFirestore.getInstance().collection("Music").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    int pos = 0;
                    for (QueryDocumentSnapshot d: task.getResult()){
                        String title = d.getString("name");
                        String artist = d.getString("singer");
                        //                                String path = d.getString("source");
                        String album = d.getString("album");
                        String id = d.getString("id");
                        String duration = "";
                        String path = d.getString("source");
                        String genre = d.getString("genre");
                        String language = d.getString("language");
                        String releaseDate = d.getString("releaseDate");
                        String thumbnailName = d.getString("thumbnailName");

                        MusicFiles c = new MusicFiles(path, title, artist, album, duration, id, genre, language, releaseDate, thumbnailName);


                        //pos = 0 here is the position of the element in musiclist?

                        int counter = 0;
                        ArrayList<String> likeShowingList = (ArrayList<String>) d.get("likeList");
                        if (likeShowingList == null || likeShowingList.isEmpty()){
                            counter = 0;
                        }
                        else{
                            counter = likeShowingList.size();
                        }
                        final int finalCounter = counter;
                        final int finalPos = pos;
                        c.setLike(counter);
                        musicList.add(c);
                        indexSortList[0].add(new HashMap<String, Object>(){{
                            put("id", d.getString("id"));
                            put("like", finalCounter);
                            put("index", finalPos);
                        }});
                        pos++;
                    }

                    //sorting
                    ArrayList<HashMap<String, Object>> sortedList = (ArrayList<HashMap<String, Object>>) indexSortList[0].stream()
                            .sorted(Comparator.comparingInt(m -> (int)m.get("like")))
                            .collect(Collectors.toList());

                    int size = sortedList.size();
                    ArrayList<MusicFiles> finalMusicList = new ArrayList<MusicFiles>();
                    for (int i = sortedList.size()-1; i >=0; i--) {
                        finalMusicList.add(musicList.get((int) sortedList.get(i).get("index")));
                        Log.d("Error check", finalMusicList.get(size-i-1).getTitle().toString());
                    }

                    for (MusicFiles c: finalMusicList)
                    {
                        ldFragment.onMessageFromMainToFrag("MAIN", c);
                    }
                    Log.d("leaderboard", "onCreateView: " + musicList.size());
//                    adapter = new MusicAdapterHorizontal(getContext(), musicList, false);
//                    list = view.findViewById(R.id.recycler_leaderboard);
//                    list.setAdapter(adapter);
//                    adapter.notifyDataSetChanged();
                }
            }

        });
    }

    private void fetchDataForUserPlaylist() {
        supposedFavoriteList = new ArrayList<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("Albums").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            if (!task.getResult().exists()){
                                //This user haven't add any file to playlist\
                                //TODO: Add UI showing for this exception (anything)
                            }
                            else {
                                //At least not null, but maybe empty
                                ArrayList<String> musicIdList = (ArrayList<String>) task.getResult().get("songList");
                                if (musicIdList.isEmpty()) {
                                    //TODO: Show the same thing as above one
                                }
                                else {
                                    for (String id : musicIdList) {
                                        firestore.collection("Music").whereEqualTo("id", id).get().addOnCompleteListener(
                                                new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()){
                                                            for (QueryDocumentSnapshot d: task.getResult()){
                                                                String title = d.getString("name");
                                                                String artist = d.getString("singer");
                                                                //                                String path = d.getString("source");
                                                                String album = d.getString("album");
                                                                String id = d.getString("id");
                                                                String duration = "";
                                                                String path = d.getString("source");
                                                                String genre = d.getString("genre");
                                                                String language = d.getString("language");
                                                                String releaseDate = d.getString("releaseDate");
                                                                String thumbnailName = d.getString("thumbnailName");

                                                                MusicFiles c = new MusicFiles(path, title, artist, album, duration, id, genre, language, releaseDate, thumbnailName);
                                                                int counter = 0;
                                                                ArrayList<String> likeShowingList = (ArrayList<String>) d.get("likeList");
                                                                if (likeShowingList == null || likeShowingList.isEmpty()){
                                                                    counter = 0;
                                                                }
                                                                else{
                                                                    counter = likeShowingList.size();
                                                                }
                                                                c.setLike(counter);
                                                                supposedFavoriteList.add(c);
                                                                mlFragment.onMessageFromMainToFrag("MAIN", c);
                                                            }
                                                        }
                                                        //done here.
                                                    }
                                                });
                                    }
                                }
                            }

                        }
                    }
                });
    }
}
