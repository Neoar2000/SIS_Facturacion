import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SistemaDAO implements AutoCloseable {
    private Connection connection;

    public SistemaDAO(Connection connection) {
        this.connection = connection;
    }

    public int obtenerIdProducto(String nombreProducto) throws SQLException {
        int idProducto = -1;

        String query = "SELECT id FROM productos WHERE nombre = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nombreProducto);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    idProducto = resultSet.getInt("id");
                }
            }
        }

        return idProducto;
    }

    // Método para obtener el ID del cliente a partir de su NIT/CI
    public int obtenerIdCliente(String nitCi) throws SQLException {
        int idCliente = -1;

        String query = "SELECT id FROM clientes WHERE nit_ci = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nitCi);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    idCliente = resultSet.getInt("id");
                }
            }
        }

        return idCliente;
    }

    // Método close() para cerrar la conexión
    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
