Summary

    * Status: Impossible to select items in Toolbar activity menu while opening a pop up
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-380 .
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Its impossible to select items in Toolbar activity menu while opening a pop up

Fix description

How is the problem fixed?

    * Correct the z-index calculation of popup menu in UIPortalNavigation.js

Patch file: EXOGTN-380.patch

Tests to perform

Reproduction test

   1. Login as root
   2. Go to Group/Organization/New Staff
   3. In Account Setting form, fill in required fields and Save
   4. A popup message appears, keep this popup message opening, click on Group
   5. Administration, WSRP options can be selected: OK
   6. Executive Board's pages, Users's pages options can't be selected: not O

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

    * No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*
