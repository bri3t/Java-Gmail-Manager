package Conn;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import models.GmailHeader;

public class EmailManager {

    private String username;
    private String password;
    private Session session;
    private Store store;

    public EmailManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Conectar al servidor IMAP
    public boolean connect() {
        Properties properties = new Properties();
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.gmail.com");
        properties.put("mail.imaps.port", "993");
        properties.put("mail.imaps.ssl.enable", "true");

        try {
            session = Session.getInstance(properties);
            store = session.getStore("imaps");
            store.connect(username, password);
            return true; // La conexión fue exitosa
        } catch (MessagingException e) {
            e.printStackTrace();  // Opcionalmente, imprimir la pila de excepciones para depuración
            return false; // La conexión falló
        }
    }

    // Desconectar del servidor
    public void disconnect() throws MessagingException {
        if (store != null && store.isConnected()) {
            store.close();
        }
    }

// Obtener los directorios del correo
    public Folder[] getMailFolders() throws MessagingException {
        if (!store.isConnected()) {
            connect();
        }
        // Directamente retornar la lista de carpetas sin convertir a String[]
        return store.getFolder("[Gmail]").list();
    }

    // Obtener correos de un directorio específico
    public List<GmailHeader> fetchMessages(String folderName) throws MessagingException {
        if (!store.isConnected()) {
            connect();
        }

        Folder folder = store.getFolder("[Gmail]/" + folderName);

        folder.open(Folder.READ_ONLY);

        // Obtener los últimos 10 correos
        int messageCount = folder.getMessageCount();
        Message[] messages = folder.getMessages(Math.max(1, messageCount - 9), messageCount);

        List<GmailHeader> headers = new ArrayList<>();
        for (Message message : messages) {
            String sentDate = message.getSentDate().toString();
            String subject = message.getSubject();
            String from = message.getFrom()[0].toString();
            int idMessage = message.getMessageNumber();

            headers.add(new GmailHeader(sentDate, subject, from, idMessage));
        }

        return headers;
    }

    // Enviar un correo electrónico
    public void sendEmail(String[] to, String[] cc, String[] bcc, String subject, String content, boolean isHtmlContent) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        addRecipients(message, Message.RecipientType.TO, to);
        addRecipients(message, Message.RecipientType.CC, cc);
        addRecipients(message, Message.RecipientType.BCC, bcc);
        message.setSubject(subject);

        if (isHtmlContent) {
            message.setContent(content, "text/html");
        } else {
            message.setText(content);
        }

        Transport.send(message);
    }

    private void addRecipients(MimeMessage message, Message.RecipientType type, String[] recipients) throws MessagingException {
        if (recipients != null) {
            for (String recipient : recipients) {
                message.addRecipient(type, new InternetAddress(recipient));
            }
        }
    }

    public void deleteEmail(String folderName, int messageId) throws MessagingException {
        if (!store.isConnected()) {
            if (!connect()) {
                throw new MessagingException("Unable to reconnect to mail store");
            }
        }

        Folder folder = store.getFolder(folderName);
        if (folder == null || !folder.exists()) {
            throw new MessagingException("Folder not found: " + folderName);
        }

        try {
            folder.open(Folder.READ_WRITE);  // Abrir la carpeta en modo lectura/escritura para permitir modificaciones
            Message messageToDelete = folder.getMessage(messageId);
            if (messageToDelete == null) {
                throw new MessagingException("Message not found: " + messageId);
            }

            messageToDelete.setFlag(Flags.Flag.DELETED, true);  // Marcar el mensaje para eliminación
            folder.expunge();  // Expurgar los mensajes marcados para eliminar de la carpeta
        } finally {
            if (folder.isOpen()) {
                folder.close(true);  // Cerrar la carpeta y aplicar los cambios
            }
        }
    }

}
