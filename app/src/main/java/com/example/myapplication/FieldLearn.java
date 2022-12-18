package com.example.myapplication;

import android.util.Log;

public class FieldLearn {
    public void field1() {
        Log.v("FieldLearn", "field log1");
        field2();
    }

    public void field3() {
        field2();
    }

    public void field2() {
        Log.v("FieldLearn", "field log2");
    }
}
