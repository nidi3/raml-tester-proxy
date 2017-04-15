/*
 * Copyright © 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramlproxy.report;

import guru.nidi.ramltester.MultiReportAggregator;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.core.ReportAggregator;
import guru.nidi.ramltester.model.Values;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportSaver {
    private final Map<String, List<ReportInfo>> reports = new HashMap<>();
    private final ReportAggregator aggregator;

    public ReportSaver() {
        this(new MultiReportAggregator());
    }

    public ReportSaver(ReportAggregator aggregator) {
        this.aggregator = aggregator;
    }

    public final synchronized void addReport(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        addingReport(report, request, response);
        aggregator.addReport(report);
        getOrCreateInfos(report.getRaml().getTitle()).add(new ReportInfo(report, request, response));
    }

    public final synchronized void flushReports() {
        flushingReports(reports.entrySet());
        reports.clear();
    }

    public final void flushUsage() {
        flushingUsage(aggregator);
        aggregator.clear();
    }

    protected void addingReport(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
    }

    protected void flushingReports(Iterable<Map.Entry<String, List<ReportInfo>>> reports) {
    }

    protected void flushingUsage(ReportAggregator aggregator) {
    }

    public synchronized Iterable<Map.Entry<String, List<ReportInfo>>> getReports() {
        return reports.entrySet();
    }

    public synchronized List<ReportInfo> getReports(String context) {
        return reports.get(context);
    }

    public ReportAggregator getAggregator() {
        return aggregator;
    }

    private List<ReportInfo> getOrCreateInfos(String name) {
        List<ReportInfo> reportList = reports.get(name);
        if (reportList == null) {
            reportList = new ArrayList<>();
            reports.put(name, reportList);
        }
        return reportList;
    }

    public static class ReportInfo {
        private final RamlReport report;
        private final SavableServletRamlRequest request;
        private final SavableServletRamlResponse response;

        public ReportInfo(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
            this.report = report;
            this.request = new SavableServletRamlRequest(request);
            this.response = new SavableServletRamlResponse(response);
        }

        public RamlReport getReport() {
            return report;
        }

        public ServletRamlRequest getRequest() {
            return request;
        }

        public ServletRamlResponse getResponse() {
            return response;
        }
    }

    private static class SavableServletRamlRequest extends ServletRamlRequest {
        private final StringBuffer requestURL;
        private final String queryString;
        private final Values headerValues;
        private final String method;

        public SavableServletRamlRequest(ServletRamlRequest delegate) {
            super(delegate);
            requestURL = delegate.getRequestURL();
            queryString = delegate.getQueryString();
            headerValues = delegate.getHeaderValues();
            method = delegate.getMethod();
        }

        @Override
        public StringBuffer getRequestURL() {
            return requestURL;
        }

        @Override
        public String getQueryString() {
            return queryString;
        }

        @Override
        public Values getHeaderValues() {
            return headerValues;
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    private static class SavableServletRamlResponse extends ServletRamlResponse {
        private final byte[] content;
        private final Values headerValues;

        public SavableServletRamlResponse(ServletRamlResponse delegate) {
            super(delegate);
            content = delegate.getContent();
            headerValues = delegate.getHeaderValues();
        }

        @Override
        public byte[] getContent() {
            return content;
        }

        @Override
        public Values getHeaderValues() {
            return headerValues;
        }
    }
}
