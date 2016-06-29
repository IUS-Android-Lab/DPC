package com.iusmaharjan.dpc;

import android.app.Application;

import com.iusmaharjan.dpc.dagger.AppModule;
import com.iusmaharjan.dpc.dagger.DPCComponent;
import com.iusmaharjan.dpc.dagger.DaggerDPCComponent;

import timber.log.Timber;

/**
 * DPC Application Class
 * @author Ayush Maharjan
 */
public class DPCApplication extends Application {

    private static DPCComponent dpcComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize DPC component
        dpcComponent = createDPCComponent();

        //Initialize timber
        Timber.plant(new Timber.DebugTree());
    }

    public DPCComponent createDPCComponent() {
        return DaggerDPCComponent.builder().appModule(new AppModule(this)).build();
    }

    public static DPCComponent getDPCComponent() {
        return dpcComponent;
    }

}
