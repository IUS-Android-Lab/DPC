package com.iusmaharjan.dpc;

import android.content.Context;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Manages Enterprise Applications
 */
public class EnterpriseApplicationManager {

    static EnterpriseApplicationManager enterpriseApplicationManager = null;

    List<Application> enterpriseApplications;

    @Inject
    Context context;

    PackageManager pm;

    private EnterpriseApplicationManager(ApplicationListSource source) {
        enterpriseApplications = source.getApplicationList();
        DPCApplication.getDPCComponent().inject(this);
        pm = context.getPackageManager();
    }

    public static EnterpriseApplicationManager getInstance() {
        if(enterpriseApplicationManager == null) {
            enterpriseApplicationManager =
                    new EnterpriseApplicationManager(new StaticApplicationList());
        }
        return enterpriseApplicationManager;
    }

    public List<Application> getInstalledApps() {
        List<Application> installedApps = new ArrayList<>();
        for(Application app: enterpriseApplications) {
            if(isPackageInstalled(app.getPackageId())) {
                installedApps.add(app);
            }
        }
        return installedApps;
    }

    public List<Application> getNotInstalledApps() {
        List<Application> notInstalledApps = new ArrayList<>();
        for(Application app: enterpriseApplications) {
            if(!isPackageInstalled(app.getPackageId())) {
                notInstalledApps.add(app);
            }
        }
        return notInstalledApps;
    }

    private boolean isPackageInstalled(String packageName) {
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
