package com.iusmaharjan.dpc.dpc;


import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;
import android.widget.Toast;

import com.iusmaharjan.dpc.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import timber.log.Timber;

public class DPCPreferenceFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener,
        DPCInterface.UserInterface {

    private static final int SET_DEVICE_ADMIN_REQUEST = 1001;

    private static final String URL_LF_MEETING_ROOM_APP = "https://www.dropbox.com/s/i7ohenua80ade5z/app-debug.apk";

    SwitchPreference prefDeviceAdmin;
    SwitchPreference prefDeviceOwner;
    Preference prefLFMeetingRoomApp;

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
        prefLFMeetingRoomApp = findPreference(getString(R.string.key_pref_lf_meeting_room_app));

        prefDeviceAdmin.setOnPreferenceChangeListener(this);
        prefDeviceOwner.setOnPreferenceChangeListener(this);
        prefLFMeetingRoomApp.setOnPreferenceClickListener(this);

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
    public boolean onPreferenceClick(Preference preference) {
        if(preference == prefLFMeetingRoomApp) {
            try {
                installSilently();
            } catch (IOException e) {
                e.printStackTrace();
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

    public class InstallAPK extends AsyncTask<String,Void,Void> {

        ProgressDialog progressDialog;
        int status = 0;

        private Context context;
        public void setContext(Context context, ProgressDialog progress){
            this.context = context;
            this.progressDialog = progress;
        }

        public void onPreExecute() {
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... arg0) {
            try {
                URL url = new URL(arg0[0]);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(false);
                c.connect();

                Timber.d("Connection:"+c.toString());

                File sdcard = Environment.getExternalStorageDirectory();
                File outputFile = new File(sdcard, "app1.apk");
                if(outputFile.exists()){
                    outputFile.delete();
                }
                FileOutputStream fos = new FileOutputStream(outputFile);

                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len1);
                }
                fos.flush();
                fos.close();
                is.close();

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/app.apk")), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

            } catch (FileNotFoundException fnfe) {
                status = 1;
                Log.e("File", "FileNotFoundException! " + fnfe);
            }

            catch(Exception e)
            {
                Log.e("UpdateAPP", "Exception " + e);
            }
            return null;
        }

        public void onPostExecute(Void unused) {
            progressDialog.dismiss();
            if(status == 1)
                Toast.makeText(context,"Game Not Available", Toast.LENGTH_LONG).show();
        }
    }


    private void installAPK() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/app.apk")), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void downloadAndInstallAPK() {
        InstallAPK installAPK = new InstallAPK();
        installAPK.setContext(getActivity(), new ProgressDialog(getActivity()));
        installAPK.execute(URL_LF_MEETING_ROOM_APP);
    }

    private void installFromGooglePlay() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=com.zeptolab.thieves.google"));
        try{
            startActivity(intent);
        }
        catch(Exception e){
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.zeptolab.thieves.google&rdid=com.zeptolab.thieves.google"));
            startActivity(intent);
        }
    }

    private void installSilently() throws IOException {
        PackageInstaller packageInstaller = getActivity().getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        int sessionID = packageInstaller.createSession(params);

        PackageInstaller.Session session = packageInstaller.openSession(sessionID);

        long sizeBytes = 0;
        final File file = new File(Environment.getExternalStorageDirectory(), "app.apk");
        if (file.isFile())
        {
            sizeBytes = file.length();
        }

        InputStream in = null;
        OutputStream out = null;

        in = new FileInputStream(file);
        out = session.openWrite("my_app_session", 0, sizeBytes);

        int total = 0;
        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1)
        {
            total += c;
            out.write(buffer, 0, c);
        }
        session.fsync(out);
        in.close();
        out.close();

        System.out.println("InstallApkViaPackageInstaller - Success: streamed apk " + total + " bytes");
        Timber.d("InstallApkViaPackageInstaller - Success: streamed apk " + total + " bytes");

//        session.commit(createIntentSender(getActivity(), sessionID));

        session.close();
    }

    private IntentSender createIntentSender(Context context, int sessionID) {
//        return
    }
}
