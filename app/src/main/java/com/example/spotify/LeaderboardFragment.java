package com.example.spotify;

import static com.example.spotify.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LeaderboardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LeaderboardFragment extends Fragment implements FragmentCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private View view;

    private RecyclerView list;
    private LeaderBoardItem adapter;
    private ArrayList<MusicFiles> musicList;
    private final ArrayList<HashMap<String, Object>> indexSortList[] = new ArrayList[1];
    public LeaderboardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LeaderboardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LeaderboardFragment newInstance(String param1, String param2) {
        LeaderboardFragment fragment = new LeaderboardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_leaderboard, container, false);

        musicList = new ArrayList<MusicFiles>();
        indexSortList[0] = new ArrayList<HashMap<String, Object>>();
        Log.d("leaderboard", "onCreateView: " + musicList.size());
        LinearLayoutManager manager = new LinearLayoutManager(this.getContext());

        adapter = new LeaderBoardItem(getContext(), musicList, musicFiles, true);
        list = view.findViewById(R.id.recycler_leaderboard);
        list.setLayoutManager(manager);
        list.setAdapter(adapter);

        return view;
    }


    @Override
    public void onMessageFromMainToFrag(String sender, MusicFiles musicFile) {
        musicList.add(musicFile);
        Log.d("Leader Board", "onMessageFromMainToFrag: " + musicFile.title + "Music files:" + musicFiles.size());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMusicFromMainToFrag(String sender, MusicFiles musicFiles) {

    }

    @Override
    public void onMessageFromMainToFrag(String sender, boolean isDarkMode) {

    }
}