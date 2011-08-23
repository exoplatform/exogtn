Summary

    * Status: Cannot add value to a multi value boolean property
    * CCP Issue: CCP-898, Product Jira Issue: EXOGTN-372.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Cannot add value to a multi value boolean property. Steps to reproduce:

   1. Go to Content Explorer
   2. Create new node (content folder for example)
   3. From system view choose 'View Node Properties'
   4. Click Add New Property
   5. Add "multiValueProperty", choose the Boolean type for this property and make "Multiple" option to true.
      Click "add item" and check it
   6. Save
      In 'View Node Properties', click 'Edit' icon in 'Action' column of "multiValueProperty"

    * You will see both checkboxes are unchecked
    * If you try to add a new value to the property, warning dialog "The field "value2" is required." will pop up: to be fixed by ECMS-2619.

Fix description

How is the problem fixed?

    * Handle "String" value in the setValue method of UIFormCheckboxInput

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: EXOGTN-372.patch

Tests to perform

Reproduction test

    * See above.

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
* Patch validated.

QA Feedbacks
*
