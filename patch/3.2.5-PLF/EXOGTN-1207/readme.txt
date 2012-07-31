Summary
	* Issue title: Issue in gadget image URL when repository name changes  
	* CCP Issue:  CCP-1377 
	* Product Jira Issue: EXOGTN-1207.
	* Complexity: N/A

Proposal

Problem description
What is the problem to fix?
	* URL of gadget thumbnail image is persisted into JCR, as there is change on repository name, that URL does not point to any resource*

Fix description
Problem analysis
	* The fact that URL of gadget thumbnail image is persisted into database is the root cause

How is the problem fixed?
	* The URL of gadget thumbnail is not persisted into database anymore, it is determined dynamically at runtime

Tests to perform
Reproduction test
	* Start server
	* Shutdown server
	* Change repository name in configuration.properties with gatein.jcr.repository.default=repository1
	* Restart server
 	* All images URL's will be broken.
Tests performed at DevLevel
	* 

Tests performed at Support Level
	* 

Tests performed at QA
	*

Changes in Test Referential
Changes in SNIFF/FUNC/REG tests
	* Yes

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

Function or ClassName change: No
Data (template, node type) migration/upgrade: No


Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)
PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*
