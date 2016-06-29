package com.iusmaharjan.dpc.dpc;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.iusmaharjan.dpc.R;

import timber.log.Timber;

public class DPCPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, DPCInterface.UserInterface{

    private static final int SET_DEVICE_ADMIN_REQUEST = 1001;

    SwitchPreference prefDeviceAdmin;
    SwitchPreference prefDeviceOwner;

    DPCInterface.Presenter dpcPresenter;

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

        prefDeviceAdmin.setOnPreferenceChangeListener(this);
        prefDeviceOwner.setOnPreferenceChangeListener(this);

        dpcPresenter.setInitialConditions();
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
}