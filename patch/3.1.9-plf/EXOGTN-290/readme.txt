Summary

    * Status: Wrong label in Todolist gadget
    * CCP Issue: CCP-827, Product Jira Issue: EXOGTN-290.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Wrong label in Todolist gadget

Fix description

How is the problem fixed?

    * Formerly, Todo gadget got resource bundle from a URI as http://localhost:8080/eXoGadgets/locale/Todo/fr_ALL.xml, so when gatein had been deployed to intranet, it couldn't get this resource, and the label is wrong.
    * So, we should move all i18n resource bundle files to gadget resources, and gadget gets these resources as the local resource instead.

Patch file: EXOGTN-290-20110311.patch

Tests to perform

Reproduction test
* OK

Tests performed at DevLevel
* OK

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
* Validated on behalf of PM.

Support Comment
* Patch validated

QA Feedbacks
*
