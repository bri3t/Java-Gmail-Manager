package front;

import Conn.EmailManager;
import javax.mail.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Reenvio extends JDialog {

    private JTextField toField, ccField, bccField;
    private JTextArea contentArea;
    private JButton sendButton, cancelButton;
    private Message originalMessage;
    private EmailManager emailManager;
    private JList<String> attachmentList;
    private DefaultListModel<String> attachmentModel;
    MailContent mc;

    public Reenvio(Frame owner, EmailManager emailManager, Message originalMessage, MailContent mc) {
        super(owner, "Reenviar Email", true);
        this.emailManager = emailManager;
        this.originalMessage = originalMessage;
        this.mc = mc;

        setupUI();
        setupActions();
        setSize(400, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        fieldsPanel.add(createLabeledField("Para:   ", toField = new JTextField()));
        fieldsPanel.add(createLabeledField("CC:     ", ccField = new JTextField()));
        fieldsPanel.add(createLabeledField("CCO:  ", bccField = new JTextField()));

        contentArea = new JTextArea(5, 10);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Mensaje adicional:"), BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        fieldsPanel.add(contentPanel);

        attachmentModel = new DefaultListModel<>();
        attachmentList = new JList<>(attachmentModel);
        JScrollPane attachmentScrollPane = new JScrollPane(attachmentList);
        attachmentScrollPane.setPreferredSize(new Dimension(200, 100));
        JPanel attachmentPanel = new JPanel(new BorderLayout());
        attachmentPanel.add(new JLabel("Attachments:"), BorderLayout.NORTH);
        attachmentPanel.add(attachmentScrollPane, BorderLayout.CENTER);
        fieldsPanel.add(attachmentPanel);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        sendButton = new JButton("Reenviar");
        buttonPanel.add(sendButton);
        cancelButton = new JButton("Cancelar");
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        generateAttachments(originalMessage);
    }

    private void generateAttachments(Message message) {
        try {
            //        try {
//            Object content = message.getContent();
//            if (content instanceof Multipart) {
//                Multipart multipart = (Multipart) content;
//                for (int i = 0; i < multipart.getCount(); i++) {
//                    BodyPart part = multipart.getBodyPart(i);
//                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
//                        attachmentModel.addElement(part.getFileName());
//                        if (!attachmentModel.contains(part)) {
//                            saveAttachment(part);
//                        }
//                    }
//                }
//            }
//        } catch (MessagingException | IOException ex) {
//            ex.printStackTrace();
//        }
            Folder folder  = message.getFolder();
            if (!folder.isOpen()) folder.open(Folder.READ_WRITE);
            
            Multipart multipart = (Multipart) message.getContent();
            List<String> attachments = new ArrayList<>();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    saveAttachment(bodyPart);
//                    attachments.add(bodyPart.getFileName());
                }
            }
        } catch (MessagingException ex) {
            Logger.getLogger(Reenvio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveAttachment(BodyPart bodyPart) throws IOException, MessagingException {
        String fileName = bodyPart.getFileName();
        InputStream is = bodyPart.getInputStream();
        File file = new File(fileName);
        try ( FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
        }
        attachmentModel.addElement(fileName);
        System.out.println("Saved attachment to: " + file.getAbsolutePath());
    }

    private JPanel createLabeledField(String label, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private void setupActions() {
        sendButton.addActionListener(e -> {
            String to = toField.getText();
            String cc = ccField.getText();
            String bcc = bccField.getText();
            String additionalContent = contentArea.getText();

            if (!to.isEmpty() || !cc.isEmpty() || !bcc.isEmpty()) {
                List<String> selectedAttachments = getSelectedAttachments();
                String[] toRecipients = to.split("\\s*,\\s*");
                String[] ccRecipients = cc.isEmpty() ? new String[0] : cc.split("\\s*,\\s*");
                String[] bccRecipients = bcc.isEmpty() ? new String[0] : bcc.split("\\s*,\\s*");

                if (emailManager.reenviarMail(originalMessage, toRecipients, ccRecipients, bccRecipients, additionalContent, selectedAttachments)) {
                    JOptionPane.showMessageDialog(this, "Correo reenviado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    mc.cerrarDialog();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al reenviar el correo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Debe llenar al menos uno de los campos de destinatarios.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private List<String> getSelectedAttachments() {
        List<String> selected = new ArrayList<>();
        for (int index : attachmentList.getSelectedIndices()) {
            selected.add(attachmentModel.getElementAt(index));
        }
        return selected;
    }
}
