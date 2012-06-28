Summary

    * Issue title: Open an "Identity Transaction" only when it is necessary
    * CCP Issue:  N/A
    * Product Jira Issue: EXOGTN-1162.
    * Complexity: medium

Proposal

 
Problem description

What is the problem to fix?

    * Open an "Identity Transaction" only when it is necessary.

Fix description

Problem analysis

    * For the moment, the Organization service component triggers a transaction (Identity Transaction) for every Http request to portal even if handling such request need not Organization service. As IdentityTransaction is quite expensive, we need to trigger it only if the Http request requires Organization service. 

How is the problem fixed?

    * Add "lazyStartOfHibernateTransaction" configuration
    * Upgrade to the new version of picketlink - 1.3.2.CR01 which introduces the fix deeper inside of PLIDM

Tests to perform

Reproduction test

    * See detail in PLF-3248

Tests performed at DevLevel

    * 

Tests performed at Support Level

    * 

Tests performed at QA

    * Cf. PLF-3248

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests

    * ...

Changes in Selenium scripts 

    * ...

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

    * Change in reference guide for GateIn - IDM integration configuration

Configuration changes

Configuration changes:

    * Add "lazyStartOfHibernateTransaction" configuration for picketlink-idm configuration file.

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?

    * There will be performance improvement.


Validation (PM/Support/QA)

PM Comment
    * Patch validated on behalf of PM.

Support Comment
    * Patch validated

QA Feedbacks
    * 
