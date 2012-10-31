Summary

    Issue title Make UI validation configurable
    Product Jira Issue: EXOGTN-1249.
    Complexity: Medium

Proposal

 
Problem description

What is the problem to fix?

    The pattern of UI validators are hard coded in Java files. System administrators or developers has no way to adapt it for their needs.

Fix description

Problem analysis

    Message and pattern of existing validators are hard-coded in Java files.
    To define a new validator, developer needs to implement Validator interface from scratch.

How is the feature implemented?
* Provide new architecture for Validators
  - AbstractValidator: This new abstract class implements the existing Validator interface. It defines the common functions for all Validator classes. Other validators will extend either directly this class or its child instead of implementing Validator interface.
  - MultipleConditionsValidator: This new abstract class extends AbstractValidator. A validator using several conditions together should extend this class.
  - UserConfigurableValidator: This new class extends MultipleConditionsValidator. It also takes charge in reading validator configuration parameters inside configuration.properties.
    + Define user validator parameter by introducing ValidatorConfiguration class (1 private static class of UserConfigurableValidator) having 4 attributes: minimal length, maximal length, configuration regular expression and resource bundle key for message error.  
      o minimal length of the field. The default value is 0.
      o maximum length of the field. The  default value is Integer.MAX_VALUE.
      o Regular expression to which values of the validated field must conform. The default value is the constant Utils.USER_NAME_VALIDATOR_REGEXP defined in org.exoplatform.portal.pom.config.Utils.
      o Information message to display when the value of the validated field does not conform to the specified regular expression. The default value is the configuration regular expression.

These parameters can be customized via the corresponding properties gatein.validators.{configuration}.length.min, gatein.validators.{configuration}.length.max, gatein.validators.{configuration}.regexp and gatein.validators.{configuration}.format.message in configuration.properties. 

* Refactor the validators in EXOGTN project to keep backward compatibility
  - The validators which have only a condition, such as  NameValidator and StringLengthValidator, extend AbstractValidator.
  - The validators having multiple conditions, such as UsernameValidator, EmailAddressValidator,  extend MultipleConditionsValidator. 

* Changes in classes to customize message error
  - AbstractApplicationMessage: new abstract class to define common function.
  - ApplicationMessage: Refactored class which extends from AbstractApplicationMessage. This class is used to handle message for single-condition validators.
  - CompoundApplicationMessage: new class extending AbstractApplicationMessage. It handles message error for multiple-condition validators.

* Make username validation configurable
  Modify addValidator parameter to validate username and group membership name by UserConfigurableValidator instead of using their proper validators.
  - Until now, username field when it is created/modified is validated by UsernameValidator in UIRegisterInputSet.java (register a new account) and UIAccountEditInputSet.java (edit a user account) but system administrator cannot customize its constraints (length and pattern). With  UserConfigurableValidator, system administrator can now customize the validation of user name field.
  - Until now, user name field in group membership management form is validated by ExpressionValidator in UIGroupMembershipForm.java. With UserConfigurableValidator, system administrator can now customize the validation of user name in this context.

Tests to perform

Reproduction test
* The username when creating a new account, modify an existing account or adding an account to a group must satisfy as follows:
  - Length must be between 3 and 30 characters.
  - Only lowercase letters, numbers, undescores (_) and period (.) can be used.
  - No consecutive undescores (_) or period (.) can be used.
  - Must start with a letter.
  - Must end with a letter or number.
We can't therefore have a username which doesn't conform these rules.

Tests performed at DevLevel
* Unit tests: TestWebuiValidator

Tests performed at Support Level
* Integration tests with the default and some customized configuration of username in length limit and pattern (allow hyphen, or email as username).

Tests performed at QA
* N/A

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
* New test cases 

Changes in Selenium scripts 
* Scripts for new test cases

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
* Gatein Reference guide (DOC-2393)

Configuration changes

Configuration changes:
* No.

Will previous configuration continue to work?
* Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Any change in API (name, signature, annotation of a class/method)?
  No, the new architecture ensures backward compatibility.
* Data (template, node type) upgrade:
  No.

Is there a performance risk/cost?
* N/A
