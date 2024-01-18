package com.example.spotify;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class downloadedAdapter extends RecyclerView.Adapter<downloadedAdapter.MyVieHolder>/* implements Filterable */{

    private Context mContext;

    private ArrayList<String> mSong;
    private ArrayList<String> mName;

    private ArrayList<String> mFile;


    TextView file_name, author;
    ImageView album_art, menuMore;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    boolean isDarkMode;

    downloadedAdapter(Context mContext, ArrayList<String> song, ArrayList<String> author, ArrayList<String> filename, boolean mode) {
        this.mSong = song;
        this.mName = author;
        this.mFile = filename;
        this.mContext = mContext;
        this.isDarkMode = mode;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isDarkMode)
            view = LayoutInflater.from(mContext).inflate(R.layout.downloaded_item, parent, false);
        else
            view = LayoutInflater.from(mContext).inflate(R.layout.downloaded_item, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(mSong.get(position));
        holder.author.setText(mName.get(position));


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] data = new String[2];
                data[0] = holder.file_name.getText().toString();
                data[1] = holder.author.getText().toString();

                Intent i = new Intent(mContext, DownloadedPlayActivity.class);
                i.putExtra("data", data);
                mContext.startActivity(i);
            }
        });
//        holder.menuMore.setOnClickListener(new View.OnClickListener() {
//            @SuppressLint("NonConstantResourceId")
//            @Override
//            public void onClick(View v) {
//                PopupMenu popupMenu = new PopupMenu(mContext, v);
//                popupMenu.getMenuInflater().inflate(R.menu.popup, popupMenu.getMenu());
//                popupMenu.show();
//
//            }
//        });
    }

    private void deleteFile(int position, View v) {
    }

    @Override
    public int getItemCount() {
        return mSong.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder {
        TextView file_name, author;
        ImageView album_art;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.TvName);
            album_art = itemView.findViewById(R.id.IvIconView);
            author = itemView.findViewById(R.id.TvAuthor);
        }

        public void setMode(boolean isDarkMode)
        {
            file_name.setTextColor(mContext.getColor(R.color.dark_200));
            author.setTextColor(mContext.getColor(R.color.dark_200));
            if (isDarkMode)
            {
                file_name.setTextColor(mContext.getColor(R.color.cream));
                author.setTextColor(mContext.getColor(R.color.cream));
            }
        }
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    public static String convert(String str) {
        str = str.replaceAll("à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ", "a");
        str = str.replaceAll("è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ", "e");
        str = str.replaceAll("ì|í|ị|ỉ|ĩ", "i");
        str = str.replaceAll("ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ", "o");
        str = str.replaceAll("ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ", "u");
        str = str.replaceAll("ỳ|ý|ỵ|ỷ|ỹ", "y");
        str = str.replaceAll("đ", "d");

        str = str.replaceAll("À|Á|Ạ|Ả|Ã|Â|Ầ|Ấ|Ậ|Ẩ|Ẫ|Ă|Ằ|Ắ|Ặ|Ẳ|Ẵ", "A");
        str = str.replaceAll("È|É|Ẹ|Ẻ|Ẽ|Ê|Ề|Ế|Ệ|Ể|Ễ", "E");
        str = str.replaceAll("Ì|Í|Ị|Ỉ|Ĩ", "I");
        str = str.replaceAll("Ò|Ó|Ọ|Ỏ|Õ|Ô|Ồ|Ố|Ộ|Ổ|Ỗ|Ơ|Ờ|Ớ|Ợ|Ở|Ỡ", "O");
        str = str.replaceAll("Ù|Ú|Ụ|Ủ|Ũ|Ư|Ừ|Ứ|Ự|Ử|Ữ", "U");
        str = str.replaceAll("Ỳ|Ý|Ỵ|Ỷ|Ỹ", "Y");
        str = str.replaceAll("Đ", "D");
        return str;
    }
}


