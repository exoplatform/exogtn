Summary
    Search result popup remains opened in Community management portlet 
    Product Jira Issue: EXOGTN-1299.
    Complexity: low

Proposal
 
Problem description

What is the problem to fix?
* A popup remains in User management portlet after a no result search. 

Fix description

Problem analysis
* The old search runs automatically when user goes to User management portlet. 

How is the problem fixed?
* Keep old search result in User management, but don't run old search automatically when return to this page.

Tests to perform

Reproduction test
Case 1: 

     Login as John
     Go to Users-> Groups and roles
     search for "toto", a popup appears saying "No Result found".
     Click "OK" to close the popup.
     return to the acme page
     Return to User management portlet
    Expected result: popup doesn't appear again.

Case 2: 

    Click search for "toto" again. 
    Close popup
    Search for "John"
    Expected result: found user John in search result list

Case 3: 

    Edit user John information
    Click Save or Cancel
    Expected result: there is only user John in search result, not all users.

Tests performed at DevLevel
* Cf. above

Tests performed at Support Level
* Cf. above

Tests performed at QA
* 

Changes in Test Referential
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
* Any change in API (name, signature, annotation of a class/method)? No
* Data (template, node type) upgrade: no

Is there a performance risk/cost?
* No
