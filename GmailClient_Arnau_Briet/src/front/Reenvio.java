package front;

import Conn.EmailManager;
import javax.swing.*;
import java.awt.*;
import javax.mail.Message;

public class Reenvio extends JDialog {

    private JTextField toField, ccField, bccField;
    private JTextArea contentArea;
    private JButton sendButton, cancelButton;
    private Message originalMessage;
    private EmailManager emailManager;

    public Reenvio(Frame owner, EmailManager emailManager, Message originalMessage) {
        super(owner, "Reenviar Email", true);
        this.emailManager = emailManager;
        this.originalMessage = originalMessage;

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

        fieldsPanel.add(createLabeledField("Para: ", toField = new JTextField()));
        fieldsPanel.add(createLabeledField("CC:     ", ccField = new JTextField()));
        fieldsPanel.add(createLabeledField("BCC:  ", bccField = new JTextField()));

        contentArea = new JTextArea(5, 10);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(contentArea);
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(new JLabel("Mensaje adicional:"), BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        fieldsPanel.add(contentPanel);

        add(fieldsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        sendButton = new JButton("Reenviar");
        buttonPanel.add(sendButton);
        cancelButton = new JButton("Cancelar");
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
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

            if (!to.isEmpty()) {
                String[] toRecipients = to.split("\\s*,\\s*");
                String[] ccRecipients = cc.isEmpty() ? new String[0] : cc.split("\\s*,\\s*");
                String[] bccRecipients = bcc.isEmpty() ? new String[0] : bcc.split("\\s*,\\s*");

                if (emailManager.reenviarMail(originalMessage, toRecipients, ccRecipients, bccRecipients, additionalContent)) {
                    JOptionPane.showMessageDialog(this, "Correo reenviado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al reenviar el correo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "El campo 'Para' no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }
}
