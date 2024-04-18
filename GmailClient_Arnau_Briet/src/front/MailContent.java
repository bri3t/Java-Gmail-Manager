package front;

import Conn.EmailManager;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.GridLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.swing.*;
import models.Mail;


public class MailContent extends JDialog {

    EmailManager emailManager;
    Mail mail;

    public MailContent(Mail m, EmailManager em, Frame owner) {
        super(owner, "Contenido del correo", true);
        this.emailManager = em;
        this.mail = m;

        // Establecer el layout
        setLayout(new BorderLayout());

        // Crear la barra de menú y el menú Acciones
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAcciones = new JMenu("Acciones");
        JMenuItem itemResponder = new JMenuItem("Responder");
        JMenuItem itemReenviar = new JMenuItem("Reenviar");
        JMenuItem itemBorrar = new JMenuItem("Borrar");
        JMenuItem itemMover = new JMenuItem("Mover de carpeta");
        JMenuItem itemDescargarAdjuntos = new JMenuItem("Descargar Adjuntos");

        // Configurar acciones del menú
        itemResponder.addActionListener(e -> new Responder(owner, emailManager, mail));
        itemReenviar.addActionListener(e -> new Reenvio(owner, emailManager, mail.getMessage()));
        itemBorrar.addActionListener(e -> {
            if (emailManager.deleteEmail(mail.getFolder(), mail.getMessage().getMessageNumber())) {
                JOptionPane.showMessageDialog(this, "Correo eliminado correctamente", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar el correo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        itemMover.addActionListener(e -> new MoveEmail(owner, emailManager, mail));
        itemDescargarAdjuntos.addActionListener(e -> descargarAdjuntos());

        // Agregar ítems al menú
        menuAcciones.add(itemResponder);
        menuAcciones.add(itemReenviar);
        menuAcciones.add(itemBorrar);
        menuAcciones.add(itemMover);
        menuAcciones.add(itemDescargarAdjuntos);
        menuBar.add(menuAcciones);
        setJMenuBar(menuBar);

        // Crear y configurar el panel de información del correo
        JPanel infoPanel = new JPanel(new GridLayout(4, 1)); // 4 filas para remitente, cc, asunto, y botones
        try {
            infoPanel.add(new JLabel("De: " + getAddressesString(mail.getMessage().getFrom())));
            infoPanel.add(new JLabel("CC: " + getAddressesString(mail.getMessage().getRecipients(Message.RecipientType.CC))));
            infoPanel.add(new JLabel("Asunto: " + mail.getMessage().getSubject()));
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        // Añadir el panel de información al norte del BorderLayout
        add(infoPanel, BorderLayout.NORTH);

        // Panel para mostrar el contenido del correo
        JTextPane contentPane = new JTextPane();
        contentPane.setContentType("text/html");
        contentPane.setText(mail.getTextHTML() != null ? mail.getTextHTML() : mail.getTextMessage()); // Preferir HTML si está disponible
        contentPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(contentPane);
        add(scrollPane, BorderLayout.CENTER);

        // Configuración del diálogo
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Método auxiliar para obtener las direcciones de correo como String
    private String getAddressesString(Address[] addresses) {
        if (addresses == null) {
            return "N/A";
        }
        StringBuilder sb = new StringBuilder();
        for (Address address : addresses) {
            sb.append(address.toString()).append("; ");
        }
        return sb.toString();
    }

// Método para descargar y abrir archivos adjuntos
    private void descargarAdjuntos() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccione la carpeta para guardar los archivos adjuntos");
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File folder = fileChooser.getSelectedFile();

            try {
                List<File> archivos = emailManager.downloadAttachments(mail, folder);
                if (archivos.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No hay archivos adjuntos en este correo.", "Información", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    for (File archivo : archivos) {
                        if (archivo.exists() && desktop != null) {
                            desktop.open(archivo);  // Abrir archivo con la aplicación predeterminada
                        }
                    }
                    JOptionPane.showMessageDialog(this, "Archivos descargados y abiertos exitosamente en: " + folder.getAbsolutePath(), "Descarga Completa", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al descargar o abrir archivos: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

}
