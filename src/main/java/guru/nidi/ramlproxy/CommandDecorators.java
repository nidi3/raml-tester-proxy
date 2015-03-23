package guru.nidi.ramlproxy;

import org.eclipse.jetty.client.api.Request;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
enum CommandDecorators {
    IGNORE_COMMANDS("X-Ignore-Commands", true),
    CLEAR_REPORTS("clear-reports", false),
    CLEAR_USAGE("clear-usage", false);

    private final String name;
    private final boolean header;

    private CommandDecorators(String name, boolean header) {
        this.name = name;
        this.header = header;
    }

    public boolean isSet(HttpServletRequest request) {
        final String value;
        if (header) {
            value = request.getHeader(name);
        } else {
            value = request.getParameter(name);
        }
        return value != null && !value.equalsIgnoreCase("false");
    }

    public void removeFrom(Request proxyRequest) {
        proxyRequest.getHeaders().remove(name);
    }

    public String getName() {
        return name;
    }
}
