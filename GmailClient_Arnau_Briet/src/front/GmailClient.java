package front;

import Conn.EmailManager;
import javax.mail.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;

public class GmailClient extends JFrame {
    private EmailManager emailManager;
    private JTree foldersTree;
    private JTextPane emailContent;
    private JSplitPane splitPane;
    private JMenuBar menuBar;
    private JMenuItem actualizarMenuItem, escribirMenuItem;
    private JPanel emailActionsPanel;

    public GmailClient(String user, String password) {
        
        // Inicializar EmailManager con las credenciales de usuario
        emailManager = new EmailManager(user, password);
        initComponents();
        initMenu();
        setupEmailActions();
        connectAndLoadFolders();  // Conectar y cargar las carpetas al iniciar
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setTitle("Gmail Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

        foldersTree = new JTree();
        emailContent = new JTextPane();
        emailContent.setContentType("text/html");
        emailContent.setEditable(false);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(foldersTree),
                new JScrollPane(emailContent));
        splitPane.setDividerLocation(200);

        emailActionsPanel = new JPanel(new FlowLayout());
        
        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(emailActionsPanel, BorderLayout.SOUTH);
    }

    private void initMenu() {
        menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menú");
        actualizarMenuItem = new JMenuItem("Actualizar");
        escribirMenuItem = new JMenuItem("Escribir");

        menu.add(actualizarMenuItem);
        menu.add(escribirMenuItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        escribirMenuItem.addActionListener(e -> mostrarDialogoComposicion());
        actualizarMenuItem.addActionListener(e -> connectAndLoadFolders());
    }

    private void connectAndLoadFolders() {
        try {
            emailManager.connect();
            Folder[] folders = emailManager.getMailFolders();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Email Account");
            for (Folder folder : folders) {
                root.add(new DefaultMutableTreeNode(folder.getName()));
            }
            foldersTree.setModel(new DefaultTreeModel(root));
        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to email server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setupEmailActions() {
        // Botones y acciones de correo aquí
    }

    private void mostrarDialogoComposicion() {
        // Lógica para abrir un JDialog y componer un correo
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(GmailClient::new);
//    }
}
