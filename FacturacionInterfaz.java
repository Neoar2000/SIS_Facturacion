import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.event.ChangeEvent;
import java.util.ArrayList;
import java.util.List;

public class FacturacionInterfaz extends JFrame {

    private DefaultTableModel tableModel;
    private JLabel totalTextArea;
    private JTextArea reciboTextArea;

    // Lista de productos de ejemplo
    private List<Producto> productos = new ArrayList<>();

    public FacturacionInterfaz() {
        setTitle("Sistema Facturación NEO");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear productos de ejemplo
        productos.add(new Producto("Combo Simple", 18.0));
        productos.add(new Producto("Combo Doble", 28.0));
        productos.add(new Producto("Combo Triple", 36.0));

        // Panel principal
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Inicialización de reciboTextArea
        reciboTextArea = new JTextArea();
        reciboTextArea.setEditable(false); // Desactivar edición
        JScrollPane reciboScrollPane = new JScrollPane(reciboTextArea);

        mainPanel.add(reciboScrollPane, BorderLayout.CENTER);

        // Panel de encabezado
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Alineación centrada

        JLabel headerLabel = new JLabel("Sistema Facturación NEO");
        headerLabel.setFont(new Font(headerLabel.getFont().getName(), Font.BOLD, 24)); // Fuente grande y negrita

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
        JTable productosTable = new JTable(tableModel);
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

        JScrollPane tableScrollPane = new JScrollPane(productosTable);

        mainPanel.add(tableScrollPane, BorderLayout.CENTER);

        // Panel de total
        JPanel totalPanel = new JPanel();
        totalPanel.setBorder(BorderFactory.createTitledBorder("Total"));
        totalTextArea = new JLabel();
        totalTextArea.setFont(totalTextArea.getFont().deriveFont(Font.BOLD, 20)); // Fuente negrita, tamaño 20
        totalTextArea.setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto
        totalPanel.add(totalTextArea);

        mainPanel.add(totalPanel, BorderLayout.SOUTH);


        // Panel de botones con BorderLayout
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Panel para los botones de acción (Agregar Producto y Finalizar Compra)
        JPanel actionButtonPanel = new JPanel();
        JButton agregarButton = new JButton("Agregar Producto");
        agregarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarVentanaProductos();
            }
        });

        JButton finalizarButton = new JButton("Finalizar Compra");
        finalizarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostrarVentanaDatosCliente();
            }
        });

        actionButtonPanel.add(agregarButton);
        actionButtonPanel.add(finalizarButton);

        // Botón "Salir del Sistema" en la parte inferior
        JButton salirButton = new JButton("Salir del Sistema");
        salirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Salir completamente del sistema
                System.exit(0);
            }
        });

        buttonPanel.add(actionButtonPanel, BorderLayout.CENTER);
        buttonPanel.add(salirButton, BorderLayout.SOUTH);

        mainPanel.add(buttonPanel, BorderLayout.EAST);

        add(mainPanel);
        setVisible(true);
    }

    private void mostrarVentanaProductos() {
        // Crear una nueva ventana para mostrar los productos
        JFrame productosFrame = new JFrame("Seleccione un Producto");
        productosFrame.setSize(400, 300);
        productosFrame.setLocationRelativeTo(null);
    
        // Crear tabla para mostrar productos
        DefaultTableModel productosTableModel = new DefaultTableModel();
        productosTableModel.addColumn("Producto");
        productosTableModel.addColumn("Precio");
        JTable productosTable = new JTable(productosTableModel);
    
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
    
                    // Pedir al usuario que ingrese la cantidad
                    String cantidadString = JOptionPane.showInputDialog(productosFrame, "Ingrese la cantidad:", "Cantidad", JOptionPane.QUESTION_MESSAGE);
                    try {
                        int cantidad = Integer.parseInt(cantidadString);
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
        JFrame datosClienteFrame = new JFrame("Datos del Cliente");
        datosClienteFrame.setSize(400, 200);
        datosClienteFrame.setLocationRelativeTo(null);

        JPanel datosClientePanel = new JPanel();
        datosClientePanel.setLayout(new GridLayout(2, 2));

        JLabel nitCiLabel = new JLabel("NIT/CI:");
        JTextField nitCiTextField = new JTextField();
        JLabel nombreLabel = new JLabel("Nombre:");
        JTextField nombreTextField = new JTextField();

        datosClientePanel.add(nitCiLabel);
        datosClientePanel.add(nitCiTextField);
        datosClientePanel.add(nombreLabel);
        datosClientePanel.add(nombreTextField);

        JButton confirmarButton = new JButton("Confirmar");
        confirmarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Obtener los datos del cliente
                String nitCi = nitCiTextField.getText();
                String nombre = nombreTextField.getText();

                // Realizar el registro de la compra con los datos del cliente y generar la factura
                registrarCompra(nitCi, nombre);

                // Cerrar la ventana de datos del cliente
                datosClienteFrame.dispose();
            }
        });

        datosClienteFrame.add(datosClientePanel, BorderLayout.CENTER);
        datosClienteFrame.add(confirmarButton, BorderLayout.SOUTH);

        datosClienteFrame.setVisible(true);
    }


    // Método para generar el recibo
    private void registrarCompra(String nitCi, String nombre) {
        // Imprimir los datos del cliente y los productos comprados en el JTextArea del recibo
        StringBuilder sb = new StringBuilder();
        sb.append("\t                  EMPRESA S.A.\n");
        sb.append("----------------------------------------------------------------------------------------------\n");
        sb.append("Datos del Cliente:\n");
        sb.append(String.format("%-5s: %s\n", "NIT/CI", nitCi));
        sb.append(String.format("%5s: %s\n", "Nombre", nombre));
        sb.append("----------------------------------------------------------------------------------------------\n");
        sb.append("Productos Comprados:\n");

        // Encabezados de las columnas
        sb.append(String.format("%-20s %-20s %-10s %-10s\n", "Producto", "Precio Unitario", "Cantidad", "Precio Total"));

        double granTotal = 0; 

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String producto = (String) tableModel.getValueAt(i, 0);
            double precioUnitario = (double) tableModel.getValueAt(i, 1);
            int cantidad = (int) tableModel.getValueAt(i, 2);
            double precioTotal = precioUnitario * cantidad; // Calcular el precio total del producto

            // Sumar el precio total al gran total
            granTotal += precioTotal;
            
            // Datos de cada producto, alineados en las columnas correspondientes
            sb.append(String.format("%-20s Bs. %-19.2f %-10d Bs. %.2f\n", producto, precioUnitario, cantidad, precioTotal));
        }

        // Mostrar el gran total
        sb.append("----------------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-5s Bs. %.2f\n", "Total a pagar:", granTotal));

        // Mostrar la vista previa del recibo
        VistaPreviaRecibo vistaPreviaRecibo = new VistaPreviaRecibo(sb.toString());
        vistaPreviaRecibo.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FacturacionInterfaz();
            }
        });
    }

    // Clase Producto de ejemplo
    private class Producto {
        private String nombre;
        private double precio;

        public Producto(String nombre, double precio) {
            this.nombre = nombre;
            this.precio = precio;
        }

        public String getNombre() {
            return nombre;
        }

        public double getPrecio() {
            return precio;
        }
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
