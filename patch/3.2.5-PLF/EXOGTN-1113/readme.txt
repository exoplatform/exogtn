Summary

	* Issue title JCR Session unclosed in NavigationServiceWrapper.start()
	* CCP Issue:  n/a
	* Product Jira Issue: EXOGTN-1113.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* JCR Session unclosed in NavigationServiceWrapper.start()

Fix description

Problem analysis
	* A JCR session is opened to serve for the starting of service component. The session need to be invalidated once the starting is completed.

How is the problem fixed?
	* The leaked JCR session is closed in a finally block in start() method

Tests to perform

Reproduction test
	* Enable JCR Session Leak Detector
	* Start PLF server
    	-> Session Leak in in NavigationServiceWrapper.start()

Tests performed at DevLevel
	* n/a

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
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
	Function or ClassName change: 
	Data (template, node type) migration/upgrade: 

Is there a performance risk/cost?
	* N/a

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	* n/a
