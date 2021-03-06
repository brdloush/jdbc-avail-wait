package net.brdloush.jdbcavailwait.cli;

import org.apache.commons.cli.Option;

/**
 * @author brdloush
 */
public class CliOptions {

    public static final Option VERBOSE = new Option("v", "verbose", false, "(optional) enable verbose output (show connection Exceptions etc) ");
    public static final Option JDBC_URL = new Option("url", "jdbc-url", true, "(required) jdbc url that's going to be accessed. Example: jdbc:oracle:thin:@localhost:1521:XE");
    public static final Option JDBC_USER = new Option("u", "user", true, "(optional) username to use with jdbc-url");
    public static final Option JDBC_PASSWORD = new Option("p", "password", true, "(optional) password to use with jdbc-url");
    public static final Option JDBC_TIMEOUT = new Option( "t", "timeout-sec", true, "(optional - default: 60) maximum number of seconds we'll be trying to access specified jdbc-url. Defaults to "+CliConstants.DEFAULT_TIMEOUT);
    public static final Option JDBC_HELP  = new Option( "h", "help", false, "prints this help");
    public static final Option JDBC_DRIVER = new Option( "d", "driver-class-name", true, "(required) classname (including package) to JDBC driver. Example: oracle.jdbc.OracleDriver");
    public static final Option OUTPUT_TIME_PASSED_MSG  = new Option( "msg", "outputs-time-passed-msg", false, "(optional) if specified, application outputs 'still waiting [5/60 sec]...'-like messages to stdout while waiting in loop");
}
