raml-tester-proxy [![Build Status](https://travis-ci.org/nidi3/raml-tester-proxy.svg?branch=master)](https://travis-ci.org/nidi3/raml-tester-proxy)
=================
A standalone proxy/mock that tests if requests/responses are compliant with a given RAML.

To see all options, execute

```
java -jar raml-tester-standalone.jar
```

##Usage as proxy
Example: If there is a service running at localhost:8080, start the proxy with

```
java -jar raml-tester-standalone.jar -t localhost:8080 -r file://<path to raml> -s.
```

and access the service using localhost:8099.

Every request or response that does not match the RAML definition will be logged.

##Run asynchronously
If the proxy is started with the -a flag, it runs asynchronously.
You can send commands to it the following way:

```
java -jar raml-tester-standalone.jar <command>
```

These commands are supported:

- ping: Ping the proxy
- stop: Stop the proxy
- reload: Reload the RAML file
- validate: Validate the RAML file
- reports: Get the reports of the RAML violations
- usage: Get information about usage of RAML elements

See also [the RAML definition](raml-tester-client/src/main/resources/proxy.raml).

##Usage as Mock
Instead of forwarding all requests to another server, the tool can also be used to return mock responses.
(Which will also be verified against a RAML file.)
Use the -m option instead of -t to run in mock mode:

```
java -jar raml-tester-standalone.jar -m mock-data -r file://<path to raml> -s.
```

The files in directory `mock-data` will be used as responses.

- A request to `localhost:8099/admin/user/` will be responded with any of the files `mock-data/admin/user.{json|xml|txt}`,
if one exists.
- Responses can be specialized by prepending the HTTP method:
A GET request will first search for `mock-data/admin/GET-user.json` and then for the general `mock-data/admin/user.json`.
- If a file named `mock-data/admin/META-user.json` exists, it is used to define response code and reponse headers.
An example file looks like `{ "code": 202, "headers": {"X-meta": "get!"} }`.
- If no matching file for a request is found, `RESPONSE.json` is searched up the directory structure and used if found.
`mock-data/RESPONSE.json` would be used whenever no exact matching file is found. The same is true for `META-` files.

##Usage from Javascript
There is a special support for usage from javascript.

See [raml-tester-js](https://github.com/nidi3/raml-tester-js) for details and
[raml-tester-uc-js](https://github.com/nidi3/raml-tester-uc-js) for examples.