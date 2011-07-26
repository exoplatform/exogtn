Summary

    * Status: Label not translated into French in upload file
    * CCP Issue: CCP-977, Product Jira Issue: EXOGTN-389.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Use UIFormUploadInput with UIFormMultivalueInputSet, move mouse over "Plus" or the  "Trash" icon to add to remove item, the tooltips are always "Add Item" or "Remove Item" and can't be localized

Fix description

How is the problem fixed?

    *  Replace "hard code" labels by localized labels in UIFormMultiValueInputSet.java

Tests to perform

Reproduction test

*When uploading a file in Files explorer, put the mouse over the "plus" sign next to "ajouter une taxonomie" (language is French), "add item" appears not translated into French.

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
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

