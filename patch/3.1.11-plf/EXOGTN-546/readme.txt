Summary

    * Status: Can't delete a user with Oracle database
    * CCP Issue: CCP-1036, Product Jira Issue: EXOGTN-546.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Can't delete a user with Oracle database

Fix description

How is the problem fixed?

    * Remove/disable some code duplication during deleting memberships of user

Patch file: EXOGTN-546.patch

Tests to perform

Reproduction test
Step to reproduce: 
If an Oracle database is used, we cannot delete an user with the organization portlet in eXoPlatform 3.0.5.

    * Login as root/gtn.
    * Add new user.
    * Try to delete a user with the organization portlet.
      The following error occurs :

      Caused by: java.sql.BatchUpdateException: ORA-02292: violation de contrainte (EXO1.FKC6F8C733577FACCB) d'intégrité - enregistrement fils existant
       
              at oracle.jdbc.driver.OraclePreparedStatement.executeBatch(OraclePreparedStatement.java:10296)
              at oracle.jdbc.driver.OracleStatementWrapper.executeBatch(OracleStatementWrapper.java:216)
              at org.apache.tomcat.dbcp.dbcp.DelegatingStatement.executeBatch(DelegatingStatement.java:297)
              at org.apache.tomcat.dbcp.dbcp.DelegatingStatement.executeBatch(DelegatingStatement.java:297)
              at org.hibernate.jdbc.BatchingBatcher.doExecuteBatch(BatchingBatcher.java:70)
              at org.hibernate.jdbc.AbstractBatcher.executeBatch(AbstractBatcher.java:2

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM

Support Comment
* Patch validated.

QA Feedbacks
*
