package front;

import Conn.EmailManager;
import models.Mail;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class Responder extends JDialog {

    private EmailManager emailManager;
    private Mail mailToReply;
    private JLabel filesCountLabel;  // Etiqueta para mostrar el número de archivos adjuntos
    private java.util.List<File> attachedFiles;  // Lista para almacenar los archivos adjuntos

    public Responder(Frame owner, EmailManager emailManager, Mail mailToReply) {
        super(owner, "Responder Email", true);
        this.emailManager = emailManager;
        this.mailToReply = mailToReply;
        attachedFiles = new ArrayList<>();  // Inicializar la lista de archivos adjuntos

        setLayout(new BorderLayout(10, 10));
        initializeComponents(owner);
        setVisible(true);
    }

    private void initializeComponents(Frame owner) {
        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextArea replyTextArea = new JTextArea(10, 10);
        replyTextArea.setLineWrap(true);
        replyTextArea.setWrapStyleWord(true);
        replyTextArea.setMargin(new Insets(10, 10, 20, 10));
        JScrollPane scrollPane = new JScrollPane(replyTextArea);
        textPanel.add(scrollPane, BorderLayout.CENTER);

        filesCountLabel = new JLabel("Número de ficheros adjuntados: 0", SwingConstants.CENTER);
        textPanel.add(filesCountLabel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton attachButton = new JButton("Adjuntar");
        attachButton.setPreferredSize(new Dimension(120, 30));
        attachButton.addActionListener(e -> handleFileAttachment());
        buttonPanel.add(attachButton);

        JButton sendReplyButton = new JButton("Responder");
        sendReplyButton.setPreferredSize(new Dimension(120, 30));
        sendReplyButton.addActionListener(e -> sendReply(replyTextArea.getText()));
        buttonPanel.add(sendReplyButton);

        add(textPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setTitle("Enviar Email");
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void handleFileAttachment() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] selectedFiles = fileChooser.getSelectedFiles();
            for (File file : selectedFiles) {
                attachedFiles.add(file);
            }
            filesCountLabel.setText("Número de ficheros adjuntados: " + attachedFiles.size());
        }
    }

    private void sendReply(String replyContent) {
        if (replyContent.isEmpty()) {
            JOptionPane.showMessageDialog(this, "El contenido de la respuesta no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean isHtmlContent = false;
        String to = mailToReply.getFromMail();
        String subject = "RE: " + mailToReply.getGh().getAsunto();
        String[] recipients = new String[]{to};
        String[] attachments = attachedFiles.stream().map(File::getPath).toArray(String[]::new);

        if (emailManager.sendEmail(recipients, null, null, subject, replyContent, isHtmlContent, attachments)) {
            JOptionPane.showMessageDialog(this, "Respuesta enviada correctamente.", "Enviado", JOptionPane.INFORMATION_MESSAGE);
            dispose();  // Cerrar el diálogo después de enviar la respuesta
        } else {
            JOptionPane.showMessageDialog(this, "Error al enviar la respuesta.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
