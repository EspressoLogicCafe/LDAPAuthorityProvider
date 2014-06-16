#!/bin/sh

java -cp target/classes;target/lib/commons-logging-1.1.1.jar;target/lib/httpclient-4.2.5.jar;target/lib/httpcore-4.2.4.jar;target/lib/rhino-1.7R4.jar org.mozilla.javascript.tools.shell.Main test.js
