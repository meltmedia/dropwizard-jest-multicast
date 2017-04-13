package com.meltmedia.dropwizard.jestmulticast;

public class AwsConfiguration {
  protected String accessKey;
  protected String secretKey;

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey( String accessKey ) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey( String secretKey ) {
    this.secretKey = secretKey;
  }
}
