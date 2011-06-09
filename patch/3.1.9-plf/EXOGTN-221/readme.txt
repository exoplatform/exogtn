Summary

    * Status: The day of the month in the calendar pop-up is not translated to French
    * CCP Issue: CCP-883, Product Jira Issue: EXOGTN-221.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Minicalendar pop-up in French is not well localized:

    * Name of day
    * Tooltip (Next/Previous Month/Year)
    * Starting day of a week should be Monday

Fix description

How is the problem fixed?

    * Add more value into javascript resource bundle file (MessageResource_*.js)
    * Add more js API to help get value from js resource bundle
    * Change how to render Calendar to help have "a week starts on Monday" as France routine

Tests to perform

Reproduction test
* Steps to reproduce:

   1. Case 1: Add Page Wizard
   2. At step 1, click to open minicalendar of starting/end publication date.

   1. Case 2: Go to siteExplorer
         1. Create an Event document or a Sample node document
         2. Click to a "Date-Time" field.

Tests performed at DevLevel
* No

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

    * Function or ClassName change: N

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* Validated the Patch on behalf of PM

Support Comment
* Patch Validated

QA Feedbacks
*

