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
mvn quarkus:dev
```

### deploy on cluster
1. Connect to desired cluster.
2. Apply Operator deployments and RBAC resources. (TBD - install by OLM)

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