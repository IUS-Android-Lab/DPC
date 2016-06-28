package com.iusmaharjan.dpc;


import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

public class DPCPreferenceFragment extends PreferenceFragment {

    SwitchPreference prefDeviceAdmin;
    SwitchPreference prefDeviceOwner;

    public DPCPreferenceFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        prefDeviceAdmin = (SwitchPreference)findPreference(getString(R.string.key_pref_device_admin));
        prefDeviceOwner = (SwitchPreference)findPreference(getString(R.string.key_pref_device_owner));

    }

}
