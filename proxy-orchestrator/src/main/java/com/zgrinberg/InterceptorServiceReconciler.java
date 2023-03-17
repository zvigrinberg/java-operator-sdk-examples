package com.zgrinberg;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

public class InterceptorServiceReconciler implements Reconciler<InterceptorService> { 
  private final KubernetesClient client;

  public InterceptorServiceReconciler(KubernetesClient client) {
    this.client = client;
  }

  // TODO Fill in the rest of the reconciler

  @Override
  public UpdateControl<InterceptorService> reconcile(InterceptorService resource, Context context) {
    // TODO: fill in logic

    return UpdateControl.noUpdate();
  }
}

