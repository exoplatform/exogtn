Summary
	* Issue title: In editing container layout of page, applications disappear after adding new column
    	* CCP Issue:  CCP-1459 
    	* Product Jira Issue: EXOGTN-1228.
    	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* Applications disappear when adding a column in column container mode.

Fix description

Problem analysis
	* In edit mode, portlet will only render its decorator, not its content. When there is ajax update, this behavior will remove the portlet from column container.

How is the problem fixed?
	* When we add column container, we should render the table container (this update already contains portlet's decorator), we don't need to render again the child portlet.

Tests to perform

Reproduction test
	* Create a new page
	* Add a 3-column container with 3 applications then save it
	* Modify the page layout (Edit > Page > layout)
	* Go to "container mode" (clicking on "Containers" in the Page Editor)
	* Go to any column, click on "column" and then on "insert left" or "insert right"
	-> all application boxes disappear, user thinks that he has lost all his work.

Tests performed at DevLevel
	* c.f above

Tests performed at Support Level
	* Same as reproduction test

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
	* Validated

QA Feedbacks
	*
