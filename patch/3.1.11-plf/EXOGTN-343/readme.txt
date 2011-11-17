Summary

    * Status: eXo Javascript is conflicting with some JQuery extensions
    * CCP Issue: CCP-920, Product Jira Issue: EXOGTN-343.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * eXo Javascript is conflicting with some JQuery extensions

Fix description
Problem analysis
    * Variable name "currentContext" is popular name so maybe this raises conflict.
    * Variable eXo.env.portal.context stores same value and its usage is unique so it can reduce conflict with 3rd party libraries. 

How is the problem fixed?

    * Remove "currentContext" and use eXo.env.portal.context instead. 

Patch file: EXOGTN-343.patch

Tests to perform

Reproduction test
Case to reproduce

    * Copy exo-store-assets-portlet.war into folder which contains war files (webapp folder for tomcat).
    * Start eXo server
    * Login as root
    * Go to Application Registry page
    * Add Light Box Tester to a category
    * Create a new page, add Light Box Tester portlet to the page
    * Save
    * Click on the image inside Light Box Tester: the image appears without any JQuery effect.

Tests performed at DevLevel
* As above. After applying the patch, there are some JQuery effects when clicking on an image.

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

    * Any customer project that uses this global variable should move to use "eXo.env.portal.context" instead (they should initialize their objects in callback in Browser.addOnLoadCallback function)
    * Any project that overrides UIPortalApplication.gtmpl template should also remove "currentContext" variable

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated on behalf of PM.

Support Comment
* Validated

QA Feedbacks
*
