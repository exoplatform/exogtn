Summary

    * Status: No difference between the two selectboxes: objectId is always null on UIFormSelectBox
    * CCP Issue: CCP-1081, Product Jira Issue: EXOGTN-597.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * objectId is always null on UIFormSelectBox

Fix description

How is the problem fixed?

    * Get ID of selected box and return this ID in Onchange event.

Patch file: EXOGTN-597.patch

Tests to perform

Reproduction test

    * Steps to reproduce:
      1. we have added two select box from The Form Generator Portlet
      ( Groups --> Form Generator on the Administration bar)
      2. we add two select boxes
      3. we add this property "onchange=true" to this line
      
      String[] select_1FieldName = ["jcrPath=/node/exo:fg_p_select_1", "defaultValues=item 1", "", "options=item 1,item 2", "onchange=true"];

      4. You can use the FireBug to see that there is no difference between the two selectBox as we can see here:
      The first:
      
      <select onchange="javascript:eXo.webui.UIForm.submitEvent('4ab67916-d73a-4f2d-8450-d005c07329b2#UIDocumentForm','Onchange','&amp;objectId=null')" name="/node/Select 1" class="selectbox">
            <option value="item 1">item 1</option>
            <option value="item 2">item 2</option>
            </select>

      The second
      
      <select onchange="javascript:eXo.webui.UIForm.submitEvent('4ab67916-d73a-4f2d-8450-d005c07329b2#UIDocumentForm','Onchange','&amp;objectId=null')" name="/node/Select 2" class="selectbox">
            <option value="item 1">item 1</option>
            <option value="item 2">item 2</option>
            </select>

      5. And we note that objectId=null

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

    * No

Validation (PM/Support/QA)

PM Comment

    * Patch validated on behalf of PM.

Support Comment

    * Patch validated

QA Feedbacks
*
