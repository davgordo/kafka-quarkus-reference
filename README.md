# Kafka Load Testing Scenario

This is an example Kafka architecture built with Red Hat AMQ Streams components.

## Installation

Before proceding, fork this repository and replace all instances of the placeholder OpenShift cluster domain with the base domain of your cluster.

For example:

1. Export your cluster's base domain to a variable by logging in with `oc` and running this command:

   `export base_domain=$(oc get dns cluster -o jsonpath='{.spec.baseDomain}')`

2. Replace the placeholder domain in your working directory of your cloned fork:

   `find . -type f -not -path '*/\.git/*' -exec sed -i "s/example.cluster.com/${base_domain}/g" {} +`

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

## Running the Quarkus Application

1. Create a namespace for the load tester

   `oc new-project load-tester`

2. Change directory to application source directory

   `cd kafka-load-tester`

3. Build the load-tester application using s2i

   `quarkus build -Dquarkus.container-image.build=true`

4. Apply the application manifest

   `oc apply -f target/kubernetes/openshift.yml -n load-tester`