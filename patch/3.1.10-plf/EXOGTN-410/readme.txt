Summary

    * Status: No content type on CSS file
    * CCP Issue: CCP-1025, Product Jira Issue: EXOGTN-410.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * No content type on CSS file

Fix description

How is the problem fixed?

    * Add ContentType in the response.

Patch file: EXOGTN-410.patch

Tests to perform

Reproduction test
There is no Content-Type on css files. This causes Internet Explorer 9 to ignore these css-files due to new security policy (a console message states: SEC7113: CSS was ignored due to mime type mismatch).

To reproduce w/o IE9:

   1. Launch portal web server
   2. Launch command line
   3. Use telnet in order to connect to web server

      telnet localhost 8080

   4. Request the header of a css file in console
          * Enter the following instruction

            HEAD http://localhost:8080/eXoResources/skin/Stylesheet-lt.css HTTP/1.1

          * Click Enter 2 times to send this request to web server.
   5. HTTP Response is displayed w/o Content-Type

      HTTP/1.1 200 OK
      Server: Apache-Coyote/1.1
      X-Powered-By: Servlet 2.5; JBoss-5.0/JBossWeb-2.1
      Cache-Control: max-age=3600,s-maxage=3600
      Last-Modified: Wed, 08 Jun 2011 05:54:57 GMT
      Transfer-Encoding: chunked
      Date: Wed, 08 Jun 2011 05:54:57 GMT

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Validated on behalf of PM.

Support Comment

    * Patch validated.

QA Feedbacks
*
Labels parameters

