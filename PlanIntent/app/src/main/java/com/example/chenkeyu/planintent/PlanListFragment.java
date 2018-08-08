package com.example.chenkeyu.planintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by chenkeyu on 2018/5/17.
 */

public class PlanListFragment extends Fragment {
    private RecyclerView mPlanRecycleView;
    private PlanAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstancedState){
        View view = inflater.inflate(R.layout.fragment_plan_list,container,false);
        mPlanRecycleView = (RecyclerView)view.findViewById(R.id.plan_recycler_view);
        mPlanRecycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        //mPlanRecycleView.getAdapter().notifyItemMoved(0,5);
        return view;
    }

    private void updateUI(){
        PlanLab planLab = PlanLab.get(getActivity());
        List<Plan> plans = planLab.getmPlans();

        mAdapter = new PlanAdapter(plans);
        mPlanRecycleView.setAdapter(mAdapter);
    }

    private class PlanHolder extends RecyclerView.ViewHolder{
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Plan mPlan;

        public PlanHolder(View view){
            super(view);
            mTitleTextView = (TextView)view.findViewById(R.id.plan_title);
            mDateTextView = (TextView)view.findViewById(R.id.plan_date);
        }

        public void bind(Plan plan){
            mPlan = plan;
            mTitleTextView.setText(mPlan.getTitle());
            mDateTextView.setText(mPlan.getDate().toString());
        }
    }

    private class PlanAdapter extends RecyclerView.Adapter<PlanHolder>{
        private List<Plan> mPlans;

        public PlanAdapter(List<Plan> plans){
            mPlans = plans;
        }

        @Override
        public PlanHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.list_item_plan,parent,false);
            PlanHolder holder = new PlanHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(PlanHolder holder, int position) {
            Plan plan = mPlans.get(position);
            holder.bind(plan);
        }

        @Override
        public int getItemCount() {
            return mPlans.size();
        }

    }
}
