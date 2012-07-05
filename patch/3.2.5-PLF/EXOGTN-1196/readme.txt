Summary

    * Issue title: Change the filter mapping of LocalizationFilter in web.xml of portal war
    * CCP Issue:  N/A
    * Product Jira Issue: EXOGTN-1196.
    * Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?

    * RequestWrapper needs to extend ServletRequestWrapper (servlet API), this will not allow us to share http session between war anymore.
      We need to provide a workaround to make some features of upper modules in Platform work properly.

Fix description

Problem analysis

    * After a request passes through LocalizationFilter, it will be wrapped. Then when it tries to get the http session from the portlet, application server will detect that this is a call in another context, it needs to create a new http session instance.

How is the problem fixed?

    * LocalizationFilter is used for jsp only, and currently it wraps the request, so we can change the url-pattern of LocalizationFilter to just /*.jsp instead of /*. For now, we temporarily allow share session between war.

Tests to perform

Reproduction test

    * Cf. KS-4505, PLF-3363

Tests performed at DevLevel

    * 

Tests performed at Support Level

    * Cf. KS-4505, PLF-3363

Tests performed at QA

    * 

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

    * No

Configuration changes

Configuration changes:

    * Yes - change the web.xml of portal.war

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Patch validated on behalf of PM.

Support Comment

    * Patch validated.
QA Feedbacks

    * 
