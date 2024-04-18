import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;
import java.sql.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public class FacturacionInterfaz extends JFrame {

    private DefaultTableModel tableModel;
    private JLabel totalTextArea;
    private JTextArea reciboTextArea;
    private JTable productosTable;

    // Lista de productos de ejemplo
    private List<Producto> productos = new ArrayList<>();

    public FacturacionInterfaz() {
        setTitle("Facturacion NEO");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Establecer la ventana a pantalla completa
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cargarProductosDesdeBaseDeDatos();

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Inicialización de reciboTextArea
        reciboTextArea = new JTextArea();
        reciboTextArea.setEditable(false); // Desactivar edición
        reciboTextArea.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
        JScrollPane reciboScrollPane = new JScrollPane(reciboTextArea);

        mainPanel.add(reciboScrollPane, BorderLayout.CENTER);

        // Panel de encabezado
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Alineación centrada

        JLabel headerLabel = new JLabel("Facturacion NEO");
        headerLabel.setFont(new Font(headerLabel.getFont().getName(), Font.BOLD, 36)); // Fuente grande y negrita
        headerLabel.setForeground(Color.BLUE); // Cambiar color de texto a azul

        headerPanel.add(headerLabel);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Tabla de productos
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableModel.addColumn("Producto");
        tableModel.addColumn("Precio");
        tableModel.addColumn("Cantidad");
        tableModel.addColumn("Precio Total"); // Nueva columna para el precio total
        productosTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                int rendererHeight = component.getPreferredSize().height;
                int rowHeight = getRowHeight(row);
                if (rowHeight != rendererHeight) {
                    setRowHeight(row, rendererHeight);
                }
                return component;
            }
        };

        productosTable.setFont(new Font(productosTable.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño de la fuente

        // Renderizador personalizado para el encabezado de la tabla
        JTableHeader tableHeader = productosTable.getTableHeader();
        tableHeader.setFont(new Font(tableHeader.getFont().getName(), Font.BOLD, 20)); // Aumentar el tamaño de la fuente en el encabezado

        productosTable.getColumnModel().getColumn(1).setCellRenderer(new DecimalFormatRenderer());
        productosTable.getColumnModel().getColumn(2).setCellRenderer(new IntegerRenderer());
        productosTable.getColumnModel().getColumn(2).setCellEditor(new IntegerEditor(1, 100)); // Establecer límites de edición para cantidad

        productosTable.getDefaultEditor(Integer.class).addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                int row = productosTable.getSelectedRow();
                int cantidad = (int) tableModel.getValueAt(row, 2);
                double precioUnitario = (double) tableModel.getValueAt(row, 1);
                double nuevoPrecioTotal = cantidad * precioUnitario;
                tableModel.setValueAt(nuevoPrecioTotal, row, 3);
                actualizarTotal(); // Actualizar el total después de cambiar la cantidad
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
            }
        });
        
        // Definir un renderizador de celdas personalizado para la columna "Precio Total"
        productosTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                // Obtener el valor de la celda
                Object displayValue = value;
                if (value != null) {
                    // Si el valor no es nulo, agregar "Bs. " al inicio
                    displayValue = "Bs. " + value;
                }
                // Llamar al método de la superclase para obtener el componente de celda renderizado
                return super.getTableCellRendererComponent(table, displayValue, isSelected, hasFocus, row, column);
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(productosTable);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Panel de total
        JPanel totalPanel = new JPanel();
        totalPanel.setBorder(BorderFactory.createTitledBorder("Total"));
        totalTextArea = new JLabel();
        totalTextArea.setFont(new Font(totalTextArea.getFont().getName(), Font.BOLD, 40)); // Fuente negrita, tamaño 40
        totalTextArea.setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto
        totalPanel.add(totalTextArea);

        mainPanel.add(totalPanel, BorderLayout.SOUTH);

        // Panel de botones con BorderLayout
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Panel para los botones de acción (Agregar Producto y Modificar Cantidad)
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton agregarButton = new JButton("Agregar Producto");
        agregarButton.setFont(new Font(agregarButton.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño del texto del botón
        agregarButton.addActionListener(e -> mostrarVentanaProductos());

        JButton modificarCantidadButton = new JButton("Modificar Cantidad");
        modificarCantidadButton.setFont(new Font(modificarCantidadButton.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño del texto del botón
        modificarCantidadButton.addActionListener(e -> modificarCantidadProducto());

        topButtonPanel.add(agregarButton);
        topButtonPanel.add(modificarCantidadButton);

        // Panel para los botones de acción (Finalizar Compra y Eliminar Producto)
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton finalizarButton = new JButton("Finalizar Compra");
        finalizarButton.setFont(new Font(finalizarButton.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño del texto del botón
        finalizarButton.addActionListener(e -> mostrarVentanaDatosCliente());

        JButton eliminarButton = new JButton("Eliminar Producto");
        eliminarButton.setFont(new Font(eliminarButton.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño del texto del botón
        eliminarButton.addActionListener(e -> eliminarProductoSeleccionado());

        bottomButtonPanel.add(eliminarButton);
        bottomButtonPanel.add(finalizarButton);

        buttonPanel.add(topButtonPanel, BorderLayout.NORTH); // Agregar los botones superiores
        buttonPanel.add(bottomButtonPanel, BorderLayout.CENTER); // Agregar los botones inferiores

        // Botón "Salir del Sistema" en el SOUTH
        JButton salirButton = new JButton("Salir del Sistema");
        salirButton.setFont(new Font(salirButton.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño del texto del botón
        salirButton.addActionListener(e -> System.exit(0)); // Salir completamente del sistema

        buttonPanel.add(salirButton, BorderLayout.SOUTH); // Agregar el botón de salir del sistema en el sur

        mainPanel.add(buttonPanel, BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);
    }

    private void modificarCantidadProducto() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow != -1) {
            int cantidad = (int) tableModel.getValueAt(selectedRow, 2); // Obtener la cantidad actual del producto
            JTextField cantidadField = new JTextField(String.valueOf(cantidad));
            cantidadField.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
            
            // Crear un panel personalizado para el mensaje con el tamaño de fuente deseado
            JPanel panel = new JPanel(new GridLayout(0, 1));
            JLabel label = new JLabel("Ingrese la nueva cantidad:");
            label.setFont(new Font("Arial", Font.BOLD, 20)); // Aumentar el tamaño de la fuente
            panel.add(label);
            panel.add(cantidadField);
    
            int option = JOptionPane.showConfirmDialog(this, panel, "Modificar Cantidad", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    int nuevaCantidad = Integer.parseInt(cantidadField.getText());
                    if (nuevaCantidad > 0) {
                        // Actualizar la cantidad en el modelo de la tabla
                        tableModel.setValueAt(nuevaCantidad, selectedRow, 2);
    
                        // Actualizar el precio total después de cambiar la cantidad
                        double precioUnitario = (double) tableModel.getValueAt(selectedRow, 1);
                        double nuevoPrecioTotal = nuevaCantidad * precioUnitario;
                        // Actualizar el precio total en la columna correspondiente del modelo de la tabla
                        tableModel.setValueAt(nuevoPrecioTotal, selectedRow, 3);
                        actualizarTotal();
                    } else {
                        JOptionPane.showMessageDialog(this, "La cantidad debe ser un número positivo.", "Error", JOptionPane.ERROR_MESSAGE);
                        productosTable.clearSelection();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Ingrese un número válido para la cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
                    productosTable.clearSelection();
                }
            } else {
                productosTable.clearSelection();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un producto para modificar su cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }                          

    private void eliminarProductoSeleccionado() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            actualizarTotal();
        } else {
            JOptionPane.showMessageDialog(null, "Por favor, seleccione un producto para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarProductosDesdeBaseDeDatos() {
        // Inicializar la lista de productos
        productos = new ArrayList<>();
    
        // Conectar a la base de datos
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion", "Neoar2000", "Guitarhero3-*$.")) {
            try (SistemaDAO sistemaDAO = new SistemaDAO(connection)) {
                // Crear una consulta SQL para seleccionar todos los productos
                String sql = "SELECT nombre, precio FROM productos";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    // Ejecutar la consulta
                    try (ResultSet resultSet = statement.executeQuery()) {
                        // Iterar sobre los resultados y agregar los productos a la lista
                        while (resultSet.next()) {
                            String nombre = resultSet.getString("nombre");
                            double precio = resultSet.getDouble("precio");
   
                            // Obtener el ID del producto desde la base de datos usando el sistemaDAO
                            int idProducto = sistemaDAO.obtenerIdProducto(nombre);
   
                            productos.add(new Producto(idProducto, nombre, precio));
                        }
                    }
                }
            } catch (SQLException e) {
                throw e;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar los productos desde la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }    

    private void mostrarVentanaProductos() {
        // Crear una nueva ventana para mostrar los productos
        JFrame productosFrame = new JFrame("Selección de Productos");
        productosFrame.setSize(500, 400); // Aumentar el tamaño de la ventana
        productosFrame.setLocationRelativeTo(null);
    
        // Crear tabla para mostrar productos
        DefaultTableModel productosTableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productosTableModel.addColumn("Producto");
        productosTableModel.addColumn("Precio");
        JTable productosTable = new JTable(productosTableModel);
        productosTable.setFont(new Font(productosTable.getFont().getName(), Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
    
        productosTable.setRowHeight(30);
    
        // Renderizador personalizado para el encabezado de la tabla
        JTableHeader tableHeader = productosTable.getTableHeader();
        tableHeader.setFont(new Font(tableHeader.getFont().getName(), Font.BOLD, 20)); // Aumentar el tamaño de la fuente en el encabezado
    
        // Agregar productos a la tabla
        for (Producto producto : productos) {
            productosTableModel.addRow(new Object[]{producto.getNombre(), producto.getPrecio()});
        }
    
        // Agregar un listener para detectar la selección del usuario
        productosTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = productosTable.getSelectedRow();
                if (selectedRow != -1) {
                    // Obtener el producto seleccionado
                    Producto selectedProduct = productos.get(selectedRow);
    
                    // Crear un panel personalizado para el mensaje con el tamaño de fuente deseado
                    JPanel panel = new JPanel(new GridLayout(0, 1));
                    JLabel label = new JLabel("Ingrese la cantidad:");
                    label.setFont(new Font("Arial", Font.BOLD, 20)); // Aumentar el tamaño de la fuente
                    panel.add(label);
                    JTextField cantidadField = new JTextField();
                    cantidadField.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
                    panel.add(cantidadField);

                    // Mostrar el cuadro de diálogo con el panel personalizado
                    int option = JOptionPane.showConfirmDialog(productosFrame, panel, "Cantidad", JOptionPane.OK_CANCEL_OPTION);
                    if (option == JOptionPane.OK_OPTION) {
                        try {
                            // Verificar si se ingresó una cantidad
                            int cantidad = Integer.parseInt(cantidadField.getText());
                            if (cantidad > 0) {
                                // Agregar el producto seleccionado con la cantidad ingresada a la tabla principal
                                Object[] rowData = {selectedProduct.getNombre(), selectedProduct.getPrecio(), cantidad, selectedProduct.getPrecio() * cantidad};
                                tableModel.addRow(rowData);

                                // Actualizar el total
                                actualizarTotal();
                                productosFrame.dispose();
                            } else {
                                JOptionPane.showMessageDialog(productosFrame, "La cantidad debe ser un número entero positivo.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(productosFrame, "Ingrese un número entero válido.", "Error", JOptionPane.ERROR_MESSAGE);
                        } finally {
                            productosTable.clearSelection();
                        }
                    }
                    else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                        productosTable.clearSelection(); // Deseleccionar la fila seleccionada
                    }
                }
            }
});
    
        // Agregar la tabla a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(productosTable);
    
        // Agregar el JScrollPane al panel principal de la ventana
        productosFrame.add(scrollPane);
    
        // Hacer visible la ventana
        productosFrame.setVisible(true);
    }           

    private void actualizarTotal() {
        double total = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            double precioUnitario = (double) tableModel.getValueAt(i, 1);
            int cantidad = (int) tableModel.getValueAt(i, 2);
            total += precioUnitario * cantidad; // Multiplicar precio unitario por cantidad
        }
        totalTextArea.setText(String.format("Bs. %.2f", total));
    }
    
    private void mostrarVentanaDatosCliente() {
        // Verificar si hay productos agregados
        if (tableModel.getRowCount() == 0) {
            // Mostrar mensaje de error
            JOptionPane.showMessageDialog(this, "Debe agregar al menos un producto para finalizar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Salir del método sin continuar
        }
    
        JFrame datosClienteFrame = new JFrame("Datos del Cliente");
        datosClienteFrame.setSize(500, 250); // Aumentar el tamaño de la ventana
        datosClienteFrame.setLocationRelativeTo(null);
    
        // Crear un JLabel para el título "Datos del Cliente"
        JLabel titleLabel = new JLabel("Datos del Cliente");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Aumentar el tamaño de la fuente y hacerla negrita
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Centrar el texto
    
        // Agregar el JLabel al panel de datos del cliente
        datosClienteFrame.add(titleLabel, BorderLayout.NORTH);
    
        JPanel datosClientePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Ajustar los márgenes para reducir el espacio alrededor de los componentes
    
        JLabel nitCiLabel = new JLabel("NIT/CI:");
        nitCiLabel.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
        JTextField nitCiTextField = new JTextField(15); // Establecer un ancho inicial
        nitCiTextField.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño del texto
    
        JLabel nombreLabel = new JLabel("Nombre:");
        nombreLabel.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
        JTextField nombreTextField = new JTextField(15); // Establecer un ancho inicial
        nombreTextField.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño del texto
    
        // Autocompletar el nombre si ya ha sido registrado previamente
        nitCiTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                // Realizar una consulta a la base de datos para buscar el cliente por NIT/CI
                String nitCi = nitCiTextField.getText();
                String nombreCliente = obtenerNombreCliente(nitCi);
                if (nombreCliente != null) {
                    // Se encontró un cliente con el NIT/CI proporcionado, llenar automáticamente el campo de nombre
                    nombreTextField.setText(nombreCliente);
                }
            }
        });

        // Agregar un filtro para convertir el texto a mayúsculas en el campo de nombre
        ((AbstractDocument) nombreTextField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                super.replace(fb, offset, length, text.toUpperCase(), attrs); // Convertir el texto a mayúsculas antes de reemplazarlo
            }
        });
    
        gbc.gridx = 0;
        gbc.gridy = 0;
        datosClientePanel.add(nitCiLabel, gbc);
        gbc.gridx = 1;
        datosClientePanel.add(nitCiTextField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        datosClientePanel.add(nombreLabel, gbc);
        gbc.gridx = 1;
        datosClientePanel.add(nombreTextField, gbc);
    
        JButton confirmarButton = new JButton("Confirmar");
        confirmarButton.setFont(new Font("Arial", Font.BOLD, 20)); // Aumentar el tamaño de la fuente en el botón
        confirmarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener los datos del cliente
                String nitCi = nitCiTextField.getText();
                String nombre = nombreTextField.getText();
    
                registrarCliente(nitCi, nombre);
    
                // Realizar el registro de la compra con los datos del cliente y generar la factura
                try {
                    registrarCompra(nitCi, nombre);
                } catch (SQLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
    
                // Cerrar la ventana de datos del cliente
                datosClienteFrame.dispose();
            }
        });
    
        datosClienteFrame.add(datosClientePanel, BorderLayout.CENTER);
        datosClienteFrame.add(confirmarButton, BorderLayout.SOUTH);
    
        datosClienteFrame.setVisible(true);
    }       

    // Método para obtener el nombre del cliente desde la base de datos
    private String obtenerNombreCliente(String nitCi) {
        String nombreCliente = null;
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            // Establecer la conexión con la base de datos
            connection = DriverManager.getConnection("jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion", "Neoar2000", "Guitarhero3-*$.");

            // Preparar la consulta SQL
            String sql = "SELECT nombre FROM clientes WHERE nit_ci = ?";
            statement = connection.prepareStatement(sql);
            statement.setString(1, nitCi);

            // Ejecutar la consulta
            resultSet = statement.executeQuery();

            // Verificar si se encontró un resultado
            if (resultSet.next()) {
                // Obtener el nombre del cliente
                nombreCliente = resultSet.getString("nombre");
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Manejar la excepción apropiadamente en tu aplicación
        } finally {
            // Cerrar los recursos
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace(); // Manejar la excepción apropiadamente en tu aplicación
            }
        }

        return nombreCliente;
    }

    private int registrarCliente(String nitCi, String nombre) {
        // Realizar la inserción de los datos del cliente en la tabla "clientes" en la base de datos MySQL
        String url = "jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion";
        String usuario = "Neoar2000";
        String contraseña = "Guitarhero3-*$.";
        String sql = "INSERT INTO clientes (nit_ci, nombre) VALUES (?, ?)";
    
        int idCliente = -1; // Inicializar el id del cliente como -1, indicando que no se pudo registrar el cliente
    
        try (Connection conn = DriverManager.getConnection(url, usuario, contraseña);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    
            pstmt.setString(1, nitCi);
            pstmt.setString(2, nombre);
    
            int filasInsertadas = pstmt.executeUpdate();
            if (filasInsertadas > 0) {
                // Obtener el ID generado automáticamente para el cliente insertado
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    idCliente = generatedKeys.getInt(1); // Obtener el valor del ID generado
                    System.out.println("Cliente registrado exitosamente en la base de datos. ID: " + idCliente);
                }
            } else {
                System.out.println("Error al registrar el cliente en la base de datos.");
            }
    
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar el cliente en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    
        return idCliente; // Devolver el ID del cliente registrado (o -1 si no se pudo registrar)
    }    

    // Método para registrar una compra en la base de datos
    private void registrarCompra(String nitCi, String nombre) throws SQLException {
        // Registrar el cliente y obtener su ID
        int idCliente = registrarCliente(nitCi, nombre);
        if (idCliente == -1) {
            System.out.println("No se pudo registrar la compra porque no se pudo registrar el cliente.");
            return;
        }
        
        // Continuar con el registro de la compra utilizando el ID del cliente
        try {
            // Obtener la fecha y la hora actual
            LocalDateTime fechaHoraActual = LocalDateTime.now();
    
            // Formatear la fecha y la hora en el formato deseado
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaHoraFormateada = fechaHoraActual.format(formatter);
    
            // Verificar si el NIT/CI y el nombre están vacíos
            if (nitCi.isEmpty()) {
                nitCi = "0"; // Asignar "0" si el NIT/CI está vacío
            }
            if (nombre.isEmpty()) {
                nombre = "S/N"; // Asignar "S/N" si el nombre está vacío
            }
    
            // Imprimir los datos del cliente y los productos comprados en el JTextArea del recibo
            StringBuilder sb = new StringBuilder();
            sb.append("\t                  EMPRESA S.A.\n");
            sb.append("----------------------------------------------------------------------------------------------\n");
            sb.append(String.format("%-5s: %s\n", "Fecha Emision", fechaHoraFormateada));
            sb.append(String.format("%-5s: %s\n", "NIT/CI", nitCi));
            sb.append(String.format("%5s: %s\n", "Nombre", nombre));
            sb.append("----------------------------------------------------------------------------------------------\n");
            sb.append("Productos Comprados:\n");
    
            // Encabezados de las columnas
            sb.append(String.format("%-20s %-20s %-10s %-10s\n", "Producto", "Precio Unitario", "Cantidad", "Precio Total"));
    
            double granTotal = 0;
            List<Producto> productosVendidos = new ArrayList<>();
    
            // Declaración de la instancia de SistemaDAO fuera del bloque try-catch
            SistemaDAO sistemaDAO = null;
            Connection connection = DriverManager.getConnection("jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion", "Neoar2000", "Guitarhero3-*$.");
            try {
                sistemaDAO = new SistemaDAO(connection); // Instanciamos el sistemaDAO
    
                // Resto del código dentro del bloque try-catch
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    String nombreProducto = (String) tableModel.getValueAt(i, 0);
                    double precioUnitario = (double) tableModel.getValueAt(i, 1);
                    int cantidad = (int) tableModel.getValueAt(i, 2);
                    double precioTotal = precioUnitario * cantidad; // Calcular el precio total del producto
    
                    // Obtener el ID del producto desde la base de datos usando el sistemaDAO
                    int idProducto = sistemaDAO.obtenerIdProducto(nombreProducto);
    
                    // Crear instancia de Producto con nombre, precio y ID
                    Producto producto = new Producto(idProducto, nombreProducto, precioUnitario);
                    productosVendidos.add(producto); // Agregar producto a la lista de productos vendidos
    
                    // Sumar el precio total al gran total
                    granTotal += precioTotal;
    
                    // Datos de cada producto, alineados en las columnas correspondientes
                    sb.append(String.format("%-20s Bs. %-19.2f %-10d Bs. %.2f\n", nombreProducto, precioUnitario, cantidad, precioTotal));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al cargar los productos desde la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
                throw e; // Lanzar la excepción hacia arriba para que sea manejada por el llamador
            } finally {
                if (sistemaDAO != null) {
                    sistemaDAO.close(); // Cerrar la instancia de SistemaDAO
                }
                if (connection != null) {
                    connection.close(); // Cerrar la conexión a la base de datos
                }
            }
    
            // Crear una instancia de Venta con los datos de la compra
            Venta venta = new Venta(nitCi, nombre, fechaHoraActual, productosVendidos, granTotal);
    
            // Mostrar el gran total
            sb.append("----------------------------------------------------------------------------------------------\n");
            sb.append(String.format("%-5s Bs. %.2f\n", "Total a pagar:", granTotal));
    
            // Mostrar la vista previa del recibo
            VistaPreviaRecibo vistaPreviaRecibo = new VistaPreviaRecibo(sb.toString());
            vistaPreviaRecibo.setVisible(true);
            dispose();
    
            // Llamar a un método en tu base de datos para registrar la venta
            registrarVentaEnBaseDeDatos(venta, idCliente, granTotal);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar la venta en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            throw ex; // Lanzar la excepción hacia arriba para que sea manejada por el llamador
        }
    }        

    private void registrarVentaEnBaseDeDatos(Venta venta, int idCliente, double granTotal) {
        // Información de conexión a la base de datos
        String url = "jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion";
        String usuario = "Neoar2000";
        String contraseña = "Guitarhero3-*$.";
        
        Connection connection = null; // Declarar la variable connection fuera del bloque try
    
        try {
            // Establecer la conexión con la base de datos
            connection = DriverManager.getConnection(url, usuario, contraseña);
            
            // Consulta SQL para insertar los datos de la venta
            String sql = "INSERT INTO ventas (id_cliente, total, fecha) VALUES (?, ?, ?)";
    
            // Preparar la declaración SQL para la inserción de datos
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Establecer los valores de los parámetros en la declaración preparada
                statement.setInt(1, idCliente); // Establecer el ID del cliente
                statement.setDouble(2, granTotal); // Establecer el granTotal como el valor del total
                statement.setTimestamp(3, Timestamp.valueOf(venta.getFechaHora()));
                //statement.setString(4, venta.getMetodoPago()); // Suponiendo que tienes un método para obtener el método de pago de la venta
    
                // Ejecutar la consulta SQL para insertar los datos en la base de datos
                int filasInsertadas = statement.executeUpdate();
                if (filasInsertadas > 0) {
                    System.out.println("La venta ha sido registrada correctamente en la base de datos.");
                } else {
                    System.out.println("Error: No se pudo registrar la venta en la base de datos.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Error al intentar preparar la consulta SQL.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al intentar establecer la conexión con la base de datos.");
        } finally {
            // Cerrar la conexión después de completar todas las operaciones
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    System.out.println("Error al cerrar la conexión.");
                }
            }
        }
    }        

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FacturacionInterfaz();
            }
        });
    }

    // Renderizador para formato decimal
    private class DecimalFormatRenderer extends DefaultTableCellRenderer {
        public DecimalFormatRenderer() {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }

        public void setValue(Object value) {
            if (value != null)
                value = String.format("Bs. %.2f", value);
            setText((String) value);
        }
    }

    // Renderizador para enteros
    private class IntegerRenderer extends DefaultTableCellRenderer {
        public IntegerRenderer() {
            super();
            setHorizontalAlignment(JLabel.RIGHT);
        }

        public void setValue(Object value) {
            setText(value.toString());
        }
    }

    // Editor de celda para enteros con límites
    private class IntegerEditor extends AbstractCellEditor implements TableCellEditor {
        private JSpinner spinner;

        public IntegerEditor(int min, int max) {
            spinner = new JSpinner(new SpinnerNumberModel(1, min, max, 1));
        }

        @Override
        public Object getCellEditorValue() {
            return spinner.getValue();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            spinner.setValue(value);
            return spinner;
        }
    }
}