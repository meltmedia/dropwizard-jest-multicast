package com.meltmedia.dropwizard.jest;

import com.meltmedia.dropwizard.jest.resources.ExampleResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by qthibeault on 8/25/16.
 */

public class ExampleApplication extends Application<ExampleConfiguration> {

    MulticastBundle<ExampleConfiguration> bundle;

    public static void main( String[] args ) throws Exception {
        new ExampleApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.addBundle(
                bundle = new MulticastBundle.Builder<ExampleConfiguration>()
                    .withConfiguration(ExampleConfiguration::getElasticsearch)
                    .build()
        );
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) {
        ExampleResource resource = new ExampleResource(bundle.getClientSupplier());
        environment.jersey().register(resource);
    }

}
