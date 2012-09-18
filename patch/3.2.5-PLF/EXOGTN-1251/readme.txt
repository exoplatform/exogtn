Summary
	* Issue title: Be able to change other user's profile information 
	* CCP Issue: N/A
	* Product Jira Issue: EXOGTN-1251.
	* Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?
	* User's profile information can be changed by another user 

Fix description

Problem analysis
	* UIAccountProfiles is used to update current user profile, but it's using user name from the request, someone can modify the form data, and make a request to portal to change other user profile

How is the problem fixed?
	* Don't use the user name from the request parameter anymore, we can retrieve user name from current ConversationState on server

Tests to perform

Reproduction test
	* Go to User Profile editing form
	* Using some browser's development tool (like FireBug) to edit the Username's HTML Input element by remove readonly property and change its value to root
	* Change sensitive information as Email in the form.
	* Submit the form, and it actually saves the changes to root user instead of current one demo ==>> Failed

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

