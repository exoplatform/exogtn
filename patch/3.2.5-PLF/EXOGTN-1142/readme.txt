Summary
	* Issue title: Remember "My Login" doesn't work properly
	* CCP Issue:  N/A
	* Product Jira Issue: EXOGTN-1142.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* "Remember My Login" doesn't work properly

Fix description

Problem analysis
	* Use incorrectly login flow

How is the problem fixed?
	* Use correctly login flow and validity time

Tests to perform

Reproduction test
	* Go to http://localhost:8080/portal/intranet , open login form, check " Remember My Login", and login successfully.
	* Close the Web Browser, open it again.
	* Access to http://localhost:8080/portal/intranet, "Remember My Login" will work well. =>OK
	* Close the Web Browser, open it again.
	* Access to http://localhost:8080/portal/g/:platform:administrators/administration/registry/
		-> "Remember My Login" does not work, can not loggin automatically
		
Tests performed at DevLevel
	* Functional test

Tests performed at Support Level
	* n/a

Tests performed at QA
	* n/a

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* n/a

Changes in Selenium scripts 
	* n/a

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* No

Configuration changes

Configuration changes:
	*No

Will previous configuration continue to work?
	*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	* Function or ClassName change: 
	* Data (template, node type) migration/upgrade: 

Is there a performance risk/cost?
	* 

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	* 
