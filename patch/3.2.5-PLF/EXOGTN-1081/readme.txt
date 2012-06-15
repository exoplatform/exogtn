Summary
	* Issue title:  No scroll bar to navigate groups in Group Navigation in IE 7
	* CCP Issue:  N/A 
	* Product Jira Issue: EXOGTN-1081.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* No scroll bar to navigate groups in Group Navigation in IE7 

Fix description

Problem analysis

How is the problem fixed?
	* scrollHeight property is not exact in IE7, so we need to use offsetHeight property
	* We must use <div width="99%"> to make compatibility for IE7, this use to force IE7 display scrollbar

Tests to perform

Reproduction test
	* Steps to reproduce:
	* Login with john
	* Go to Group Navigation by clicking on Group:
        	* In FF, we have a scroll bar to navigate all groups: OK
        	* In IE7, the scroll bar doesn't display. We cannot navigate to all groups: NOK

Tests performed at DevLevel
	* c/f above

Tests performed at Support Level
	* c/f above

Tests performed at QA
	* n/a

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
	* Function or ClassName change:  n/a
	* Data (template, node type) migration/upgrade: n/a 

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated on behalf of PM

Support Comment
	* Validated

QA Feedbacks
	*
