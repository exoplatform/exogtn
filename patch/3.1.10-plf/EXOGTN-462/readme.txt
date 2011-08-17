Status: Upload tooltip isn't translated in French

CCP Issue: N/A, Product Jira Issue: EXOGTN-462.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?

 Upload tooltip isn't translated in French

Fix description
How is the problem fixed?

  Replace hard-coded texts by appropriate keys of ResourceBundle file

Tests to perform
Reproduction test
*Step to reproduce:

Login
Change language to French
Go to Sites Explorer/Sites Management/acme/document
Click Upload
Click browse & select file
Click Upload icon
=> Upload tooltip "Remove Uploaded" isn't translated in French
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

Function or ClassName change
Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM
Support Comment
* Support review: Patch validated

QA Feedbacks
*
