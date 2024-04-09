import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Main extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    // Datos de conexión a la base de datos MySQL
    private static final String DB_URL = "jdbc:mysql://localhost:3306/SIS_Facturacion";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Guitarhero3-*$.";

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

        // Establecer la conexión con la base de datos
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String query = "SELECT * FROM usuarios WHERE usuario = ? AND contraseña = ?";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, username);
                statement.setString(2, password);
                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    JOptionPane.showMessageDialog(this, "Bienvenido, " + username + "!");
                } else {
                    JOptionPane.showMessageDialog(this, "Credenciales incorrectas", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al conectar con la base de datos", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Main app = new Main();
            app.setVisible(true);
        });
    }
}