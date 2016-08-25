package com.qthibeau.dropwizard.jest.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.qthibeau.dropwizard.jest.MulticastBundle;
import com.qthibeau.dropwizard.jest.MulticastClient;

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
    public MulticastClient provideClient() {
        return this.bundle.getClientSupplier().get();
    }

    @Provides
    @Singleton
    public Supplier<MulticastClient> provideClientSupplier() {
        return this.bundle.getClientSupplier();
    }

}
