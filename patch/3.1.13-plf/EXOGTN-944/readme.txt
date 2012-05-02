Summary

    * Issue title: Login URL can be used to redirect to external sites 
    * CCP Issue:  CCP-1281 
    * Product Jira Issue: EXOGTN-944.
    * Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?

    * Login URL can be used to redirect to external sites

Fix description

Problem analysis
The login URL can be used to redirect to external sites. There are possible phishing implications.

How is the problem fixed?

    * The initialURL is set to relative and not absolute. 
    * Encode initialURI before processing it

Tests to perform

Reproduction test

    * Steps to reproduce:
      Enter this link -> redirect to google.com
      http://localhost:8080/portal/login?username=gtn&password=gtn&initialURI=http://www.google.com

Tests performed at DevLevel
*

Tests performed at Support Level
*

Tests performed at QA
*

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests

    *  No

Changes in Selenium scripts 

    *  No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

    *  No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
    * Function or ClassName change: no
    * Data (template, node type) migration/upgrade: no

Is there a performance risk/cost?
*  No

Validation (PM/Support/QA)

PM Comment
*  Validated

Support Comment
*  Validated

QA Feedbacks
    * 
