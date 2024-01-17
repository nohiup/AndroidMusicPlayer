package com.example.spotify;

import static android.app.ProgressDialog.show;
import static com.example.spotify.R.id.delete;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.MyVieHolder>/* implements Filterable */ {

    private Context mContext;
    static ArrayList<MusicFiles> albumFiles;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    AlbumDetailsAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.albumFiles = albumFiles;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_items_horizontal, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(albumFiles.get(position).getTitle());

        final long ONE_MEGABYTE = 1024 * 1024;
//        Log.e("thumbnail", albumFiles.get(position).getAlbum());
        storageReference.child("Thumbnails/" + albumFiles.get(position).getthumbnailName())
                .getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        holder.album_art.setImageBitmap(bmp);
                    }
                });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
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
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(albumFiles.get(position).getId())); //content://

        File file = new File(albumFiles.get(position).getPath());
        boolean deleted = file.delete();

        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            albumFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, albumFiles.size());
            Snackbar.make(v, "File Deleted: ", Snackbar.LENGTH_LONG)
                    .show();
        } else {
            Snackbar.make(v, "Can't delete File", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder {
        TextView file_name, author;
        ImageView album_art, menuMore;

        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.TvName);
            album_art = itemView.findViewById(R.id.IvIconView);
            author = itemView.findViewById(R.id.TvAuthor);
        }
    }

    private byte[] getAlbumArt(String uri) throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

}


