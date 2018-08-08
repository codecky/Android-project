package com.example.chenkeyu.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by chenkeyu on 2018/7/30.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_CACHING = 1;
    private Boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    LruCache<String,Bitmap> mBitmapCache;
    Map<String,String> requestMapCache = Collections.synchronizedMap(new HashMap<String, String>());

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener){
        mThumbnailDownloadListener = listener;
    }


    public ThumbnailDownloader(Handler responseHandler,LruCache<String,Bitmap> bitmapCache){
        super(TAG);
        mResponseHandler = responseHandler;
        mBitmapCache = bitmapCache;
    }

    @Override
    protected void onLooperPrepared(){
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if (msg.what == MESSAGE_DOWNLOAD){
                    T target = (T)msg.obj;
                    Log.i(TAG,"Got a request for URL: "+ mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit(){
        mHasQuit = true;
        return super.quit();
    }
    public void queueThumbnail(T target,String url){
        Log.i(TAG,"Got a URL: "+url);

        if (url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    public void queueThumbnailCache(String id,String url){
        requestMapCache.put(id,url);
        mRequestHandler.obtainMessage(MESSAGE_CACHING,id).sendToTarget();
    }

    private void handleRequest(final T target){
        try{
            final String url = mRequestMap.get(target);
            if (url == null){
                return;
            }
            final Bitmap bitmap;
            if (mBitmapCache.get(url) != null){
                bitmap = mBitmapCache.get(url);
            }else{
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes,0,bitmapBytes.length);
                mBitmapCache.put(url,bitmap);
            }

            Log.i(TAG,"Bitmap created");

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if(mRequestMap.get(target) != url || mHasQuit){
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });

        }catch (IOException ioe){
            Log.e(TAG,"Error downloading image", ioe);
        }
    }

    public void clearQueue(){
        mResponseHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }
}
