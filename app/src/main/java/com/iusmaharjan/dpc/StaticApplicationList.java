package com.iusmaharjan.dpc;

import java.util.ArrayList;
import java.util.List;

/**
 * Static source of application list
 */
public class StaticApplicationList implements ApplicationListSource {

    List<Application> applications;

    public StaticApplicationList() {
        applications = new ArrayList<>();
        applications.add(new Application("App1","com.afwsamples.testdpc","https://www.dropbox.com/s/7506st3oi2vjoiq/TestDPC_3008.apk?dl=1"));
        applications.add(new Application("App2","org.wikidata.quiz","https://www.dropbox.com/s/cqmk854asfiv7lc/wikidata-1.apk?dl=1"));
    }

    @Override
    public List<Application> getApplicationList() {
        return applications ;
    }
}
