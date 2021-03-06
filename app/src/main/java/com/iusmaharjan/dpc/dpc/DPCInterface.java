package com.iusmaharjan.dpc.dpc;

import android.content.Intent;

/**
 * Interface to present main screen
 */
public interface DPCInterface {

    interface UserInterface {
        void setDeviceAdminPrefOff();
        void setDeviceAdminPrefOn();
        void setDeviceOwnerPrefOff();
        void setDeviceOwnerPrefOn();
        void disableCreateWorkProfile();
        void requestToSetAdmin(Intent intent);
        void requestToCreateProfile(Intent intent);
        void disableDownloadApp();
        void enableDownloadApp();
    }

    interface Presenter {
        void setInitialConditions();
        void registerAdmin();
        void unregisterAdmin();
        void unregisterOwner();
        void createWorkProfile();
    }

}
