package com.iusmaharjan.dpc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AppInstallerService extends Service {
    public AppInstallerService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
