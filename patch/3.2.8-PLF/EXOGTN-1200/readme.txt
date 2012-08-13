Summary
	* Issue title:  Cannot login to workspaces with Crash on JBoss server  
	* CCP Issue: N/A
	* Product Jira Issue: EXOGTN-1200.
	* Complexity: N/A

Proposal

Problem description
What is the problem to fix?
	* NPE when login by CLI (using CRASH)

Fix description

Problem analysis
	* PortalLoginModule doesn't check if HttpServletRequest is null 

How is the problem fixed?
	* Check if this is CLI (HttpServletRequest is null) then bypass to other login module

Tests to perform

Reproduction test
* In command line: 
	telnet localhost 5000
	% repo use container=portal
	% ws login -u root -p gtn portal-system
* Unexpected exception: Login failed for root javax.security.auth.login.LoginException

Tests performed at DevLevel
	* Reproduction test

Tests performed at Support Level
	* Reproduction test

Tests performed at QA
	*  Reproduction test

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
	* Validated on behalf of PM

Support Comment
	* Validated
