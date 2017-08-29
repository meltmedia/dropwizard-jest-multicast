package com.meltmedia.dropwizard.jestmulticast;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastClientTest {

    private MulticastClient client;

    @Rule
    public WireMockRule esMock = new WireMockRule(8606);

    private MulticastConfiguration generateClientConfiguration(String url, Boolean isCritical) {

        MulticastConfiguration clientConfiguration1 = new MulticastConfiguration();
        clientConfiguration1.setClusterName("elasticsearch");
        clientConfiguration1.setServers(Collections.singletonList(url));
        clientConfiguration1.setCritical(isCritical);

        return clientConfiguration1;
    }

    public MulticastClientTest() {

        List<MulticastConfiguration> configurations = new ArrayList<>();
        configurations.add(generateClientConfiguration("http://localhost:8606", true));
        configurations.add(generateClientConfiguration("http://localhost:8606", false));

        this.client = new MulticastClient.Builder()
                .withConfigurations(configurations)
                .build();
    }

    @Test
    public void searchTest() throws IOException {

        /*
         * The search function of the MulticastClient should only query the first critical database
         * and return its result. Therefore, in this scenario, there should be a single request to the mock service.
         */

        esMock.stubFor(post(urlEqualTo("/testindex/testtype/_search"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

        Search search = new Search.Builder("{ \"query\": { \"match_all\": {} } }")
                .addIndex("testindex")
                .addType("testtype")
                .build();

        this.client.execute(search);

        esMock.verify(1, postRequestedFor(urlEqualTo("/testindex/testtype/_search")));

    }

    @Test
    public void indexTest() throws IOException {
        esMock.stubFor(post(urlEqualTo("/testindex/testtype"))
                .inScenario("Index Test")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("NonCritical Index")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

        esMock.stubFor(post(urlEqualTo("/testindex/testtype"))
                .inScenario("Index Test")
                .whenScenarioStateIs("NonCritical Index")
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{}")));

        Index index = new Index.Builder(new Object())
                .index("testindex")
                .type("testtype")
                .build();

        this.client.execute(index);

        esMock.verify(2, postRequestedFor(urlEqualTo("/testindex/testtype")));
    }

    @Test
    public void multiExecuteTest() throws IOException, Exception {

        AtomicInteger operationCounter = new AtomicInteger();

        esMock.stubFor(post(urlEqualTo("/testindex/testtype/_search"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));

        this.client.multiExecute((JestClient client, Boolean isCritical) -> {
            operationCounter.incrementAndGet();

            Search search = new Search.Builder("{ \"query\": { \"match_all\": {} } }")
                    .addIndex("testindex")
                    .addType("testtype")
                    .build();

            client.execute(search);
        });

        esMock.verify(2, postRequestedFor(urlEqualTo("/testindex/testtype/_search")));
        assertThat(operationCounter.get(), is(equalTo(2)));
    }

    @Test(expected = UncheckedIOException.class)
    public void testCriticalFlag() throws IOException {
        this.client.execute(new Index.Builder(new Object()).build());
    }
    
    @Test
    public void shouldComputeRegionFromUrl() {
      String region = MulticastClient.regionFromUrl("https://example-cluster.us-west-2.es.amazonaws.com");
      assertThat(region, Matchers.equalTo("us-west-2"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIfNoRegion() {
      String value = MulticastClient.regionFromUrl("https://example-cluster.us-west-2.es.elsewhere.com");
      System.out.println(value);
    }

}
