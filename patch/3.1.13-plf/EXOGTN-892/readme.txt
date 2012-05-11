Summary
	* Issue title Invalidate HttpSession on Logout
	* CCP Issue:  CCP-1146 
	* Product Jira Issue: EXOGTN-892.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* Invalidate HttpSession on Logout

Fix description

Problem analysis
	* After user logout, all session of webapp must be invalidated

How is the problem fixed?
	* Use logout feature of WCI to help invalidate all session of webapp

Patch file: EXOGTN-892.patch

Tests to perform

Reproduction test
    Steps to reproduce:
    -Connect as user A
    -Add the https://jira.exoplatform.org/secure/attachment/46359/test-portlet.zip in your site.
    -Fell the input text
    -Click on submit button
     the value is set in portlet session attributes.
    -Logout.
    -Reconnect as user A.
    -The test-portlet retain the last value.
    the problem is not reproduced with tomcat. 

Tests performed at DevLevel
	*

Tests performed at Support Level
	*

Tests performed at QA
	*

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* N/A

Changes in Selenium scripts 
	* N/A

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
	* Function or ClassName change: 
	* Data (template, node type) migration/upgrade: 

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated on behalf of PM

Support Comment
	* Validated

QA Feedbacks
	*
