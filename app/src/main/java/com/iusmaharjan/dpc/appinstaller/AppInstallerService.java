package com.iusmaharjan.dpc.appinstaller;

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

/**
 * Background Service that downloads and installs apps.
 * It maintains a queue of apps to be installed and installs them one at a time.
 */
public class AppInstallerService extends Service {

    /**
     * Queue of apps to be downloaded
     */
    List<Application> downloadQueue;

    /**
     * Current app being downloaded
     */
    Application currentDownloading;

    /**
     * DownloadManager is used to download apk
     */
    DownloadManager downloadManager;

    /**
     * ID of app being enqueued in Download Manager
     */
    long downloadId;

    /**
     * Binder to bind service
     */
    private IBinder serviceBinder = new ServiceBinder();

    /**
     * Default Constructor
     */
    public AppInstallerService() {
    }

    /**
     * Provides intent to launch {@link AppInstallerService}
     * @param context Context from where the service is being launched
     * @return Intent to launch service
     */
    public static Intent getAppInstallerServiceLaunchIntent(Context context) {
        return new Intent(context, AppInstallerService.class);
    }


    /**
     * Initialization of {@link AppInstallerService#downloadQueue} and {@link AppInstallerService#downloadManager}
     * downloadStatusReceiver registered.
     */
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

    /**
     * Adds application to download queue after checking it is not present in the queue and starts download
     * @param application {@link Application} to be added to the queue
     */
    public void addToDownloadQueue(Application application) {
        if(notInQueue(application)) {
            downloadQueue.add(application);
            downloadNextApp();
        }
    }

    /**
     * Checks if no apps are being downloaded currently. If not, first item is removed from
     * {@link AppInstallerService#downloadQueue} and download provided for download
     */
    private void downloadNextApp() {
        if(currentDownloading == null && !downloadQueue.isEmpty()) {
            currentDownloading = downloadQueue.remove(0);
            download();
        }
    }

    /**
     * Enqueues {@link AppInstallerService#currentDownloading} in {@link AppInstallerService#downloadManager}
     * to start download
     */
    private void download() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(currentDownloading.getDownloadURL()));
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk");
        request.setTitle("Downloading "+currentDownloading.getApplicationName()+"...");
        downloadId = downloadManager.enqueue(request);
    }

    /**
     * Receiver for the event when download completes
     */
    BroadcastReceiver downloadStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if action is that of download complete
            String action = intent.getAction();
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {

                // Query for download id
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

                            // install the app
                            installSilently(new File(path));

                            // delete APK
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

    /**
     * Silently installs the provided app
     * @param file APK file to be installed
     * @throws IOException Exception while installing
     */
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

    /**
     * {@link IntentSender} for silently installing the app
     * @param context Context of the installer
     * @param sessionId Session id
     * @return Intent Sender
     */
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
     * Checks if the {@link Application} is in queue
     * @param application Application provided
     * @return  If the app is not in queue, returns true. Else, false.
     */
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
