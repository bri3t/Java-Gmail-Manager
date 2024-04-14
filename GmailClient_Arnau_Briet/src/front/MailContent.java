package front;

import Conn.EmailManager;
import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import models.Mail;

public class MailContent extends JDialog {

    EmailManager emailManager;

    public MailContent(Mail m, EmailManager em, Frame owner) {
        super(owner);
        this.emailManager = em;

        // Crear la barra de menú y el menú Acciones
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAcciones = new JMenu("Acciones");
        menuBar.add(menuAcciones);

        // Crear y añadir ítems al menú Acciones
        JMenuItem itemResponder = new JMenuItem("Responder");
        JMenuItem itemReenviar = new JMenuItem("Reenviar");
        JMenuItem itemBorrar = new JMenuItem("Borrar");
        JMenuItem itemMover = new JMenuItem("Mover de carpeta");

        // Acción de borrar el correo
        itemBorrar.addActionListener(e -> {
            boolean delete = emailManager.deleteEmail(m.getFolder().getFullName(), m.getGh().getIdMessage());
            if (delete) {
                JOptionPane.showMessageDialog(null, "Correo eliminado correctamente", "Eliminado", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo eliminar el correo", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
         itemReenviar.addActionListener(e -> {
             new Reenvio((Frame) getOwner(), emailManager, m.getMessage());
        });
        
         
          itemMover.addActionListener(e -> {
              new MoveEmail((Frame) getOwner(), emailManager, m);
        });
        

        menuAcciones.add(itemResponder);
        menuAcciones.add(itemReenviar);
        menuAcciones.add(itemBorrar);
        menuAcciones.add(itemMover);

        // Añadir la barra de menú al diálogo
        setJMenuBar(menuBar);

        // Crear un panel para mostrar el contenido del correo
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");  // Configura el tipo de contenido a HTML si se espera contenido en ese formato
        textPane.setText(m.getTextMessage());  // Añade el mensaje de correo al JTextPane
        textPane.setEditable(false);  // Hace que JTextPane no sea editable
        panel.add(new JScrollPane(textPane), BorderLayout.CENTER);  // Añade scroll al JTextPane

        // Añadir el panel al diálogo
        add(panel);

        // Configurar el diálogo
        setTitle("Contenido del correo");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setModal(true);
        setVisible(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
}
