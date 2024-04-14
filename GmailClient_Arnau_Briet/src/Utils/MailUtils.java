package Utils;

import javax.mail.*;
import java.io.IOException;
import Conn.*;

/**
 *
 * @author arnau
 */

public class MailUtils {
    
    private IMAPConnection mailConnection;

    public MailUtils(IMAPConnection mailConnection) {
        this.mailConnection = mailConnection;
    }
    

    public static void listDirectories(Store store) throws MessagingException {
        Folder[] folders = store.getDefaultFolder().list("*");
        System.out.println("Directories:");
        for (Folder folder : folders) {
            System.out.println(folder.getName());
        }
    }

    
    public static void getLastTenMessagesHeaders(Store store, String folderName) throws MessagingException {
        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_ONLY);
        Message[] messages = folder.getMessages(folder.getMessageCount() - 9, folder.getMessageCount());

        System.out.println("Last 10 Message Headers:");
        for (Message message : messages) {
            System.out.println("Subject: " + message.getSubject());
            System.out.println("From: " + javax.mail.Message.RecipientType.FROM);
            System.out.println("To: " + javax.mail.Message.RecipientType.TO);
        }

        folder.close(false);
    }

    public static void getFullMessageContent(Store store, String folderName) throws MessagingException, IOException {
        Folder folder = store.getFolder(folderName);
        folder.open(Folder.READ_ONLY);
        Message message = folder.getMessage(folder.getMessageCount());

        System.out.println("Full Message Content:");
        System.out.println("Subject: " + message.getSubject());
        System.out.println("From: " + javax.mail.Message.RecipientType.FROM);
        System.out.println("To: " + javax.mail.Message.RecipientType.TO);
        System.out.println("Text: " + message.getContent());

        folder.close(false);
    }
}
