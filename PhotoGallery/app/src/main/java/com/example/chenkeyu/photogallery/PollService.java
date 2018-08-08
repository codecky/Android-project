package com.example.chenkeyu.photogallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by chenkeyu on 2018/8/5.
 */

// 为了实现在后台下载新的搜索结果的功能，新建名为PollService的IntentService子类。IntentService服务能顺利执行命令队列里的命令（即服务的intent）
// 在菜单栏新增Start PollService按钮，点击后触发setServiceAlarm（）方法，使用AlarmManager间隔性的运行服务。
// 收到第一条命令时，IntentService启动，触发一个后台线程，随后每接收一条命令，都在后台线程上调用onHandleIntent（）方法
// 在HandleIntent（）方法中，获取图片ID，与之前获取并存储的第一张图片ID对比，ID不同则说明获取到了新的图片，从而发布通知告诉用户
public class PollService extends IntentService {
    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    public static Intent newIntent(Context context){
        return new Intent(context,PollService.class);
    }

    public PollService(){
        super(TAG);
    }

    public static void setServiceAlarm(Context context,boolean isOn){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,0);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(isOn){
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),POLL_INTERVAL_MS,pi);
        }else{
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PollService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context,0,i,PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    @Override
    protected void onHandleIntent(Intent intent){
        if(!isNetworkAvailableAndConnected()){
            return;
        }

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);
        List<GalleryItem> items;

        if(query == null){
            items = new FlickrFetchr().fetchRecentPhotos();
        }else{
            items = new FlickrFetchr().searchPhotos(query);
        }

        if (items.size() == 0){
            return;
        }
        String resultId = items.get(0).getId();
        if (resultId.equals(lastResultId)){
            Log.i(TAG,"Got an old result: "+resultId);
        }else {
            Log.i(TAG,"Got an new result: "+resultId);

            String CHANNEL_ID = "channel_1";
            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this,0,i,0);

            NotificationManager notificationManager = (NotificationManager)this.getSystemService(NOTIFICATION_SERVICE);

            Bitmap Bill = BitmapFactory.decodeResource(getResources(),R.drawable.bill_up_close);
            Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(resources.getString(R.string.new_pictures_title))
                    .setContentText(resources.getText(R.string.new_picture_text))
                    .setLargeIcon(Bill)
                    .setContentIntent(pi)
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(0,notification);
            //NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            //notificationManager.notify(0,notification);
        }
        QueryPreferences.setLastResultId(this,resultId);
    }
    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }
}
