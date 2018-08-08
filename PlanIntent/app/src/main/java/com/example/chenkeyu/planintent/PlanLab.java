package com.example.chenkeyu.planintent;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by chenkeyu on 2018/5/17.
 */

public class PlanLab {
    private static PlanLab sPlanLab;
    private List<Plan> mPlans;

    public static PlanLab get(Context context){
        if(sPlanLab == null){
            sPlanLab = new PlanLab(context);
        }
        return sPlanLab;
    }

    private PlanLab(Context context){
        mPlans = new ArrayList<>();
        for(int i = 0;i<100;i++){
            Plan plan = new Plan();
            plan.setTitle("Plan #"+ i);
            plan.setSolved(i%2 == 0);
            mPlans.add(plan);
        }
    }

    public List<Plan> getmPlans(){
        return mPlans;
    }

    public Plan getPlan(UUID id){
        for(Plan plan : mPlans){
            if(plan.getId().equals(id)){
                return plan;
            }
        }
        return null;
    }
}
