package front;

import javax.swing.*;
import java.awt.*;
import models.Mail;

public class MailContent extends JDialog {

    
    
    public MailContent(Mail m) {
        // Configurar el diálogo
        setTitle("Mail content");
        setSize(500, 500);
        setLocationRelativeTo(null);
        setModal(true);

        // Crear la barra de menú y el menú Acciones
        JMenuBar menuBar = new JMenuBar();
        JMenu menuAcciones = new JMenu("Acciones");
        menuBar.add(menuAcciones);

        // Crear y añadir ítems al menú Acciones
        JMenuItem itemResponder = new JMenuItem("Responder");
        JMenuItem itemReenviar = new JMenuItem("Reenviar");
        JMenuItem itemBorrar = new JMenuItem("Borrar");
        JMenuItem itemMover = new JMenuItem("Mover de carpeta");

        menuAcciones.add(itemResponder);
        menuAcciones.add(itemReenviar);
        menuAcciones.add(itemBorrar);
        menuAcciones.add(itemMover);

        // Añadir la barra de menú al diálogo
        setJMenuBar(menuBar);

        // Crear un panel con texto
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JTextArea textArea = new JTextArea("Este es un panel con texto.");
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER); // Agrega scroll al área de texto

        // Añadir el panel al diálogo
        add(panel);

        // Configuración de cierre del diálogo
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public static void main(String[] args) {
        // Mostrar el diálogo
        MailContent dialogo = new MailContent(null);
        dialogo.setVisible(true);
    }
}
