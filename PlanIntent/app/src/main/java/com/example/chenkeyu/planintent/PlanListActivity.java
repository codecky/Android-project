package com.example.chenkeyu.planintent;

import android.support.v4.app.Fragment;

/**
 * Created by chenkeyu on 2018/5/17.
 */

public class PlanListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment(){
        return new PlanListFragment();
    }
}
