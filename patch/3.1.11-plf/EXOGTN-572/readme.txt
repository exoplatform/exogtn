Summary

    * Status: Listeners are not executed after updating a user's group
    * CCP Issue: CCP-1069, Product Jira Issue: EXOGTN-572.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Listeners are not executed after updating a user's group

Fix description

How is the problem fixed?

    * Modify portlet/exoadmin/src/main/java/org/exoplatform/organization/webui/component/UIGroupForm.java file to activate listeners after saving Group:
      
      service.getGroupHandler().saveGroup(currentGroup, true);

Patch file: EXOGTN-572.patch

Tests to perform

Reproduction test

    * Steps to reproduce:
      1. Add exo.samples.Listner-1.0.0-SNAPSHOT.jar to lib folder in EXOGTN Tomcat
      2. Go to Group/Users and groups management
      3. Select a group (e.g Platform/Administrators)
      4. Edit the group and Save
      No messages indicated in server console: not OK

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*

Configuration changes

Configuration changes:
*

Will previous configuration continue to work?
*

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?
*

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Patch validated.

QA Feedbacks
*
