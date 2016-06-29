package com.iusmaharjan.dpc.archive;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.iusmaharjan.dpc.DeviceAdminReceiver;
import com.iusmaharjan.dpc.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int SET_DEVICE_ADMIN_REQUEST = 1001;

    @BindView(R.id.statusText)
    TextView statusText;

    @BindView(R.id.clearOwnerButton)
    Button clearOwnerButton;

    @BindView(R.id.setAdminButton)
    Button setAdminButton;

    @BindView(R.id.clearAdminButton)
    Button clearAdminButton;

    private DevicePolicyManager devicePolicyManager;

    private ComponentName deviceAdminReceiverComponentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize variables
        init();

        // Set contents in view
        setContentView(R.layout.activity_main);

        // Bind views
        ButterKnife.bind(this);

        // Update status
        updateStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == SET_DEVICE_ADMIN_REQUEST) {
            if(resultCode == RESULT_OK) {
                updateStatus();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.clearOwnerButton)
    void onClearOwnerButtonClicked() {
        if(devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            devicePolicyManager.clearDeviceOwnerApp(getPackageName());
            updateStatus();
        }
    }

    @OnClick(R.id.setAdminButton)
    void onSetAdminButtonClicked() {
        if(!devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName)) {
            requestToSetAdmin();
        }
    }

    @OnClick(R.id.clearAdminButton)
    void onClearAdminButtonClicked() {
        if(devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName)) {
            // removeActiveAdmin is not synchronous and admin may be active even after method call
            devicePolicyManager.removeActiveAdmin(deviceAdminReceiverComponentName);

            // Temporarily disable the button
            clearAdminButton.setEnabled(false);

            // Handler for posting back result
            final Handler mainHandler = new Handler(getMainLooper());

            //TODO: Declare in a better position
            // Create a new thread to wait for removeActiveAdmin to reflect changes
            Thread waitForAdminClearance = new Thread(new Runnable() {
                @Override
                public void run() {
                    //noinspection StatementWithEmptyBody : just waiting for admin to be inactive
                    while (devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName));
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Re-enable the button
                            clearAdminButton.setEnabled(true);

                            // Update status
                            updateStatus();
                        }
                    });
                }
            });

            // Start the new thread
            waitForAdminClearance.start();
        }
    }

    /**
     * Method to initialize {@link MainActivity#devicePolicyManager} and
     * {@link MainActivity#deviceAdminReceiverComponentName}
     */
    private void init() {
        devicePolicyManager =
                (DevicePolicyManager)getSystemService(DEVICE_POLICY_SERVICE);

        deviceAdminReceiverComponentName = DeviceAdminReceiver.getComponentName(this);
    }

    /**
     * Updates the status of the app. It recognizes the app as device owner, device admin, or none
     * and displays the status in {@link MainActivity#statusText}.
     *
     * It also toggles visibility of the {@link MainActivity#clearAdminButton},
     * {@link MainActivity#setAdminButton} and {@link MainActivity#clearOwnerButton} according to
     * the status
     */
    private void updateStatus() {
        if(devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
            statusText.setText(getString(R.string.status_device_owner));
            clearAdminButton.setVisibility(View.GONE);
            setAdminButton.setVisibility(View.GONE);
            clearOwnerButton.setVisibility(View.VISIBLE);
        } else if(devicePolicyManager.isAdminActive(deviceAdminReceiverComponentName)) {
            statusText.setText(getString(R.string.status_device_admin));
            clearAdminButton.setVisibility(View.VISIBLE);
            setAdminButton.setVisibility(View.GONE);
            clearOwnerButton.setVisibility(View.GONE);
        } else {
            statusText.setText(getString(R.string.status_not_admin));
            clearAdminButton.setVisibility(View.GONE);
            setAdminButton.setVisibility(View.VISIBLE);
            clearOwnerButton.setVisibility(View.GONE);
        }
    }

    /**
     * Start activity with ACTION_ADD_DEVICE_ADMIN intent requesting to set the current app as a
     * device admin;
     */
    private void requestToSetAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, deviceAdminReceiverComponentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.info_admin_required));
        startActivityForResult(intent, SET_DEVICE_ADMIN_REQUEST);
    }
}
