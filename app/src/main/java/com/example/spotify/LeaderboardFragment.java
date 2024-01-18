package com.example.spotify;

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
public class LeaderboardFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private RecyclerView list;
    private MusicAdapterHorizontal adapter;
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
    private void fetchDataFromFirestore(){
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

                        musicList.add(c);
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

                    musicList.clear();
                    musicList.addAll(finalMusicList);
                    Log.d("leaderboard", "onCreateView: " + musicList.size());
//                    adapter = new MusicAdapterHorizontal(getContext(), musicList, false);
//                    list = view.findViewById(R.id.recycler_leaderboard);
//                    list.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        musicList = new ArrayList<MusicFiles>();
        indexSortList[0] = new ArrayList<HashMap<String, Object>>();
        fetchDataFromFirestore();
        Log.d("leaderboard", "onCreateView: " + musicList.size());

        adapter = new MusicAdapterHorizontal(getContext(), musicList, false);
        list = (RecyclerView) view.findViewById(R.id.recycler_leaderboard);
        list.setAdapter(adapter);
        return view;
    }


}