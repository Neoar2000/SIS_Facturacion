import javax.swing.*;
import java.awt.*;
import javax.swing.border.EmptyBorder;

public class MetodoPago extends JFrame {
    private FacturacionInterfaz facturacionInterfaz;
    private JLabel headerLabel;
    private JButton efectivoButton;
    private JButton qrButton;
    private JButton tarjetaButton;
    private MetodoPagoListener listener;

    public MetodoPago(FacturacionInterfaz facturacionInterfaz) {
        this.facturacionInterfaz = facturacionInterfaz;
        
        setTitle("Seleccionar Método de Pago");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        headerLabel = new JLabel("Método de Pago", SwingConstants.CENTER); // Alinear el texto al centro
        Font headerFont = headerLabel.getFont();
        headerLabel.setFont(new Font(headerFont.getName(), Font.BOLD, 24));

        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        efectivoButton = new JButton("Efectivo");
        qrButton = new JButton("QR");
        tarjetaButton = new JButton("Tarjeta");
        efectivoButton.setFont(buttonFont);
        qrButton.setFont(buttonFont);
        tarjetaButton.setFont(buttonFont);

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 10, 10)); // Agregamos espacios de 10px entre los botones
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Agregamos un borde vacío con 10px de espacio
        buttonPanel.add(efectivoButton);
        buttonPanel.add(qrButton);
        buttonPanel.add(tarjetaButton);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(headerLabel, BorderLayout.NORTH);
        panel.add(buttonPanel, BorderLayout.CENTER); // Movemos el panel de botones al centro
        add(panel);
        
        efectivoButton.addActionListener(e -> seleccionarMetodoPago("Efectivo"));
        qrButton.addActionListener(e -> seleccionarMetodoPago("QR"));
        tarjetaButton.addActionListener(e -> seleccionarMetodoPago("Tarjeta"));
    }

    private void seleccionarMetodoPago(String metodoPago) {
        if (listener != null) {
            listener.metodoPagoConfirmado(metodoPago);
        }
        dispose();
    }    

    public void setFacturacionInterfaz(FacturacionInterfaz facturacionInterfaz) {
        this.facturacionInterfaz = facturacionInterfaz;
    }

    public FacturacionInterfaz getFacturacionInterfaz() {
        return facturacionInterfaz;
    }

    public void setMetodoPagoListener(MetodoPagoListener listener) {
        this.listener = listener;
    }
}
