package front;

import Conn.EmailManager;
import javax.mail.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultTreeModel;
import models.GmailHeader;
import models.Mail;

public class App extends JFrame {

    final int WFRAME = 600;
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
    private JPanel emailActionsPanel;

    java.util.List<GmailHeader> cabezerasActuales;
    Message[] mensajesActuales;
    Mail mailSeleccionado;
    String folderName = "";
    Folder[] folders;
    Folder carpetaActual;

    // Constructor
    public App(String user, String password) {
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

    // Método para iniciar el menú
    private void iniciarMenu() {
        menuBar = new JMenuBar();
        menu = new JMenu("Menú");
        menuBar.add(menu);

        actualizarMenuItem = new JMenuItem("Actualizar");
        menu.add(actualizarMenuItem);
        actualizarMenuItem.addActionListener(e -> actualizarContenidoCarpeta());

        escribirMenuItem = new JMenuItem("Escribir");
        escribirMenuItem.addActionListener(e -> new SendMail(emailManager, (Frame) getOwner()));
        menu.add(escribirMenuItem);

        setJMenuBar(menuBar);

        foldersTree = new JTree();
        listModel = new DefaultListModel<>();
        emailList = new JList<>(listModel);
        emailList.setLayoutOrientation(JList.VERTICAL);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(foldersTree),
                new JScrollPane(emailList));
        splitPane.setDividerLocation(200);

        emailActionsPanel = new JPanel(new FlowLayout());

        getContentPane().add(splitPane, BorderLayout.CENTER);
        getContentPane().add(emailActionsPanel, BorderLayout.SOUTH);
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
                            break;
                        }
                    }
                    actualizarContenidoCarpeta();
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
                                        System.out.println(carpetaActual.getFullName());
                                        mailSeleccionado.setFolder(carpetaActual);
                                        mailSeleccionado.setMessage(mensajesActuales[i]);
                                        break; // Detiene el bucle una vez que encuentra el ID correcto
                                    } catch (MessagingException ex) {
                                        ex.printStackTrace();
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }

                            new MailContent(mailSeleccionado, emailManager, (Frame) getOwner());
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
    private void actualizarContenidoCarpeta() {
        if (folderName != null && !folderName.isEmpty()) {
            try {
                mensajesActuales = emailManager.fetchMessages(folderName);
                cabezerasActuales = emailManager.obtenerHeaders(mensajesActuales);

                listModel.clear(); // Limpiar el modelo antes de agregar nuevos elementos
                for (GmailHeader header : cabezerasActuales) {
                    listModel.addElement(header.getFrom() + " - " + header.getAsunto() + " - " + header.getFecha());
                }
            } catch (MessagingException me) {
                listModel.clear();
                listModel.addElement("Error retrieving messages: " + me.getMessage());
            }
        } else {
            JOptionPane.showMessageDialog(this, "No folder is selected or folder is empty.", "Update Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App("mail", "password"));
    }
}
