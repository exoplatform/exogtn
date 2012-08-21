Summary
	* Issue title: Cannot DnD Popup message  
	* CCP Issue:  N/A
	* Product Jira Issue: EXOGTN-1140.
	* Complexity: N/A

Proposal

Problem description
What is the problem to fix?
	* GateIn webui's popup messages could not DnD anymore

Fix description

Problem analysis
	*The method that init DnD event for popup message is not called

How is the problem fixed?
	* Call the init DnD method to make popup message dragable when it's shown

Tests to perform
Reproduction test
	* Login as John
	* Click Edit page on home page.
	* Drag and drop a portlet.
	* Click "Abort" on Page Editor
	* Confirm Message popup will be displayed. And we can drag and drop this Popup message.

Tests performed at DevLevel
	*

Tests performed at Support Level
	*

Tests performed at QA
	* 

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
	* No

Changes in Selenium scripts 
	*No

Documentation changes
Documentation (User/Admin/Dev/Ref) changes:
	*No

Configuration changes
Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts
Can this bug fix have any side effects on current client projects?

Function or ClassName change: No
Data (template, node type) migration/upgrade: No
Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)
PM Comment
	* Validated 

Support Comment
	* 

QA Feedbacks
	*
