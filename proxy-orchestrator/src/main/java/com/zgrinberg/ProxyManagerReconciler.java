package com.zgrinberg;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class ProxyManagerReconciler implements Reconciler<ProxyManager> { 
  private final KubernetesClient client;

  public ProxyManagerReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<ProxyManager> reconcile(ProxyManager resource, Context context) {
    // TODO: fill in logic

    return UpdateControl.noUpdate();
  }
}

