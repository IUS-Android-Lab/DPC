package com.iusmaharjan.dpc.appinstaller;

import android.content.Context;
import android.content.pm.PackageManager;

import com.iusmaharjan.dpc.DPCApplication;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Manages Enterprise Applications
 */
public class EnterpriseApplicationManager {

    /**
     * Instance for singleton
     */
    static EnterpriseApplicationManager enterpriseApplicationManager = null;

    /**
     * List of enterprise applications
     */
    List<Application> enterpriseApplications;

    @Inject
    Context context;

    PackageManager pm;

    /**
     * Private contructor to construct object
     * @param source
     */
    private EnterpriseApplicationManager(ApplicationListSource source) {
        enterpriseApplications = source.getApplicationList();
        DPCApplication.getDPCComponent().inject(this);
        pm = context.getPackageManager();
    }

    /**
     * Provides an instance of the singleton class. If already created, the existing instance is
     * returned. Else, a new istance is created and returned.
     * @return Instance of EnterpriseApplicationManager
     */
    public static EnterpriseApplicationManager getInstance() {
        if(enterpriseApplicationManager == null) {
            enterpriseApplicationManager =
                    new EnterpriseApplicationManager(new StaticApplicationList());
        }
        return enterpriseApplicationManager;
    }

    /**
     * Provides a list of installed apps
     * @return List of installed apps
     */
    public List<Application> getInstalledApps() {
        List<Application> installedApps = new ArrayList<>();
        for(Application app: enterpriseApplications) {
            if(isPackageInstalled(app.getPackageName())) {
                installedApps.add(app);
            }
        }
        return installedApps;
    }

    /**
     * Provides a list of apps that are not installed
     * @return List of apps that are not installed
     */
    public List<Application> getNotInstalledApps() {
        List<Application> notInstalledApps = new ArrayList<>();
        for(Application app: enterpriseApplications) {
            if(!isPackageInstalled(app.getPackageName())) {
                notInstalledApps.add(app);
            }
        }
        return notInstalledApps;
    }


    /**
     * Check if the application with the given package name is already installed
     * @param packageName Package name of the application to be checked
     * @return If already installed, returns true. If not, returns false
     */
    private boolean isPackageInstalled(String packageName) {
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
