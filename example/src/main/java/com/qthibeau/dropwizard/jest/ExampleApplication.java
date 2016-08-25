package com.qthibeau.dropwizard.jest;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * Created by qthibeault on 8/25/16.
 */
public class ExampleApplication extends Application<ExampleConfiguration> {

    public static void main( String[] args ) throws Exception {
        new ExampleApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {

    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) {

    }

}
