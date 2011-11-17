Summary

    * Status: Popup Resizing problem
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-579.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Popup Resizing problem

Fix description

How is the problem fixed?

    * Fix JS bug when calculate popup's height, and prevent select text event of browser

Patch file: EXOGTN-579.patch

Tests to perform

Reproduction test

    * Popup Resizing problem:
          o Open a resizable popup.
          o Try to resize the pop up, the texts on the page are selected

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
* Validated on behalf of PM.

Support Comment
* Validated

QA Feedbacks
*
