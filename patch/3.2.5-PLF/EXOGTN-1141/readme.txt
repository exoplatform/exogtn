Summary
	* Issue title:  canEdit option of dashboard works not properly
	* CCP Issue:  N/A
	* Product Jira Issue: EXOGTN-1061
	* Complexity: N/A

The Proposal

Problem description

What is the problem to fix?
	* In dashboard, we have condition (canEdit) for showing "Add Gadgets" option or not. It shows if current user has edit permission on the page owning the dashboard portlet. But now, this option doesn't work properly, it's being based on access permission.

Fix description

How is the problem fixed?
	* Using edit permission instead of access permission to validate canEdit permission in dashboard portlet
	* Check edit permission from server side to show or hide button

Tests to perform

Reproduction test
	* Add a new page and edit page's properties
	* Select "everyone can access" for Access Permission Settings
	* Select /platform/administrators/manager for Edit Permission Settings.
	* Add a Dashboard porlet to this page
	* Log in as Mary and open the page. 
	* Mary can use "Add Gadgets" function although she doesn't have edit permission: not OK.
	* Mary can delete, minimize and edit Gadget: not OK

Tests performed at DevLevel
	* n/a
	
Tests performed at QA/Support Level
	* n/a
	
Documentation changes

Configuration changes
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* No

Function or ClassName change
	* No
	
Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment:
	* Validated

Support Comment:
	* Validated
	
QA Feedbacks
	* 
