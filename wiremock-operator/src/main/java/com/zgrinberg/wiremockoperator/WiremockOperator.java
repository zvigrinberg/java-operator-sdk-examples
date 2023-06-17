package com.zgrinberg.wiremockoperator;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.javaoperatorsdk.operator.Operator;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;

import jakarta.inject.Inject;

@QuarkusMain
public class WiremockOperator implements QuarkusApplication {
    @Inject
    Operator operator;

    public static void main(String... args) {
        Quarkus.run(WiremockOperator.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        KubernetesClient client = new KubernetesClientBuilder().build();
//        operator.register(new WiremockReconciler(client));
        operator.start();
        Quarkus.waitForExit();
        return 0;
    }
}
