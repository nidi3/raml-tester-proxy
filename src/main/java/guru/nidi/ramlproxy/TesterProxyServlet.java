package guru.nidi.ramlproxy;

import guru.nidi.ramltester.RamlDefinition;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
public class TesterProxyServlet extends ProxyServlet.Transparent {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final RamlDefinition ramlDefinition;

    public TesterProxyServlet(String proxyTo, RamlDefinition ramlDefinition) {
        super(proxyTo, "");
        this.ramlDefinition = ramlDefinition;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final ServletRamlRequest ramlRequest = new ServletRamlRequest(request);
        final ServletRamlResponse ramlResponse = new ServletRamlResponse(response);
        super.service(ramlRequest, ramlResponse);
    }

    @Override
    protected void onResponseSuccess(HttpServletRequest request, HttpServletResponse response, Response proxyResponse) {
        super.onResponseSuccess(request, response, proxyResponse);
        test(request, response);
    }

    @Override
    protected void onResponseFailure(HttpServletRequest request, HttpServletResponse response, Response proxyResponse, Throwable failure) {
        super.onResponseFailure(request, response, proxyResponse, failure);
        test(request, response);
    }

    private void test(HttpServletRequest request, HttpServletResponse response) {
        test((ServletRamlRequest) request, (ServletRamlResponse) response);
    }

    private void test(ServletRamlRequest request, ServletRamlResponse response) {
        final RamlReport report = ramlDefinition.testAgainst(request, response);
        if (!report.isEmpty()) {
            log.error("{}\nRequest:  {}\nResponse: {}", request.getMethod() + " " + request.getRequestURL(), report.getRequestViolations(), report.getResponseViolations());
        }
    }
}
