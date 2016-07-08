package com.iusmaharjan.dpc.dpc;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.iusmaharjan.dpc.DPCApplication;
import com.iusmaharjan.dpc.R;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Presenter for {@link DPCPreferenceFragment}
 */
public class DPCPresenter implements DPCInterface.Presenter{

    private DPCInterface.UserInterface userInterface;
    private String packageName;

    @Inject
    DevicePolicyManager devicePolicyManager;

    @Inject
    ComponentName deviceAdminReceiverComponentName;

    @Inject
    Context context;

    public DPCPresenter(Context context, DPCInterface.UserInterface userInterface) {
        DPCApplication.getDPCComponent().inject(this);
        this.userInterface = userInterface;
        packageName = context.getPackageName();
    }


    @Override
    public void registerAdmin() {
        if(!devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminReceiverComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                    context.getString(R.string.info_admin_required));
            userInterface.requestToSetAdmin(intent);
        }
    }

    @Override
    public void unregisterAdmin() {

        // If the app is owner, first unregister owner
        unregisterOwner();

        removeActiveAdmin();
    }


    @Override
    public void createWorkProfile() {
        if(!devicePolicyManager.isProfileOwnerApp(packageName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE);
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, deviceAdminReceiverComponentName);
            userInterface.requestToCreateProfile(intent);
        }
    }

    @Override
    public void unregisterOwner() {
        // Check if the app is owner and clear device owner if true
        if(devicePolicyManager.isDeviceOwnerApp(packageName)) {
            devicePolicyManager.clearDeviceOwnerApp(packageName);
            userInterface.setDeviceOwnerPrefOff();
        }
    }

    @Override
    public void setInitialConditions() {
        Timber.d("setInitialConditions");
        if(devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName)) {
            Timber.d("Is Device Admin");
            userInterface.setDeviceAdminPrefOn();
        } else {
            Timber.d("Is not Device Admin");
            userInterface.setDeviceAdminPrefOff();
        }

        if(devicePolicyManager.isDeviceOwnerApp(packageName)) {
            Timber.d("Is Device Owner");
            userInterface.setDeviceOwnerPrefOn();
        } else {
            Timber.d("Is not Device Owner");
            userInterface.setDeviceOwnerPrefOff();
        }

        if(devicePolicyManager.isProfileOwnerApp(packageName)) {
            Timber.d("Is Profile Owner App");
            userInterface.disableCreateWorkProfile();
        }
    }

    private void removeActiveAdmin() {
        if(devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName)) {
            // removeActiveAdmin is not synchronous and admin may be active even after method call
            devicePolicyManager.removeActiveAdmin(deviceAdminReceiverComponentName);

            // Handler for posting back result
            final Handler mainHandler = new Handler();

            // Create a new thread to wait for removeActiveAdmin to reflect changes
            Thread waitForAdminClearance = new Thread(new Runnable() {
                @Override
                public void run() {
                    //noinspection StatementWithEmptyBody : just waiting for admin to be inactive
                    while (devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName));
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Update status
                            userInterface.setDeviceAdminPrefOff();
                        }
                    });
                }
            });

            // Start the new thread
            waitForAdminClearance.start();
        }
    }
}
