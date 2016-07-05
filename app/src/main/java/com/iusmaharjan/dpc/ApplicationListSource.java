package com.iusmaharjan.dpc;

import java.util.List;

/**
 * Interface for datasource of list of applications
 */
public interface ApplicationListSource {

    List<Application> getApplicationList();

}