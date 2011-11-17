Summary

    * Status: Remove limit of portlet title length
    * CCP Issue: CCP-1126, Product Jira Issue: EXOGTN-673.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Remove limit of portlet title length

Fix description

How is the problem fixed?

    * Remove length constraint of input portlet title

Patch file: EXOGTN-673.patch

Tests to perform

Reproduction test

    * Steps to reproduce :
      1. Login platform, create a new page
      2. Add a portlet
      3. Edit portlet title, type "A"
      4. Click Save and Close ---> there is a message notifying the limit of portlet title (3 and 60).

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

    * Validated on behalf of PM.

Support Comment

    * Validated

QA Feedbacks
*
