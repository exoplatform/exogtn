Summary

	* Issue title: User management paginator is wrong when connected to ldap 
	* CCP Issue:  N/A
	* Product Jira Issue: EXOGTN-1130.
	* Complexity: N/A

Proposal

Problem description

What is the problem to fix?
	* User management paginator is wrong when connected to ldap 

Fix description

Problem analysis
	* GateIn performs whole non paginated query to LDAP server, this can affect performance and get wrong paginated list size info

How is the problem fixed?
	* Add "countPaginatedUsers", which is configurable in the idm-configuration.xml file. User list will be filtered before returning search page


Tests to perform

Reproduction test
	* Use picketlink configurations to have my platform 3.5.3 connected to openldap.
    	* At the beginning I have 5 users (root, john, james, demo, mary) and nothing is wrong with user management page.
    	* Then I create 6 more users, so I have 11 in total. 10 users per page. It comes wrong when it shows 3 pages

Tests performed at DevLevel
	*

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
	* Need update on reference guide for PicketLinkIDMOrganizationService configuration

Configuration changes

Configuration changes:
	* Yes

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
