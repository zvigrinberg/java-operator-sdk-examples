# Wiremock Operator.

## Objectives

- Create a demo operator that will automate deployment of [Wiremock HTTP API Mock Server](https://wiremock.org/)
- It will deploy a deployment and a service, and possible an Openshift Route / Ingress Route to expose the Mock Api Server outside the cluster.
- Support only basic stub mappings through a field in the `Wiremock` custom resource

## Example Custom resource
```yaml
apiVersion: proxying.zgrinberg.com/v1
kind: Wiremock
metadata:
  name: wiremock
spec:
  replicas: 2
  imageRegistry: quay.io/zgrinber/wiremock
  version: latest  
  serverPort: 8082
  stubMappings: "{\"mappings\":[{\"request\":{\"method\":\"POST\",\"urlPath\":\"/employees\"},\"response\":{\"status\":200,\"jsonBody\":{\"employees\":{\"employee\":[{\"id\":\"1\",\"firstName\":\"Robert\",\"lastName\":\"Brownforest\",\"fullName\":\"Robert Brownforest\",\"DOB\":\"18/12/1965\",\"email\":\"RB1@exate.com\",\"photo\":\"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg\"},{\"id\":\"2\",\"firstName\":\"Rip\",\"lastName\":\"Van Winkle\",\"fullName\":\"Rip Van Winkle\",\"DOB\":\"18/01/1972\",\"email\":\"RVW1@exate.com\",\"photo\":\"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg\"}]}},\"headers\":{\"content-type\":\"application/json\"}}}]}"
```

## Configuration 

We'll use quarkus extension for Java Operator SDK.
Running the operator locally will just use the file ~/.kube/config' current-context in order to communicate with the cluster. 

application.properties
```properties
quarkus.container-image.build=true
quarkus.container-image.name=wiremock-operator
quarkus.operator-sdk.bundle.channels=alpha
# Automatically apply crd to cluster, also when they get regenerated because of a change.
quarkus.operator-sdk.crd.apply=true
quarkus.http.test-port=0
```

### TLS Configuration
If Running locally, then you should use in application.properties one of the following ( you don't need to use both of them)
```properties
#Option 1 - Trust certs , the easy solution, by default it's false.
quarkus.kubernetes-client.trust-certs=true

#Option 2 - specify path to Certificate authority and certificate of the cluster that it's connecting to.
quarkus.kubernetes-client.ca-cert-file=/path/to/ca.crt
quarkus.kubernetes-client.client-cert-file=/path/to/certificate.crt
```

In case you want to use option 2, you can extract the ca and certificate from one of the service accounts secrets in some namespace in cluster:
```shell
export SECRET=$(oc get secrets -n default | grep default-token | awk '{print $1}')
oc get secret $SECRET -o yaml | yq  '.data."ca.crt"' | base64 -d > /path/to/ca.crt
oc get secret $SECRET -o yaml | yq  '.data."service-ca.crt"' | base64 -d > /path/to/certificate.crt
```

## Deployment Methods

### Run it locally

1. Connect to desired cluster.

2. Run the operator
```shell
QUARKUS_K8_CLIENT_TRUST_CERT=true mvn quarkus:dev
```

### deploy on cluster and run using OLM

#### Prerequisites 

- Please [Download And install Operator Package manager](https://mirror.openshift.com/pub/openshift-v4/x86_64/clients/ocp/stable/opm-linux.tar.gz)
- Download and install [Openshift cli](https://mirror.openshift.com/pub/openshift-v4/x86_64/clients/ocp/stable/openshift-client-linux.tar.gz)
- Need a Podman/Docker CLI. (In case of docker, need docker engine - both Docker cli and Daemon).

1. Connect to desired cluster, and go to the desired namespace
```shell
oc project wiremock
#or if not exists 
oc new-project wiremock
```
2. Build the bundle image resources and build + push the operator image
```shell
mvn clean package -Dquarkus.container-image.build=true -Dquarkus.container-image.push=true -Dquarkus.container-image.registry=quay.io -Dquarkus.container-image.build=true -Dquarkus.container-image.name=wiremock-operator -Dquarkus.container-image.group=zgrinber -Dquarkus.kubernetes.namespace=wiremock-test -Dquarkus.operator-sdk.bundle.package-name=wiremock-operator -Dquarkus.operator-sdk.bundle.channels=alpha
```
3. Build the bundle image
```shell
make bundle-build BUNDLE_IMG=quay.io/zgrinber/wiremock-operator-bundle:0.0.1-SNAPSHOT
```

4. push the bundle image
```shell
podman push quay.io/zgrinber/wiremock-operator-bundle:0.0.1-SNAPSHOT
```

5. Define the catalog image name and bundle image name
```shell
export CATALOG_IMAGE=quay.io/zgrinber/wiremock-catalog:v1
export BUNDLE_IMG=quay.io/zgrinber/wiremock-operator-bundle:0.0.1-SNAPSHOT
```
6. build the index image/catalog image using opm tool
```shell
opm index add --bundles $BUNDLE_IMG --tag $CATALOG_IMAGE --build-tool podman
```

7. push the index/catalog image
```shell
podman push $CATALOG_IMAGE
```

8. Create the catalogsource in OLM namespace, in order to create registry pod to serve the bundle image to subscriptions to install the operator:
```shell
export OLM_NAMESPACE=openshift-marketplace
cat <<EOF | kubectl apply -f -
apiVersion: operators.coreos.com/v1alpha1
kind: CatalogSource
metadata:
  name: my-catalog-source
  namespace: $OLM_NAMESPACE
spec:
  sourceType: grpc
  image: $CATALOG_IMAGE
EOF
```

8. Create Operator group for `AllNamespaces` install mode
```shell
cat <<EOF | kubectl create -f -
apiVersion: operators.coreos.com/v1alpha2
kind: OperatorGroup
metadata:
  name: wiremock-og
  namespace: wiremock

EOF
```

9. Create Subscription to install the operator on the cluster
```shell
cat <<EOF | kubectl create -f -
apiVersion: operators.coreos.com/v1alpha1
kind: Subscription
metadata:
  name: wiremock-subscription
  namespace: wiremock
spec:
  channel: alpha
  name: wiremock-operator
  source: wiremock-catalog-source
  sourceNamespace: openshift-marketplace
EOF
```
## Test it
1. Create new project
```shell
oc new-project wiremock-test
```
2. After the operator is installed, either locally or in cluster, apply wiremock Custom resource to the cluster.
```shell
oc apply -f wiremock.yaml
```

3. Check that the pods of wiremock deployed 
```shell
oc get pods
```

Output:
```shell
NAME                        READY   STATUS    RESTARTS   AGE
wiremock-57f78b5dfc-grncq   1/1     Running   0          17s
wiremock-57f78b5dfc-trn6s   1/1     Running   0          17s
```

4. Check the status of the wiremock custom resource:
```shell
oc describe wiremock wiremock
```
**_Output:_**
```shell
Spec:
  Image Registry:  quay.io/zgrinber/wiremock
  Replicas:        2
  Server Port:     8082
  Stub Mappings:   {"mappings":[{"request":{"method":"POST","urlPath":"/employees"},"response":{"status":200,"jsonBody":{"employees":{"employee":[{"id":"1","firstName":"Robert","lastName":"Brownforest","fullName":"Robert Brownforest","DOB":"18/12/1965","email":"RB1@exate.com","photo":"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg"},{"id":"2","firstName":"Rip","lastName":"Van Winkle","fullName":"Rip Van Winkle","DOB":"18/01/1972","email":"RVW1@exate.com","photo":"https://pbs.twimg.com/profile_images/735509975649378305/B81JwLT7.jpg"}]}},"headers":{"content-type":"application/json"}}}]}
  Version:         latest
Status:
  Ready Replicas:    2
  Wiremock Address:  http://wiremock.wiremock-test:8082
```

5. Update .spec.replicas in wiremock resource to 1, and apply it to cluster
```shell
oc apply -f wiremock.yaml
```

6. Check to see that only one replica of wiremock stayed.
```shell
[zgrinber@zgrinber wiremock-operator]$ oc get pods
NAME                        READY   STATUS    RESTARTS   AGE
wiremock-57f78b5dfc-grncq   1/1     Running   0          11m
```

7. Delete the wiremock instance
```shell
oc delete wiremocks.proxying.zgrinberg.com wiremock
```
Output:
```shell
wiremock.proxying.zgrinberg.com "wiremock" deleted
```
8. See that wiremock app instance was removed
```shell
[zgrinber@zgrinber wiremock-operator]$ oc get pods
No resources found in wiremock-test namespace.
```