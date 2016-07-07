package com.iusmaharjan.dpc.appinstaller;

import java.util.List;

/**
 * Interface for datasource of list of applications
 */
public interface ApplicationListSource {

    /**
     * provide list of applications from the source
     * @return list of applications
     */
    List<Application> getApplicationList();

}