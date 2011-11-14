Summary

    * Status: Possible NPE in org.gatein.portal.wsrp.structure.MOPConsumerStructureProvider
    * CCP Issue: N/A, Product Jira Issue: PLF:EXOGTN-566.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Method

public void addWindow(String windowName, String uuid)
      {
         // add suffix in case we have several windows with the same name in the page
         if (childrenWindows.containsKey(windowName))
         {
            if (windowName.endsWith("|"))
            {
               windowName += "|";
            }
            else
            {
               windowName += windowName + " |";
            }
         }

         childrenWindows.put(windowName, uuid);
      }

--> windowName can be null and we can have NPE with this line : windowName.endsWith("|")

Fix description

How is the problem fixed?

    * We simply replaced null title with a 'null' sequence

Patch file: EXOGTN-566.patch

Tests to perform

Reproduction test
* No

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
*

Documentation changes:
* No

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

    * The patch fixes current bug. However, with a triple of portlet windows (A, B, C) having special configuration as follows
      
      A.title = B.title
      B.title + "|" = C.title

      Then, MOPConsumerStructureProvider might eventually provide wrong information due to conflicted IDs. By the way, such scenario seems not to happen in real production.

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*
