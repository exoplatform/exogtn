Summary

    * Status: Translation of navigation does not work for language with country variant
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-395.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Translation of navigation does not work for language with country variant

Fix description

How is the problem fixed?

    * For language with country variant, we load ResourceBundle with locale including both language and its variant.

Patch file: EXOGTN-395.patch

Tests to perform

Reproduction test

   1. Login
   2. Change language to
          * 'Simplified Chinese' (zh_CN) ( Traditional Chinese(zh_TW) or Portuguese Brazil (pt_BR) ) -> the navigation, breadcrumb portlets and functions at Site or Group level still show the navigation elements in English. All labels are translated into languages corresponding in files .xml or .properties but cannot be displayed.
          * 'Korean' (or any other which is just defined as 'language') -> Well display

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* Validated on behalf of PM

Support Comment
* Validated by Support

QA Feedbacks
*

