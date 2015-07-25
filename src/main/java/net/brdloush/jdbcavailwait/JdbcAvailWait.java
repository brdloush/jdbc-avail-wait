package net.brdloush.jdbcavailwait;

import net.brdloush.jdbcavailwait.cli.CliConstants;
import net.brdloush.jdbcavailwait.cli.CliOptions;
import net.brdloush.jdbcavailwait.cli.CliParserOptionsProvider;
import net.brdloush.jdbcavailwait.cli.CliParserOptionsProviderImpl;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Main class of JdbcAvailWait responsible for handling CLI options & spawning up loop which is checking availability of
 * specified jdbc URL.
 */
public class JdbcAvailWait
{
    private final static Logger log = Logger.getLogger(JdbcAvailWait.class.getName());

    private static final int ERROR_CODE_NO_ERROR = 0;
    private static final int ERROR_CODE_WAIT_TIMEOUT = 1;
    private static final int ERROR_CODE_ILLEGAL_PARAMS = 2;
    private static final int ERROR_CODE_GENERIC_ERROR = 3;
    private static final int ERROR_CODE_NO_JDBC_DRIVER = 4;

    private static final long WAITING_LOOP_SLEEP_TIME = 1000L;
    private static final int MSG_EVERY_SEC = 5;

    private CliParserOptionsProvider cliOptionsProvider = new CliParserOptionsProviderImpl();

    /**
     * CLI entry-point method.
     * @param args command-line arguments
     */
    public static void main( String[] args )
    {
        JdbcAvailWait instance = new JdbcAvailWait();
        instance.execute(args);
    }

    /**
     * The actual non-static entry-point method.
     * @param args command-line arguments
     */
    private void execute(String[] args) {
        CommandLineParser cliParser = new DefaultParser();
        Options cliOptions = null;
        try {
            cliOptions = cliOptionsProvider.getCliOptions();
            CommandLine line = cliParser.parse(cliOptions, args );

            if (line.getOptions().length == 0 || line.hasOption(CliOptions.JDBC_HELP.getLongOpt())) {
                printHelp(cliOptions);
                return;
            }

            String jdbcUrl = line.getOptionValue(CliOptions.JDBC_URL.getLongOpt());
            String user = line.getOptionValue(CliOptions.JDBC_USER.getLongOpt());
            String password = line.getOptionValue(CliOptions.JDBC_PASSWORD.getLongOpt());
            String timeoutStr = line.getOptionValue(CliOptions.JDBC_TIMEOUT.getLongOpt());
            String driverClassName = line.getOptionValue(CliOptions.JDBC_DRIVER.getLongOpt());
            boolean outputTimePassedMsg = line.hasOption(CliOptions.OUTPUT_TIME_PASSED_MSG.getLongOpt());

            if (StringUtils.isBlank(jdbcUrl)) {
                exitMissingValue(CliOptions.JDBC_URL);
            }
            if (StringUtils.isBlank(user)) {
                exitMissingValue(CliOptions.JDBC_USER);
            }
            if (StringUtils.isBlank(password)) {
                exitMissingValue(CliOptions.JDBC_PASSWORD);
            }
            int timeoutSec = CliConstants.DEFAULT_TIMEOUT;
            if (StringUtils.isNotBlank(timeoutStr)) {
                timeoutSec = Integer.valueOf(timeoutStr);
            }

            if (StringUtils.isBlank(driverClassName)) {
                exitMissingValue(CliOptions.JDBC_DRIVER);
            } else {
                try {
                    Class aClass = Class.forName(driverClassName);
                } catch (ClassNotFoundException e) {
                    log.error("Class with jdbc driver not found ",e);
                    System.exit(ERROR_CODE_NO_JDBC_DRIVER);
                }
            }

            executeWaitingLoop(jdbcUrl, user, password, timeoutSec, outputTimePassedMsg);

        } catch (ParseException e) {
            log.error( "Parsing failed.  Reason: " + e.getMessage() );
            printHelp(cliOptions);
        }

    }

    /**
     * Main loop that periodically triggers checking whether specified jdbcUrl is accessible.
     * @param jdbcUrl jdbc url
     * @param user database user
     * @param password password for database user
     * @param timeoutSec maximum number of seconds the loop shall wait until specified jdbcUrl becomes available
     * @param outputTimePassedMsg when set to true, a "still waiting [5/60 sec]..." message will get written to stdout
     */
    private void executeWaitingLoop(String jdbcUrl, String user, String password, int timeoutSec, boolean outputTimePassedMsg) {

        final long millisBefore = System.currentTimeMillis();
        final long waitUntil = millisBefore + (timeoutSec*1000);
        final long msgIncrementMillis = (MSG_EVERY_SEC * 1000);
        long nextMsgMillis = millisBefore + msgIncrementMillis;

        log.info("Starting waiting loop (max "+timeoutSec+" seconds) for accessibility of jdbc url "+jdbcUrl);
        long currentMillis = System.currentTimeMillis();
        while (currentMillis < waitUntil) {
            currentMillis = System.currentTimeMillis();

            if (outputTimePassedMsg && (currentMillis > nextMsgMillis )) {
                nextMsgMillis+=msgIncrementMillis;
                int elapsedSeconds = (int) ((currentMillis-millisBefore)/1000);
                log.info("["+elapsedSeconds+"/"+timeoutSec+" sec] still waiting ...");
            }

            boolean jdbcUrlIsAccessible = isJdbcUrlAccessible(jdbcUrl, user, password);

            if (jdbcUrlIsAccessible) {
                log.info("the wait is over, the jdbc url is accessible!");
                System.exit(ERROR_CODE_NO_ERROR);
            }

            try {
                Thread.sleep(WAITING_LOOP_SLEEP_TIME);
            } catch (InterruptedException e) {
                log.error("error while sleeping in waiting loop",e);
                System.exit(ERROR_CODE_GENERIC_ERROR);
            }
        }

        // timeout occured
        log.error("timed out (after "+((int)((System.currentTimeMillis()-millisBefore) / 1000))+") while waiting for jdbc resource");
        System.exit(ERROR_CODE_WAIT_TIMEOUT);
    }

    /**
     * Tries to access a specified jdbc url using specified user and password. In case anything goes wrong,
     * false is returned and nothing gets logged.
     *
     * @param jdbcUrl jdbc url
     * @param user database user
     * @param password password for database user
     * @return true if the connection was succesfully estabilished, false otherwise
     */
    private boolean isJdbcUrlAccessible(String jdbcUrl, String user, String password) {

        Properties connectionProps = new Properties();
        connectionProps.put("user", user);
        connectionProps.put("password", password);

        Connection conn = null;
        try {
            conn = DriverManager.getConnection(jdbcUrl, user,password);

            if (conn != null) {
                return true;
            }
        } catch (SQLException e) {
            // exception is actually expected here, so we don't  bother logging.
            //
            // We'll only "No suitable driver found"
            String errorMessage = e.getMessage();
            if (StringUtils.isNotBlank(errorMessage) && errorMessage.contains("No suitable driver found")) {
                log.error("Got 'No suitable driver found' error, it's useless to continue waiting. Perhaps you misspelled your jdbcUrl or forgot to supply -jar with your jdbc driver");
                System.exit(ERROR_CODE_NO_JDBC_DRIVER);
            }

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e) {
                    // we promised not to log anything in this method, thus no logging here
                }
            }
        }

        return false;
    }

    /**
     * Logs "Missing value" message and exits with error code ERROR_CODE_ILLEGAL_PARAMS.
     * @param option
     */
    private void exitMissingValue(Option option) {
        log.error("Missing required value for "+option.getArgName());
        System.exit(ERROR_CODE_ILLEGAL_PARAMS);
    }


    /**
     * Prints out CLI help.
     * @param options definition of CLI options - needed for printout of H
     */
    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "java -cp target/jdbc-avail-wait-0.1-jar-with-dependencies.jar;jdbc/ojdbc6-11.2.0.1.0.jar net.brdloush.jdbcavailwait.JdbcAvailWait -d oracle.jdbc.OracleDriver -url jdbc:oracle:thin:@localhost:1521:XE -u MYUSERNAME -p MYPASSWORD -msg", options );
    }
}
