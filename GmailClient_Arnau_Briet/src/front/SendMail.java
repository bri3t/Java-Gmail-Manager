package front;

import Conn.EmailManager;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SendMail extends JDialog {

    private EmailManager emailManager;
    private List<File> attachedFiles;  // Lista para almacenar los archivos adjuntos
    private JLabel filesCountLabel;  // Etiqueta para mostrar el número de archivos adjuntos

    public SendMail(EmailManager em, Frame owner) {
        super(owner, true);
        this.emailManager = em;
        attachedFiles = new ArrayList<>();  // Inicializar la lista de archivos adjuntos

        setLayout(new BorderLayout(10, 10)); // Margen entre los bordes del frame y los paneles

        // Panel para los campos de texto con margen
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));  // Ajustado para incluir la nueva fila
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Crear y añadir campos de texto y etiquetas
        panel.add(new JLabel("Para: (example@gmail.com example2@gmail.com ...)"));
        JTextField toTextField = new JTextField();
        panel.add(toTextField);

        panel.add(new JLabel("CC:"));
        JTextField ccTextField = new JTextField();
        panel.add(ccTextField);

        panel.add(new JLabel("CCO:"));
        JTextField bccTextField = new JTextField();
        panel.add(bccTextField);

        panel.add(new JLabel("Asunto:"));
        JTextField subjectTextField = new JTextField();
        panel.add(subjectTextField);

        panel.add(new JLabel("Mensaje:"));
        JTextArea messageTextArea = new JTextArea();
        messageTextArea.setLineWrap(true);
        messageTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageTextArea);
        panel.add(scrollPane);

        // Etiqueta y botón para adjuntar archivos
        filesCountLabel = new JLabel("Número de ficheros adjuntados: 0");  // Etiqueta inicial
        panel.add(filesCountLabel);
        
        JButton attachButton = new JButton("Adjuntar Archivos");
        attachButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setMultiSelectionEnabled(true);  // Permitir seleccionar múltiples archivos
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    attachedFiles.add(file);
                }
                filesCountLabel.setText("Número de ficheros adjuntados: " + attachedFiles.size());  // Actualizar la etiqueta
            }
        });
        panel.add(attachButton);

        // Añadir el panel al frame
        add(panel, BorderLayout.CENTER);

        // Botón para enviar el correo
        JButton sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> {
            String toText = toTextField.getText().trim();
            String ccText = ccTextField.getText().trim();
            String bccText = bccTextField.getText().trim();
            String subject = subjectTextField.getText().trim();
            String content = messageTextArea.getText();
            boolean isHtmlContent = false; // Asumir texto plano, ajustar según sea necesario

            if (toText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El campo 'Para' no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String[] to = toText.split("\\s+");
            String[] cc = ccText.isEmpty() ? new String[0] : ccText.split("\\s+");
            String[] bcc = bccText.isEmpty() ? new String[0] : bccText.split("\\s+");
            String[] attachments = attachedFiles.stream().map(File::getPath).toArray(String[]::new);

            if (emailManager.sendEmail(to, cc, bcc, subject, content, isHtmlContent, attachments)) {
                JOptionPane.showMessageDialog(this, "Correo enviado correctamente.", "Enviado", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al enviar correo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(sendButton, BorderLayout.SOUTH);

        setTitle("Enviar Email");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setVisible(true);
    }
}
