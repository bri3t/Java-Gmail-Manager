package Conn;

import front.App;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.swing.JOptionPane;
import models.GmailHeader;
import models.Mail;

public class EmailManager {

    private String username;
    private String password;
    private Session session;
    private Store store;
    private Folder carpetaActual;

    public EmailManager(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setCarpetaActual(Folder carpetaActual) {
        this.carpetaActual = carpetaActual;
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

    // Conectar al servidor IMAP
    public void connectSMTP() {
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.host", "smtp.gmail.com");
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.starttls.enable", "true");
        properties.setProperty("mail.smtp.port", "587");

        session = Session.getDefaultInstance(properties);

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
    public Message[] fetchMessages(Folder folder, int num, int messageCountActual) throws MessagingException {
        if (!store.isConnected()) {
            connect();
        }

        if (!folder.isOpen()) {
            folder.open(Folder.READ_ONLY);
        }

        int messageCount = folder.getMessageCount();
        int start = Math.max(1, messageCount - 9), end = messageCount;

        if (num == 1) {
            if (messageCount >= messageCountActual + 18) {
                start = Math.min(messageCountActual + 9, messageCount);
                end = Math.min(start + 9, messageCount);

            }
        } else if (num == 0) {
            // Retroceder a la página anterior de mensajes
            start = Math.max(1, messageCountActual - 9); // Retrocede 10 mensajes desde el inicio de la página actual
            end = Math.max(1, messageCountActual);  // Hace que la página finalice donde comenzó la actual

        } else if (num == 2 && messageCount-9 != messageCountActual) {
            start = Math.max(1, messageCountActual); 
            end = Math.max(1, messageCountActual + 9); 
        }

        App.messageCountActual = start;  // Actualizar el conteo actual al inicio de la nueva página
        Message[] messages = folder.getMessages(start, end);

        return messages;
    }

    public List<GmailHeader> obtenerHeaders(Message[] messages) throws MessagingException {
        List<GmailHeader> headers = new ArrayList<>();

        for (int i = 0; i < messages.length; i++) {

            String sentDate = messages[i].getSentDate().toString();
            String subject = messages[i].getSubject();
            String from = messages[i].getFrom()[0].toString();
            int idMessage = messages[i].getMessageNumber();

            headers.add(new GmailHeader(sentDate, subject, from, idMessage));
        }

        return headers;
    }

    public Mail extractMessageParts(Message message) throws MessagingException, IOException {
        String textMessage = null;
        String textHTML = null;
        List<BodyPart> bodyParts = new ArrayList<>();

        if (!carpetaActual.isOpen()) {
            carpetaActual.open(Folder.READ_WRITE);
        }

        // Chequear si el contenido es multipart (tiene varias partes)
        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();

            // Recorrer todas las partes del multipart
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                bodyParts.add(bodyPart); // Agregar a la lista de partes

                // Chequear si es texto plano
                if (bodyPart.isMimeType("text/plain") && textMessage == null) {
                    textMessage = (String) bodyPart.getContent();
                } // Chequear si es HTML
                else if (bodyPart.isMimeType("text/html") && textHTML == null) {
                    textHTML = (String) bodyPart.getContent();
                }
            }
        } else {
            // Manejar mensajes que no son multipart (simple texto plano o HTML)
            if (message.isMimeType("text/plain")) {
                textMessage = (String) message.getContent();
            } else if (message.isMimeType("text/html")) {
                textHTML = (String) message.getContent();
            }
        }

        Mail mail = new Mail();
        mail.setBodyParts(bodyParts);
        mail.setTextMessage(textMessage);
        mail.setTextHTML(textHTML);

        carpetaActual.close();
        return mail;
    }

    public boolean reenviarMail(Message originalMessage, String[] to, String[] cc, String[] bcc, String additionalContent, List<String> attachments) {
        try {
            connectSMTP();

            MimeMessage newMessage = new MimeMessage(session);

            addRecipients(newMessage, Message.RecipientType.TO, to);
            addRecipients(newMessage, Message.RecipientType.CC, cc);
            addRecipients(newMessage, Message.RecipientType.BCC, bcc);

            newMessage.setSubject("Fwd: " + originalMessage.getSubject());

            Multipart multipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(additionalContent);
            multipart.addBodyPart(textPart);

            for (String filePath : attachments) {
                File file = new File(filePath);
                if (file.exists() && file.canRead()) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(file);
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(file.getName());
                    multipart.addBodyPart(attachmentPart);
                } else {
                    System.err.println("File not found or not readable: " + filePath);
                    continue; // Skip this attachment
                }
            }

            newMessage.setContent(multipart);

            Transport transport = session.getTransport("smtp");
            transport.connect(username, password);
            transport.sendMessage(newMessage, newMessage.getAllRecipients());  // Usar sendMessage para enviar a través del Transport conectado
            transport.close();
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean moveEmail(Message message, Folder fromFolder, String toFolderName) {
        Folder toFolder = null;
        try {
            // Asegurar que la conexión está activa, reconectar si es necesario
            if (!store.isConnected()) {
                store.connect();
            }
            if (fromFolder.isOpen()) {
                fromFolder.close();
            }

            toFolder = store.getFolder("[Gmail]/" + toFolderName);

            // Abrir la carpeta de origen en modo de lectura/escritura
            fromFolder.open(Folder.READ_WRITE);

            // Verificar y preparar la carpeta destino
            if (!toFolder.exists()) {
                toFolder.create(Folder.HOLDS_MESSAGES);
            }
            toFolder.open(Folder.READ_WRITE);

            // Realizar la operación de mover
            fromFolder.copyMessages(new Message[]{message}, toFolder);

            App.messageCountActual -= 1;
            return true;
        } catch (MessagingException e) {
            System.err.println("Error al mover el mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                // Cerrar ambas carpetas asegurando que los cambios se aplican
                fromFolder.close();
                toFolder.close();
            } catch (MessagingException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Enviar un correo electrónico
    public boolean sendEmail(String[] to, String[] cc, String[] bcc, String subject, String content, boolean isHtmlContent, String[] attachments) {
        try {
            connectSMTP();  // Asegurar que la sesión SMTP esté configurada

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));

            addRecipients(message, Message.RecipientType.TO, to);
            addRecipients(message, Message.RecipientType.CC, cc);
            addRecipients(message, Message.RecipientType.BCC, bcc);

            Multipart multipart = new MimeMultipart();
            MimeBodyPart contentPart = new MimeBodyPart();
            if (isHtmlContent) {
                contentPart.setContent(content, "text/html");
            } else {
                contentPart.setText(content);
            }
            multipart.addBodyPart(contentPart);

            if (attachments != null) {
                for (String filePath : attachments) {
//                    System.out.println(filePath);
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    File file = new File(filePath);
                    attachmentPart.attachFile(file);
                    multipart.addBodyPart(attachmentPart);
                }
            }

            message.setContent(multipart);
            message.setSubject(subject);

            Transport transport = session.getTransport("smtp");
            transport.connect(username, password);
            transport.sendMessage(message, message.getAllRecipients());  // Usar sendMessage para enviar a través del Transport conectado
            transport.close();

            return true;
        } catch (IOException | MessagingException e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
            return false;
        }
    }

    private void addRecipients(MimeMessage message, Message.RecipientType type, String[] recipients) throws MessagingException {
        if (recipients != null) {
            for (String recipient : recipients) {
                if (recipient.length() > 4) {
                    message.addRecipient(type, new InternetAddress(recipient));
                }
            }
        }
    }

    private String getTrashFolderFullName() throws MessagingException {
        if (!store.isConnected()) {
            connect(); // Asegúrate de estar conectado
        }

        // Recuperar todas las carpetas disponibles
        Folder[] folders = store.getFolder("[Gmail]").list();

        for (Folder folder : folders) {
            if (folder.getName().equalsIgnoreCase("Trash") || folder.getName().equalsIgnoreCase("Papelera") || folder.getName().equalsIgnoreCase("Bin") || folder.getName().equalsIgnoreCase("Paperera")) {
                return folder.getFullName();
            }
        }

        return "Carpeta papelera no encontrado"; // Devuelve un mensaje si no se encuentra la carpeta
    }

    public boolean deleteEmail(Folder folder, int messageId) {
        Folder toFolder = null;
        try {
            // Asegurar que la conexión está activa, reconectar si es necesario
            if (!store.isConnected()) {
                store.connect();
            }

            if (folder.isOpen()) {
                folder.close();
            }

            toFolder = store.getFolder(getTrashFolderFullName());

            // Abrir la carpeta de origen en modo de lectura/escritura
            folder.open(Folder.READ_WRITE);

            toFolder.open(Folder.READ_WRITE);

            Message messageToDelete = folder.getMessage(messageId);
            folder.copyMessages(new Message[]{messageToDelete}, toFolder);

            App.messageCountActual -= 1;
            return true;
        } catch (MessagingException e) {
            System.err.println("Error al mover el mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                toFolder.close();
                folder.close();
            } catch (MessagingException ex) {
                ex.printStackTrace();
            }
        }
    }

//    public boolean deleteEmail(String folderName, int messageId) {
//        Folder folder = null;
//        try {
//            if (!store.isConnected()) {
//                if (!connect()) {
//                    System.err.println("No se pudo conectar al servidor IMAP para eliminar el correo.");
//                    return false;  // No se pudo reconectar
//                }
//            }
//
//            folder = store.getFolder(folderName);
//            if (!folder.exists()) {
//                System.err.println("La carpeta especificada no existe.");
//                return false;  // La carpeta no existe
//            }
//
//            folder.open(Folder.READ_WRITE);
//
//            Message messageToDelete = folder.getMessage(messageId);
//            if (messageToDelete == null) {
//                System.err.println("El mensaje con el ID especificado no existe.");
//                return false;  // El mensaje no existe
//            }
//
//            messageToDelete.setFlag(Flags.Flag.DELETED, true);  // Marcar el mensaje para eliminación
//            folder.expunge();  // Expurgar los mensajes marcados para eliminar de la carpeta
//
//            return true;  // Devuelve true si el mensaje fue eliminado exitosamente
//        } catch (MessagingException e) {
//            e.printStackTrace();  // Imprimir el stack trace para depuración
//            return false;  // Devuelve false en caso de excepción
//        } finally {
//            try {
//                if (folder != null && folder.isOpen()) {
//                    folder.close(true);  // Cerrar la carpeta y aplicar los cambios
//                }
//            } catch (MessagingException e) {
//                System.err.println("Error al cerrar la carpeta: " + e.getMessage());
//            }
//        }
//    }
    public List<File> downloadAttachments(Mail mail, File destinationFolder) throws MessagingException, IOException {
        List<File> attachments = new ArrayList<>();
        Message message = mail.getMessage();

        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    InputStream is = bodyPart.getInputStream();
                    String filename = bodyPart.getFileName();
                    File file = new File(destinationFolder, filename);
                    saveFile(is, file);
                    attachments.add(file);
                }
            }
        }
        return attachments;
    }

    private void saveFile(InputStream is, File file) throws IOException {
        try ( FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
        }
    }

}
