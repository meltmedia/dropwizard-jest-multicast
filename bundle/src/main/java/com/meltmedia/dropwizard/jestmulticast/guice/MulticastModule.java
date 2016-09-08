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

    protected Supplier<MulticastClient> clientSupplier;

    public MulticastModule( MulticastBundle<?> bundle ) {
        this.clientSupplier = bundle.getClientSupplier();
    }

    public MulticastModule() {
        this.clientSupplier = null;
    }

    @Override
    protected void configure() {
        // no op
    }

    @Provides
    @Singleton
    public JestClient provideClient() {
        return this.clientSupplier.get();
    }

    @Provides
    @Singleton
    public Supplier<JestClient> provideClientSupplier() {
        return ()->this.clientSupplier.get();
    }

    @Provides
    @Singleton
    public MulticastClient provideMulticastClient() {
        return this.clientSupplier.get();
    }

    @Provides
    @Singleton
    public Supplier<MulticastClient> provideMulticastClientSupplier() {
        return this.clientSupplier;
    }

}
