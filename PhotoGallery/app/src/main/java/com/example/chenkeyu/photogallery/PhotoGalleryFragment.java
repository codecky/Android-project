package com.example.chenkeyu.photogallery;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenkeyu on 2018/6/30.
 */

// 在onCreate()方法中，通过FetchItemTask().execute()方法触发FetchItemTask.doInBackground()方法，获取图片缩略图的URL；
// 而后通过onPostExecute（）方法将获取到的items传递给mItems对象，至此图片的URL获取成功。并通过setupAdapter()触发PhotoAdapter。

// 从主线程向后台线程发起请求，下载图片：
// 新建ThumbnailDownloader类建立后台线程，其中包含onLooperPrepared（）方法，获取并发送消息给他的目标Handler，queueThumbnail（）方法处理下载请求。
// 在onBindViewHolder（）中，通过mThumbnailDownloader.queueThumbnail(photoHolder,galleryItem.getUrl())方法将要下载图片的URL传递给后台线程，将该消息加入到消息队列的尾部，
// 之后在onLooperPrepared（）.handleRequest（）方法中下载图片。

// 从后台线程回到主线程，更新UI
// 在主线程onCreate（）方法中创建responseHandler自动与主线程Looper相连，通过一系列的setThumbnailDownloadListener（）、ThumbnailDownloadListener（）、onThumbnailDownloaded（）
// 方法将responseHandler传递到后台线程。（因为responseHandler始终与主线程Looper相连，所以responseHandler负责处理的所有消息都将在主线程队列中处理。
// 当handleRequest（）中下载图片完成后，通过调用responseHandler.post()方法发送Message给主线程，并调用onThumbnailDownloaded（）方法在主线程更新UI。

public class PhotoGalleryFragment extends Fragment {
    private RecyclerView mPhotoRecyclerView;
    private static final String TAG = "PhotoGalleryFragment";
    private int current_page = 1;
    private int fetched_page = 0;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;
    LruCache<String,Bitmap> mBitmapCache;

    public static PhotoGalleryFragment newInstance(){
        return new PhotoGalleryFragment();
    }
    @Override
    public void onCreate(Bundle savedInstancedState){
        super.onCreate(savedInstancedState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();

        int cacheSize = 4 * 1024 * 1024;//4MB
        mBitmapCache = new LruCache<String, Bitmap>(cacheSize);

        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler,mBitmapCache);
        mThumbnailDownloader.setThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(),bitmap);
                photoHolder.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG,"Background thread started");
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View v = inflater.inflate(R.layout.fragment_photo_gallery,container,false);
        mItems = new ArrayList<GalleryItem>();
        mPhotoRecyclerView = (RecyclerView)v.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        /*mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(),3);

                if(dy > 0){     //向下滑动
                    int visibleItemCount = gridLayoutManager.getChildCount();
                    int totalItemCount = gridLayoutManager.getItemCount();
                    int pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();

                    if((visibleItemCount + pastVisibleItems) >= totalItemCount && current_page == fetched_page && totalItemCount > 0){
                        new FetchItemTask().execute(current_page);

                        current_page++;
                    }
                }
            }
        });*/
        setupAdapter();
        return v;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG,"Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater){
        super.onCreateOptionsMenu(menu,menuInflater);
        menuInflater.inflate(R.menu.fragment_photo_gallery,menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView)searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                if(searchView != null){
                    InputMethodManager imm = (InputMethodManager)searchView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(imm != null){
                        imm.hideSoftInputFromWindow(searchView.getWindowToken(),0);
                    }
                }
                QueryPreferences.setStoredQuery(getActivity(),query);
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query,false);
            }
        });

        MenuItem toogleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if(PollService.isServiceAlarmOn(getActivity())){
            toogleItem.setTitle(R.string.stop_polling);
        }else{
            toogleItem.setTitle(R.string.start_polling);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(),null);
                updateItems();
                return true;
            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(),shouldStartAlarm);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems(){
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemTask(query).execute();
    }

    private void setupAdapter(){
        if(isAdded()){
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{
        private ImageView mItemImageView;
        public PhotoHolder(View itemView){
            super(itemView);
            mItemImageView = (ImageView)itemView.findViewById(R.id.item_image_view);
        }
        public void bindDrawable(Drawable drawable){
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        private List<GalleryItem> mGalleryItems;
        public PhotoAdapter(List<GalleryItem> galleryItems){
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup viewGroup,int ViewType){
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery,viewGroup,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder,int position){
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            photoHolder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(photoHolder,galleryItem.getUrl());
            String url = galleryItem.getUrl();
            if(mGalleryItems.size() > 1){
                int endPos = position - 10;
                if(endPos <= 0) endPos = 0;
                if (endPos > 0){
                    for(int i = position-1;i>=endPos;i--){
                        if(i < mGalleryItems.size()){
                            url = mGalleryItems.get(i).getUrl();
                            String id = mGalleryItems.get(i).getId();
                            if(url != null) mThumbnailDownloader.queueThumbnailCache(id,url);
                        }
                    }
                }
                for (int i = position+1;i<=position+10;i++){
                    if(i<mGalleryItems.size()){
                        url = mGalleryItems.get(i).getUrl();
                        String id = mGalleryItems.get(i).getId();
                        if(url != null) mThumbnailDownloader.queueThumbnailCache(id,url);
                    }
                }
            }
        }

        @Override
        public int getItemCount(){
            return mGalleryItems.size();
        }

    }

    private class FetchItemTask extends AsyncTask<Integer,Void,List<GalleryItem>>{
        private String mQuery;

        public FetchItemTask(String query){
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Integer... params){
            if(mQuery == null)
                return new FlickrFetchr().fetchRecentPhotos();
            else
                return new FlickrFetchr().searchPhotos(mQuery);
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items){
            mItems = items;
            setupAdapter();
            fetched_page++;
        }
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }
}
