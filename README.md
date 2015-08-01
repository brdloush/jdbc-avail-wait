jdbc-avail-wait
===============
A very simple command-line utility that's able to wait until specified JDBC connection is either accessible or the wait
period timed out.

# Motivation
Suppose you're starting a docker container with database (oracle) and at the same time you're starting
another container with tomcat which has a datasource that's using the oracle container. If you start your tomcat too
soon, oracle might still not be accessible. One of the options you have is to start your tomcat using a wrapper, which
will first wait until DB is actually accessible and only then will it continue with startup of tomcat server.

I Ended up using followin fragment in tomcat server's startup wrapper script..
```
# if we're asked to wait until JDBC URL is accessible, let's do so..
if [ ! -z "$WAIT_FOR_JDBC_URL" ]; then
    echo "Requested waiting before JDBC URL is accessible, so let's wait.. (URL: $WAIT_FOR_JDBC_URL)"
    java -cp ojdbc6.jar:jdbc-avail-wait-0.1-jar-with-dependencies.jar net.brdloush.jdbcavailwait.JdbcAvailWait -d oracle.jdbc.OracleDriver -url $WAIT_FOR_JDBC_URL -t 120 -msg

	if [ $? -ne 0 ]; then
		echo "Waiting for JDBC uri failed, errorcode is: $?"
		exit 1;
	fi
fi
```

Then all that's needed is to pass something like ```-e "WAIT_FOR_JDBC_URL=jdbc:oracle:thin:MY_DB_USER/MY_DB_PASSWORD@db-hostname:1521:DB_SID"```
when starting container with tomcat.


# How to use?
One example is worth thousand words:
```
java -cp target/jdbc-avail-wait-0.1-jar-with-dependencies.jar:jdbc/ojdbc6-11.2.0.1.0.jar net.brdloush.jdbcavailwait.JdbcAvailWait -d oracle.jdbc.OracleDriver -url jdbc:oracle:thin:@localhost:1521:XE -u DBUSER -p DBPASSWORD -msg
2015-07-25 22:40:38 INFO  JdbcAvailWait:117 - Starting waiting loop (max 60 seconds) for accessibility of jdbc url jdbc:oracle:thin:@localhost:1521:XE
2015-07-25 22:40:39 INFO  JdbcAvailWait:130 - the wait is over, the jdbc url is accessible!
```

# How do I know whether the wait failed?
There's a couple of error-levels being returned by the java process. 0=OK, 1=Timeout etc. For more info [see contstants in JdbcAvailWait class](src/main/java/net/brdloush/jdbcavailwait/JdbcAvailWait.java)

# What's the CLI usage?'
```
usage: java -cp
            jdbc-avail-wait-0.1-jar-with-dependencies.jar:ojdbc6-11.2.0.1.
            0.jar net.brdloush.jdbcavailwait.JdbcAvailWait -d
            oracle.jdbc.OracleDriver -url
            jdbc:oracle:thin:@localhost:1521:XE -u MYUSERNAME -p
            MYPASSWORD -msg
 -d,--driver-class-name <arg>     (required) classname (including package)
                                  to JDBC driver. Example:
                                  oracle.jdbc.OracleDriver
 -h,--help                        prints this help
 -msg,--outputs-time-passed-msg   (optional) if specified, application
                                  outputs 'still waiting [5/60
                                  sec]...'-like messages to stdout while
                                  waiting in loop
 -p,--password <arg>              (optional) password to use with jdbc-url
 -t,--timeout-sec <arg>           (optional - default: 60) maximum number
                                  of seconds we'll be trying to access
                                  specified jdbc-url. Defaults to 60
 -u,--user <arg>                  (optional) username to use with jdbc-url
 -url,--jdbc-url <arg>            (required) jdbc url that's going to be
                                  accessed. Example:
                                  jdbc:oracle:thin:@localhost:1521:XE
 -v,--verbose                     (optional) enable verbose output (show
                                  connection Exceptions etc)
```
