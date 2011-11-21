Summary

    * Status: Remove embbeded logger configuration
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-624.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Remove embbeded logger configuration

Fix description

How is the problem fixed?

    * Remove logs-configuration.xml file

Patch file: EXOGTN-624.patch

Tests to perform

Reproduction test

    * Steps to reproduce:
      1. Go to jboss/bin/run.conf and edit JAVA_OPTS parameter:
      JMX_OPTS="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9004 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Djava.rmi.server.hostname=localhost"
      JAVA_OPTS="$JAVA_OPTS -Dexo.conf.dir.name=gatein $JMX_OPTS $EXO_PROFILES"

      2. Start server
      3. Start jvisualvm and connect to the server: Errors are shown in server console:
      ...
      FINE: RMI TCP Connection(1)-192.168.1.63: (port 52732) op = 80
      15:43:47,482 ERROR [STDERR] Sep 14, 2011 3:43:47 PM sun.rmi.server.LoaderHandler loadClass
      FINE: RMI TCP Connection(1)-192.168.1.63: name = "javax.management.ObjectName", codebase = "", defaultLoader = sun.misc.Launcher$AppClassLoader@f4a24a
      15:43:47,484 ERROR [STDERR] Sep 14, 2011 3:43:47 PM sun.rmi.transport.tcp.TCPTransport handleMessages
      FINE: RMI TCP Connection(1)-192.168.1.63: (port 52732) op = 80
      15:43:47,485 ERROR [STDERR] Sep 14, 2011 3:43:47 PM sun.rmi.server.LoaderHandler loadClass
      FINE: RMI TCP Connection(1)-192.168.1.63: name = "javax.management.ObjectName", codebase = "", defaultLoader = sun.misc.Launcher$AppClassLoader@f4a24a
      15:43:47,485 ERROR [STDERR] Sep 14, 2011 3:43:47 PM sun.rmi.transport.tcp.TCPTransport handleMessages
      FINE: RMI TCP Connection(1)-192.168.1.63: (port 52732) op = 80
      ...

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * Remove logs-configuration.xml

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Validated

Support Comment

    * Patch validated

QA Feedbacks
*
