package com.zgrinberg.wiremockoperator;

public class WiremockStatus {

    // Add Status information here
    private Integer readyReplicas = 0;
    private String wiremockAddress;

    public Integer getReadyReplicas() {
        return readyReplicas;
    }

    public void setReadyReplicas(Integer readyReplicas) {
        this.readyReplicas = readyReplicas;
    }

    public String getWiremockAddress() {
        return wiremockAddress;
    }

    public void setWiremockAddress(String wiremockAddress) {
        this.wiremockAddress = wiremockAddress;
    }
}
