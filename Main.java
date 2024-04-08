import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public Main() {
        setTitle("Sistema de Facturación NEO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Crear los componentes de la interfaz de usuario
        JLabel usernameLabel = new JLabel("Usuario:");
        usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordField = new JPasswordField(20);
        
        JButton loginButton = new JButton("Iniciar sesión");
        loginButton.addActionListener(e -> login());

        // Crear el panel y establecer el diseño
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(5, 5, 5, 5);

        // Agregar los componentes al panel
        constraints.gridx = 0;
        constraints.gridy = 0;
        panel.add(usernameLabel, constraints);

        constraints.gridx = 1;
        panel.add(usernameField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        panel.add(passwordLabel, constraints);

        constraints.gridx = 1;
        panel.add(passwordField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, constraints);

        // Agregar el panel a la ventana
        add(panel);

        // Centrar la ventana en la pantalla
        pack();
        setLocationRelativeTo(null);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Verificar las credenciales
        if (username.equals("admin") && password.equals("123")) {
            JOptionPane.showMessageDialog(this, "Bienvenido, " + username + "!");
        } else {
            JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}
