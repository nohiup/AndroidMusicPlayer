package com.example.spotify;

import static com.example.spotify.MainActivity.musicFiles;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DownloadedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DownloadedFragment extends Fragment {

    private static ArrayList<String> nameList = new ArrayList<>();
    private static ArrayList<String> authorList = new ArrayList<>();
    private static ArrayList<String> fileNames = new ArrayList<>();


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    downloadedAdapter adapter;

    RecyclerView list;

    public DownloadedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DownloadedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DownloadedFragment newInstance(String param1, String param2) {
        DownloadedFragment fragment = new DownloadedFragment();
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
        View view = inflater.inflate(R.layout.fragment_downloaded, container, false);

        fetchAllMusicDownloaded();

        LinearLayoutManager manager = new LinearLayoutManager(this.getContext());

        adapter = new downloadedAdapter(getContext(), nameList, authorList, fileNames, true);
        list = view.findViewById(R.id.recycler_leaderboard);
        list.setLayoutManager(manager);
        list.setAdapter(adapter);

        return view;
    }

    private void fetchAllMusicDownloaded(){
        File musicFolder = new File(this.getContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC).toURI());
        if (musicFolder.exists() && musicFolder.isDirectory()){
            File[] files = musicFolder.listFiles();
            if (files != null && files.length > 0) {
                for (File file: files) {
                    String fileName = file.getName();
                    parseDownloadedFile(fileName);
                    fileNames.add(file.getAbsolutePath());
                }
            }
            else Log.e("Folder exists", "No file");
        }
        else Log.e("Folder exists", "No folder");
    }
    private void parseDownloadedFile(String fileName){
        Log.d("fileName", fileName);
        String nameWithoutExtension = fileName.split("\\.")[0];
        if (!nameWithoutExtension.contains("-")) return;
        String songName = nameWithoutExtension.split("-")[0];
        String songAuthor = nameWithoutExtension.split("-")[1];
        nameList.add(songName);
        authorList.add(songAuthor);

        //Nên là mỗi lần gọi for i thì sẽ đồng bộ được data.
        //Làm 1 cái Item layout đơn giản chỉ có tên với tác giả nhá, t không fetch thêm data được bị dài tên.

    }

    //Cách phát bài hát đã download lúc offline
    //Truyền được absolutePath thì dùng path, không thì filename. Filename là có cả extension.
    private void playAnExistMusicFile(String filename){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(new File(this.getContext().getExternalFilesDir(Environment.DIRECTORY_MUSIC), filename).getAbsolutePath());
            mediaPlayer.prepare(); //prepare là chạy full source rồi (kiểu chuẩn bị source)
            mediaPlayer.start(); //là bắt đầu. Có thể sẽ khác của Toàn nên chắc phải xử lý phần seeking lại, nhưng trước mắt
            // phát được đã :>>
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}