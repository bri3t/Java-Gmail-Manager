package Conn;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class IMAPConnection {

    private Store store;

    // Conectar al servidor IMAP
    public void conectar(String host, String user, String password) throws MessagingException {
        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "imaps");
        Session session = Session.getInstance(props, null);
        store = session.getStore();
        store.connect(host, user, password);
    }

    // Desconectar del servidor
    public void desconectar() throws MessagingException {
        if (store != null && store.isConnected()) {
            store.close();
        }
    }

    // Obtener la lista de directorios
    public List<String> obtenerDirectorios() throws MessagingException {
        List<String> directorios = new ArrayList<>();
        Folder[] folders = store.getDefaultFolder().list("*");
        for (Folder folder : folders) {
            directorios.add(folder.getName());
        }
        return directorios;
    }

    // Obtener las cabeceras de los correos
    public List<Message> obtenerCabeceras(String directorio, int n) throws MessagingException {
        Folder folder = store.getFolder(directorio);
        folder.open(Folder.READ_ONLY);
        int totalMensajes = folder.getMessageCount();
        int desde = Math.max(1, totalMensajes - n + 1);
        Message[] mensajes = folder.getMessages(desde, totalMensajes);
        List<Message> cabeceras = new ArrayList<>();
        for (Message mensaje : mensajes) {
            cabeceras.add(mensaje);
        }
        folder.close();
        return cabeceras;
    }

    // Obtener el contenido de un correo
    public String obtenerContenidoCorreo(Message mensaje) throws IOException, MessagingException {
        Object contenido = mensaje.getContent();
        if (contenido instanceof String) {
            return (String) contenido;
        } else if (contenido instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) contenido;
            StringBuilder contenidoTexto = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContentType().startsWith("text/plain")) {
                    contenidoTexto.append(bodyPart.getContent());
                }
            }
            return contenidoTexto.toString();
        } else {
            return "";
        }
    }

    // Descargar un archivo adjunto
    public void descargarAdjunto(Part adjunto, String directorioDestino) throws IOException, MessagingException {
        String nombreArchivo = adjunto.getFileName();
        InputStream is = adjunto.getInputStream();
        OutputStream os = new FileOutputStream(new File(directorioDestino + File.separator + nombreArchivo));
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        is.close();
    }

    // Eliminar un correo
    public void eliminarCorreo(Message mensaje) throws MessagingException {
        mensaje.setFlag(Flags.Flag.DELETED, true);
    }

    // Enviar un correo electrónico
    public void enviarCorreo(String remitente, String[] destinatarios, String asunto, String cuerpo, File[] adjuntos) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("tucorreo@gmail.com", "tucontraseña");
            }
        });

        Message mensaje = new MimeMessage(session);
        mensaje.setFrom(new InternetAddress(remitente));
        for (String destinatario : destinatarios) {
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(destinatario));
        }
        mensaje.setSubject(asunto);
        mensaje.setText(cuerpo);

        for (File adjunto : adjuntos) {
            BodyPart adjuntoParte = new MimeBodyPart();
            adjuntoParte.setDataHandler(new DataHandler(new FileDataSource(adjunto)));
            adjuntoParte.setFileName(adjunto.getName());
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(adjuntoParte);
            mensaje.setContent(multipart);
        }

        Transport.send(mensaje);
    }
}