package com.example.spotify;

import static com.example.spotify.MainActivity.musicFiles;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyLibraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class MyLibraryFragment extends Fragment implements FragmentCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static ArrayList<MusicFiles> supposedFavoriteList;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RecyclerView list;
    private LeaderBoardItem adapter;
    View view;
    MainActivity main;
    public static ArrayList<MusicFiles> musicList = new ArrayList<>();;

    public MyLibraryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyLibraryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyLibraryFragment newInstance(String param1, String param2) {
        MyLibraryFragment fragment = new MyLibraryFragment();
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
        main = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_my_library, container, false);
        LinearLayoutManager manager = new LinearLayoutManager(this.getContext());
        adapter = new LeaderBoardItem(getContext(), musicList, musicFiles, true);
        list = view.findViewById(R.id.recycler_leaderboard);
        main.onMessageFromFragToMain("library", "mode");

        list.setLayoutManager(manager);
        list.setAdapter(adapter);
        
        return view;
    }

    @Override
    public void onMessageFromMainToFrag(String sender, MusicFiles musicFiles) {
        musicList.add(musicFiles);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onMusicFromMainToFrag(String sender, MusicFiles musicFiles) {

    }

    @Override
    public void onMessageFromMainToFrag(String sender, boolean isDarkMode) {
        if (list == null)
        {
            return;
        }
        LinearLayoutManager manager = new LinearLayoutManager(this.getContext());
        adapter = new LeaderBoardItem(getContext(), musicList, musicFiles, isDarkMode);
        list.setLayoutManager(manager);
        list.setAdapter(adapter);

        TextView tv = view.findViewById(R.id.leaderBoardText);

        tv.setTextColor(getResources().getColor(R.color.dark_200));

        if (isDarkMode)
        {
            tv.setTextColor(getResources().getColor(R.color.cream_200));
        }
    }
}