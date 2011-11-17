Summary

    * Status: Problem with gadgets when the host name includes the word "portal"
    * CCP Issue: CCP-1114, Product Jira Issue: EXOGTN-742.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Problem with gadgets when the host name includes the word "portal"

Fix description

How is the problem fixed?

    * Get gadget server address by phrasing URI, instead of phrasing URL

Patch file: EXOGTN-742.patch

Tests to perform

Reproduction test
Steps to reproduce:

    * Add a new name host to the OS including the word portal (for example: portal-local)
    * Run the Gatein server and log in as root
    * Add a page (test) with gadgets
      + if we use the following url: http://localhost:8080/portal/private/classic/test => the gadgets are displayed without any problem.
      + if we use the following url: http://portal-local:8080/portal/private/classic/test => the gadgets are not displayed and we get the exception
      
      org.exoplatform.groovyscript.TemplateRuntimeException: Groovy template exception at DataText[pos=Position[col=1,line=10],data=      uicomponent.renderChild(UIGadgetViewMode.class) ;] for template app:/groovy/gadget/webui/component/UIGadgetPortlet.gtmpl
          at org.exoplatform.groovyscript.GroovyScript.buildRuntimeException(GroovyScript.java:178)
          at org.exoplatform.groovyscript.GroovyScript.render(GroovyScript.java:121)
          at org.exoplatform.groovyscript.GroovyTemplate.render(GroovyTemplate.java:118)
          at org.exoplatform.groovyscript.text.TemplateService.merge(TemplateService.java:117)
      ...

Tests performed at DevLevel

    * C.f above

Tests performed at QA/Support Level

    *

Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * Yes

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?

    * N/A

Validation (PM/Support/QA)

PM Comment

    * Validated on behalf of PM

Support Comment

    * Validated

QA Feedbacks
*
