# Kafka Quarkus Reference

This is an example Kafka architecture built with Red Hat AMQ Streams components.

Included in this example:
- Persistent Kafka cluster with SCRAM-SHA, mTLS, and OAuth listeners
- Apicurio Registry that requires authentication for writes
- Keycloak instance, realm, and clients required for OAuth support
- An example Quarkus application that uses Avro schemas for Kafka messages

## Installation

Before proceding, fork this repository and replace all instances of the placeholder OpenShift cluster domain with the base domain of your cluster.

For example:

1. Export your cluster's base domain to a variable by logging in with `oc` and running this command:

   `export base_domain=$(oc get dns cluster -o jsonpath='{.spec.baseDomain}')`

2. Replace the placeholder domain in your working directory of your cloned fork:

   `find . -type f -not -path '*/\.git/*' -exec sed -i "s/cluster-m4646.m4646.sandbox2400.opentlc.com/${base_domain}/g" {} +`

### Provision Cert Manager

1. Install the cert-manager operator for all namespaces

2. Apply the certificate issuer resource instances

   `oc apply -f openshift-cert-manager/ -n openshift-cert-manager`

### Provision the Identity Provider

1. Create a namespace for Keycloak

   `oc new-project keycloak`

2. Install the Single Sign-On operator the `keycloak` namespace

3. Apply the Keycloak resource instances

   `oc apply -f keycloak/ -n keycloak`

### Provision the Kafka Cluster and Schema Registry

1. Create a namespace for Kafka
   
   `oc new-project kafka`

2. Install the AMQ Streams operator for all namespaces

3. Install the Service Registry Operator for all namespaces

4. Apply Kafka and ApicurioRegistry resource instances

   `oc apply -f kafka/ -n kafka`

## Build and deploy the Apicurio Registry Content Sync version

1. Create a Keycloak client for the content-sync instance

   `oc apply -f keycloak/KeycloakClient_registry-kube-sync.yaml -n keycloak`

2. Create an s2i build configuration to create an image from the oauth feature branch

   `oc apply -f kafka/registry-content-sync/ImageStream_openjdk-11.yaml -f kafka/registry-content-sync/ImageStream_apicurio-registry-kube-sync.yaml -f kafka/registry-content-sync/BuildConfig_apicurio-registry-kube-sync.yaml -n kafka`

3. Start a build to publish the image to the local registry

   `oc start-build apicurio-registry-kube-sync -n kafka --follow`

4. Add the custom resource definition for artifacts to the cluster

   `oc apply -f kafka/registry-content-sync/CustomResourceDefinition_artifact.yaml`

5. Deploy the content-sync instance

   `oc apply -f kafka/registry-content-sync/Role_apicurio-registry-kube-sync.yaml -f kafka/registry-content-sync/ServiceAccount_apicurio-registry-kube-sync.yaml -f kafka/registry-content-sync/RoleBinding_apicurio-registry-kube-sync.yaml -f kafka/registry-content-sync/Deployment_apicurio-registry-kube-sync.yaml -n kafka`

6. Test adding an `Artifact` custom resource instance.

   `oc apply -f kafka/registry-content-sync/Artifact_movie.yaml -n kafka`


## Build and deploy the Quarkus Application

1. Create a namespace for the load tester

   `oc new-project load-tester`

2. Change directory to application source directory

   `cd load-tester`

3. Build the load-tester application using s2i

   `quarkus build -Dquarkus.container-image.build=true`

4. Apply the application manifest

   `oc apply -f target/kubernetes/openshift.yml -n load-tester`
   
## Run a test

1. Invoke a test

   ```
   curl -v --header "Content-Type: application/json" \
        --request POST \
        --data '{"id":"test-0001","messages":3}' \
        http://demo-tester-load-tester.apps.$base_domain/test
   ```

2. View the test status

   `curl http://demo-tester-load-tester.apps.$base_domain/test`