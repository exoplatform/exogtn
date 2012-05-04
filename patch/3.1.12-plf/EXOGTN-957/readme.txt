Summary

    * Status: JBoss Packaging
    * CCP Issue: CCP-1173, Product Jira Issue: EXOGTN-957.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * JBoss Packaging

Fix description

How is the problem fixed?

    * Move context.xml file from META-INF folder to WEB-INF folder in Jboss server.
    * Update the appropriate class name of Realm for Jboss.

Tests to perform

Reproduction test

    * Check errors in current context.xml file in web/rest/src/main/webapp/WEB-INF/context.xml

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * Change the position of context.xml file in Jboss server. Update class name of Realm in the file.
    * If rest.war is renamed, tomcat/conf/Catalina/localhost/rest.xml needs to be renamed appropriately to avoid exceptions.

Will previous configuration continue to work?
    * yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* Validated on behalf of PM.

Support Comment
* Validated

QA Feedbacks
*
