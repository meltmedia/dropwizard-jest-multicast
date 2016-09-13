package com.meltmedia.dropwizard.jestmulticast.guice;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.meltmedia.dropwizard.jestmulticast.MulticastBundle;
import com.meltmedia.dropwizard.jestmulticast.MulticastClient;
import com.meltmedia.dropwizard.jestmulticast.MulticastConfiguration;
import io.dropwizard.Configuration;
import io.searchbox.client.JestClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by qthibeault on 9/8/16.
 */

public class MulticastModuleTest {

    private static Injector injector;

    public static class JestClientInjectedTestClass {
        @Inject
        public JestClient jestClient;
    }

    public static class MulticastClientSupplierInjectedTestClass {
        @Inject
        public Supplier<MulticastClient> multicastClientSupplier;
    }

    public static class JestClientSupplierInjectedTestClass {
        @Inject
        public Supplier<JestClient> jestClientSupplier;
    }

    public static class MulticastClientInjectedTestClass {
        @Inject
        public MulticastClient multicastClient;
    }

    @BeforeClass
    public static void setUp() throws Exception {
        MulticastConfiguration config = new MulticastConfiguration();
        config.setServers(Collections.singletonList("http://localhost:9200"));
        config.setClusterName("cluster");
        config.setCritical(false);

        MulticastClient client = new MulticastClient.Builder()
                .withConfigurations(Collections.singletonList(config))
                .build();

        MulticastBundle<?> bundle = mock(MulticastBundle.class);
        when(bundle.getClientSupplier()).thenReturn(()->client);

        MulticastModule module = new MulticastModule(bundle);
        injector = Guice.createInjector(module);
    }


    @Test
    public void testMulticastInjector() {
        MulticastClientInjectedTestClass testClass = injector.getInstance(MulticastClientInjectedTestClass.class);
        assertThat(testClass.multicastClient, is(not(nullValue())));
    }

    @Test
    public void testMJestInjector() {
        JestClientInjectedTestClass testClass = injector.getInstance(JestClientInjectedTestClass.class);
        assertThat(testClass.jestClient, is(not(nullValue())));
    }

    @Test
    public void testMulticastSupplierInjector() {
        MulticastClientSupplierInjectedTestClass testClass = injector.getInstance(MulticastClientSupplierInjectedTestClass.class);
        assertThat(testClass.multicastClientSupplier, is(not(nullValue())));
        assertThat(testClass.multicastClientSupplier.get(), is(not(nullValue())));
    }

    @Test
    public void testJestSupplierInjector() {
        JestClientSupplierInjectedTestClass testClass = injector.getInstance(JestClientSupplierInjectedTestClass.class);
        assertThat(testClass.jestClientSupplier, is(not(nullValue())));
        assertThat(testClass.jestClientSupplier.get(), is(not(nullValue())));
    }

}
