package com.meltmedia.dropwizard.jestmulticast.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.meltmedia.dropwizard.jestmulticast.MulticastBundle;
import com.meltmedia.dropwizard.jestmulticast.MulticastClient;
import io.searchbox.client.JestClient;

import java.util.function.Supplier;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastModule extends AbstractModule {

    private MulticastBundle<?> bundle;

    public MulticastModule( MulticastBundle<?> bundle ) {
        this.bundle = bundle;
    }

    @Override
    protected void configure() {
        // no op
    }

    @Provides
    @Singleton
    public JestClient provideClient() {
        return this.bundle.getClientSupplier().get();
    }

    @Provides
    @Singleton
    public Supplier<JestClient> provideClientSupplier() {
        return ()->this.bundle.getClientSupplier().get();
    }

    @Provides
    @Singleton
    public MulticastClient provideMulticastClient() {
        return this.bundle.getClientSupplier().get();
    }

    @Provides
    @Singleton
    public Supplier<MulticastClient> provideMulticastClientSupplier() {
        return this.bundle.getClientSupplier();
    }

}
