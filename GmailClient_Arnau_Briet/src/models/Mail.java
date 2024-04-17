package models;

import java.util.List;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;

/**
 *
 * @author arnau
 */
public class Mail {

     GmailHeader gh;
     String textMessage, textHTML;
     List<BodyPart> bodyParts;
     Folder folder;
     Message message;
     String fromMail;

    public String getFromMail() {
        return fromMail;
    }

    public void setFromMail(String fromMail) {
        this.fromMail = fromMail;
    }

    public Mail() {
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

   
    public GmailHeader getGh() {
        return gh;
    }

    public void setGh(GmailHeader gh) {
        this.gh = gh;
    }

    public String getTextMessage() {
        return textMessage;
    }

    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    public String getTextHTML() {
        return textHTML;
    }

    public void setTextHTML(String textHTML) {
        this.textHTML = textHTML;
    }

    public List<BodyPart> getBodyParts() {
        return bodyParts;
    }

    public void setBodyParts(List<BodyPart> bodyParts) {
        this.bodyParts = bodyParts;
    }
    
    
     

}
