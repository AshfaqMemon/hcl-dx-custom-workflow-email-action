package com.ashfaq.workflow.custom.action;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;

import com.ibm.portal.um.Group;
import com.ibm.portal.um.Principal;
import com.ibm.portal.um.PumaController;
import com.ibm.portal.um.PumaHome;
import com.ibm.portal.um.PumaLocator;
import com.ibm.portal.um.PumaProfile;
import com.ibm.portal.um.User;
import com.ibm.workplace.wcm.api.Content;
import com.ibm.workplace.wcm.api.Document;
import com.ibm.workplace.wcm.api.WcmCustomWorkflowService;
import com.ibm.workplace.wcm.api.Workspace;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowAction;
import com.ibm.workplace.wcm.api.custom.CustomWorkflowActionResult;
import com.ibm.workplace.wcm.api.custom.Directive;
import com.ibm.workplace.wcm.api.custom.Directives;
import com.ibm.workplace.wcm.api.security.Access;

public class SendEmailToApproversWithCustomBodyWorkFlowAction implements CustomWorkflowAction {
	
    InitialContext initContext = null;
    CustomWorkflowActionResult result = null;
    // PUMA Objects
    PumaHome pHome = null;
    PumaController pController = null;
    PumaLocator pLocator = null;
    PumaProfile pProfile = null;
    Principal approverPrincipal = null;
    Content content = null;
    String message = null;
    
    public SendEmailToApproversWithCustomBodyWorkFlowAction() {
        System.out.println("Inside the TestSendEmailToApproversWithCustomBodyWorkFlowAction.... ");
    }
    
    public CustomWorkflowActionResult execute(Document arg0) {
        System.out.println("Executing TestSendEmailToApproversWithCustomBodyWorkFlowAction.... ");
        Directive directive = Directives.CONTINUE;
        if (arg0 instanceof Content) {
            try {
                content = (Content) arg0;
                Workspace ws = content.getSourceWorkspace();
                ws.useDistinguishedNames(true);
                initContext = new InitialContext();
                //PUMA related
                pHome = (com.ibm.portal.um.PumaHome) initContext.lookup(com.ibm.portal.um.PumaHome.JNDI_NAME);
                pController = pHome.getController();
                pLocator = pHome.getLocator();
                pProfile = pHome.getProfile();
                //User Attributes
                List<String> userAttributes = new ArrayList<String>();
                userAttributes.add("ibm-primaryEmail");
                userAttributes.add("cn");
                List<Group> groupsList = new ArrayList<Group>();
                Set<User> usersList = new HashSet<User>();
                List<Principal> membersList = null;
                List<Map<String, Object>> approversDetails = new ArrayList<Map<String, Object>>();
                //Below additional logic is check for for bother users and groups as approvers,
                String currentApprovers[] = content.getMembersForAccess(Access.REVIEWER)
                ;
                //create a separate lists for users and groups
                for (int i = 0; i < currentApprovers.length; i++) {
                    if(currentApprovers[i].contains("uid=")){
                        usersList.add(pLocator.findUserByIdentifier(currentApprovers[i]));
                    }else{
                        groupsList.add(pLocator.findGroupByIdentifier(currentApprovers[i]));
                    }
                }
                //find all users from groups and add it to users list
                for(Group group : groupsList){
                    membersList = pLocator.findMembersByGroup(group,false);
                    for (Principal member : membersList) {
                        usersList.add((User) member);
                    }
                }
                //Get required attributes from users list
                for(User user : usersList){
                    approversDetails.add(pProfile.getAttributes(user,userAttributes));
                }
                for (Map<String, Object> userDetail : approversDetails) {
                    System.out.println("Username::" + userDetail.get("cn")+ "email::" + userDetail.get("ibm-primaryEmail"));
                }
                //call mail utility method to send email.
                TestSendEmail.sendEmail(approversDetails, content);
            } catch (Exception ex) {
                message = "An exception has occured " + ex.getMessage();
                directive = Directives.ROLLBACK_DOCUMENT;
            }
        }
	    try {
	    	WcmCustomWorkflowService webContentCustomWorkflowService = (WcmCustomWorkflowService) initContext.lookup(WcmCustomWorkflowService.JNDI_NAME);
	        result = webContentCustomWorkflowService.createResult(directive,message);
	    } catch (Exception ex) {
	        message = "An exception has occured in ex " + ex.getMessage();
	        directive = Directives.ROLLBACK_DOCUMENT;
	    }
        return result;
    }
    
    public Date getExecuteDate(Document arg0) {
        return DATE_EXECUTE_NOW;
    }
}