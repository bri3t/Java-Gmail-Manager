package front;

import Conn.EmailManager;
import auth.Login;
import javax.mail.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import javax.mail.Message;

import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import models.GmailHeader;
import models.Mail;

public class App extends JFrame {

    final int WFRAME = 800;
    final int HFRAME = 400;

    private EmailManager emailManager;

    // Variables del menú
    private JMenuBar menuBar;
    private JMenu menu;
    private JMenuItem actualizarMenuItem;
    private JMenuItem escribirMenuItem;

    // Variables de la interfaz de usuario
    private JTree foldersTree;
    private JList<String> emailList;
    private DefaultListModel<String> listModel;
    private JSplitPane splitPane;

    java.util.List<GmailHeader> cabezerasActuales;
    Message[] mensajesActuales;
    Mail mailSeleccionado;
    String folderName = "";
    Folder[] folders;
    Folder carpetaActual;
    String userSesion;
    App app = this;

    public static int messageCountActual = 0;

    // Constructor
    public App(String user, String password) {
        userSesion = user;
        emailManager = new EmailManager(user, password);

        iniciarMenu();
        iniciarTablaCarpetas();
        iniciarPantalla();
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

    private void iniciarMenu() {
        // Configuración existente del menú
        menuBar = new JMenuBar();
        menu = new JMenu("Menú");
        actualizarMenuItem = new JMenuItem("Actualizar");
        escribirMenuItem = new JMenuItem("Escribir");

        menu.add(actualizarMenuItem);
        menu.add(escribirMenuItem);
        menuBar.add(menu);

        actualizarMenuItem.addActionListener(e -> actualizarContenidoCarpeta(2));
        escribirMenuItem.addActionListener(e -> new SendMail(emailManager, (Frame) getOwner()));

        // Panel para botones de navegación
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton leftArrowButton = new JButton("<--  ");
        JLabel cambiarPagina = new JLabel("Cambiar de página");
        JButton rightArrowButton = new JButton("  -->");

        leftArrowButton.addActionListener(e -> actualizarContenidoCarpeta(1));
        rightArrowButton.addActionListener(e -> actualizarContenidoCarpeta(0));

        navigationPanel.add(leftArrowButton);
        navigationPanel.add(cambiarPagina);
        navigationPanel.add(rightArrowButton);

        // Panel de usuario con botón de cerrar sesión
        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        JLabel userLabel = new JLabel("Cuenta abierta: " + userSesion);
        JButton logoutButton = new JButton("Cerrar sesión");
        logoutButton.addActionListener(e -> logout());
        userPanel.add(userLabel, BorderLayout.WEST);
        userPanel.add(logoutButton, BorderLayout.EAST);

        // Añadir menú de navegación y panel de usuario al frame
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(menuBar, BorderLayout.NORTH);
        topPanel.add(navigationPanel, BorderLayout.SOUTH);

        getContentPane().add(topPanel, BorderLayout.NORTH);
        getContentPane().add(userPanel, BorderLayout.SOUTH);

        // Configurar el área central con el árbol y la lista de correos
        foldersTree = new JTree();
        listModel = new DefaultListModel<>();
        emailList = new JList<>(listModel);
        emailList.setLayoutOrientation(JList.VERTICAL);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(foldersTree),
                new JScrollPane(emailList));
        splitPane.setDividerLocation(200);

        getContentPane().add(splitPane, BorderLayout.CENTER);
    }

// Método para manejar el cierre de sesión
    private void logout() {
        try {
            emailManager.disconnect();
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        dispose(); // Cerrar la ventana actual
        new Login(); // Abrir la ventana de login (debes implementar esta clase)
    }

    // Método para iniciar la tabla de carpetas
    private void iniciarTablaCarpetas() {
        try {
            emailManager.connect();
            folders = emailManager.getMailFolders();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Email Account");
            for (Folder folder : folders) {
                root.add(new DefaultMutableTreeNode(folder.getName()));
            }
            foldersTree.setModel(new DefaultTreeModel(root));

            foldersTree.addTreeSelectionListener(e -> {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) foldersTree.getLastSelectedPathComponent();
                if (selectedNode != null) {
                    folderName = selectedNode.getUserObject().toString();
                    for (Folder folder : folders) {
                        if (folder.getName().equalsIgnoreCase(folderName)) {
                            carpetaActual = folder;
                            emailManager.setCarpetaActual(carpetaActual);
                            break;
                        }
                    }
                    try {
                        messageCountActual = carpetaActual.getMessageCount();
                        actualizarContenidoCarpeta(99);
                    } catch (MessagingException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            emailList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        int selectedIdx = emailList.getSelectedIndex();
                        if (selectedIdx != -1) {
                            mailSeleccionado = new Mail();

                            int id = cabezerasActuales.get(selectedIdx).getIdMessage();

                            for (int i = 0; i < mensajesActuales.length; i++) {
                                if (id == mensajesActuales[i].getMessageNumber()) {
                                    try {
                                        mailSeleccionado = emailManager.extractMessageParts(mensajesActuales[i]);
                                        mailSeleccionado.setGh(cabezerasActuales.get(i));
                                        mailSeleccionado.setFolder(carpetaActual);
                                        mailSeleccionado.setMessage(mensajesActuales[i]);
                                        mailSeleccionado.setFromMail(cabezerasActuales.get(i).getFrom());
                                        break; // Detiene el bucle una vez que encuentra el ID correcto
                                    } catch (MessagingException ex) {
                                        ex.printStackTrace();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }

                            new MailContent(mailSeleccionado, emailManager, (Frame) getOwner(), app);
                        } else {
                        }
                    }
                }
            });

        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to email server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para actualizar el contenido de la carpeta seleccionada
    public void actualizarContenidoCarpeta(int num) {
        if (folderName != null && !folderName.isEmpty()) {
            try {
                mensajesActuales = emailManager.fetchMessages(carpetaActual, num, messageCountActual);
                cabezerasActuales = emailManager.obtenerHeaders(mensajesActuales);

                // Limpiar el modelo antes de agregar nuevos elementos
                listModel.clear();

                // Crear listas temporales para invertir el orden
                List<GmailHeader> reversedHeaders = new ArrayList<>(cabezerasActuales);
                List<Message> reversedMessages = new ArrayList<>(Arrays.asList(mensajesActuales));

                // Invertir las listas
                Collections.reverse(reversedHeaders);
                Collections.reverse(reversedMessages);

                // Actualizar las listas actuales a las invertidas
                cabezerasActuales = reversedHeaders;
                mensajesActuales = reversedMessages.toArray(new Message[reversedMessages.size()]);

                // Añadir los elementos invertidos al modelo de lista
                for (GmailHeader header : cabezerasActuales) {
                    listModel.addElement(header.getFrom() + "     ---    " + header.getAsunto() + "     ---    " + header.getFecha());
                }
            } catch (MessagingException me) {
                listModel.clear();
                listModel.addElement("Error retrieving messages: " + me.getMessage());
            } finally {
                try {
                    if (carpetaActual != null && carpetaActual.isOpen()) {
                        carpetaActual.close(false); // Cierra la carpeta sin aplicar cambios
                    }
                } catch (MessagingException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No hay carpetas seleccionadas.", "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(() -> new App("mail", "pass"));
//    }
}
