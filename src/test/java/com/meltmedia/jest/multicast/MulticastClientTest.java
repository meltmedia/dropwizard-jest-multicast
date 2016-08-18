package com.meltmedia.jest.multicast;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import io.searchbox.core.Delete;
import io.searchbox.core.Get;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;


/**
 * Created by qthibeault on 8/18/16.
 */

public class MulticastClientTest {

    @Rule
    public WireMockRule esMock1 = new WireMockRule(8081);

    @Rule
    public WireMockRule esMock2 = new WireMockRule(8082);

    private MulticastClient client;

    @Before
    public void beforeEach() {
        client = new MulticastClient();
    }

    @Test(expected = RuntimeException.class)
    public void testSearchWithNoClients() throws RuntimeException, IOException {
        Search search = new Search.Builder("")
                .addIndex("index")
                .addType("type")
                .build();

        client.execute(search);
    }

    @Test(expected = RuntimeException.class)
    public void testIndexWithNoClients() throws RuntimeException, IOException {
        Index index = new Index.Builder(new Object())
                .index("index")
                .type("type")
                .id("1")
                .build();

        client.execute(index);
    }

    @Test
    public void testSearchOneCriticalDatabase() throws IOException {
        esMock1.stubFor(post(urlEqualTo("/index/type/_search"))
                .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(" { } ")
                    .withStatus(200)));

        client = client.addCriticalDatabase("http://localhost:8081");

        Search search = new Search.Builder("")
                .addIndex("index")
                .addType("type")
                .build();

        client.execute(search);
        esMock1.verify(1, postRequestedFor(urlEqualTo("/index/type/_search")));
    }

    @Test
    public void testIndexOneCriticalDatabase() throws IOException {
        esMock1.stubFor(put(urlEqualTo("/index/type/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(" { } ")
                        .withStatus(200)));

        Index index = new Index.Builder(new Object())
                .index("index")
                .type("type")
                .id("1")
                .build();

        client = client.addCriticalDatabase("http://localhost:8081");
        client.execute(index);

        esMock1.verify(1, putRequestedFor(urlEqualTo("/index/type/1")));
    }

    @Test(expected = IOException.class)
    public void testFailedCriticalOperation() throws IOException {
        esMock1.stubFor(put(urlEqualTo("/index/type/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(" { } ")
                        .withStatus(200)));

        client = client.addCriticalDatabase("http://localhost:8081", "http://localhost:8082");
        Index index = new Index.Builder(new Object())
                .index("index")
                .type("type")
                .id("1")
                .build();

        client.execute(index);
    }

    @Test
    public void testFailedNonCriticalOperation() throws IOException {
        esMock1.stubFor(put(urlEqualTo("/index/type/1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(" { } ")
                        .withStatus(200)));

        client = client.addCriticalDatabase("http://localhost:8081")
                .addNonCriticalDatabase("http://localhost:8082");

        Index index = new Index.Builder(new Object())
                .index("index")
                .type("type")
                .id("1")
                .build();

        client.execute(index);
    }


}
