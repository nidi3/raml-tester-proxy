package guru.nidi.ramlproxy;

import org.apache.commons.cli.*;

import java.io.File;

/**
 *
 */
public class OptionContainer {
    private int port;
    private String target;
    private File saveDir;
    private String ramlUri;
    private String baseUri;

    public OptionContainer(String[] args) {
        final Options options = createOptions();
        try {
            CommandLineParser parser = new BasicParser();
            CommandLine cmd = parser.parse(options, args);
            port = Integer.parseInt(cmd.getOptionValue('p'));
            target = cmd.getOptionValue('t');
            ramlUri = cmd.getOptionValue('r');
            baseUri = cmd.getOptionValue('b');
            String saveDirName = cmd.getOptionValue('s');
            if (saveDirName != null) {
                saveDir = new File(saveDirName);
                saveDir.mkdirs();
            }
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
        options.addOption(OptionBuilder.withDescription("RAML resource, possible schemas are classpath://, file://, http://, https://").isRequired(true).withArgName("URL").hasArg(true).create('r'));
        options.addOption(OptionBuilder.withDescription("Base URI that should be assumed").isRequired(false).withArgName("URI").hasArg(true).create('b'));
        options.addOption(OptionBuilder.withDescription("Save directory for failing requests/responses").isRequired(false).withArgName("directory").hasArg(true).create('s'));
        return options;
    }

    public int getPort() {
        return port;
    }

    public String getTarget() {
        return target;
    }

    public String getRamlUri() {
        return ramlUri;
    }

    public File getSaveDir() {
        return saveDir;
    }

    public String getBaseUri() {
        return baseUri;
    }
}
