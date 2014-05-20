raml-tester-proxy
=================

A standalone proxy that tests if requests/responses are raml compliant.

Example: If there is a service running at localhost:8080, start the proxy with

```
java -jar raml-tester-proxy.jar -p 8090 -t localhost:8080 -r file://<path to raml> -s .
```

and access the service using localhost:8090.

Every request or response that does not match the RAML definition will be logged.
