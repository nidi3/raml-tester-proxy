raml-tester-proxy [![Build Status](https://travis-ci.org/nidi3/raml-tester-proxy.svg?branch=master)](https://travis-ci.org/nidi3/raml-tester-proxy)
=================
A standalone proxy/mock that tests if requests/responses are compliant with a given RAML.

##Usage as proxy
Example: If there is a service running at localhost:8080, start the proxy with

```
java -jar raml-tester-proxy.jar -p8090 -t localhost:8080 -r file://<path to raml> -s.
```

and access the service using localhost:8090.

Every request or response that does not match the RAML definition will be logged.

##Usage as Mock
Instead of forwarding all requests to another server, the tool can also be used to return mock responses.
(Which will also be verified against a RAML file.)
Use the -m option instead of -t to run in mock mode:

```
java -jar raml-tester-proxy.jar -p8090 -m mock-data -r file://<path to raml> -s.
```

The files in directory `mock-data` will be used as responses.

- A request to `localhost:8090/admin/user/` will be responded with any of the files `mock-data/admin/user.{json|xml|txt}`,
if one exists.
- Responses can be specialized by prepending the HTTP method:
A GET request will first search for `mock-data/admin/GET-user.json` and then for the general `mock-data/admin/user.json`.
