Summary

    * Status: Maximum upload size for file is checked improperly
    * CCP Issue: CCP-567, Product Jira Issue: EXOGTN-268.
    * Fixes also: ECMS-1200
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
How to reproduce:

    * Externalize the maximum upload size configuration (set the size as 3M): adding the following configuration to webapps/ecmexplorer.war/WEB-INF/conf/portlet/JCRExplorerPortlet/webui/configuration.xml

      <configuration>
      	<component>
      		<type>org.exoplatform.upload.UploadService</type>
      		<init-params>
      			<value-param>
      				<name>upload.limit.size</name>
      				<description>Maximum size of the file to upload in MB</description>
      				<value>3</value>
      			</value-param>
      		</init-params>
      	</component>
      </configuration>

    * Create a document when you have an upload file (example Podcast document)

      String[] fieldMedia = ["jcrPath=/node/jcr:content/jcr:data"] ;
       uicomponent.addUploadField("media", fieldMedia) ;

      => it allows the uploading of file of size 3.9 MB, but not 4 MB.
      => If the size is set to 3, it should not allow upload of file size greater than 3 MB.

Fix description

Problem analysis
* Maximum upload size for file in MB unit is checked improperly cause of below code.

in isLimited() method of UploadService class

int estimatedSizeMB = (int)((contentLength / 1024) / 1024);
      if (limitMB > 0 && estimatedSizeMB > limitMB)
      { // a limit set to 0 means unlimited
         if (log.isDebugEnabled())
         {
            log.debug("Upload cancelled because file bigger than size limit : " + estimatedSizeMB + " MB > " + limitMB
               + " MB");
         }
         return true;
      }

Should compare in byte instead of megabyte, or using double/float instead of integer.

How is the problem fixed?

    *  Change int type into float or double data type (in isLimited() method of UploadService.java class)

Patch file: EXOGTN-268.patch

Tests to perform

Reproduction test
*

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
* No

Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

