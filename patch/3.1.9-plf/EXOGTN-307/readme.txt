Summary

    * Status: Add page wizard in French
    * CCP Issue: N/A, Product Jira Issue: EXOGTN-307.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

Choose French.
Go to Site Editor -> Add Page Wizard.
In the second step of page creation, Page Layout Template names are not well translated: see AddPage-step2.png

    * Configs Page -> Configuration de la page
    * Configs Page en Colonnes -> Configuration de la page en colonnes
    * Configs Page en Lignes -> Configuration de la page en lignes
    * Configs Page en Onglets -> Configuration de la page en onglets
    * Configs Page Mixte -> Configuration de la page mixte

In the third step: see AddPage-step3.png.

    * Containers -> Conteneurs
    * The whole content of the message when clicking on Abort icon.
      o The title: Confirm message -> Message de confirmation
      o "Modifications have been made. Are you sure you want to close without saving ?"
      -> "Il y a des modifications. Etes-vous certain de fermer sans sauvegarde ?"
      o Yes/No buttons -> "Oui/Non"

Fix description

How is the problem fixed?

    * Add new keys in the webui_fr.properties file
      o UIEditInlineWorkspace.confirm.close=Il y a des modifications. Etes-vous certain de fermer sans sauvegarde ?
      o UIEditInlineWorkspace.confirm.yes=Oui
      o UIEditInlineWorkspace.confirm.no=Non
      o UIConfirmation.title.exoMessages=Message de confirmation
    * Modify the incorrect
      o UITabPane.title.UIContainerList=Containers --> UITabPane.title.UIContainerList=Conteneurs
    * Translate in to French
      o Configs Page -> Configuration de la page
      o Configs Page en Colonnes -> Configuration de la page en colonnes
      o Configs Page en Lignes -> Configuration de la page en lignes
      o Configs Page en Onglets -> Configuration de la page en onglets
      o Configs Page Mixte -> Configuration de la page mixte

Patch files:EXOGTN-307.patch

Tests to perform

Reproduction test

    * cf. above

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

    * Function or ClassName change: None

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment

    * PL review: patch validated

Support Comment

    * Support review: patch validated

QA Feedbacks
*

