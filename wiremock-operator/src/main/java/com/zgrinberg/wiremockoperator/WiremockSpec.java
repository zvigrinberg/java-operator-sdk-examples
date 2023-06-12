package com.zgrinberg.wiremockoperator;

import java.util.List;

public class WiremockSpec {

    private Integer replicas;
    private String version;
    private String imageRegistry;

    private Integer serverPort;

    private String stubMappings;
    private List<String> stubMappingsList;


    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getImageRegistry() {
        return imageRegistry;
    }

    public void setImageRegistry(String imageRegistry) {
        this.imageRegistry = imageRegistry;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getStubMappings() {
        return stubMappings;
    }

    public void setStubMappings(String stubMappings) {
        this.stubMappings = stubMappings;
    }

    public List<String> getStubMappingsList() {
        return stubMappingsList;
    }

    public void setStubMappingsList(List<String> stubMappingsList) {
        this.stubMappingsList = stubMappingsList;
    }
}
