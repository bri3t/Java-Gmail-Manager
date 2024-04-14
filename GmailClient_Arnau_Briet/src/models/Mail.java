package models;

import java.util.List;
import javax.mail.BodyPart;

/**
 *
 * @author arnau
 */
public class Mail {

     GmailHeader gh;
     String textMessage, textHTML;
     List<BodyPart> bodyParts;

    public Mail() {
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
