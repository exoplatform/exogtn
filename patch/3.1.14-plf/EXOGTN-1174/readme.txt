Summary

    * Issue title: Encrypt strongly rememberme password tokens by default
    * CCP Issue: N/A
    * Product Jira Issue: EXOGTN-1173.
    * Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?
    * User password associated with rememberme token must be encrypted before being persisted.

Fix description

Problem analysis
    * Password is encrypted by a symmetric cipher whose secret key is private to product owner.

How is the problem fixed?
    * Password is encrypted using AES ciphering by default. If product owner creates secret key via keytool and configure it via configuration.properties, that secret key is used to encrypte password token. Otherwise, a 128-bit AES key is generated dynamically (at the first bootstrap) for encryption.

Tests to perform

Reproduction test
    * Check if password is encrypted: 
	1. Open /portal
	2. Register
	3. Login and click Remember me
	4. Admin can check that the password is still in plain text (in portal-work workspace)

Tests performed at DevLevel
    * Unit test.

Tests performed at Support Level
    * Check if password is encrypted: 
	1. Open /portal
	2. Register
	3. Login and click Remember me
	4. Admin can check that the password is encrypted (in portal-work workspace)
    * Check if codeckey.txt is generated automatically:
	1. Go to http://localhost:8080/portal/, open login form, check " Remember My Login", and login successfully.
	2. Stop the server
	3. Remove tomcat/gatein/conf/codec/codeckey.txt file
	4. Restart the server, and new codeckey.txt file is created in tomcat/gatein/conf/codec directory.
	5. Access to http://localhost:8080/portal/classic
	6. Login form will appear => OK

Tests performed at QA
    * 

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
    * To add test cases for that feature.

Changes in Selenium scripts 
    * No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
    *

Configuration changes

Configuration changes:
	*

Will previous configuration continue to work?
*

Risks and impacts

Can this bug fix have any side effects on current client projects?
    * Function or ClassName change: no
    * Data (template, node type) migration/upgrade: no

Is there a performance risk/cost?
    * No

Validation (PM/Support/QA)

PM Comment
    * Patch validated on behalf of PM.

Support Comment
    * Patch validated

QA Feedbacks
    * 

