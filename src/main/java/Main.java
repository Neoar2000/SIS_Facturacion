import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.sql.*;

public class Main extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean loginSuccessful = false;

    // Datos de conexión a la base de datos MySQL
    private static final String DB_URL = "jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion";
    private static final String DB_USER = "Neoar2000";
    private static final String DB_PASSWORD = "Guitarhero3-*$.";

    public Main() {
        initializeGUI();
    }

    // Constructor que acepta un objeto FacturacionInterfaz
    public Main(FacturacionInterfaz app) {
        initializeGUI();
        app.setVisible(true);
    }

    private void initializeGUI() {
        setTitle("Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crear los componentes de la interfaz de usuario
        JLabel usernameLabel = new JLabel("Usuario:");
        usernameLabel.setFont(new Font(usernameLabel.getFont().getName(), Font.PLAIN, 24)); // Aumentar el tamaño del texto
        usernameField = new JTextField(20);
        usernameField.setFont(new Font(usernameField.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño del texto

        JLabel passwordLabel = new JLabel("Contraseña:");
        passwordLabel.setFont(new Font(passwordLabel.getFont().getName(), Font.PLAIN, 24)); // Aumentar el tamaño del texto
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font(passwordField.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño del texto
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

        JButton loginButton = new JButton("Iniciar Sesión");
        loginButton.setFont(new Font(loginButton.getFont().getName(), Font.PLAIN, 24)); // Aumentar el tamaño del texto
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });

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

    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    public void setLoginSuccessful(boolean loginSuccessful) {
        this.loginSuccessful = loginSuccessful;
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
                    // Si las credenciales son correctas, cerramos la ventana de inicio de sesión
                    setLoginSuccessful(true); // Establecer el estado del inicio de sesión como exitoso
                    dispose();

                    // Obtener la instancia de FacturacionInterfaz actual
                    Window[] windows = Window.getWindows();
                    for (Window window : windows) {
                        if (window instanceof FacturacionInterfaz) {
                            FacturacionInterfaz app = (FacturacionInterfaz) window;
                            app.setVisible(true); // Hacer visible la instancia existente de FacturacionInterfaz
                            break;
                        }
                    }
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
            Main loginFrame = new Main();
            loginFrame.setVisible(true);

            // Agregar un WindowListener a la ventana de inicio de sesión
            loginFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Verificar si el inicio de sesión fue exitoso
                    if (loginFrame.isLoginSuccessful()) {
                        // Crear una instancia de FacturacionInterfaz solo si el inicio de sesión es exitoso
                        FacturacionInterfaz facturaFrame = new FacturacionInterfaz();
                        facturaFrame.setVisible(true); // Hacer visible la ventana principal
                    }
                }
            });
        });
    }
}