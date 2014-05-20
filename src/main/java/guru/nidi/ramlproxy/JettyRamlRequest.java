package guru.nidi.ramlproxy;

import guru.nidi.ramltester.core.RamlRequest;
import guru.nidi.ramltester.util.ParameterValues;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.util.Fields;

import java.util.Map;

/**
 *
 */
public class JettyRamlRequest{}// implements RamlRequest {
//    private final Request request;
//    private final String servletUri;
//
//    public JettyRamlRequest(Request request, String servletUri) {
//        this.request = request;
//        this.servletUri = servletUri;
//    }
//
//    @Override
//    public String getRequestUrl() {
//        return servletUri == null
//                ? request.getURI().toString()
//                : servletUri + request.getPath();
//    }
//
//    @Override
//    public String getMethod() {
//        return request.getMethod();
//    }
//
//    @Override
//    public Map<String, String[]> getParameterMap() {
//        final ParameterValues params = new ParameterValues();
//        for (Fields.Field field : request.getParams()) {
//            params.addValue(field.getName(), field.getValue());
//        }
//        return params.getValues();
//    }
//}
