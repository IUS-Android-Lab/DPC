package com.iusmaharjan.dpc.dagger;

import com.iusmaharjan.dpc.dpc.DPCPresenter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, DPCModule.class})
public interface DPCComponent {
    void inject(DPCPresenter dpcPresenter);
}
