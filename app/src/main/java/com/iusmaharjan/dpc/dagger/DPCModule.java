package com.iusmaharjan.dpc.dagger;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;

import com.iusmaharjan.dpc.DeviceAdminReceiver;

import dagger.Module;
import dagger.Provides;

@Module
public class DPCModule {

    @Provides
    DevicePolicyManager providesDevicePolicyManager(Context context) {
        return (DevicePolicyManager)context.getSystemService(Activity.DEVICE_POLICY_SERVICE);
    }

    @Provides
    ComponentName providesDeviceAdminReceiverComponentName(Context context) {
        return DeviceAdminReceiver.getComponentName(context);
    }
}
