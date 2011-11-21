Summary

    * Status: Out Of Memory while starting up with too many pages
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-412.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Out Of Memory while starting up with too many pages

Fix description

How is the problem fixed?

    * We commit transaction more frequently during database initialization. That reduces heavy memory storage and therefore avoid OutOfMemory.
       
Patch file: EXOGTN-412.patch

Tests to perform

Reproduction test

    * Steps to reproduce:
      1. Create many pages (more than 500 pages) for a portal
      2. Start server
      Open link http://localhost:8080/portal in browser, we cannot connect to server.

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

    * Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment

    * Validated on behalf of PM

Support Comment

    * Validated

QA Feedbacks
*
