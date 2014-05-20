package guru.nidi.ramlproxy;

import org.apache.commons.cli.*;

/**
 *
 */
public class OptionContainer {
    private int port;
    private String target;

    public OptionContainer(String[] args) {
        final Options options = createOptions();
        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            port = Integer.parseInt(cmd.getOptionValue('p'));
            target = cmd.getOptionValue('t');
        } catch (Exception e) {
            System.out.println(e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar raml-proxy.jar", options);
            System.exit(1);
        }
    }

    private static Options createOptions() {
        final Options options = new Options();
        options.addOption(OptionBuilder.withDescription("The port to listen to").isRequired(true).withArgName("port").hasArg(true).create('p'));
        options.addOption(OptionBuilder.withDescription("The target uri to forward to").isRequired(true).withArgName("URL").hasArg(true).create('t'));
        return options;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }
}
