Summary
	* Issue title: Error when drag & drop Page Body whilst editing layout of portal in IE7 
    	* CCP Issue: N/A
    	* Product Jira Issue: EXOGTN-1092.
    	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* UI error when Drag and Drop PageBody in IE7

Fix description

Problem analysis
	* IE7 browser doesn't render correctly DOM when Drag and Drop

How is the problem fixed?
	* Change Drag and Drop's flow to make IE7 refresh DOM after Drag and Drop terminates

Tests to perform

Reproduction test
	* Login as root.
	* Click Edit Layout of portal.
	* Remove all portlets in Portal page.
    	* Click on Portal page => the same error UI.

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
	* No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* No

Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* Function or ClassName change: No
	* Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	*

QA Feedbacks
	*

