Summary

    * Status: Unable to load IDE gadget at startup
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-346.
    * Needed for: PLF-1300
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
At startup, Weblogic doesn't load the IDE gadget, with the following exception:

20 avr. 2011 10:26:57 org.gatein.common.logging.Logger log
INFO: Importing gadget name=IDE description=IDE thumbnail= title= title=IDE
20 avr. 2011 10:26:59 org.gatein.common.logging.Logger log
GRAVE: Could not process gadget file file:/opt/wls1034_dev/user_projects/domains/exoplatform_domain/servers/AdminServer/tmp/_WL_user/exoplatform/kvrbhp/war/WEB-INF/gadget.xml
java.lang.NullPointerException
at org.exoplatform.application.gadget.LocalImporter.visitChildren(LocalImporter.java:230)
at org.exoplatform.application.gadget.LocalImporter.visit(LocalImporter.java:223)
at org.exoplatform.application.gadget.LocalImporter.visitChildren(LocalImporter.java:232)
at org.exoplatform.application.gadget.LocalImporter.doImport(LocalImporter.java:174)
at org.exoplatform.application.gadget.GadgetDeployer.handle(GadgetDeployer.java:139)
at org.exoplatform.application.gadget.GadgetDeployer.access$000(GadgetDeployer.java:53)
at org.exoplatform.application.gadget.GadgetDeployer$1.execute(GadgetDeployer.java:92)
at org.exoplatform.container.RootContainer.executeInitTasks(RootContainer.java:618)
at org.exoplatform.container.RootContainer.createPortalContainer(RootContainer.java:363)
at org.exoplatform.container.RootContainer.createPortalContainers(RootContainer.java:249)
at org.exoplatform.container.web.PortalContainerCreator.contextInitialized(PortalContainerCreator.java:57)
at weblogic.servlet.internal.EventsManager$FireContextListenerAction.run(EventsManager.java:481)
at weblogic.security.acl.internal.AuthenticatedSubject.doAs(AuthenticatedSubject.java:321)
at weblogic.security.service.SecurityManager.runAs(SecurityManager.java:120)
at weblogic.servlet.internal.EventsManager.notifyContextCreatedEvent(EventsManager.java:181)
at weblogic.servlet.internal.WebAppServletContext.preloadResources(WebAppServletContext.java:1872)
at weblogic.servlet.internal.WebAppServletContext.start(WebAppServletContext.java:3153)
at weblogic.servlet.internal.WebAppModule.startContexts(WebAppModule.java:1508)
at weblogic.servlet.internal.WebAppModule.start(WebAppModule.java:482)
at weblogic.application.internal.flow.ModuleStateDriver$3.next(ModuleStateDriver.java:425)
at weblogic.application.utils.StateMachineDriver.nextState(StateMachineDriver.java:52)
at weblogic.application.internal.flow.ModuleStateDriver.start(ModuleStateDriver.java:119)
at weblogic.application.internal.flow.ScopedModuleDriver.start(ScopedModuleDriver.java:200)
at weblogic.application.internal.flow.ModuleListenerInvoker.start(ModuleListenerInvoker.java:247)
at weblogic.application.internal.flow.ModuleStateDriver$3.next(ModuleStateDriver.java:425)
at weblogic.application.utils.StateMachineDriver.nextState(StateMachineDriver.java:52)
at weblogic.application.internal.flow.ModuleStateDriver.start(ModuleStateDriver.java:119)
at weblogic.application.internal.flow.StartModulesFlow.activate(StartModulesFlow.java:27)
at weblogic.application.internal.BaseDeployment$2.next(BaseDeployment.java:636)
at weblogic.application.utils.StateMachineDriver.nextState(StateMachineDriver.java:52)
at weblogic.application.internal.BaseDeployment.activate(BaseDeployment.java:205)
at weblogic.application.internal.EarDeployment.activate(EarDeployment.java:58)

Fix description

How is the problem fixed?

    * There is a difference between WebLogic and other servlet containers, so we return Collections.EMPTY_SET to avoid NullPointerException.

Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* Validated on behalf of PM

Support Comment
* Validated

QA Feedbacks
*

