package com.ashfaq.workflow.custom.action;
import java.util.*;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import com.ibm.workplace.wcm.api.Content;

public class TestSendEmail {
	
    public static void sendEmail(List<Map<String, Object>> usersDetails, Content content){
        String from = "wcmsupport@mail.ashfaq.com";
        final String SMTP_AUTH_USER = "admin@mail.ashfaq.com";
        final String SMTP_AUTH_PASSWORD = "password";
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "smtp.ashfaq.com");
        properties.setProperty("mail.smtp.port", "25");
        properties.setProperty("mail.smtp.auth", "true");
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_AUTH_USER, SMTP_AUTH_PASSWORD);
            }
        });
        
        MimeMessage message = null;
        String approverEmail = "";
        for(Map<String, Object> user : usersDetails){
            approverEmail = (String)user.get("ibm-primaryEmail");
            if(null != approverEmail && 0 < approverEmail.trim().length()){
                try{
                    message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(from));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(approverEmail));
                    message.setSubject("Workflow Notification :: " + content.getTitle());
                    message.setContent(customEmailBody(user, content), "text/html" );
                    Transport.send(message);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static String customEmailBody(Map<String, Object> user, Content content) throws Exception {
        StringBuffer emailBody = new StringBuffer();
        emailBody.append("Hi " + user.get("cn") +",");
        emailBody.append("<br/><br/>");
        emailBody.append("The content '"+content.getTitle()+"' has moved to Workflow Stage '"+(content.getWorkflowStageId()).getName()+"." );
        emailBody.append("<br/>");
        emailBody.append("Click on the following link to review the item, <a href='https://ashfaq.portal.com/wps/myportal/wcmAuthoring?wcmAuthoringAction=read&docid=com.aptrix.pluto.content.Content/"+(content.getId()).getId()+"'>"+content.getTitle()+"</a>");
        emailBody.append("<br/><br/>");
        emailBody.append("Thanks<br/>WCM Team");
        return emailBody.toString();
    }
}