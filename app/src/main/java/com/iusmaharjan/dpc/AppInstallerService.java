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

    public static final Intent getAppInstallerServiceLaunchIntent(Context context) {
        return new Intent(context, AppInstallerService.class);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        // Queue to hold applications to be downloaded
        downloadQueue = new LinkedList<>();

        // Get Download Service
        downloadManager = (DownloadManager)this.getSystemService(Context.DOWNLOAD_SERVICE);

        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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
        downloadQueue.add(application);
        downloadNextApp();
    }

    public void addToDownloadQueueImmediately(Application application) {
        downloadQueue.add(0, application);
        downloadNextApp();
    }

    private void downloadNextApp() {
        if(currentDownloading == null && !downloadQueue.isEmpty()) {
            currentDownloading = downloadQueue.remove(0);
            // Start Current Download
            download();
        }
    }

    private void download() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(currentDownloading.getDownloadURL()));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk");
        request.setTitle("Downloading "+currentDownloading.getApplicationName()+"...");
        downloadId = downloadManager.enqueue(request);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                long downloadId = intent.getLongExtra(
                        DownloadManager.EXTRA_DOWNLOAD_ID, 0);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor c = downloadManager.query(query);
                if (c.moveToFirst()) {
                    int columnIndex = c
                            .getColumnIndex(DownloadManager.COLUMN_STATUS);
                    String uriString = c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                    Uri uri = Uri.parse(uriString);
                    String path = uri.getPath();
                    if (DownloadManager.STATUS_SUCCESSFUL == c
                            .getInt(columnIndex)) {
                        Timber.d(path);
                        try {
                            installSilently(new File(path));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Timber.d("Download Successful");
                    }
                }
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

    /**
     * Installs APK with prompt
     */
    private void installAPK(File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public class ServiceBinder extends Binder {
        public AppInstallerService getService() {
            return AppInstallerService.this;
        }
    }

}
