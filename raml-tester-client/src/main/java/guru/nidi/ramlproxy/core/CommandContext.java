package guru.nidi.ramlproxy.core;

import guru.nidi.ramlproxy.report.ReportSaver;

/**
 *
 */
public interface CommandContext {
    void reloadRamlDefinition();

    void stopProxy() throws Exception;

    ReportSaver getSaver();
}
