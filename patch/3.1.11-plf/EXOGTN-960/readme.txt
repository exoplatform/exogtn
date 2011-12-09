Summary

    * Status: Exception when editing the detail of a group 
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-960.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Exception when editing the detail of /platform/administrators group on PLF. 
      The same exception occurs in EXOGTN when editing the detail of /organization/management/executive-board group.

Fix description

How is the problem fixed?

   1. When we edit a group, the PortalConfig of this group is always reinitialised. When the portlet Organization Service places in the GroupNavigation of a group and we edit this group, the exception throws due to lack of PortalConfig in rendering.
   2. Solution: Only create new Portal Config for new portal.

Patch file: EXOGTN-960.patch

Tests to perform

Reproduction test
In PLF 3.0.7-SNAPSHOT after EXOGTN-572 commit:
   1. Login as root or john
   2. Go to My Groups > Portal Administration > Manage User and Groups
   3. Edit any detail for /platform/administrators > Save -> The Organisation Service portlet disappears and exception is raised on console.

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

    * Function or ClassName change: N/A

Is there a performance risk/cost?

    * N/A

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM. 

Support Comment
* Patch validated.

QA Feedbacks
*
