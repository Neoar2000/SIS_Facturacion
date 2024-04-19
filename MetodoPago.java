import javax.swing.*;

public class MetodoPago extends JFrame {
    private FacturacionInterfaz facturacionInterfaz; // Agregar referencia a FacturacionInterfaz
    private JLabel headerLabel;
    private JButton efectivoButton;
    private JButton qrButton;
    private JButton tarjetaButton;
    private MetodoPagoListener listener; // Campo para almacenar el listener

    public MetodoPago(FacturacionInterfaz facturacionInterfaz) {
        this.facturacionInterfaz = facturacionInterfaz; // Almacenar referencia a FacturacionInterfaz
        
        // Configurar la ventana
        setTitle("Seleccionar Método de Pago");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializar componentes
        headerLabel = new JLabel("Método de Pago");
        efectivoButton = new JButton("Efectivo");
        qrButton = new JButton("QR");
        tarjetaButton = new JButton("Tarjeta");

        // Agregar componentes al panel
        JPanel panel = new JPanel();
        panel.add(headerLabel);
        panel.add(efectivoButton);
        panel.add(qrButton);
        panel.add(tarjetaButton);
        add(panel);

        // Establecer listeners para los botones de método de pago
        efectivoButton.addActionListener(e -> seleccionarMetodoPago("Efectivo"));
        qrButton.addActionListener(e -> seleccionarMetodoPago("QR"));
        tarjetaButton.addActionListener(e -> seleccionarMetodoPago("Tarjeta"));
    }

    private void seleccionarMetodoPago(String metodoPago) {
        // Llama al método metodoPagoConfirmado del listener cuando se selecciona un método de pago
        if (listener != null) {
            listener.metodoPagoConfirmado(metodoPago);
        }
        // Cerrar la ventana de Método de Pago
        dispose();
    }    

    // Métodos para establecer y obtener facturacionInterfaz
    public void setFacturacionInterfaz(FacturacionInterfaz facturacionInterfaz) {
        this.facturacionInterfaz = facturacionInterfaz;
    }

    public FacturacionInterfaz getFacturacionInterfaz() {
        return facturacionInterfaz;
    }

    // Método para establecer el MetodoPagoListener
    public void setMetodoPagoListener(MetodoPagoListener listener) {
        this.listener = listener;
    }
}
