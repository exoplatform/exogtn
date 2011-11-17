Summary

    * Status: It should be possible to disable Javascript compressor without throwing an ERROR
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-405.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * It should be possible to disable Javascript compressor without throwing an ERROR

Fix description

How is the problem fixed?

    * Change error to warn in log

Patch file: EXOGTN-405.patch

Tests to perform

Reproduction test

    * Steps to reproduce:
      1. Disable the Javascript compressor by removing JSMinCompressorPlugin in web/portal/src/main/webapp/WEB-INF/conf/common/resource-compressor-configuration.xml file
      2. Start server and login, we have errors in server console:

      SEVERE: Error when generating minified javascript, will use normal javascript instead
      org.exoplatform.portal.resource.compressor.ResourceCompressorException: There is no compressor for JAVASCRIPT type
          at org.exoplatform.portal.resource.compressor.impl.ResourceCompressorService.compress(Res[INFO] ComponentRegistry - JBoss Cache version: JBossCache 'Malagueta' 3.2.6.GA
      Oct 4, 2011 4:59:55 PM org.gatein.common.logging.Logger log
      SEVERE: Error when generating minified javascript, will use normal javascript instead
      org.exoplatform.portal.resource.compressor.ResourceCompressorException: There is no compressor for JAVASCRIPT type
          at org.exoplatform.portal.resource.compressor.impl.ResourceCompressorService.compress(ResourceCompressorService.java:108)
          at org.exoplatform.web.application.javascript.JavascriptConfigService.getMergedJavascript(JavascriptConfigService.java:323)
          at org.exoplatform.portal.webui.javascript.JavascriptServlet.service(JavascriptServlet.java:77)
      ...

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*No
Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
*Validated

Support Comment
* Validated

QA Feedbacks
*

