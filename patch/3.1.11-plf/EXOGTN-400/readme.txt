Summary

    * Status: "Preferences" tab is showed only if portlet has been customized
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-400.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * A bug prevented the 'Preferences' tab from appearing in some portlets. The tab would only appear in portlets that were customized during the first deployment through the portal.xml descriptor. Non-customized portlets would not show a 'Preferences' tab.

Fix description

How is the problem fixed?

    * Check all default preferences in portlet form.

Patch file: EXOGTN-400.patch

Tests to perform

Reproduction test

    * Steps to reproduce:
      Case 1:
      1. Login as root.
      2. Go to the classic site
      3. Under the Site Editor click Edit Layout.
      4. Click the Edit Portlet icon on Banner Portlet, it shows a tab Preferences. If clicking the Edit Portlet icon on Breadcumbs Portlet, it doesn't show the Preferences tab.
      Case 2:
      1. Login as root.
      2. Go to Group > Administration > Application Registry
      3. Click on Porlet in Dialog for selecting portlet to register
      4. Add Banner porlet to one category
      5. Go to the classic site
      6. Under the Site Editor click Edit Layout.
      7. Add new Banner porlet to page, it doesn't show the Preferences tab.

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*

Validation (PM/Support/QA)

PM Comment

    * Validated on behalf of PM.

Support Comment

    * Validated

QA Feedbacks
*
