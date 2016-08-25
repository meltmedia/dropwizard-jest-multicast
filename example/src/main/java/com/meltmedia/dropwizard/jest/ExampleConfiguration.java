package com.meltmedia.dropwizard.jest;

import io.dropwizard.Configuration;

import java.util.List;

/**
 * Created by qthibeault on 8/25/16.
 */

public class ExampleConfiguration extends Configuration {

    private String template;
    private List<MulticastConfiguration> elasticsearch;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<MulticastConfiguration> getElasticsearch() {
        return elasticsearch;
    }

    public void setElasticsearch(List<MulticastConfiguration> elasticsearch) {
        this.elasticsearch = elasticsearch;
    }

}
