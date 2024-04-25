import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

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
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FacturacionInterfaz extends JFrame {
    // Definición de la clase Cliente
    static class Cliente {
        private int id;
        private String nombre;

        public Cliente(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }
    }

    private DefaultTableModel tableModel;
    private JLabel totalTextArea;
    private JTextArea reciboTextArea;
    private JTable productosTable;
    private Map<String, Integer> mapaClientes = new HashMap<>();
    private String metodoPagoSeleccionado;
    private JTextField nitCiTextField;
    private JTextField nombreTextField;
    private JDialog datosClienteDialog;
    private double granTotal = 0;

    // Lista de productos de ejemplo
    private List<Producto> productos = new ArrayList<>();
    // Lista de clientes
    private List<Cliente> listaClientes;

    public FacturacionInterfaz() {
        listaClientes = new ArrayList<>();
        setTitle("Facturacion NEO");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Establecer la ventana a pantalla completa
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 20));

        cargarProductosDesdeBaseDeDatos();

        nitCiTextField = new JTextField(15);
        nombreTextField = new JTextField(15);

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
        totalTextArea.setText("Bs. 0.00");

        mainPanel.add(totalPanel, BorderLayout.SOUTH);

        // Panel de botones con BorderLayout
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Panel para los botones de acción (Agregar Producto y Modificar Cantidad)
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton agregarButton = new JButton("Agregar Producto");
        agregarButton.setFont(new Font(agregarButton.getFont().getName(), Font.BOLD, 20)); // Aumentar el tamaño del texto del botón
        agregarButton.addActionListener(e -> mostrarVentanaProductos());

        JButton modificarCantidadButton = new JButton("Modificar Cantidad");
        modificarCantidadButton.setFont(new Font(modificarCantidadButton.getFont().getName(), Font.BOLD, 20)); // Aumentar el tamaño del texto del botón
        modificarCantidadButton.addActionListener(e -> modificarCantidadProducto());

        topButtonPanel.add(agregarButton);
        topButtonPanel.add(modificarCantidadButton);

        // Panel para los botones de acción (Finalizar Compra y Eliminar Producto)
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton finalizarButton = new JButton("Finalizar Compra");
        finalizarButton.setFont(new Font(finalizarButton.getFont().getName(), Font.BOLD, 20)); // Aumentar el tamaño del texto del botón
        finalizarButton.addActionListener(e -> mostrarVentanaDatosCliente());

        JButton eliminarButton = new JButton("Eliminar Producto");
        eliminarButton.setFont(new Font(eliminarButton.getFont().getName(), Font.BOLD, 20)); // Aumentar el tamaño del texto del botón
        eliminarButton.addActionListener(e -> eliminarProductoSeleccionado());

        bottomButtonPanel.add(eliminarButton);
        bottomButtonPanel.add(finalizarButton);

        buttonPanel.add(topButtonPanel, BorderLayout.NORTH); // Agregar los botones superiores
        buttonPanel.add(bottomButtonPanel, BorderLayout.CENTER); // Agregar los botones inferiores

        // Botón "Salir del Sistema" en el SOUTH
        JButton salirButton = new JButton("Salir del Sistema");
        salirButton.setFont(new Font(salirButton.getFont().getName(), Font.BOLD, 20)); // Aumentar el tamaño del texto del botón
        salirButton.addActionListener(e -> System.exit(0)); // Salir completamente del sistema

        buttonPanel.add(salirButton, BorderLayout.SOUTH); // Agregar el botón de salir del sistema en el sur

        mainPanel.add(buttonPanel, BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);
    }

    public String getMetodoPagoSeleccionado() {
        return metodoPagoSeleccionado; // Método getter para obtener el método de pago seleccionado
    }

    public void setMetodoPagoSeleccionado(String metodoPagoSeleccionado) {
        this.metodoPagoSeleccionado = metodoPagoSeleccionado;
    }

    private void modificarCantidadProducto() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow != -1) {
            int cantidad = (int) tableModel.getValueAt(selectedRow, 2); // Obtener la cantidad actual del producto
            JTextField cantidadField = new JTextField(String.valueOf(cantidad));
            cantidadField.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
    
            // Crear un panel personalizado para el mensaje con el tamaño de fuente deseado
            JPanel panel = new JPanel(new GridLayout(0, 1, 10, 10)); // Espacio entre componentes
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen exterior
            JLabel label = new JLabel("Ingrese la nueva cantidad:");
            label.setFont(new Font("Arial", Font.BOLD, 20)); // Aumentar el tamaño de la fuente
            panel.add(label);
            panel.add(cantidadField);
    
            // Mostrar el cuadro de diálogo con el panel personalizado
            int option = JOptionPane.showConfirmDialog(this, panel, "Modificar Cantidad", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    int nuevaCantidad = Integer.parseInt(cantidadField.getText());
                    if (nuevaCantidad > 0) {
                        // Actualizar la cantidad en el modelo de la tabla
                        tableModel.setValueAt(nuevaCantidad, selectedRow, 2);
    
                        // Actualizar el precio total después de cambiar la cantidad
                        double precioUnitario = (double) tableModel.getValueAt(selectedRow, 1);
                        double nuevoPrecioTotal = nuevaCantidad * precioUnitario;
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
            }
        } else {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un producto para modificar su cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }        
    
    private void eliminarProductoSeleccionado() {
        int selectedRow = productosTable.getSelectedRow();
        if (selectedRow != -1) {
            tableModel.removeRow(selectedRow);
            actualizarTotal();
        } else {
            JOptionPane.showMessageDialog(null, "Debe seleccionar un producto para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
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
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar productos desde la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }    

    private void mostrarVentanaProductos() {
        // Crear una nueva ventana de diálogo para mostrar los productos
        JDialog productosDialog = new JDialog();
        productosDialog.setTitle("Selección de Productos");
        productosDialog.setSize(500, 400); // Aumentar el tamaño de la ventana
        productosDialog.setLocationRelativeTo(null);
    
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
                    int option = JOptionPane.showConfirmDialog(productosDialog, panel, "Cantidad", JOptionPane.OK_CANCEL_OPTION);
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
                            } else {
                                JOptionPane.showMessageDialog(productosDialog, "La cantidad debe ser un número entero positivo.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(productosDialog, "Ingrese un número entero válido.", "Error", JOptionPane.ERROR_MESSAGE);
                        } finally {
                            productosTable.clearSelection();
                        }
                    } else if (option == JOptionPane.CANCEL_OPTION || option == JOptionPane.CLOSED_OPTION) {
                        productosTable.clearSelection(); // Deseleccionar la fila seleccionada
                    }
                }
            }
        });
    
        // Agregar la tabla a un JScrollPane
        JScrollPane scrollPane = new JScrollPane(productosTable);
    
        // Agregar el JScrollPane al panel principal de la ventana
        productosDialog.add(scrollPane);
    
        // Agregar botón "Confirmar" en la parte inferior de la ventana
        JButton confirmarButton = new JButton("Confirmar");
        confirmarButton.setFont(new Font("Arial", Font.BOLD, 20));
        confirmarButton.addActionListener(e -> {
            if (tableModel.getRowCount() > 0) {
                productosDialog.dispose(); // Cerrar la ventana si hay al menos un producto
            } else {
                JOptionPane.showMessageDialog(productosDialog, "Agregue al menos un producto antes de confirmar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        productosDialog.add(confirmarButton, BorderLayout.SOUTH);
    
        // Hacer la ventana de diálogo modal
        productosDialog.setModal(true);
    
        // Hacer visible la ventana de diálogo
        productosDialog.setVisible(true);
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
            JOptionPane.showMessageDialog(this, "Agregue al menos un producto para finalizar compra.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Salir del método sin continuar
        }
    
        // Crear un cuadro de diálogo en lugar de una ventana
        datosClienteDialog = new JDialog();
        datosClienteDialog.setTitle("Datos del Cliente");
        datosClienteDialog.setSize(400, 200); // Aumentar el tamaño del cuadro de diálogo
        datosClienteDialog.setModal(true); // Hacer que el cuadro de diálogo sea modal
        datosClienteDialog.setLocationRelativeTo(this); // Centrar el cuadro de diálogo respecto a la ventana principal
    
        // Crear un JPanel para el contenido del cuadro de diálogo
        JPanel datosClientePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10); // Ajustar los márgenes para reducir el espacio alrededor de los componentes

        JLabel titleLabel = new JLabel("Datos del Cliente");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Aumentar el tamaño de la fuente y hacerla negrita
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER); // Centrar el texto
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        datosClientePanel.add(titleLabel, gbc);
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
    
        JLabel nitCiLabel = new JLabel("NIT/CI:");
        nitCiLabel.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
        JTextField nitCiTextField = new JTextField(15); // Establecer un ancho inicial
        nitCiTextField.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño del texto
    
        JLabel nombreLabel = new JLabel("Nombre:");
        nombreLabel.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño de la fuente
        JTextField nombreTextField = new JTextField(15); // Establecer un ancho inicial
        nombreTextField.setFont(new Font("Arial", Font.PLAIN, 20)); // Aumentar el tamaño del texto
    
        // Agregar validación del campo NIT/CI
        nitCiTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validateNitCi();
            }
    
            @Override
            public void removeUpdate(DocumentEvent e) {
                validateNitCi();
            }
    
            @Override
            public void changedUpdate(DocumentEvent e) {
                validateNitCi();
            }
    
            private void validateNitCi() {
                String nitCi = nitCiTextField.getText();
                if (!nitCi.matches("[0-9EeGgDd-]{0,10}")) {
                    // El texto ingresado no cumple con los criterios
                    JOptionPane.showMessageDialog(datosClienteDialog, "Solo se acepta números, - y EGD, y un máximo de 10 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
                    nitCiTextField.setText("");
                }
            }
        });
    
        // Agregar validación del campo Nombre
        nombreTextField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (nombreTextField.getText().length() >= 50) {
                    // El carácter ingresado no es una letra o se ha alcanzado el límite de caracteres
                    e.consume(); // Evitar que se ingrese el carácter
                    JOptionPane.showMessageDialog(datosClienteDialog, "Solo se acepta un máximo de 50 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
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
    
        gbc.gridx = 0;
        gbc.gridy = 1;
        datosClientePanel.add(nitCiLabel, gbc);
        gbc.gridx = 1;
        datosClientePanel.add(nitCiTextField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        datosClientePanel.add(nombreLabel, gbc);
        gbc.gridx = 1;
        datosClientePanel.add(nombreTextField, gbc);
    
        JButton confirmarButton = new JButton("Confirmar");
        confirmarButton.setFont(new Font("Arial", Font.BOLD, 20));
        confirmarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener los datos del cliente y asignarlos a las variables de instancia
                String nitCi = nitCiTextField.getText();
                String nombre = nombreTextField.getText();
    
                registrarCliente(nitCi, nombre);
    
                // Realizar el registro de la compra con los datos del cliente y generar la factura
                try {
                    registrarCompra(nitCi, nombre, metodoPagoSeleccionado);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
    
                // Cerrar el cuadro de diálogo de datos del cliente
                datosClienteDialog.dispose();
            }
        });
    
        datosClienteDialog.add(datosClientePanel, BorderLayout.CENTER);
        datosClienteDialog.add(confirmarButton, BorderLayout.SOUTH);
    
        datosClienteDialog.setVisible(true);
    }    

    private String obtenerNombreCliente(String nitCi) {
        // Siempre verificar primero en el mapa
        if (mapaClientes.containsKey(nitCi)) {
            int idCliente = mapaClientes.get(nitCi);
            String nombreCliente = obtenerNombreClienteDesdeMapa(idCliente);
            if (nombreCliente != null) {
                return nombreCliente;
            }
        }
    
        // Si no está en el mapa o no se encontró en la función anterior, consultar la base de datos
        String nombreCliente = null;
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion", "Neoar2000", "Guitarhero3-*$.");
             PreparedStatement statement = connection.prepareStatement("SELECT id, nombre FROM clientes WHERE nit_ci = ?")) {
            statement.setString(1, nitCi);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int idCliente = resultSet.getInt("id");
                    nombreCliente = resultSet.getString("nombre");
                    // Actualizar el mapa si se encuentra nuevo o diferente
                    mapaClientes.put(nitCi, idCliente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    
        return nombreCliente;
    }    
    
    private String obtenerNombreClienteDesdeMapa(int idCliente) {
        if (listaClientes != null && !listaClientes.isEmpty()) {
            for (Cliente cliente : listaClientes) {
                if (cliente.getId() == idCliente) {
                    return cliente.getNombre();
                }
            }
        }
        return null; // Devuelve null si la lista es nula o está vacía
    }               

    private int registrarCliente(String nitCi, String nombre) {
        // Verificar si los campos están vacíos y asignarles los valores predeterminados si es así
        if (mapaClientes.containsKey(nitCi)) {
            System.out.println("El cliente ya está registrado en la base de datos.");
            return mapaClientes.get(nitCi); // Devolver el ID del cliente existente
        }
    
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
            JOptionPane.showMessageDialog(this, "Error al registrar cliente en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    
        mapaClientes.put(nitCi, idCliente); // Guardar la relación en el mapa
    
        return idCliente;
    }   

    private void registrarCompra(String nitCi, String nombre, String metodoPago) throws SQLException {
        List<Producto> productosVendidos = new ArrayList<>();
        
        Connection connection = DriverManager.getConnection("jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion", "Neoar2000", "Guitarhero3-*$.");
        connection.setAutoCommit(false);  // Importante para manejar la transacción manualmente
        SistemaDAO sistemaDAO = new SistemaDAO(connection);
    
        try {
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String nombreProducto = (String) tableModel.getValueAt(i, 0);
                double precioUnitario = (double) tableModel.getValueAt(i, 1);
                int cantidad = (int) tableModel.getValueAt(i, 2);
                double precioTotal = precioUnitario * cantidad;
                int idProducto = sistemaDAO.obtenerIdProducto(nombreProducto);
                Producto producto = new Producto(idProducto, nombreProducto, precioUnitario);
                productosVendidos.add(producto);
                granTotal += precioTotal;
            }
    
            int idCliente = registrarCliente(nitCi, nombre);  // Asumiendo que esta función también maneja correctamente las transacciones
            if (idCliente != -1) {
                int idVenta = registrarVentaEnBaseDeDatos(new Venta(nitCi, nombre, LocalDateTime.now(), productosVendidos, granTotal), idCliente, granTotal, metodoPago);
                connection.commit();  // Solo hacer commit si todo ha ido bien
                
                mostrarVistaPreviaRecibo(nitCi, nombre, metodoPago, granTotal, productosVendidos, 0, 0, idVenta, this);
                abrirMetodoPago(nitCi, nombre, granTotal, productosVendidos);
            } else {
                connection.rollback();
                JOptionPane.showMessageDialog(this, "No se pudo registrar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            connection.rollback();
            JOptionPane.showMessageDialog(this, "Error al procesar la compra: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            throw ex;
        } finally {
            sistemaDAO.close();  // Asegúrate de cerrar el DAO y la conexión
            connection.close();
        }
    }    

    private void abrirMetodoPago(String nitCi, String nombre, double granTotal, List<Producto> productosVendidos) {
        MetodoPago metodoPagoVentana = new MetodoPago(this, this);
        metodoPagoVentana.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                obtenerNombreCliente(nitCi);
                reiniciarGranTotal();
            }
        });
        metodoPagoVentana.setMetodoPagoListener(new MetodoPagoListener() {
            @Override
            public void metodoPagoConfirmado(String metodoPago) {
                int idCliente = registrarCliente(nitCi, nombre);
                if (idCliente == -1) {
                    JOptionPane.showMessageDialog(null, "No se pudo registrar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                Venta venta = new Venta(nitCi, nombre, LocalDateTime.now(), productosVendidos, granTotal);
                int idVenta;
                try {
                    idVenta = registrarVentaEnBaseDeDatos(venta, idCliente, granTotal, metodoPago);
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(null, "Error al registrar la venta en la base de datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
    
                if (metodoPago.equals("Efectivo")) {
                    double cantidadPagada = solicitarCantidadPagada(granTotal);
                    if (cantidadPagada < 0) {
                        JOptionPane.showMessageDialog(null, "Transacción cancelada o entrada inválida.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    double cambio = cantidadPagada - granTotal;
                    mostrarVistaPreviaRecibo(nitCi, nombre, metodoPago, granTotal, productosVendidos, cantidadPagada, cambio, idVenta, FacturacionInterfaz.this);
                } else {
                    mostrarVistaPreviaRecibo(nitCi, nombre, metodoPago, granTotal, productosVendidos, 0, 0, idVenta, FacturacionInterfaz.this);
                }
                System.out.println("Método de pago seleccionado: " + metodoPago);
            }
        });
        metodoPagoVentana.setVisible(true);
    }
    
    
    private double solicitarCantidadPagada(double granTotal) {
        JTextField cantidadPagadaField = new JTextField();
        cantidadPagadaField.setFont(new Font("Arial", Font.PLAIN, 20));
        Object[] message = {"Ingrese la cantidad pagada:", cantidadPagadaField};
        int option = JOptionPane.showConfirmDialog(null, message, "Pago en Efectivo", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                double cantidadPagada = Double.parseDouble(cantidadPagadaField.getText());
                if (cantidadPagada >= granTotal) {
                    return cantidadPagada;
                } else {
                    JOptionPane.showMessageDialog(null, "La cantidad debe ser igual o mayor al total.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Cantidad inválida. Intente nuevamente.");
            }
        }
        return -1; // Devuelve -1 si el usuario cancela o hay un error de formato
    }                          

    public void metodoPagoSeleccionado(String metodoPago) {
        try {
            registrarCompra(nitCiTextField.getText(), nombreTextField.getText(), metodoPago);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al registrar la compra.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
        
    private void mostrarVistaPreviaRecibo(String nitCi, String nombre, String metodoPago, double granTotal, List<Producto> productosVendidos, double cantidadPagada, double cambio, int idVenta, FacturacionInterfaz facturacionInterfaz) {
        System.out.println("Valor de NIT/CI recibido: " + nitCi);
        System.out.println("Valor de nombre recibido: " + nombre);
        if (metodoPago != null) {
            // Si el método de pago no es nulo, entonces mostrar la vista previa del recibo
    
            // Obtener la fecha y la hora actual
            LocalDateTime fechaHoraActual = LocalDateTime.now();
    
            // Formatear la fecha y la hora en el formato deseado
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String fechaHoraFormateada = fechaHoraActual.format(formatter);
    
            // Verificar si el NIT/CI está vacío y ajustar el valor si es necesario
            String nitCiMostrar = nitCi;
    
            // Verificar si el nombre está vacío y ajustar el valor si es necesario
            String nombreMostrar = nombre;
    
            // Construir el recibo completo aquí usando los datos proporcionados
            StringBuilder sb = new StringBuilder();
            sb.append("                                        FACTURA\n");
            sb.append("                   CON DERECHO A CREDITO FISCAL\n");
            sb.append("                                    EMPRESA S.A.\n");
            sb.append("                                    CASA MATRIZ\n");
            sb.append("                          Calle Antonio Gonzales #740\n");
            sb.append("                                       71525880\n");
            sb.append("                                  LA PAZ - BOLIVIA\n");
            sb.append("-------------------------------------------------------------------------------\n");
            sb.append(String.format("%-5s: %s\n", "                                NIT", "1234567890"));
            sb.append(String.format("%5s: %s\n", "                     Cod. Autorizacion", "1234567890"));
            sb.append(String.format("%5s: %s\n", "                                 N° Factura", idVenta));
            sb.append("-------------------------------------------------------------------------------\n");
            sb.append(String.format("%-5s: %s\n", "                         Fecha", fechaHoraFormateada));
            sb.append(String.format("%-5s: %s\n", "                                 NIT/CI", nitCiMostrar));
            sb.append(String.format("%5s: %s\n", "                                 Nombre", nombreMostrar));
            sb.append("-------------------------------------------------------------------------------\n");
            sb.append(String.format("%-20s %-20s %-10s %-10s\n", "Producto", "P. Unitario", "Cantidad", "Total"));
    
            // Calcula el gran total correctamente sin sumarlo dentro del bucle
            double granTotalCalculado = granTotal;
    
            // Resto del código dentro del bloque try-catch
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                String nombreProducto = (String) tableModel.getValueAt(i, 0);
                double precioUnitario = (double) tableModel.getValueAt(i, 1);
                int cantidad = (int) tableModel.getValueAt(i, 2);
                double precioTotal = precioUnitario * cantidad; // Calcular el precio total del producto
    
                // Datos de cada producto, alineados en las columnas correspondientes
                sb.append(String.format("%-20s Bs. %-19.2f %-10d Bs. %.2f\n", nombreProducto, precioUnitario, cantidad, precioTotal));
                
                // Sumar el precio total al gran total calculado
                granTotalCalculado += precioTotal;
            }
            sb.append("-------------------------------------------------------------------------------\n\n");
    
            // Verifica si hay una discrepancia entre el gran total calculado y el gran total pasado como argumento
            if (granTotalCalculado != granTotal) {
                // Imprime un mensaje de advertencia si hay una discrepancia
                System.out.println("¡Advertencia! El gran total calculado difiere del gran total proporcionado.");
            }
    
            sb.append(String.format("%-5s Bs. %.2f\n\n", "Monto Total:", granTotal));
            sb.append(String.format("%-5s: %s\n\n", "Metodo de Pago", metodoPago));
    
            if (metodoPago.equals("Efectivo")) {
                // Mostrar la cantidad pagada y el cambio si el método de pago es efectivo
                sb.append(String.format("%-5s Bs. %.2f\n", "Efectivo pagado:", cantidadPagada));
                sb.append(String.format("%-5s Bs. %.2f\n", "Cambio:", cambio));
                sb.append("\nESTA FACTURA CONTRIBUYE AL DESARROLLO \nDEL PAÍS. EL USO ILÍCITO SERÁ SANCIONADO\nPENALMENTE DE ACUERDO A LEY\n");
                sb.append("\nLey N° 453: Los servicios deben suministrarse en \ncondiciones de inocuidad, calidad y seguridad.\n");
                sb.append("\nEste documento es la representacion grafica de un\ndocumento fiscal digtal emitido en una modalidad\nde facturacion en linea\n\n");
                sb.append("                             Facturacion NEO v1.0");
            } else {
                sb.append("ESTA FACTURA CONTRIBUYE AL DESARROLLO \nDEL PAÍS. EL USO ILÍCITO SERÁ SANCIONADO\nPENALMENTE DE ACUERDO A LEY\n");
                sb.append("\nLey N° 453: Los servicios deben suministrarse en \ncondiciones de inocuidad, calidad y seguridad.\n");
                sb.append("\nEste documento es la representacion grafica de un\ndocumento fiscal digtal emitido en una modalidad\nde facturacion en linea\n\n");
                sb.append("                             Facturacion NEO v1.0");
            }
    
            VistaPreviaRecibo vistaPreviaRecibo = new VistaPreviaRecibo(this, sb.toString(), 0.0, facturacionInterfaz);
            vistaPreviaRecibo.setVisible(true);
        } else {
            // Si el método de pago es nulo, imprimir un mensaje de error o manejar la situación según sea necesario
            System.out.println("El método de pago es nulo.");
        }
    }          
    
    private int registrarVentaEnBaseDeDatos(Venta venta, int idCliente, double granTotal, String metodoPagoSeleccionado) throws SQLException {
        String url = "jdbc:mysql://MacBook-Pro-de-Neo.local:3306/SIS_Facturacion";
        String usuario = "Neoar2000";
        String contraseña = "Guitarhero3-*$.";
        Connection connection = null;
        int idVenta = -1; // Inicializar el ID de la venta a -1

        try {
            connection = DriverManager.getConnection(url, usuario, contraseña);
            String sql = "INSERT INTO ventas (id_cliente, total, fecha, metodo_pago) VALUES (?, ?, ?, ?)";

            // Preparar la declaración SQL con la opción de recuperar claves generadas
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, idCliente);
                statement.setDouble(2, granTotal);
                statement.setTimestamp(3, Timestamp.valueOf(venta.getFechaHora()));
                statement.setString(4, metodoPagoSeleccionado);

                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("La creación de la venta falló, no se insertaron filas.");
                }

                // Recuperar el ID de la venta generada
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idVenta = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("La creación de la venta falló, no se pudo obtener el ID.");
                    }
                }
            }
        } catch (SQLException e) {
            // Propagar la excepción para manejarla en un nivel superior
            throw e;
        } finally {
            // Cerrar la conexión
            if (connection != null) {
                connection.close();
            }
        }
        return idVenta; // Devolver el ID de la venta
    }               
    
    public void limpiarCampos() {
        nitCiTextField.setText("");
        nombreTextField.setText("");
        // Limpiar la tabla
        DefaultTableModel model = (DefaultTableModel) productosTable.getModel();
        model.setRowCount(0);
        // Reiniciar el contador del total y gran total
        granTotal = 0.0;
        // Reiniciar el total mostrado en el panel de total
        totalTextArea.setText("Bs. 0.00");
    }

    private void reiniciarGranTotal() {
        // Suponiendo que granTotal es una variable de instancia
        granTotal = 0;
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