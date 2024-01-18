package com.example.spotify;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class PlayBarFragment extends Fragment implements FragmentCallback{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    ImageView img;
    TextView author, songName;
    Button startBtn;
    boolean isPause = false;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    public PlayBarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment PlayBarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayBarFragment newInstance(String param1) {
        PlayBarFragment fragment = new PlayBarFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_bar, container, false);

        img = (ImageView) view.findViewById(R.id.IvIconPlayBar);
        songName = (TextView) view.findViewById(R.id.songInfoPlayBar);

        startBtn = (Button) view.findViewById(R.id.btnPause);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPause) {
                    startBtn.setBackground(getResources().getDrawable(R.drawable.pause_btn));
                }
                else
                    startBtn.setBackground(getResources().getDrawable(R.drawable.play_btn));
                isPause = !isPause;
            }
        });

        return view;
    }

    @Override
    public void onMessageFromMainToFrag(String sender, MusicFiles musicFiles) {
        songName.setText(musicFiles.getTitle());

        final long ONE_MEGABYTE = 1024*1024;
        storageReference.child("Thumbnails/" + musicFiles.getAlbum())
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        img.setImageBitmap(bmp);
                    }
                });
    }

    @Override
    public void onMusicFromMainToFrag(String sender, MusicFiles musicFiles) {

    }

    @Override
    public void onMessageFromMainToFrag(String sender, boolean isDarkMode) {

    }
}