Welcome to GateIn:
===========================

This will explain you how to build a package of GateIn with Tomcat or JBoss.

*****************
* COMPILATION
*****************

* mvn install
For example: mvn install

Note: If you run "mvn install" twice in a row without cleaning, one test will
fail. To workaround this issue you will need to delete the test data located
here: component/portal/target/temp/

**********************
* MAVEN CONFIGURATION:
**********************

By default the maven configuration will build all the required artifact for a release which means:
* all packaging
* all examples
* all documentations

There is a friendly development mode that build none of those:

* mvn -Dgatein.dev

This property can be combined with explicit profile activation to build a specific module, for instance:

* mvn -Dgatein.dev -Ppkg-tomcat

builds only the tomcat packaging.

Here is a list of the existing profiles:
* packaging
** pkg-tomcat
** pkg-jbossear
** pkg-jbossas
** pkg-tomcat-tests
** pkg-jbossas-tests
* archive
** arc-tomcat
** arc-jbossas
* other
** "example" : all examples
** "doc" : all documentations

*****************
* PACKAGING:
*****************

Per default GateIn generates all packaging required for a release.

* mvn install -Ppkg-tomcat
** Creates a Tomcat delivery in packaging/pkg/target/tomcat/ 

* mvn install -Ppkg-jbossas
** Creates a JBossAS delivery in packaging/pkg/target/jboss/

*****************
* DOCUMENTATIONS:
*****************
 
Per default GateIn documentations are generated in the build process.

*****************
* STARTING:
*****************

* On Tomcat: go to the tomcat directory (or unzip the archive in your favorite location) and execute 'bin/gatein.sh start' ('bin/gatein.bat start' on Windows)

* On JBoss: go to the jboss directory (or unzip the archive in your favorite location) and execute 'bin/run.sh start' ('bin/run.bat start' on Windows)
* Go to http://localhost:8080/portal to see the homepage of the portal. That's it.

