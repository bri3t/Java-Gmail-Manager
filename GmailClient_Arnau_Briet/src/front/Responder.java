package front;

import Conn.EmailManager;
import models.Mail;

import javax.swing.*;
import java.awt.*;

public class Responder extends JDialog {

    private EmailManager emailManager;
    private Mail mailToReply;

    public Responder(Frame owner, EmailManager emailManager, Mail mailToReply) {
        super(owner, "Responder Email", true);
        this.emailManager = emailManager;
        this.mailToReply = mailToReply;

        setLayout(new BorderLayout(10, 10));

        // Panel para el área de texto donde se escribe la respuesta
        JTextArea replyTextArea = new JTextArea(10, 40);
        replyTextArea.setLineWrap(true);
        replyTextArea.setWrapStyleWord(true);
        replyTextArea.setMargin(new Insets(10, 10, 10, 10)); // Establecer márgenes internos para el área de texto
        JScrollPane scrollPane = new JScrollPane(replyTextArea);
        add(scrollPane, BorderLayout.CENTER);

        // Botón para enviar la respuesta
        JButton sendReplyButton = new JButton("Responder");
        sendReplyButton.addActionListener(e -> sendReply(replyTextArea.getText()));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(sendReplyButton);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private void sendReply(String replyContent) {
        if (replyContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El contenido de la respuesta no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Asumir que el contenido es texto plano; se necesita ajustar si se desea soportar HTML
        boolean isHtmlContent = false;

        // Preparar la respuesta: establecer los destinatarios, el asunto, etc.
        String to = mailToReply.getFromMail();
        String subject = "RE: " + mailToReply.getGh().getAsunto();
        String[] recipients = new String[]{to};

        // Envío del correo
        if (emailManager.sendEmail(recipients, null, null, subject, replyContent, isHtmlContent, null)) {
            JOptionPane.showMessageDialog(this, "Respuesta enviada correctamente.", "Enviado", JOptionPane.INFORMATION_MESSAGE);
            dispose();  // Cerrar el diálogo después de enviar la respuesta
        } else {
            JOptionPane.showMessageDialog(this, "Error al enviar la respuesta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
