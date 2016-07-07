package com.iusmaharjan.dpc.dpc;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.iusmaharjan.dpc.appinstaller.AppInstallerService;
import com.iusmaharjan.dpc.appinstaller.Application;
import com.iusmaharjan.dpc.appinstaller.EnterpriseApplicationManager;
import com.iusmaharjan.dpc.R;

import timber.log.Timber;

public class DPCPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        DPCInterface.UserInterface {

    private static final int SET_DEVICE_ADMIN_REQUEST = 1001;

    SwitchPreference prefDeviceAdmin;
    SwitchPreference prefDeviceOwner;
    Preference prefDownloadApps;

    DPCInterface.Presenter dpcPresenter;

    AppInstallerService boundService;
    boolean mServiceBound;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AppInstallerService.ServiceBinder serviceBinder =
                    (AppInstallerService.ServiceBinder)iBinder;
            boundService = serviceBinder.getService();
            mServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceBound = false;
        }
    };

    public DPCPreferenceFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dpcPresenter = new DPCPresenter(getActivity(), this);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        prefDeviceAdmin = (SwitchPreference)findPreference(getString(R.string.key_pref_device_admin));
        prefDeviceOwner = (SwitchPreference)findPreference(getString(R.string.key_pref_device_owner));
        prefDownloadApps = findPreference(getString(R.string.key_pref_download_apps));

        prefDeviceAdmin.setOnPreferenceChangeListener(this);
        prefDeviceOwner.setOnPreferenceChangeListener(this);
        prefDownloadApps.setOnPreferenceClickListener(this);

        dpcPresenter.setInitialConditions();

    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = AppInstallerService.getAppInstallerServiceLaunchIntent(getActivity());
        getActivity().startService(intent);
        getActivity().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mServiceBound) {
            getActivity().unbindService(mServiceConnection);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SET_DEVICE_ADMIN_REQUEST) {
            if(resultCode == Activity.RESULT_OK) {
                setDeviceAdminPrefOn();
            } else {
                setDeviceAdminPrefOff();
            }
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if(preference == prefDeviceAdmin) {
            if((boolean)newValue) {
                dpcPresenter.registerAdmin();
            } else {
                dpcPresenter.unregisterAdmin();
            }
        } else if(preference == prefDeviceOwner) {
            if(!(boolean)newValue) {
                dpcPresenter.unregisterOwner();
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference == prefDownloadApps) {
            EnterpriseApplicationManager applicationManager = EnterpriseApplicationManager.getInstance();
            for(Application application: applicationManager.getNotInstalledApps()) {
                boundService.addToDownloadQueue(application);
            }
        }
        return false;
    }

    @Override
    public void setDeviceAdminPrefOff() {
        Timber.d("setDeviceAdminPrefOff");
        prefDeviceAdmin.setChecked(false);
    }

    @Override
    public void setDeviceAdminPrefOn() {
        Timber.d("setDeviceAdminPrefOff");
        prefDeviceAdmin.setChecked(true);
    }

    @Override
    public void setDeviceOwnerPrefOff() {
        Timber.d("setDeviceOwnerPrefOff");
        prefDeviceOwner.setChecked(false);
        prefDeviceOwner.setEnabled(false);
    }

    @Override
    public void setDeviceOwnerPrefOn() {
        Timber.d("setDeviceOwnerPrefOn");
        prefDeviceOwner.setChecked(true);
    }

    @Override
    public void requestToSetAdmin(Intent intent) {
        Timber.d("requestToSetAdmin");
        startActivityForResult(intent, SET_DEVICE_ADMIN_REQUEST);
    }

    @Override
    public void disableDownloadApp() {
        Timber.d("disableDownloadApp");
        prefDownloadApps.setEnabled(false);
    }

    @Override
    public void enableDownloadApp() {
        Timber.d("enableDownloadApp");
        prefDownloadApps.setEnabled(true);
    }
}
