Summary
	* Issue title: List of sites is duplicated in menu 
	* CCP Issue:  CCP-1430 
	* Product Jira Issue: EXOGTN-1215.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* List of sites is duplicated in menu. When the list of sites exceed 10, 12 for example, the menu bar displays 12 but the sites are duplicated.

Fix description

Problem analysis
	* Wrong loop in org.exoplatform.portal.pom.config.tasks.SearchTask class

How is the problem fixed?
	* Make the loop work properly : traverse all elements

Tests to perform

Reproduction test
	* List of sites is duplicated in menu. When the list of sites exceed 10, 12 for example, the menu bar displays 12 but the sites are duplicated.

Tests performed at DevLevel
	*

Tests performed at Support Level
	*

Tests performed at QA
	*

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* N/a

Changes in Selenium scripts 
	* N/a

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
	* No

Is there a performance risk/cost?
	* No

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*
