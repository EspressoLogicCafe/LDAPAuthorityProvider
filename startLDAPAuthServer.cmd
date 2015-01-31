
set cp=target/classes
set cp=%cp%;target/lib/commons-codec-1.6.jar
set cp=%cp%;target/lib/mysql-connector-java-5.1.29.jar
set cp=%cp%;target/lib/commons-logging-1.1.1.jar
set cp=%cp%;target/lib/httpclient-4.2.5.jar
set cp=%cp%;target/lib/httpcore-4.2.4.jar
set cp=%cp%;target/lib/rhino-1.7R4.jar

set cp=%cp%;target/lib/derby.jar
set cp=%cp%;target/lib/jackson-annotations-2.4.1.jar
set cp=%cp%;target/lib/jackson-core-2.4.1.jar
set cp=%cp%;target/lib/jackson-databind-2.4.1.jar
set cp=%cp%;target/lib/rssbus.jdbc.ldap.jar
set cp=%cp%;target/lib/sqlitejdbc.jar
set cp=%cp%;target/lib/ldapservice.jar
java -cp %cp% org.mozilla.javascript.tools.shell.Main test.js