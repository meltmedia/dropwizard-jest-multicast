package com.meltmedia;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.*;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by qthibeault on 8/18/16.
 */

public class Client {
    private ArrayList<JestClient> criticalClients;
    private ArrayList<JestClient> nonCriticalClients;

    public Client() {
        this.criticalClients = new ArrayList<JestClient>();
        this.nonCriticalClients = new ArrayList<JestClient>();
    }

    private void addClient(String url, ArrayList<JestClient> clients) {
        HttpClientConfig config = new HttpClientConfig.Builder(url)
                .multiThreaded(true)
                .build();

        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(config);

        clients.add(factory.getObject());
    }

    private Boolean hasClient() {
        return criticalClients.size() > 0 || nonCriticalClients.size() > 0;
    }

    private JestClient selectClient() {
        JestClient client;

        if(criticalClients.size() < 1) {
            if(nonCriticalClients.size() < 1) {
                throw new RuntimeException("No urls provided");
            }
            else {
                client = nonCriticalClients.get(0);
            }
        }
        else {
            client = criticalClients.get(0);
        }

        return client;
    }

    public Client addCriticalDatabase(String... urls) {
        for(String url : urls) {
            this.addClient(url, criticalClients);
        }
        return this;
    }

    public Client addNonCriticalDatabase(String... urls) {
        for(String url: urls) {
            this.addClient(url, nonCriticalClients);
        }
        return this;
    }

    public SearchResult execute(Search search) throws IOException {
        return this.selectClient()
                .execute(search);
    }

    public JestResult execute(Get get) throws IOException {
        return this.selectClient()
                .execute(get);
    }

    public void execute(Index index) throws IOException {
        if(!hasClient()) {
            throw new RuntimeException("Cannot execute index query without providing client");
        }

        for(JestClient client : criticalClients) {
            client.execute(index);
        }

        for(JestClient client : nonCriticalClients) {
            try {
                client.execute(index);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void execute(Delete delete) throws IOException {
        if(!hasClient()) {
            throw new RuntimeException("Cannot execute index query without providing client");
        }

        for(JestClient client : criticalClients) {
            client.execute(delete);
        }

        for(JestClient client : nonCriticalClients) {
            try {
                client.execute(delete);
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
