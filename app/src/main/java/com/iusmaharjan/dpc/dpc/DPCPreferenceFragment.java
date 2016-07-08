package com.iusmaharjan.dpc.dpc;


import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.Toast;

import com.iusmaharjan.dpc.R;

import timber.log.Timber;

public class DPCPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        DPCInterface.UserInterface{

    private static final int SET_DEVICE_ADMIN_REQUEST = 1001;
    private static final int PROVISION_MANAGED_PROFILE_REQUEST = 10020;

    SwitchPreference prefDeviceAdmin;
    SwitchPreference prefDeviceOwner;
    Preference prefProvisionManagedProfile;

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
        prefProvisionManagedProfile = findPreference(getString(R.string.key_pref_provision_managed_profile));

        prefDeviceAdmin.setOnPreferenceChangeListener(this);
        prefDeviceOwner.setOnPreferenceChangeListener(this);
        prefProvisionManagedProfile.setOnPreferenceClickListener(this);

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
        } else if(resultCode == PROVISION_MANAGED_PROFILE_REQUEST) {
            if (requestCode == Activity.RESULT_OK) {
                Timber.d("Successful");
            } else {
                Timber.d("Unsuccessful");
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
        if(preference == prefProvisionManagedProfile) {
            dpcPresenter.createWorkProfile();
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
    public void disableCreateWorkProfile() {
        Timber.d("disableCreateWorkProfile");
        prefProvisionManagedProfile.setEnabled(false);
    }

    @Override
    public void requestToSetAdmin(Intent intent) {
        Timber.d("requestToSetAdmin");
        startActivityForResult(intent, SET_DEVICE_ADMIN_REQUEST);
    }

    @Override
    public void requestToCreateProfile(Intent intent) {
        Timber.d("requestToCreateProfile");
        startActivityForResult(intent, PROVISION_MANAGED_PROFILE_REQUEST);
        getActivity().finish();
    }
}
