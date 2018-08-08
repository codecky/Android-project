package com.example.chenkeyu.planintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Created by chenkeyu on 2018/5/10.
 */

public class PlanFragment extends Fragment {

    private Plan mPlan;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mFinishedCheckBox;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        mPlan = new Plan();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        View v =inflater.inflate(R.layout.fragment_plan,container,false);

        mTitleField = (EditText)v.findViewById(R.id.plan_title);
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPlan.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

     mDateButton = (Button)v.findViewById(R.id.plan_date);
     mDateButton.setText(mPlan.getDate().toString());
     mDateButton.setEnabled(false);

     mFinishedCheckBox = (CheckBox)v.findViewById(R.id.plan_finished);
     mFinishedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
         @Override
         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
             mPlan.setSolved(isChecked);
         }
     });
        return v;
    }

}
