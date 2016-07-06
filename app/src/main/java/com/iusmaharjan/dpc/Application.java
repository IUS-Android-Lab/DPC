package com.iusmaharjan.dpc;

/**
 * Contains information about the apps to be downloaded
 */
public class Application {

    private String applicationName;
    private String packageId;
    private String downloadURL;

    public Application(String applicationName, String packageId, String downloadURL) {
        this.applicationName = applicationName;
        this.packageId = packageId;
        this.downloadURL = downloadURL;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public void setDownloadURL(String downloadURL) {
        this.downloadURL = downloadURL;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    public String getPackageId() {
        return packageId;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Application) && packageId.equals(((Application) obj).getPackageId());
    }
}
