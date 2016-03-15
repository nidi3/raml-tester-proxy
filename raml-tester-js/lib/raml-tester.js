const fs = require('fs'),
    http = require('http'),
    children = require('child_process'),
    bin = require('path').resolve(__dirname + '/../bin');

function server(args, out) {
    const params = '-jar ' + bin + '/raml-tester-standalone.jar' + (args ? ' ' : '') + args,
        proc = children.spawn('java', params.split(' '));

    proc.stdout.on('data', function (data) {
        out(('' + data).substring(0, data.length - 1));
    });
    proc.stderr.on('data', function (data) {
        out(('' + data).substring(0, data.length - 1));
    });
}

module.exports = {
    help: function (out) {
        server('', out);
    },
    start: function (opts, out) {
        server(renderServerCli(opts), out);
    },
    stop: function (opts, ok, fail) {
        this.command('stop', opts, ok, fail);
    },
    ping: function (opts, ok, fail) {
        this.command('ping', opts, ok, fail);
    },
    reload: function (opts, ok, fail) {
        this.command('reload', opts, ok, fail);
    },
    reports: function (opts, ok, fail) {
        this.command('reports', opts, ok, fail);
    },
    usage: function (opts, ok, fail) {
        this.command('usage', opts, ok, fail);
    },
    /**
     *
     * @param {string} [url=opts.command]
     * @param {ClientOpts} opts
     * @param {function} ok
     * @param {function} fail
     */
    command: function (url, opts, ok, fail) {
        if (arguments.length < 4) {
            fail = ok;
            ok = opts;
            opts = url;
            url = opts.command;
        }
        const req = http.request({
            port: opts.port,
            path: '/@@@proxy/' + url + '?' + (opts.clearReports ? 'clear-reports=true' : '') + (opts.clearUsage ? '&clear-usage=true' : '')
        }, function (res) {
            res.on('data', function (chunk) {
                ok('' + chunk);
            });
        });
        req.on('error', function (e) {
            fail(e);
        });
        req.end();
    },
    parseCli: function (args) {
        if (args.length === 0) {
            return {mode: 'none'};
        }
        return args[0].substring(0, 1) === '-' ? parseServerCli(args) : parseClientCli(args);
    }

};

function parseOpts(args, parseFunc) {
    for (var i = 0; i < args.length; i++) {
        var arg = args[i], opt = arg.substring(1, 2), val = arg.substring(2);
        if (arg.substring(0, 1) === '-') {
            parseFunc(opt, val);
        }
    }
}
function parseServerCli(args) {
    const res = {
        mode: 'server',
        port: 8099,
        format: 'text',
        ignoreX: false,
        async: false,
        minDelay: 0,
        maxDelay: 0
    };
    parseOpts(args, function (opt, val) {
        switch (opt) {
        case 'p':
            res.port = parseInt(val);
            break;
        case 't':
            res.target = val;
            break;
        case 'm':
            res.mockDir = val ? val : 'mock-files';
            break;
        case 'r':
            res.raml = val;
            break;
        case 'b':
            res.baseUri = val;
            break;
        case 's':
            res.saveDir = val;
            break;
        case 'f':
            res.format = val;
            break;
        case 'i':
            res.ignoreX = true;
            break;
        case 'a':
            res.async = true;
            break;
        case 'd':
            const pos = val.indexOf('-');
            res.maxDelay = parseInt(val.substring(pos + 1));
            if (pos < 0) {
                res.minDelay = res.maxDelay;
            } else {
                res.minDelay = parseInt(val.substring(0, pos));
            }
            break;
        }
    });
    return res;
}
function parseClientCli(args) {
    const res = {
        mode: 'client',
        port: 8099,
        command: args[0],
        clearReports: false,
        clearUsage: false
    };
    parseOpts(args, function (opt, val) {
        switch (opt) {
        case 'r':
            res.clearReports = true;
            break;
        case 'u':
            res.clearUsage = true;
            break;
        }
    });
    return res;
}

function renderServerCli(opts) {
    var res = '';
    for (var prop in opts) {
        var val = opts[prop];
        switch (prop) {
        case 'port':
            res += ' -p' + val;
            break;
        case 'target':
            res += ' -t' + val;
            break;
        case 'mockDir':
            res += ' -m' + val;
            break;
        case 'raml':
            res += ' -r' + val;
            break;
        case 'baseUri':
            res += ' -b' + val;
            break;
        case 'saveDir':
            res += ' -s' + val;
            break;
        case 'format':
            res += ' -f' + val;
            break;
        case 'ignoreX':
            if (val) {
                res += ' -i';
            }
            break;
        case 'async':
            if (val) {
                res += ' -a';
            }
            break;
        case 'minDelay':
            res += ' -d' + val + '-' + opts.maxDelay;
            break;
        case 'maxDelay':
            //handled in minDelay
            break;
        }

    }
    return res.substring(1);
}