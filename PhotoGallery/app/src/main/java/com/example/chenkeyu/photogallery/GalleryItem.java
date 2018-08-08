package com.example.chenkeyu.photogallery;

/**
 * Created by chenkeyu on 2018/7/2.
 */

public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;

    @Override
    public String toString(){
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String Caption) {
        this.mCaption = Caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String Id) {
        this.mId = Id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String Url) {
        this.mUrl = Url;
    }
}
