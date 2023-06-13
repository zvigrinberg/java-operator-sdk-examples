package com.zgrinberg.wiremockoperator;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceStatus;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.dependent.Dependent;
import io.javaoperatorsdk.operator.processing.event.source.EventSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@ControllerConfiguration(
        dependents = {
            @Dependent(type = DeploymentDependentResource.class),
            @Dependent(type = ServiceDependentResource.class)
        })
public class WiremockReconciler implements Reconciler<Wiremock>{
  private final KubernetesClient client;

  public WiremockReconciler(KubernetesClient client) {
    this.client = client;
  }


  private final Logger log = LoggerFactory.getLogger(getClass());

  @Override
  public UpdateControl<Wiremock> reconcile(Wiremock wiremock, Context context) {

    Deployment deployment = context.getSecondaryResource(Deployment.class).isPresent() ? (Deployment) context.getSecondaryResource(Deployment.class).get() : null;
    Service service = context.getSecondaryResource(Service.class).isPresent() ? (Service) context.getSecondaryResource(Service.class).get() : null;
    if(deployment == null || service == null)
    {
     return UpdateControl.noUpdate();
    }
    else {
      Wiremock updateWiremock = updateWiremockStatus(wiremock, deployment, service);
      log.info("Updating status of Wiremock {} in namespace {} to {} ready Replicas, and address = {}",
              wiremock.getMetadata().getName(),
              wiremock.getMetadata().getNamespace(),
              wiremock.getStatus().getReadyReplicas(),
              wiremock.getStatus().getWiremockAddress());
      return UpdateControl.patchStatus(updateWiremock);
    }

  }

  private Wiremock updateWiremockStatus(Wiremock wiremock, Deployment deployment, Service service) {
    DeploymentStatus deploymentStatus = Objects.requireNonNull(deployment.getStatus(), (Supplier<String>) new DeploymentStatus());
    String fullWiremockAddress = String.format("http://%s.%s:%s",service.getMetadata().getName(),service.getMetadata().getNamespace(),service.getSpec().getPorts().get(0).getPort());
    WiremockStatus wiremockStatus = new WiremockStatus();
    wiremockStatus.setReadyReplicas(deploymentStatus.getReadyReplicas());
    wiremockStatus.setWiremockAddress(fullWiremockAddress);
    wiremock.setStatus(wiremockStatus);
    return wiremock;
  }


}

