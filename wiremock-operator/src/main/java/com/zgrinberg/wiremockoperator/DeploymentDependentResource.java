package com.zgrinberg.wiremockoperator;

import io.fabric8.kubernetes.api.model.ExecAction;
import io.fabric8.kubernetes.api.model.ExecActionBuilder;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.javaoperatorsdk.operator.ReconcilerUtils;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.List;

public class DeploymentDependentResource extends CRUDKubernetesDependentResource<Deployment, Wiremock> {

    public DeploymentDependentResource() {
        super(Deployment.class);
    }

    @Override
    protected Deployment desired(Wiremock wiremock, Context<Wiremock> context) {
        Deployment deployment = ReconcilerUtils.loadYaml(Deployment.class, this.getClass(),"wiremock-deployment.yaml");
        ObjectMeta metadata = wiremock.getMetadata();
        WiremockSpec wiremockSpec = wiremock.getSpec();
        String postStartCommand;
        if(wiremockSpec.getStubMappings() != null && !wiremockSpec.getStubMappings().trim().equals("")) {
            postStartCommand = String.format(" sleep 2 ; curl -X POST http://localhost:%s/__admin/mappings/import -d '%s'", wiremockSpec.getServerPort(), wiremockSpec.getStubMappings());
        }
        else
        {
            postStartCommand = "echo numbed post start!";
        }
        return new DeploymentBuilder(deployment)
                  .editMetadata()
                  .withName(metadata.getName())
                  .withNamespace(metadata.getNamespace())
                  .addToLabels(metadata.getLabels())
                  .addToLabels("app.kubernetes.io/managed-by","wiremock-operator")
                  .addToLabels("app.kubernetes.io/part-of",metadata.getName())
                  .addToLabels("app",metadata.getName())
                  .endMetadata()
                  .editSpec()
                  .editSelector().addToMatchLabels("app",metadata.getName()).endSelector()
                  .withReplicas(wiremockSpec.getReplicas())
                  .editTemplate()
                  .editMetadata()
                  .addToLabels("app.kubernetes.io/managed-by","wiremock-operator")
                  .addToLabels("app.kubernetes.io/part-of",metadata.getName())
                  .addToLabels("app",metadata.getName())
                  .endMetadata()
                  .editSpec()
                  .editFirstContainer().withImage(String.format("%s:%s",wiremockSpec.getImageRegistry(),wiremockSpec.getVersion()))
                  .editLifecycle().editPostStart().withExec(new ExecActionBuilder().withCommand(List.of("/bin/sh","-c", postStartCommand)).build()).endPostStart().endLifecycle()
                  .withImagePullPolicy("IfNotPresent")
                  .editFirstPort().withName("http").withContainerPort(wiremockSpec.getServerPort()).endPort()
                  .editEnv().withName("LISTENING_PORT")


                .build();

    }
}
