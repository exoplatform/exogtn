Summary

    * Issue title: WebAppController needs to start RequestLifeCycle for every processing (including static resource processing) 
    * CCP Issue: N/A
    * Product Jira Issue: EXOGTN-1075.
    * Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?

    * WebAppController needs to start RequestLifeCycle for every processing (including static resource processing)

Fix description

Problem analysis

    * In source codes of WebAppController:

if (!started)
              {

                 RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
                 started = true;
              }
.....
              processed = handler.execute(new ControllerContext(this,
              router, req, res, parameters));
.....
             if (started)
              {
                 RequestLifeCycle.end();
              }

This means that we need to start RequestLifeCycle for processing of every resource including static resource. And startup of some services
is quite expensive (like startup of OrganizationService requires startup of Hibernate transaction). In other words, currently we are starting
Hibernate transaction for processing images and other static resources.

How is the problem fixed?

    *  Introduce abstract method getRequireLifeCycle on WebRequestHandler.
       It returns false for: DefaultRequestHandler, StaticResourceRequestHandler, DownloadHandler, UploadHandler (so these handlers do not require RequestLifeCycle)
       It returns true for: PortalRequestHandler, LegacyRequestHandler (so these handlers require RequestLifeCycle)

Tests to perform

Tests performed at DevLevel
* N/A

Tests performed at Support Level
* 

Tests performed at QA
* N/A

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

    * Function or ClassName change: no
    * Data (template, node type) migration/upgrade: no

Is there a performance risk/cost?

    * Performance could be improved after the problem is fixed.

Validation (PM/Support/QA)

PM Comment

    * Validated on behalf of PM.

Support Comment

    * Validated

QA Feedbacks

    * 
