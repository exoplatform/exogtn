Summary

    * Issue title: Drop-down box's for the membership not in order
    * CCP Issue:  CCP-1117 
    * Product Jira Issue: EXOGTN-695.
    * Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?

    * Drop-down box's for the membership is not in order

Fix description

How is the problem fixed?

    *  Implement sort function for membership values.
       Any value (*) is at the first position in list because it will be selected more often than other ones. 

Tests to perform

Reproduction test

    * Steps to reproduce:
          Login as admin 
          Go to Manage users and groups
          Go to Group Management
          Select the drop-down box of membership type
-> The membership types are not in logic order, they should be ordered alphabetically.

Tests performed at DevLevel

    * Cf. above

Tests performed at Support Level

    * Cf. above

Tests performed at QA

    * 

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests

    *  Memberships are ordered alphabetically. The any value (*) is on the top.

Changes in Selenium scripts 

    *  No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

    *  No

Configuration changes

Configuration changes:

    * Yes

Will previous configuration continue to work?
*
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: 
    * Data (template, node type) migration/upgrade: 

Is there a performance risk/cost?

    *  No

Validation (PM/Support/QA)

PM Comment

    *  Validated

Support Comment

    *  Validated

QA Feedbacks

    * ...
