package com.qthibeau.dropwizard.jest;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastBundle <C extends Configuration> implements ConfiguredBundle<C> {

    /* Builder pattern private constructor */
    private MulticastBundle() {}

    public static interface ConfigurationAccessor<C extends Configuration> {
        public Collection<MulticastConfiguration> getConfiguration(C configuration);
    }

    public static class Builder<C extends Configuration> {
        MulticastBundle<C> bundle;

        public Builder() {
            this.bundle = new MulticastBundle<C>();
        }

        public Builder<C> withConfiguration(ConfigurationAccessor<C> configurationAccessor) {
            this.bundle.accessor = configurationAccessor;
            return this;
        }

        public MulticastBundle<C> build() {
            if(bundle.accessor == null) {
                throw new IllegalStateException("Cannot build MulticastBundle without configuration");
            }

            return this.bundle;
        }
    }

    private ConfigurationAccessor<C> accessor;
    private Collection<MulticastConfiguration> multicastConfigurations;
    private MulticastManager clientManager;
    private Supplier<MulticastClient> clientSupplier;

    public Supplier<MulticastClient> getClientSupplier() {
        return this.clientSupplier;
    }

    @Override
    public void run(C configuration, Environment environment) throws Exception {
        this.multicastConfigurations = this.accessor.getConfiguration(configuration);
        this.clientManager = new MulticastManager(this.multicastConfigurations);
        this.clientSupplier = clientManager::getClient;

        environment.lifecycle().manage(this.clientManager);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

}
