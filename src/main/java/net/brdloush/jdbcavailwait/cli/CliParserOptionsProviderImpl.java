package net.brdloush.jdbcavailwait.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

/**
 * Created by brdloush on 25.7.2015.
 */
public class CliParserOptionsProviderImpl implements CliParserOptionsProvider {

    public Options getCliOptions() {

        Options options = new Options();
        options.addOption(CliOptions.JDBC_URL );
        options.addOption(CliOptions.JDBC_USER );
        options.addOption(CliOptions.JDBC_PASSWORD );
        options.addOption(CliOptions.JDBC_TIMEOUT );
        options.addOption(CliOptions.OUTPUT_TIME_PASSED_MSG);
        options.addOption(CliOptions.JDBC_DRIVER);
        options.addOption(CliOptions.VERBOSE);
        options.addOption(CliOptions.JDBC_HELP);
        return options;
    }
}

