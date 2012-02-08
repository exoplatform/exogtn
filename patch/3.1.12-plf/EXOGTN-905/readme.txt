Summary

    * Status: Problem when moving nodes of navigation
    * CCP Issue: CCP-1155, Product Jira Issue: EXOGTN-905.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Problem when moving nodes of navigation

Fix description

How is the problem fixed?

    * Problem occurs when moving a node which is a root node's child. The URI isn't updated.

Tests to perform

Reproduction test

    * Steps to reproduce:
      1. Create 3 pages: test1(first level)/test2(second level)/test3(third level)
      2. Cut node "test2"
      3. Paste the node "test2" at the top level (same level of "test1")
      => When we click on the test3 on the navigation we get a Wrong page.
      => When we go to "Edit navigation" > Edit page node of the "test3" node, the page URI remains "test1/test2/test3"

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
