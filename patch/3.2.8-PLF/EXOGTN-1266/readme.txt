Summary

    Issue title: User's search box takes too much time to display
    Product Jira Issue: EXOGTN-1266.
    Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?
* Improve the performance when searching in User Management portlet with large user collection.

Fix description

Problem analysis
* Server is forced to load all users before processing it. 

How is the problem fixed?
* Use lazy load service to display appropriated part of list users.

Tests to perform

Reproduction test
*   Initialize many users in DB (~ 1 million)
*   Go to User Management
*   In a specific group, type a username in the search box
*   Click on search icon
*   Check the interval to display user list

Tests performed at DevLevel
* Quick check with PLF server and Open DS 2.0 with 500 and 2000 users

Tests performed at Support Level
* Cf. above

Tests performed at QA
* Response time and stability 

Changes in Test Referential
* No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
* No

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Any change in API (name, signature, annotation of a class/method)?: No
* Data (template, node type) upgrade: No

Is there a performance risk/cost?
* Improve performance
