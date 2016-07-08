package com.iusmaharjan.dpc.appinstaller;

/**
 * Contains information about the apps to be downloaded
 */
public class Application {

    /**
     * Name of the application
     */
    private String applicationName;

    /**
     * Package name of the application
     */
    private String packageName;

    /**
     * URL where the apk is hosted
     */
    private String downloadURL;

    /**
     * Construct to initialize provided fields
     * @param applicationName name of the application
     * @param packageName package name of the application
     * @param downloadURL url where app is hosted
     */
    public Application(String applicationName, String packageName, String downloadURL) {
        this.applicationName = applicationName;
        this.packageName = packageName;
        this.downloadURL = downloadURL;
    }

    /**
     * Get the name of the application
     * @return name of the application
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Get the download URL
     * @return URL where app is hosted
     */
    public String getDownloadURL() {
        return downloadURL;
    }

    /**
     * Get the package name
     * @return name of the package
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Compares if the provided object is {@link Application} with same {@link Application#packageName}
     * @param obj Object to be compared
     * @return If the provided object is application with same package name, return true. Else false
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Application) && packageName.equals(((Application) obj).getPackageName());
    }
}
