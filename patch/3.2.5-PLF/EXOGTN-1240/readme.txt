Summary
	* Issue title: Form submission of sensitive actions in PLF still does not have validation mechanism to prevent XSRF 
	* CCP Issue:  N/A
	* Product Jira Issue: EXOGTN-1240.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* GateIn webui framework should support machanism to prevent CSRF attack

Fix description

Problem analysis
	* Our action URL and form don't have security token, some one can create his own form and submit it to portal, we can't check if a request is from portal or from another domain

How is the problem fixed?
	* Add csrfCheck boolean option to EventConfig, so URL or html form which use that Webui event listener will have csrf token, and it will be check by portal before the action is executed

Tests to perform

Reproduction test
	* Login as John
	* Open Users and Groups Manager/User Management
	* Click Edit User Info of John (here the value of Email field is still "john@localhost")
	* Open another tab on the same browser
	* Open attached html file (csrf_portalUserInfo.html) after update the value into the form action parameter
	* Click on [Submit Querry] button
    	
	Result:
    	* "Email" field's value has been changed from "john@localhost" to "john.smith@exoplatform.com"

Tests performed at DevLevel
	* c/f above

Tests performed at Support Level
	*

Tests performed at QA
	*

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	*

Changes in Selenium scripts 
	*

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
	* Change in the name of a class/method: No
	* Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*
