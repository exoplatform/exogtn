Summary

    * Status: When upload a file with size = 0, UIFormUploadInput returns UploadResource as null
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-511.
    * Needed for ECMS-2392.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * When upload a file with size = 0, UIFormUploadInput returns UploadResource as null so we can not get the filename.

Fix description

How is the problem fixed?

    * When upload a file with size = 0, UploadService doesn't make file uploaded. UploadResource stores file path location but this file doesn't exist on physical disk. To fix this bug we check existed file on physical disk and make file if it is not existed.

Patch file: EXOGTN-511.patch

Tests to perform

Reproduction test
    * Also, in the UI, the progress bar stops working, and the Cancel button does not disappear.

Tests performed at DevLevel
    * When upload a file with size = 0, UIFormUploadInput return uploadResource as null so we can not get the file name to create node.

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

    * Validated on behalf of PM

Support Comment

    * Validated

QA Feedbacks
*
