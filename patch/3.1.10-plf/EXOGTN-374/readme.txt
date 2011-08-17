Summary

Status: Mime Type definition for file uploading
CCP Issue: CCP-734, Product Jira Issue: EXOGTN-374.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?

 Mime Type definition for file uploading
Fix description
How is the problem fixed?

 Create MimeTypeUploadPlugin. This is an external plugin for UploadSevice
 MimeTypeUploadPlugin will load a custom mime type (mimetypes.properties) and use it for detecting mime type of the file by extension file

Tests to perform
Reproduction test
* Steps to reproduce:
1)In site explorer, upload a first RTF created with MS wordpad and preview it without problem
2)Press Edit document
3)Within Edition form, Press the trash button to delete the document previously uploaded
4)In the field content, upload instead of it another RTF created also with MS wordpad and save as draft
5)Press Close
==>after doing some page refresh we are able to see the new RTF content but the icon of RTF file doesn't display correctly

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes
Documentation changes:
* Yes

Configuration changes
Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change
Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*
