var RAML = RAML || {};
RAML.tester = RAML.tester || (function () {
    var port = 8099;

    function request(url, options, ok, fail) {
        var http = new XMLHttpRequest();
        options = options || {};
        options.method = options.method || 'GET';
        options.timeout = options.timeout || 1000;

        http.timeout = options.timeout;
        http.onreadystatechange = function () {
            if (http.readyState === 4) {
                if (200 <= http.status && http.status < 300) {
                    ok(http.responseText);
                } else {
                    !fail || fail(http);
                }
            }
        };
        http.open(options.method, 'http://localhost:' + port + '/@@@proxy/' + url, true);
        http.send();
    }

    function trim(s, maxLen) {
        return s.length > maxLen ? (s.substring(0, maxLen - 3) + '...') : s;
    }

    return {
        setPort: function (p) {
            port = p;
        },
        reload: function (ok, fail) {
            request('reload?clear-reports=true&clear-usage=true', {}, ok, fail);
        },
        clearReports: function (ok, fail) {
            request('ping?clear-reports=true', {}, ok, fail);
        },
        clearUsage: function (ok, fail) {
            request('ping?clear-usage=true', {}, ok, fail);
        },
        ping: function (ok, fail) {
            request('ping', {}, ok, fail);
        },
        reports: function (ok, fail) {
            request('reports', {}, function (res) {
                ok(JSON.parse(res));
            }, fail);
        },
        usage: function (ok, fail) {
            request('usage', {}, function (res) {
                ok(JSON.parse(res));
            }, fail);
        },
        request: function (url, options, ok, fail) {
            request(url, options, ok, fail);
        },
        reportToString: function (report, tab, elements) {
            var res = '';
            for (var j = 0; j < elements.length; j++) {
                res += tab + elements[j] + ': ' + trim(JSON.stringify(report[elements[j]]), 400) + '\n';
            }
            return res;
        },
        dirtyReports: function (reports) {
            var res = '';
            if (reports !== undefined) {
                for (var i = 0; i < reports.length; i++) {
                    var report = reports[i];
                    if (report['requestViolations'].length > 0 || report['responseViolations'].length > 0) {
                        res += '\nRequest number ' + (i + 1) + '\n' + RAML.tester.reportToString(report, '  ', ['request', 'requestHeaders']);
                        res += '\nCaused the following violations:\n' + RAML.tester.reportToString(report, '  ', ['requestViolations', 'responseViolations']) + '\n';
                    }
                }
            }
            return res;
        },
        unusedElements: function (usage) {
            var i, msg = '',
                args = (arguments.length === 1)
                    ? ['', 'resources', 'actions', 'requestHeaders', 'queryParameters', 'formParameters', 'responseHeaders', 'responseCodes']
                    : Array.prototype.slice.call(arguments);
            for (i = 1; i < args.length; i++) {
                if (usage.unused[args[i]] !== undefined) {
                    msg += '  ' + args[i] + ': ' + JSON.stringify(usage.unused[args[i]]) + '\n';
                }
            }
            return msg;
        },
        addJasmineMatchers: function () {
            jasmine.addMatchers({
                    toHaveNoViolations: function (util, customEqualityTesters) {
                        return {
                            compare: function (actual) {
                                var dirty = RAML.tester.dirtyReports(actual);
                                return {message: dirty, pass: dirty === ''};
                            },
                            negativeCompare: function (actual) {
                                return {message: "'Not' not supported."};
                            }
                        };
                    },
                    toBeFullyUsed: function (util, customEqualityTesters) {
                        return {
                            compare: function (actual) {
                                var i, msg;
                                if (!actual) {
                                    msg = 'Expected full usage, but not a single request registered.';
                                } else {
                                    msg = RAML.tester.unusedElements.apply(RAML.tester, arguments);
                                    if (msg !== '') {
                                        msg = '\nExpected no unused elements, but found these:\n' + msg;
                                    }
                                }
                                return {message: msg, pass: msg === ''};
                            },
                            negativeCompare: function (actual) {
                                return {message: "'Not' not supported."};
                            }
                        };
                    }
                }
            );
        }
    };
}());

