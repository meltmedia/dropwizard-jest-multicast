package com.meltmedia.dropwizard.jestmulticast;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

/**
 * Created by qthibeault on 8/25/16.
 */

public class MulticastConfiguration {

    private String clusterName;
    private List<String> servers;
    private Integer connectionTimeout = 30000;
    private Integer readTimeout = 30000;
    private Integer maxTotalConnections = 1;
    private Integer maxTotalConnectionsPerRoute = 1;
    private Boolean critical = false;
    private AwsConfiguration aws;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public MulticastConfiguration withClusterName(String clusterName) {
        this.clusterName = clusterName;
        return this;
    }

    public List<String> getServers() {
        return servers;
    }

    public void setServers(List<String> servers) {
        this.servers = servers;
    }
    
    public MulticastConfiguration withServers(List<String> servers) {
      this.servers = servers;
      return this;
    }
    
    public MulticastConfiguration withServers(String... servers ) {
      this.servers = Lists.newArrayList(servers);
      return this;
    }

    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public MulticastConfiguration withConnectionTimeout(Integer connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
        return this;
    }

    public Integer getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
    }

    public MulticastConfiguration withReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public Integer getMaxTotalConnections() {
        return maxTotalConnections;
    }

    public void setMaxTotalConnections(Integer maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
    }

    public MulticastConfiguration withMaxTotalConnections(Integer maxTotalConnections) {
        this.maxTotalConnections = maxTotalConnections;
        return this;
    }

    public Integer getMaxTotalConnectionsPerRoute() {
        return maxTotalConnectionsPerRoute;
    }

    public void setMaxTotalConnectionsPerRoute(Integer maxTotalConnectionsPerRoute) {
        this.maxTotalConnectionsPerRoute = maxTotalConnectionsPerRoute;
    }

    public MulticastConfiguration withMaxTotalConnectionsPerRoute(Integer maxTotalConnectionsPerRoute) {
        this.maxTotalConnectionsPerRoute = maxTotalConnectionsPerRoute;
        return this;
    }

    public Boolean isCritical() {
        return critical;
    }

    public void setCritical(Boolean critical) {
        this.critical = critical;
    }

    public MulticastConfiguration withCritical(Boolean critical) {
        this.critical = critical;
        return this;
    }

    public AwsConfiguration getAws() {
      return aws;
    }

    public void setAws( AwsConfiguration awsConfiguration ) {
      this.aws = awsConfiguration;
    }
}
