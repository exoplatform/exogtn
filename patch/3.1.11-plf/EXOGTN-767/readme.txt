Summary

    * Status: PageBody placeholder disappears on Site Layout Edit
    * CCP Issue: CCP-1132, Product Jira Issue: EXOGTN-767.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * PageBody placeholder disappears on Site Layout Edit

Fix description

How is the problem fixed?

    * Fix org.exoplatform.portal.pom.data.Mapper class to distinguish between deleted component and moved component

Patch file: EXOGTN-767.patch

Tests to perform

Reproduction test

   1. Edit site layout
   2. Add a new two columns container
   3. Move pagebody inside one column of the new container
   4. Save the layout
   5. Re-edit site layout
   6. Add another two columns container then move pagebody to one column of this new container
   7. page body disappears and the exception is raised

Tests performed at DevLevel

    * C.f above

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

    * If customers meet this issue with their data, they need to apply the CraSH script attached in JIRA to restore the missing pageBody. 

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Patch validated on behalf of PM

Support Comment

    * Patch validated

QA Feedbacks
*
