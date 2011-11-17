Summary

    * Status: The max length of Group label is under 50 characters
    * CCP Issue: CCP-1126, Product Jira Issue: EXOGTN-672.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The max length of Group label is under 50 characters

Fix description

How is the problem fixed?

    * Change the max length of Group label from 30 to 50 characters

Patch file: EXOGTN-672.patch

Tests to perform

Reproduction test
Steps to reproduce:

   1. Login by admin
   2. Go to Group → Community Management
   3. Choose Group Management tab
   4. Click + icon in group tree from left pane
   5. Input valid Name
   6. Input less than 50 characters into Label
   7. Click Save --> show message alert that max length of Group label is limited to 30 characters

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

    * Validated

Support Comment

    * Validated

QA Feedbacks
*
