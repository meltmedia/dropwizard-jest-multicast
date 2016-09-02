package com.meltmedia.dropwizard.jestmulticast;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.searchbox.client.JestClient;

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

        public Builder<C> withHealthcheckName(String healthcheckName) {
            this.bundle.healthcheckName = healthcheckName;
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
    private String healthcheckName;
    private MulticastHealthcheck multicastHealthcheck;

    public Supplier<? extends JestClient> getClientSupplier() {
        return this.clientSupplier;
    }

    @Override
    public void run(C configuration, Environment environment) throws Exception {
        this.multicastConfigurations = this.accessor.getConfiguration(configuration);
        this.clientManager = new MulticastManager(this.multicastConfigurations);
        this.clientSupplier = clientManager::getClient;
        this.multicastHealthcheck = new MulticastHealthcheck(this.clientSupplier);

        environment.lifecycle().manage(this.clientManager);
        environment.healthChecks().register(this.healthcheckName, this.multicastHealthcheck);
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {

    }

}
