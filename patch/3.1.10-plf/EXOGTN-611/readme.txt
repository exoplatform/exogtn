Summary

    * Status: Avoid LDAP direct requests
    * CCP Issue: CCP-1032, Product Jira Issue: EXOGTN-611.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Avoid LDAP direct requests

Fix description

How is the problem fixed?

    * Using ConversationState.getCurrent().getIdentity() to get groups of the current user instead of using OrganizationService.

Patch file: EXOGTN-611.patch

Tests to perform

Reproduction test

    * Many requests to ldap

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

    * No, this fix is to improve the performance.

Validation (PM/Support/QA)

PM Comment

    * Validated on behalf of PM.

Support Comment
*

QA Feedbacks
*
