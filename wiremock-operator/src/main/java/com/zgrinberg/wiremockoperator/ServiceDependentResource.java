package com.zgrinberg.wiremockoperator;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.List;
import java.util.Map;

public class ServiceDependentResource extends CRUDKubernetesDependentResource<Service, Wiremock> {

    public ServiceDependentResource() {
        super(Service.class);
    }

    @Override
    protected Service desired(Wiremock wiremock, Context<Wiremock> context) {
        Service service = ReconcilerUtils.loadYaml(Service.class, this.getClass(),"wiremock-service.yaml");
        ObjectMeta metadata = wiremock.getMetadata();
        WiremockSpec wiremockSpec = wiremock.getSpec();

        return new ServiceBuilder(service)
                  .editMetadata()
                  .withName(metadata.getName())
                  .withNamespace(metadata.getNamespace())
                  .addToLabels(metadata.getLabels())
                  .addToLabels("app.kubernetes.io/managed-by","wiremock-operator")
                  .addToLabels("app.kubernetes.io/part-of",metadata.getName())
                  .addToLabels("app",metadata.getName())
                  .endMetadata()
                  .editSpec()
                  .withSelector(Map.of("app",metadata.getName()))
                  .editFirstPort().withProtocol("TCP").withPort(wiremockSpec.getServerPort()).withTargetPort(new IntOrString(wiremockSpec.getServerPort()))
                  .endPort()
                  .endSpec()
                  .build();

    }
}
