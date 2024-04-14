package auth;

import Conn.EmailManager;
import front.GmailClient;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

/**
 *
 * @author arnau
 */
public class Login extends JFrame {

    final int WFRAME = 400;
    final int HFRAME = 300;

    JPanel panel;
    GridBagConstraints gbc;

    JLabel labelUsuari, labelContrasenya;
    JTextField tfGmail;
    JPasswordField pfPassword;

    JButton btnLogin;

    EmailManager em;

    public Login() {
        iniciarPanel();
        iniciarPantalla();
    }

    private void _clear() {
        tfGmail.setText("");
        pfPassword.setText("");
    }

    private void iniciarPanel() {
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        labelUsuari = new JLabel("Gmail:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(labelUsuari, gbc);

        labelContrasenya = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        panel.add(labelContrasenya, gbc);

        tfGmail = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(tfGmail, gbc);

        pfPassword = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(pfPassword, gbc);

        btnLogin = new JButton("Login");
        btnLogin.setPreferredSize(new Dimension(150, 25));
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = tfGmail.getText();
                String password = new String(pfPassword.getPassword());
                em = new EmailManager(user, password);
                if (em.connect()) {
                    new GmailClient(user, password);
                } else {
                    JOptionPane.showMessageDialog(null, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
                    _clear();
                }
            }

        });

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnLogin, gbc);

        add(panel);
    }

    private void iniciarPantalla() {
        setTitle("Gmail Login");
        setMinimumSize(new Dimension(WFRAME, HFRAME));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(Color.ORANGE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public static void main(String[] args) {
        new Login();
    }

}
