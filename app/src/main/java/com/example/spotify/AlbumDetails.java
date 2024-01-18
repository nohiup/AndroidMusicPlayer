package com.example.spotify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import static com.example.spotify.MainActivity.musicFiles;
import static com.example.spotify.MainActivity.shuffleBoolean;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class AlbumDetails extends AppCompatActivity implements savedState {

    RecyclerView recyclerView;
    ImageView albumPhoto;
    String albumName;

    boolean isDarkMode = true;

    AlbumDetailsAdapter albumDetailsAdapter;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();
    ArrayList<MusicFiles> albumSongs = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        recyclerView = findViewById(R.id.recylerALbumView);
        albumPhoto = findViewById(R.id.albumPhoto);
        albumName = getIntent().getStringExtra("albumName");

        albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
        recyclerView.setAdapter(albumDetailsAdapter);

        //Firestore album for all
//        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//        firestore.collection("Albums").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if(task.isSuccessful()){
//                    albumSongs.clear();
//                    //Of course only 1 doc for favorite
//                    for (QueryDocumentSnapshot doc: task.getResult()){
//                        ArrayList<MusicFiles> showing = (ArrayList<MusicFiles>) doc.get("songList");
//                        albumSongs.addAll(showing);
//                    }
//                    //Fetch_ed albumSongs.
//                    albumDetailsAdapter.notifyDataSetChanged();
//                }
//            }
//        });
        //Dèaul
        int j = 0;
        for(int i = 0; i < musicFiles.size();i++){
            if(albumName.equals(musicFiles.get(i).getAlbum())){
                albumSongs.add(j, musicFiles.get(i));
                j++;
            }
        }
//        try {
//            byte[] image = getAlbumArt(albumSongs.get(0).getPath());
//            if(image != null){
//                Glide.with(this).load(image)
//                        .into(albumPhoto);
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveModeStateData(isDarkMode);
    }

    @Override
    protected void onStart() {
        super.onStart();
        updateModeState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!(albumSongs.size()<1)){
            albumDetailsAdapter = new AlbumDetailsAdapter(this, albumSongs);
            recyclerView.setAdapter(albumDetailsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        }
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
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
        findViewById(R.id.albumPhoto).setBackgroundColor(getResources().getColor(R.color.lavender_200));

        if (isDarkMode)
        {
            findViewById(R.id.albumPhoto).setBackgroundColor(getResources().getColor(R.color.lavender_200));
        }
    }
}