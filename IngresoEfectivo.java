import javax.swing.*;

public class IngresoEfectivo extends JFrame {
    private JLabel instruccionLabel;
    private JTextField cantidadTextField;
    private JButton aceptarButton;

    private double granTotal;

    public IngresoEfectivo(double granTotal) {
        // Configurar la ventana
        setTitle("Ingreso de Efectivo");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        this.granTotal = granTotal;

        // Inicializar componentes
        instruccionLabel = new JLabel("Ingrese la cantidad en efectivo:");
        cantidadTextField = new JTextField(10);
        aceptarButton = new JButton("Aceptar");

        // Agregar componentes al panel
        JPanel panel = new JPanel();
        panel.add(instruccionLabel);
        panel.add(cantidadTextField);
        panel.add(aceptarButton);
        add(panel);

        // Acción del botón Aceptar
        aceptarButton.addActionListener(e -> {
            verificarCantidadEfectivo();
        });
    }

    private void verificarCantidadEfectivo() {
        try {
            double cantidadIngresada = Double.parseDouble(cantidadTextField.getText());
            if (cantidadIngresada >= granTotal) {
                // La cantidad ingresada es suficiente, abrir la ventana de vista previa del recibo
                VistaPreviaRecibo vistaPreviaRecibo = new VistaPreviaRecibo("Efectivo", cantidadIngresada);
                vistaPreviaRecibo.setVisible(true);
                dispose(); // Cerrar la ventana actual
            } else {
                // La cantidad ingresada es insuficiente, calcular el cambio y mostrarlo en pantalla
                double cambio = granTotal - cantidadIngresada;
                JOptionPane.showMessageDialog(this, String.format("Cantidad insuficiente. Se necesita Bs. %.2f más.", cambio), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}