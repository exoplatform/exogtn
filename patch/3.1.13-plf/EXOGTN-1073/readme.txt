Summary

    * Issue title: Remove external gadget in default packaging
    * CCP Issue:  CCP-1255 
    * Product Jira Issue: EXOGTN-1073.
    * Complexity: low

Proposal

 
Problem description

What is the problem to fix?

    *  Remove external gadget in default packaging

Fix description

Problem analysis

    *  By default, Currency gadget is defined in eXoGadgets/WEB-INF/gadget.xml. It's an external gadget and requires Internet connection. 
       When deploying in no internet environment, there's an error.
      In the packaging provided by default, an external gadget are defined in eXoGadgets/WEB-INF/gadget.xml:

<gadget name="Currency">
<url>http://www.donalobrien.net/apps/google/currency.xml</url>
</gadget>

When we deploy the platform in internal network (without internet connection), an error in the console are logged:

SEVERE: Cannot import gadget http://www.donalobrien.net/apps/google/currency.xml because its data could not be found

Most of case, in customer environment there is not internet connection, we should remove this external gadget in default packaging.

How is the problem fixed?

    *  Remove Currency gadget to avoid errors when deploying in no internet environment.

Tests to perform

Reproduction test

    * Steps to reproduce:
      + Disconnect from Internet.
      + Start Platform server -> error message in the server console.
        SEVERE: Cannot import gadget http://www.donalobrien.net/apps/google/currency.xml because its data could not be found

Tests performed at DevLevel
* Cf. above

Tests performed at Support Level
* Cf. above

Tests performed at QA
*
Changes in Test Referential

Changes in SNIFF/FUNC/REG tests

    *  No

Changes in Selenium scripts 

    *  No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

    *  No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no
    * Data (template, node type) migration/upgrade: no

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Validated

Support Comment

    * Validated

QA Feedbacks

    * ...
