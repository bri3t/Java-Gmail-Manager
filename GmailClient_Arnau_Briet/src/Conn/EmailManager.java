package Conn;

import java.io.File;
import java.io.FileOutputStream;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import models.GmailHeader;
import models.Mail;

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
    public Message[] fetchMessages(String folderName) throws MessagingException {
        if (!store.isConnected()) {
            connect();
        }

        Folder folder = store.getFolder("[Gmail]/" + folderName);

        folder.open(Folder.READ_ONLY);

        // Obtener los últimos 10 correos
        int messageCount = folder.getMessageCount();
        Message[] messages = folder.getMessages(Math.max(1, messageCount - 9), messageCount);

        return messages;
    }

    public List<GmailHeader> obtenerHeaders(Message[] messages) throws MessagingException {
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

    public Mail extractMessageParts(Message message) throws MessagingException, IOException {
        String textMessage = null;
        String textHTML = null;
        List<BodyPart> bodyParts = new ArrayList<>();

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

        return mail;
    }

    public boolean reenviarMail(Message originalMessage, String[] to, String[] cc, String[] bcc, String additionalContent) {
        try {
            connectSMTP();  // Asegurar que la sesión SMTP esté configurada

            // Crear un nuevo mensaje utilizando la misma sesión que el mensaje original
            MimeMessage newMessage = new MimeMessage(session);

            // Configurar los destinatarios del nuevo mensaje
            addRecipients(newMessage, Message.RecipientType.TO, to);
            addRecipients(newMessage, Message.RecipientType.CC, cc);
            addRecipients(newMessage, Message.RecipientType.BCC, bcc);

            // Configurar el asunto del nuevo mensaje
            newMessage.setSubject("Fwd: " + originalMessage.getSubject());

            // Crear el contenido del mensaje combinando el contenido adicional con el original
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(additionalContent);

            // Crear un multipart para contener el mensaje original y el adicional
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart); // Primero el texto adicional

            // Añadir el mensaje original como una segunda parte del multipart
            MimeBodyPart originalBodyPart = new MimeBodyPart();
            originalBodyPart.setContent(originalMessage.getContent(), originalMessage.getContentType());
            multipart.addBodyPart(originalBodyPart);

            // Establecer el multipart como el contenido del nuevo mensaje
            newMessage.setContent(multipart);

            // Enviar el mensaje
            Transport transport = session.getTransport("smtp");
            transport.connect(username, password);
            transport.sendMessage(newMessage, newMessage.getAllRecipients());  // Usar sendMessage para enviar a través del Transport conectado
            transport.close();
            return true;
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean moveEmail(Message message, String fromFolderName, String toFolderName) {
        Folder fromFolder = null;
        Folder toFolder = null;
        try {
            // Asegurar que la conexión está activa, reconectar si es necesario
            if (!store.isConnected()) {
                store.connect();
            }

            fromFolder = store.getFolder(fromFolderName);
            toFolder = store.getFolder("[Gmail]/" + toFolderName);

            // Abrir la carpeta de origen en modo de lectura/escritura
            if (!fromFolder.isOpen()) {
            }
            fromFolder.open(Folder.READ_WRITE);

            // Verificar y preparar la carpeta destino
            if (!toFolder.exists()) {
                toFolder.create(Folder.HOLDS_MESSAGES);
            }
            if (!toFolder.isOpen()) {
            }
            toFolder.open(Folder.READ_WRITE);

            // Realizar la operación de mover
            fromFolder.copyMessages(new Message[]{message}, toFolder);
//            message.setFlag(Flags.Flag.DELETED, true);  // Marcar el mensaje para ser eliminado en la carpeta origen
//            fromFolder.expunge();  // Limpiar los mensajes marcados como eliminados

            return true;
        } catch (MessagingException e) {
            System.err.println("Error al mover el mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            // Cerrar ambas carpetas asegurando que los cambios se aplican
            closeFolder(fromFolder);
            closeFolder(toFolder);
        }
    }

    private void closeFolder(Folder folder) {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(true); // Aplicar los cambios pendientes al cerrar
            }
        } catch (MessagingException e) {
            System.err.println("Error al cerrar la carpeta: " + e.getMessage());
            e.printStackTrace();
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
        } catch (Exception e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
            return false;
        }
    }

    private void addRecipients(MimeMessage message, Message.RecipientType type, String[] recipients) throws MessagingException {
        if (recipients != null) {
            for (String recipient : recipients) {
                message.addRecipient(type, new InternetAddress(recipient));
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
            System.out.println(folder.getName());
            // Puedes adaptar el nombre según el servidor o la configuración del idioma
            if (folder.getName().equalsIgnoreCase("Trash") || folder.getName().equalsIgnoreCase("Papelera") || folder.getName().equalsIgnoreCase("Bin") || folder.getName().equalsIgnoreCase("Paperera")) {
                return folder.getFullName();
            }
        }

        return "Trash folder not found"; // Devuelve un mensaje si no se encuentra la carpeta
    }

    public boolean deleteEmail(String folderName, int messageId) {
        Folder folder = null;
        Folder toFolder = null;
        try {
            // Asegurar que la conexión está activa, reconectar si es necesario
            if (!store.isConnected()) {
                store.connect();
            }

            folder = store.getFolder(folderName);
            toFolder = store.getFolder(getTrashFolderFullName());

            // Abrir la carpeta de origen en modo de lectura/escritura
            if (!folder.isOpen()) {
            }
            folder.open(Folder.READ_WRITE);

           
            toFolder.open(Folder.READ_WRITE);

            Message messageToDelete = folder.getMessage(messageId);
            folder.copyMessages(new Message[]{messageToDelete}, toFolder);

            return true;
        } catch (MessagingException e) {
            System.err.println("Error al mover el mensaje: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            closeFolder(toFolder);
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
    
    
    
    
    

    public List<File> downloadAttachments(Mail mail) throws MessagingException, IOException {
        List<File> attachments = new ArrayList<>();
        Message message = mail.getMessage();

        if (message.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) message.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    String filename = bodyPart.getFileName();
                    InputStream is = bodyPart.getInputStream();
                    File file = new File(System.getProperty("java.io.tmpdir") + File.separator + filename);
                    copyInputStreamToFile(is, file);
                    attachments.add(file);
                }
            }
        }
        return attachments;
    }

    private void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        try ( FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
        }
    }

}
