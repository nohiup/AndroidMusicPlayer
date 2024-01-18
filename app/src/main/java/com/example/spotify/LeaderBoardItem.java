package com.example.spotify;

import static com.example.spotify.MainActivity.musicFiles;
import static com.example.spotify.ServiceMusic.*;

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

public class LeaderBoardItem extends RecyclerView.Adapter<LeaderBoardItem.MyVieHolder>/* implements Filterable */{

    private Context mContext;
    private ArrayList<MusicFiles> mFiles;
    private ArrayList<MusicFiles> mFilesNew;

    TextView file_name, author;
    ImageView album_art, menuMore;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    boolean isDarkMode;

    LeaderBoardItem(Context mContext, ArrayList<MusicFiles> mFiles, ArrayList<MusicFiles> mList, boolean mode) {
        this.mFiles = mFiles;
        this.mFilesNew = musicFiles;
        this.mContext = mContext;
        this.isDarkMode = mode;
    }

    @NonNull
    @Override
    public MyVieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (isDarkMode)
            view = LayoutInflater.from(mContext).inflate(R.layout.leader_board_item, parent, false);
        else
            view = LayoutInflater.from(mContext).inflate(R.layout.leader_board_item_light_mode, parent, false);
        return new MyVieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyVieHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        holder.author.setText(mFiles.get(position).getArtist());
        holder.ranking.setText( "" + (position + 1));
        holder.like.setText("Likes: " + mFiles.get(position).like);

        final long ONE_MEGABYTE = 1024*1024;
//        Log.e("thumbnail", mFiles.get(position).getAlbum());
        storageReference.child("Thumbnails/" + mFiles.get(position).getthumbnailName())
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
                int position = 0;
                position = mFilesNew.indexOf(mFiles.get(holder.getAdapterPosition()));
                Log.d("pos", "mFiles: " + mFiles.get(holder.getAdapterPosition()).title);
                Log.d("pos", "size: " + mFilesNew.size());

                for(int i = 0; i < mFilesNew.size(); i++)
                {
                    Log.d("pos", "c: " + mFilesNew.get(i).title);
                    Log.d("pos", "current: " + mFiles.get(holder.getAdapterPosition()).title);
                    if (mFilesNew.get(i).title.equals(mFiles.get(holder.getAdapterPosition()).title))
                    {
                        Log.d("pos", "yes ");
                        position = i;

                        break;
                    }
                }

                Log.d("pos", "mFilesNew: " + position);
                if (position < 0)
                {
                    position = 0;
                }
                Intent intent = new Intent(mContext, PlayerActivity.class);
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
                Long.parseLong(mFiles.get(position).getId())); //content://

        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();

        if (deleted) {
            mContext.getContentResolver().delete(contentUri, null, null);
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(v, "File Deleted: ", Snackbar.LENGTH_LONG).show();
        }
        else
        {
            Snackbar.make(v, "Can't delete File", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class MyVieHolder extends RecyclerView.ViewHolder {
        TextView file_name, author, ranking, like;
        ImageView album_art, menuMore;
        public MyVieHolder(@NonNull View itemView) {
            super(itemView);
            file_name = itemView.findViewById(R.id.TvName);
            album_art = itemView.findViewById(R.id.IvIconView);
            author = itemView.findViewById(R.id.TvAuthor);
            ranking = itemView.findViewById(R.id.ranking);
            like = itemView.findViewById(R.id.like);
        }

        public void setMode(boolean isDarkMode)
        {
            file_name.setTextColor(mContext.getColor(R.color.dark_200));
            author.setTextColor(mContext.getColor(R.color.dark_200));
            ranking.setTextColor(mContext.getColor(R.color.dark_200));
            if (isDarkMode)
            {
                file_name.setTextColor(mContext.getColor(R.color.cream));
                author.setTextColor(mContext.getColor(R.color.cream));
                ranking.setTextColor(mContext.getColor(R.color.cream));
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

    public android.widget.Filter getFilter() {
        // TODO Auto-generated method stub
        return new android.widget.Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // TODO Auto-generated method stub
                String strSearch = constraint.toString();
                if(strSearch.isEmpty()){
                    mFiles = mFilesNew;
                } else{
                    List<MusicFiles> list = new ArrayList<>();
                    for(MusicFiles user:mFilesNew){
                        String str = convert(user.getTitle());
                        if(str.toLowerCase().contains(strSearch.toLowerCase())){
                            list.add(user);
                        }
                    }
                    mFiles = (ArrayList<MusicFiles>) list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mFiles;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                // TODO Auto-generated method stub
                mFiles = (ArrayList<MusicFiles>) results.values;
                notifyDataSetChanged();
            }
        };
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


