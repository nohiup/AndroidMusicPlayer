package com.example.spotify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements FragmentCallback{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    MainActivity main;
    boolean isDarkMode;
    ArrayList<MusicFiles> popMusic = new ArrayList<>();

    ArrayList<MusicFiles> vietnam = new ArrayList<>();

    ArrayList<MusicFiles> international = new ArrayList<>();

    ArrayList<MusicFiles> ballad = new ArrayList<>();
    ArrayList<MusicFiles> youngGen = new ArrayList<>();
    ArrayList<MusicFiles> musicFile = new ArrayList<>();
    MusicAdapter internationalSongAdapter, popSongAdapter,
            vietnameseSongAdapter, balladSongAdapter, youngGenAdapter;
    RecyclerView internationalView, pop,
            vietnameseSong, balladSong, youngGenView;

    TextView home_category_1, home_category_2, home_category_3, home_category_4, home_category_5;
    ImageView album_art, menuMore;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private static Context mContext;
    public HomeFragment() {
        // Required empty public constructor
    }
    public static HomeFragment newInstance(String param1, Context mcontext) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        mContext = mcontext;

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
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        LinearLayoutManager popLM = new LinearLayoutManager(this.getContext());
        LinearLayoutManager internationalViewLM = new LinearLayoutManager(this.getContext());
        LinearLayoutManager vietnameseSongLM = new LinearLayoutManager(this.getContext());
        LinearLayoutManager balladSongLM = new LinearLayoutManager(this.getContext());
        LinearLayoutManager youngGenSongLM = new LinearLayoutManager(this.getContext());

        popLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        internationalViewLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        vietnameseSongLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        balladSongLM.setOrientation(LinearLayoutManager.HORIZONTAL);
        youngGenSongLM.setOrientation(LinearLayoutManager.HORIZONTAL);

        home_category_1 = view.findViewById(R.id.home_category_1);
        home_category_2 = view.findViewById(R.id.home_category_2);
        home_category_3 = view.findViewById(R.id.home_category_3);
        home_category_4 = view.findViewById(R.id.home_category_4);
        home_category_5 = view.findViewById(R.id.home_category_5);

        vietnameseSong = (RecyclerView) view.findViewById(R.id.vietnameseSongList);
        internationalView = (RecyclerView) view.findViewById(R.id.internationalView);
        pop = (RecyclerView) view.findViewById(R.id.pop);
        balladSong = (RecyclerView) view.findViewById(R.id.ballad);
        youngGenView = (RecyclerView) view.findViewById(R.id.youngPeople);

        main.onMessageFromFragToMain("HOME-FRAG", "mode");

        vietnameseSongAdapter = new MusicAdapter(this.getContext(), musicFile, vietnam, isDarkMode);
        internationalSongAdapter = new MusicAdapter(this.getContext(), musicFile, international, isDarkMode);
        popSongAdapter = new MusicAdapter(this.getContext(), musicFile, popMusic, isDarkMode);
        balladSongAdapter = new MusicAdapter(this.getContext(), musicFile, ballad, isDarkMode);
        youngGenAdapter = new MusicAdapter(this.getContext(), musicFile, youngGen, isDarkMode);

        pop.setLayoutManager(popLM);
        internationalView.setLayoutManager(internationalViewLM);
        vietnameseSong.setLayoutManager(vietnameseSongLM);
        balladSong.setLayoutManager(balladSongLM);
        youngGenView.setLayoutManager(youngGenSongLM);


        pop.setAdapter(popSongAdapter);
        internationalView.setAdapter(internationalSongAdapter);
        vietnameseSong.setAdapter(vietnameseSongAdapter);
        balladSong.setAdapter(balladSongAdapter);
        youngGenView.setAdapter(youngGenAdapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("mode", isDarkMode);
        super.onSaveInstanceState(outState);


    }

    @Override
    public void onPause()
    {
        super.onPause();
        onSaveInstanceState(new Bundle());
    }


    @Override
    public void onMessageFromMainToFrag(String sender, MusicFiles musicFiles) {
        if (sender.equals("MAIN")) {
            musicFile.add(musicFiles);

            if (musicFiles.language.equals("Việt Nam"))
            {
                vietnam.add(musicFiles);
            }

            if (musicFiles.genre.contains("Pop") || musicFiles.genre.contains("pop"))
            {
                popMusic.add(musicFiles);
            }

            if (!musicFiles.language.equals("Việt Nam"))
            {
                international.add(musicFiles);
            }

            if (musicFiles.genre.contains("Ballad") || musicFiles.genre.contains("ballad"))
            {
                ballad.add(musicFiles);
            }

            if (musicFiles.genre.contains("nhạc trẻ"))
            {
                youngGen.add(musicFiles);
            }

            popSongAdapter.notifyDataSetChanged();
            vietnameseSongAdapter.notifyDataSetChanged();
            internationalSongAdapter.notifyDataSetChanged();
            balladSongAdapter.notifyDataSetChanged();
            youngGenAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onMusicFromMainToFrag(String sender, MusicFiles musicFiles) {

    }

    @Override
    public void onMessageFromMainToFrag(String sender, boolean isDarkMode) {
        this.isDarkMode = isDarkMode;

        popSongAdapter = new MusicAdapter(this.getContext(), musicFile,popMusic, isDarkMode);
        vietnameseSongAdapter = new MusicAdapter(this.getContext(), musicFile, vietnam, isDarkMode);
        internationalSongAdapter = new MusicAdapter(this.getContext(), musicFile, international, isDarkMode);
        balladSongAdapter = new MusicAdapter(this.getContext(), musicFile, ballad, isDarkMode);
        youngGenAdapter = new MusicAdapter(this.getContext(), musicFile, youngGen, isDarkMode);

        pop.setAdapter(popSongAdapter);
        vietnameseSong.setAdapter(vietnameseSongAdapter);
        internationalView.setAdapter(internationalSongAdapter);
        balladSong.setAdapter(balladSongAdapter);
        youngGenView.setAdapter(youngGenAdapter);

        home_category_1.setTextColor(getResources().getColor(R.color.black_300));
        home_category_2.setTextColor(getResources().getColor(R.color.black_300));
        home_category_3.setTextColor(getResources().getColor(R.color.black_300));
        home_category_4.setTextColor(getResources().getColor(R.color.black_300));
        home_category_5.setTextColor(getResources().getColor(R.color.black_300));

        if (isDarkMode) {
            home_category_1.setTextColor(getResources().getColor(R.color.white));
            home_category_2.setTextColor(getResources().getColor(R.color.white));
            home_category_3.setTextColor(getResources().getColor(R.color.white));
            home_category_4.setTextColor(getResources().getColor(R.color.white));
            home_category_5.setTextColor(getResources().getColor(R.color.white));
        }
    }
}