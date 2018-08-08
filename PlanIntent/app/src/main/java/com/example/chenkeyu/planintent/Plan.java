package com.example.chenkeyu.planintent;

import java.util.Date;
import java.util.UUID;

/**
 * Created by chenkeyu on 2018/5/10.
 */

public class Plan {

    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;

    public Plan(){
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public void setDate(Date mData) {
        this.mDate = mDate;
    }

    public Date getDate() {
        return mDate;
    }

    public void setSolved(boolean mSolved) {
        this.mSolved = mSolved;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getTitle() {
        return mTitle;
    }
}
