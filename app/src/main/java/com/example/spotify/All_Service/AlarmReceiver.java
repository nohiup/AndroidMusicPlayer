package com.example.spotify.All_Service;

import static android.os.Build.*;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.spotify.LoginActivity;
import com.example.spotify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AlarmReceiver extends BroadcastReceiver {
    private final String CHANNEL_ID = "Daily Notification";
    private final String NOTIFICATION_TITLE = "Daily Recommendation";
    private String Notification_Context;
    long size;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, LoginActivity.class);//on tap this activity will open

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);//getting the pendingIntent

        Notification.Builder builder = new Notification.Builder(context);//building the notification


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Query query = firestore.collection("Music");

        firestore.collection("Music").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    ArrayList<String> docNames = new ArrayList<String>();
                    int i = 0;
                    for (DocumentSnapshot doc: task.getResult().getDocuments()) {
                        docNames.add(doc.getId());
                        i++;
                    }

                    Log.d("Size", String.valueOf(i));
                    Random rng = new Random();
                    int rand = rng.nextInt(i);

                    firestore.collection("Music").document(docNames.get(rand)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.d("random", String.valueOf(rand));
                                Notification notification = builder.setContentTitle(NOTIFICATION_TITLE)
                                        .setContentText("Try this song: " + task.getResult().get("name"))
                                        .setTicker("New Message Alert!")
                                        .setSmallIcon(R.mipmap.ic_launcher)
                                        .setContentIntent(pendingIntent).build();
                                if (VERSION.SDK_INT >= VERSION_CODES.O) {
                                    builder.setChannelId(CHANNEL_ID);
                                }

                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//below creating notification channel, because of androids latest update, O is Oreo
                                if (VERSION.SDK_INT >= VERSION_CODES.O) {
                                    NotificationChannel channel = new NotificationChannel(
                                            CHANNEL_ID,
                                            "NotificationDemo",
                                            NotificationManager.IMPORTANCE_DEFAULT
                                    );
                                    notificationManager.createNotificationChannel(channel);
                                }

                                notificationManager.notify(0, notification);
                            }
                        }
                    });

                }
            }
        });
    }
}
