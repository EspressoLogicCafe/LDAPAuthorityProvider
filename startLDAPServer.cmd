node startLDAPServer.js

set cp=target/classes
set cp=%cp%;target/lib/commons-codec-1.6.jar
set cp=%cp%;target/lib/commons-logging-1.1.1.jar
set cp=%cp%;target/lib/httpclient-4.2.5.jar
set cp=%cp%;target/lib/httpcore-4.2.4.jar
set cp=%cp%;target/lib/rhino-1.7R4.jar
java -cp %cp% org.mozilla.javascript.tools.shell.Main test.js