Summary

    * Status: New bug on WEBUI
    * CCP Issue: CCP-1101, Product Jira Issue: EXOGTN-657.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * At the level of this component: exoplatform.webui.form.ext.UIFormInputSetWithAction

it is impossible to modify the attribute using the method isView setIsView
?
public void setIsView(boolean isView)
   {
      isView = isView;
   }

"this" must be added at the beginning, otherwise we should change the name of the input variable.

Fix description

How is the problem fixed?

    * Add key word "this" for used attributes.

Patch file: EXOGTN-657.patch

Tests to perform

Reproduction test

    * No

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

    * No

Is there a performance risk/cost?
* no

Validation (PM/Support/QA)

PM Comment
* Patch validated on behalf of PM.

Support Comment
* Patch validated.

QA Feedbacks
*
