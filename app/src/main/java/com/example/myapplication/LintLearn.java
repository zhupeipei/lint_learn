package com.example.myapplication;

import android.util.Log;

import org.jetbrains.annotations.Nullable;

public class LintLearn {
    private FieldLearn mFieldLearn;

    public void aa() {
        Log.i("TestLog", "test 1112");
        if (mFieldLearn != null) {
            mFieldLearn.field2();
        }
    }

    public LintLearn() {
//        mFieldLearn = new FieldLearn();
//        if (mFieldLearn != null) {
            mFieldLearn.field1();
//        }
    }


}
