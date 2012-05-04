Summary

    * Status: Show duplicate portlet in content category after import portlet
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-650.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Show duplicate portlet in content category after import portlet

Fix description

How is the problem fixed?

    * Check existed portlets by porletID to avoid importing duplicated portlets.

Tests to perform

Reproduction test
Case 1:

   1. Go to Application Registry page and click Import applications
   2. See category list, some portlets are duplicated

Case 2:

   1. Go to Application registry page and import applications
   2. Go to list portlets form and select Group navigation portlet (or whatever portlet that haven't added to any category yet)
   3. Add Group navigation portlet to Administration category
   4. We can see that in Administration category, Group navigation portlets are duplicated with different names

Tests performed at DevLevel

    * c/f above

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

    * Function or ClassName change : No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Validated

Support Comment

    * Validated

QA Feedbacks
*
