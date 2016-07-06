package com.iusmaharjan.dpc;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;

public class AppInstallerService extends Service {

    List<Application> downloadQueue;

    Application currentDownloading;

    DownloadManager downloadManager;
    long downloadId;

    private IBinder serviceBinder = new ServiceBinder();

    public AppInstallerService() {
    }

    public static Intent getAppInstallerServiceLaunchIntent(Context context) {
        return new Intent(context, AppInstallerService.class);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Queue to hold applications to be downloaded
        downloadQueue = new LinkedList<>();

        // Get Download Service
        downloadManager = (DownloadManager)this.getSystemService(Context.DOWNLOAD_SERVICE);

        registerReceiver(downloadStatusReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void addToDownloadQueue(Application application) {
        if(notInQueue(application)) {
            downloadQueue.add(application);
            downloadNextApp();
        }
    }

    private void downloadNextApp() {
        if(currentDownloading == null && !downloadQueue.isEmpty()) {
            currentDownloading = downloadQueue.remove(0);
            download();
        }
    }

    private void download() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(currentDownloading.getDownloadURL()));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk");
        request.setTitle("Downloading "+currentDownloading.getApplicationName()+"...");
        downloadId = downloadManager.enqueue(request);
    }

    BroadcastReceiver downloadStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                DownloadManager.Query query = new DownloadManager.Query();
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                query.setFilterById(downloadId);

                Cursor c = downloadManager.query(query);
                Timber.d("ACTION_DOWNLOAD_COMPLETE");
                if (c.moveToFirst()) {
                    int columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    if(c.getInt(columnIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                        Timber.d("Download Successful");

                        try {
                            String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            Uri uri = Uri.parse(uriString);
                            String path = uri.getPath();

                            installSilently(new File(path));

                            //TODO delete file
                            downloadManager.remove(downloadId);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                c.close();
            }
        }
    };

    private void installSilently(final File file) throws IOException {

        // Get package installer
        PackageInstaller packageInstaller = this.getPackageManager().getPackageInstaller();

        // Define installer params
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        // Create a session ID
        int sessionID = packageInstaller.createSession(params);

        // Open Session
        PackageInstaller.Session session = packageInstaller.openSession(sessionID);

        // Calculate file size
        long sizeBytes = 0;
        if (file.isFile())
        {
            sizeBytes = file.length();
        }

        // Open file input stream
        InputStream in = new FileInputStream(file);

        // Define session as output stream
        OutputStream out = session.openWrite("installation_session", 0, sizeBytes);

        byte[] buffer = new byte[65536];
        int c;
        while ((c = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, c);
        }

        session.fsync(out);

        in.close();
        out.close();

        session.commit(createIntentSender(this, sessionID));

        session.close();

        currentDownloading = null;
        downloadNextApp();

    }

    // TODO Replace with custom intent
    private static IntentSender createIntentSender(Context context, int sessionId) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId,
                new Intent(Intent.ACTION_INSTALL_PACKAGE),
                0);
        return pendingIntent.getIntentSender();
    }

    private boolean notInQueue(Application application) {
        for(Application app: downloadQueue) {
            if (app.equals(application)) {
                return false;
            }
        }
        return !application.equals(currentDownloading);
    }

    /**
     * Service Binder Class
     */
    public class ServiceBinder extends Binder {
        public AppInstallerService getService() {
            return AppInstallerService.this;
        }
    }
}
