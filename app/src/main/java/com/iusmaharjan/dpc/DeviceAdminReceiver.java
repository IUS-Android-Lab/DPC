package com.iusmaharjan.dpc;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast Receiver that handles android.app.action.DEVICE_ADMIN_ENABLED intent action
 * @see
 *  <a href="https://developer.android.com/reference/android/app/admin/DeviceAdminReceiver.html">
 *     DeviceAdminReceiver</a>
 * @author Ayush Maharjan
 */
public class DeviceAdminReceiver extends android.app.admin.DeviceAdminReceiver {

    /**
     * Provides the component name
     * @param context Context of the package
     * @return ComponentName for DeviceAdminReceiver
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context, DeviceAdminReceiver.class);
    }

    @Override
    public void onProfileProvisioningComplete(Context context, Intent intent) {
        DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        ComponentName componentName = DeviceAdminReceiver.getComponentName(context);
        manager.setProfileName(componentName, context.getString(R.string.work_profile_name));
        manager.setProfileEnabled(componentName);
    }
}
