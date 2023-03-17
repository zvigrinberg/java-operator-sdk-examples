package com.zgrinberg;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class WiremockReconciler implements Reconciler<Wiremock> { 
  private final KubernetesClient client;

  public WiremockReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<Wiremock> reconcile(Wiremock resource, Context context) {
    // TODO: fill in logic

    return UpdateControl.noUpdate();
  }
}

