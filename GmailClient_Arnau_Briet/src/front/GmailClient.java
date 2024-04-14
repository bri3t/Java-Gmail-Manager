package front;

import Conn.EmailManager;
import javax.mail.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import javax.swing.tree.DefaultTreeModel;
import models.GmailHeader;
import java.util.List;
import models.Mail;

public class GmailClient extends JFrame {

    private EmailManager emailManager;
    private JTree foldersTree;
    private JList<String> emailList;
    private DefaultListModel<String> listModel;
    private JSplitPane splitPane;
    private JPanel emailActionsPanel;
    List<GmailHeader> cabezerasActuales;
    List<Mail> correosActuales;

    public GmailClient(String user, String password) {
        emailManager = new EmailManager(user, password);
        initComponents();
        connectAndLoadFolders();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setTitle("Gmail Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));

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

    private void connectAndLoadFolders() {
        try {
            emailManager.connect();
            Folder[] folders = emailManager.getMailFolders();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("Email Account");
            for (Folder folder : folders) {
                root.add(new DefaultMutableTreeNode(folder.getName()));
            }
            foldersTree.setModel(new DefaultTreeModel(root));

            foldersTree.addTreeSelectionListener(new TreeSelectionListener() {
                @Override
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) foldersTree.getLastSelectedPathComponent();
                    if (selectedNode != null) {
                        String folderName = selectedNode.getUserObject().toString();
                        try {
                            cabezerasActuales = emailManager.fetchMessages(folderName);
                            
                            listModel.clear(); // Limpiar el modelo antes de agregar nuevos elementos
                            for (GmailHeader header : cabezerasActuales) {
                                listModel.addElement(header.getFrom() + "     -     " + header.getAsunto() + "     -     " + header.getFecha());
                            }
                        } catch (MessagingException me) {
                            listModel.clear();
                            listModel.addElement("Error retrieving messages: " + me.getMessage());
                        }
                    }
                }
            });

            emailList.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {  // Este chequeo asegura que el evento se procesa solo una vez
                        int selectedIdx = emailList.getSelectedIndex();
                        if (selectedIdx != -1) {
                            System.out.println(cabezerasActuales.get(selectedIdx).getIdMessage());
                            // Puedes añadir más lógica aquí, por ejemplo, mostrar detalles del elemento seleccionado
                        }
                    }
                }
            });

        } catch (MessagingException e) {
            JOptionPane.showMessageDialog(this, "Error connecting to email server: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
    public static void main(String[] args) {
        new GmailClient("adam22abriet@inslaferreria.cat", "Arnauinsti-22");
    }
}
