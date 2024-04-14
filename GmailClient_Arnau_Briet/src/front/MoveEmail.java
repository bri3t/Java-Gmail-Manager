package front;

import Conn.EmailManager;
import javax.mail.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import models.Mail;

public class MoveEmail extends JDialog {

    private JComboBox<String> folderComboBox;
    private JButton moveButton;
    private Message messageToMove;
    private EmailManager emailManager;
    private Mail mail;

    public MoveEmail(Frame owner, EmailManager emailManager, Mail m) {
        super(owner, "Mover Email", true);
        this.emailManager = emailManager;
        this.messageToMove = m.getMessage();
        this.mail = m;

        setupUI();
        populateFolders();
        setupActions();
        pack();
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Crear y configurar JComboBox para las carpetas
        folderComboBox = new JComboBox<>();
        JLabel label = new JLabel("Seleccione la carpeta destino:");
        JPanel comboPanel = new JPanel(new BorderLayout());
        comboPanel.add(label, BorderLayout.NORTH);
        comboPanel.add(folderComboBox, BorderLayout.CENTER);

        // Botón para mover el correo
        moveButton = new JButton("Mover mail");
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(moveButton);

        add(comboPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void populateFolders() {
        try {
            Folder[] folders = emailManager.getMailFolders();
            for (Folder folder : folders) {
                if (!folder.getName().equalsIgnoreCase("INBOX")) { // No permitir mover a INBOX por simplicidad
                    folderComboBox.addItem(folder.getName());
                }
            }
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Error al cargar las carpetas: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupActions() {
        moveButton.addActionListener((ActionEvent e) -> {
            String targetFolderName = (String) folderComboBox.getSelectedItem();
            if (targetFolderName != null && !targetFolderName.isEmpty()) {
                try {
                    boolean result = emailManager.moveEmail(messageToMove, mail.getFolder().getFullName(), targetFolderName);
                    if (result) {
                        JOptionPane.showMessageDialog(this, "Correo movido exitosamente a " + targetFolderName, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al mover el correo.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error al mover el correo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
