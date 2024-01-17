package com.example.spotify;

//import static androidx.core.app.AppOpsManagerCompat.Api29Impl.getSystemService;



import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchFragment extends Fragment implements FragmentCallback{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    ArrayList<MusicFiles> musicFiles = new ArrayList<>();
    MusicAdapterHorizontal musicSearchSongAdapter;

    boolean isDarkMode;
    MainActivity main;
    RecyclerView musicSearch;
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    SearchView searchBar;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(String param1) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
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
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = (SearchView) view.findViewById(R.id.searchBar);

        LinearLayoutManager musicSearchLM = new LinearLayoutManager(this.getContext());
        musicSearchLM.setOrientation(LinearLayoutManager.VERTICAL);

        musicSearch = (RecyclerView) view.findViewById(R.id.searchMusicList);
        musicSearch.setLayoutManager(musicSearchLM);

        //musicSearch.setVisibility(view.GONE);
        musicSearchSongAdapter = new MusicAdapterHorizontal(this.getContext(), musicFiles, isDarkMode);
        musicSearch.setAdapter(musicSearchSongAdapter);

        RecyclerView.ItemDecoration itemDecoration;
        itemDecoration = new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL);
        musicSearch.addItemDecoration(itemDecoration);

        SearchManager searchManager =
                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchBar.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        searchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                musicSearchSongAdapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                musicSearchSongAdapter.getFilter().filter(newText);
                return true;
            }
        });

        main.onMessageFromFragToMain("SEARCH-FRAG", "mode");
        return view;
    }


    //   @Override
//    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        //super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.search,menu);
//
//        SearchManager searchManager =
//                (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
//        searchBar = (SearchView) menu.findItem(R.id.action_search).getActionView();
//        searchBar.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
//        searchBar.getMaxWidth();
//
//        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                musicSearchSongAdapter.getFilter().filter(query);
//                return false;
//            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                musicSearchSongAdapter.getFilter().filter(newText);
//                return false;
//            }
//        });
//    }

//@Override
//public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//    super.onCreateOptionsMenu(menu, inflater);
//    menu.clear();
//    inflater.inflate(R.menu.search, menu);
//    MenuItem item = menu.findItem(R.id.action_search);
//
//     searchBar = new SearchView(((MainActivity) this.getContext()).getSupportActionBar().getThemedContext());
//    // MenuItemCompat.setShowAsAction(item, //MenuItemCompat.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | //MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
//    //  MenuItemCompat.setActionView(item, searchView);
//    // These lines are deprecated in API 26 use instead
//    item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW | MenuItem.SHOW_AS_ACTION_IF_ROOM);
//    item.setActionView(searchBar);
//    searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//        @Override
//        public boolean onQueryTextSubmit(String query) {
//            musicSearchSongAdapter.getFilter().filter(query);
//            return false;
//        }
//        @Override
//        public boolean onQueryTextChange(String newText) {
//            musicSearchSongAdapter.getFilter().filter(newText);
//
//            return false;
//        }
//    });
//    searchBar.setOnClickListener(new View.OnClickListener() {
//                                      @Override
//                                      public void onClick(View v) {
//
//                                      }
//                                  }
//    );
//}

    public void onMessageFromMainToFrag(String sender, MusicFiles musicFiles) {
        if (sender.equals("MAIN")) {
            this.musicFiles.add(musicFiles);
        }
    }

    @Override
    public void onMusicFromMainToFrag(String sender, MusicFiles musicFiles) {

    }

    @Override
    public void onMessageFromMainToFrag(String sender, boolean isDarkMode) {
        this.isDarkMode = isDarkMode;

        musicSearchSongAdapter = new MusicAdapterHorizontal(this.getContext(), musicFiles, isDarkMode);
        musicSearch.setAdapter(musicSearchSongAdapter);
        musicSearchSongAdapter.notifyDataSetChanged();
    }
}