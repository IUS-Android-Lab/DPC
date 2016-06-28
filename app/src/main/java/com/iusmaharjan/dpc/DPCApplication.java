package com.iusmaharjan.dpc;

import android.app.Application;

import timber.log.Timber;

/**
 * DPC Application Class
 * @author Ayush Maharjan
 */
public class DPCApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Initialize timber
        Timber.plant(new Timber.DebugTree());
    }

}
