package com.zgrinberg;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("proxying.zgrinberg.com")
public class InterceptorService extends CustomResource<InterceptorServiceSpec, InterceptorServiceStatus> implements Namespaced {}

