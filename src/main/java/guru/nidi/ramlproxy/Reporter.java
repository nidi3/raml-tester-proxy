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
import guru.nidi.ramltester.core.Usage;
import guru.nidi.ramltester.servlet.ServletRamlRequest;
import guru.nidi.ramltester.servlet.ServletRamlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
public class Reporter extends ReportSaver {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final File saveDir;
    private final ReportFormat reportFormat;

    private String startup;
    private AtomicLong id;

    public Reporter(File saveDir, ReportFormat reportFormat) {
        this.saveDir = saveDir;
        this.reportFormat = reportFormat;
        if (saveDir == null) {
            log.info("NOT reporting into a file.");
        } else {
            log.info("Reporting in {} format into: {}", reportFormat, saveDir);
        }
        flushingReports(null);
    }

    @Override
    protected void addingReport(RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        if (!report.isEmpty()) {
            final long idValue = id.incrementAndGet();
            logViolations(idValue, report, request);
            fileViolations(idValue, report, request, response);
        }
    }

    @Override
    protected void flushingReports(List<ReportInfo> reports) {
        startup = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(new Date());
        id = new AtomicLong();
    }

    @Override
    public void flushingUsage(MultiReportAggregator aggregator) {
        for (Map.Entry<String, Usage> entry : aggregator.usages()) {
            final DescribedUsage describedUsage = new DescribedUsage(entry.getValue());
            logUsage(entry.getKey(), describedUsage);
            fileUsage(entry.getKey(), describedUsage);
        }
    }

    public File violationsFile(long idValue) {
        final String filename = "raml-violation-" + startup + "--" + idValue + "." + reportFormat.fileExtension;
        return new File(saveDir, filename);
    }

    public File usageFile(String key) {
        final String filename = "raml-usage-" + startup + "--" + key.replace(" ", "-") + "." + reportFormat.fileExtension;
        return new File(saveDir, filename);
    }

    private void logViolations(long idValue, RamlReport report, ServletRamlRequest request) {
        log.error("<{}> {}\n           Request:  {}\n           Response: {}", idValue, ReportFormat.formatRequest(request), report.getRequestViolations(), report.getResponseViolations());
    }

    private void fileViolations(long id, RamlReport report, ServletRamlRequest request, ServletRamlResponse response) {
        if (saveDir == null) {
            return;
        }
        try {
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(violationsFile(id)))) {
                out.write(reportFormat.formatViolations(id, report, request, response));
            }
        } catch (IOException e) {
            log.error("Problem writing error file", e);
        }
    }

    private void logUsage(String key, DescribedUsage describedUsage) {
        log.error(key + "\n" + describedUsage.toString());
    }

    private void fileUsage(String key, DescribedUsage describedUsage) {
        if (saveDir == null) {
            return;
        }
        try {
            try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(usageFile(key)), "utf-8")) {
                out.write(reportFormat.formatUsage(key, describedUsage));
            }
        } catch (IOException e) {
            log.error("Problem writing error file", e);
        }
    }
}
