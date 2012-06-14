Summary
	* Issue title: Failed testRemoteGadget when build exogtn-3.2.x 
	* CCP Issue: N/A
	* Product Jira Issue: EXOGTN-1172.
	* Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* Testcase testRemoteGadget of TestGadgetRegistryService fails when Labpixies server has problem

Fix description

Problem analysis
	* The testcase fails because it cannot access gadget data from Labpixies server

How is the problem fixed?
	* If there is a network corruption, GadgetImporter will raise an IOException and then testcase catch this exception and print an ERROR logging. It means testcase will be passed but there will be an ERROR in console log

Tests to perform

Reproduction test
	* Build exogin without network -> Build fails due to testRemoteGadget 

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
	* 

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*

