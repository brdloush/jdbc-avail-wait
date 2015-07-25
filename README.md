jdbc-avail-wait
===============
A very simple command-line utility that's able to wait until specified JDBC connection is either accessible or the wait
period timed out.

# Motivation
Suppose you're starting a docker container with database (oracle) and at the same time you're starting
another container with tomcat, which has a datasource that's using the oracle container. If you start your tomcat too
soon, oracle might still not be accessible. One of the options you have is to start your tomcat using a wrapper, which
will first wait until DB is actually accessible and only then will it continue with startup of tomcat server.

# How to use?
One example is worth thousand words:
```
java -cp target/jdbc-avail-wait-0.1-jar-with-dependencies.jar:jdbc/ojdbc6-11.2.0.1.0.jar net.brdloush.jdbcavailwait.JdbcAvailWait -d oracle.jdbc.OracleDriver -url jdbc:oracle:thin:@czprguxdv48:18096:ORCL -u SY_O2SKO4_PROD -p o4 -msg
2015-07-25 22:40:38 INFO  JdbcAvailWait:117 - Starting waiting loop (max 60 seconds) for accessibility of jdbc url jdbc:oracle:thin:@czprguxdv48:18096:ORCL
2015-07-25 22:40:39 INFO  JdbcAvailWait:130 - the wait is over, the jdbc url is accessible!
```

# How do I know whether the wait failed?
There's a couple of error-levels being returned by the java process. 0=OK, 1=Timeout etc. For more info [see contstants in JdbcAvailWait class](src/main/java/net/brdloush/jdbcavailwait/JdbcAvailWait.java)