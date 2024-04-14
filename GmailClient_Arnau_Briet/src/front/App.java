import javax.mail.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

public class App extends JFrame {
    
    String USER = "yorMail";
    String PASSWD = "pass";
    
    final int WFRAME = 600;
    final int HFRAME = 400;
    
    // Variables del menú
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem actualizarMenuItem;
    private JMenuItem escribirMenuItem;

    // Variables de la interfaz de usuario
    private JTree tree;

    // Constructor
    public App() {
        iniciarPantalla();
        iniciarMenu();
        iniciarTablaCarpetas();
    }

    // Método para iniciar la ventana principal
    private void iniciarPantalla() {
        setTitle("Mail App");
        setMinimumSize(new Dimension(WFRAME, HFRAME));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(new Color(74, 157, 240));
        setVisible(true);
    }

    // Método para iniciar el menú
    private void iniciarMenu() {
        menuBar = new JMenuBar();
        menu = new JMenu("Menú");
        menuBar.add(menu);

        // Opción Actualizar con ícono
        ImageIcon actualizarIcono = new ImageIcon("../images/actualizar.png"); // Ajusta la ruta a tu icono
        actualizarMenuItem = new JMenuItem("Actualizar", actualizarIcono);
        menu.add(actualizarMenuItem);

        // Opción Escribir con ícono
        ImageIcon escribirIcono = new ImageIcon("escribir.png"); // Ajusta la ruta a tu icono
        escribirMenuItem = new JMenuItem("Escribir", escribirIcono);
        menu.add(escribirMenuItem);

        setJMenuBar(menuBar);

        // Acción del botón Escribir
        escribirMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implementa la lógica para escribir un correo nuevo
                // Abre un JDialog para pedir los datos necesarios para el envío
                // Implementa la funcionalidad del envío
            }
        });

        // Acción del botón Actualizar
        actualizarMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Implementa la lógica para actualizar los correos
                // Descarga los nuevos correos si los hay
            }
        });
    }

    // Método para iniciar la tabla de carpetas
    private void iniciarTablaCarpetas() {
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Carpetas");

        // Obtener nombres de carpetas desde el servidor IMAP
        String[] nombresCarpetas = obtenerNombresCarpetas();
        for (String nombre : nombresCarpetas) {
            raiz.add(new DefaultMutableTreeNode(nombre));
        }

        tree = new JTree(raiz);

        // Agregar el árbol al panel
        JScrollPane panelScroll = new JScrollPane(tree);
        add(panelScroll, BorderLayout.WEST);
    }

    // Método para obtener nombres de carpetas desde el servidor IMAP
    private String[] obtenerNombresCarpetas() {
        String[] nombresCarpetas = null;
        try {
            Properties props = new Properties();
            props.setProperty("mail.store.protocol", "imaps");
            Session session = Session.getInstance(props, null);
            Store store = session.getStore();
            store.connect("imap.gmail.com", USER, PASSWD);
            Folder[] folders = store.getFolder("[Gmail]").list();
            nombresCarpetas = new String[folders.length];
            for (int i = 0; i < folders.length; i++) {
                nombresCarpetas[i] = folders[i].getName();
            }
            store.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return nombresCarpetas;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new App();
            }
        });
    }
}
