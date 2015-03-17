/*
 * Copyright (C) 2014 Stefan Niederhauser (nidin@gmx.ch)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package guru.nidi.ramlproxy;

import guru.nidi.ramltester.MultiReportAggregator;
import guru.nidi.ramltester.core.RamlReport;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ReportSaver {
    private final List<ReportInfo> reports = new ArrayList<>();
    private MultiReportAggregator aggregator = new MultiReportAggregator();

    public final void addReport(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        addingReport(report, request, response);
        aggregator.addReport(report);
        reports.add(new ReportInfo(report, request, response));
    }

    public final void flushReports() {
        flushingReports(reports);
        reports.clear();
    }

    public final void flushUsage() {
        flushingUsage(aggregator);
        aggregator = new MultiReportAggregator();
    }

    protected void addingReport(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
    }

    protected void flushingReports(List<ReportInfo> reports) {
    }

    protected void flushingUsage(MultiReportAggregator aggregator) {
    }

    public List<ReportInfo> getReports() {
        return reports;
    }

    public MultiReportAggregator getAggregator() {
        return aggregator;
    }

    public static class ReportInfo {
        private final RamlReport report;
        private final ServletRamlRequest request;
        private final ServletRamlResponse response;

        public ReportInfo(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
            this.report = report;
            this.request = request;
            this.response = response;
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

}
