import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;

public class VistaPreviaRecibo extends JDialog {  // Cambiado de JFrame a JDialog
    private JTextArea reciboTextArea;
    private FacturacionInterfaz facturacionInterfaz;

    public VistaPreviaRecibo(JFrame owner, String recibo, double dummy, FacturacionInterfaz facturacionInterfaz) {
        super(owner, "Factura de Venta", true);  // Se agrega el constructor de JDialog con modalidad
        this.facturacionInterfaz = facturacionInterfaz;
        setSize(450, 800); // Aumentar el tamaño de la ventana
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        reciboTextArea = new JTextArea(recibo);
        reciboTextArea.setEditable(false);
        reciboTextArea.setFont(new Font("Arial", Font.PLAIN, 16)); // Aumentar el tamaño de la fuente

        JButton imprimirButton = new JButton("Imprimir");
        imprimirButton.setFont(new Font("Arial", Font.BOLD, 16)); // Aumentar el tamaño de la fuente del botón
        imprimirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imprimirRecibo();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(imprimirButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(reciboTextArea), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        
        // Agregar un WindowListener para detectar el cierre de la ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Volver a la ventana FacturacionInterfaz al cerrar la ventana de vista previa
                volverAFacturacionInterfaz();
            }
        });
    }

    private void imprimirRecibo() {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        PageFormat pageFormat = printerJob.defaultPage();
    
        // Configuración del papel y márgenes
        Paper paper = new Paper();
        double margin = 2; // Margen pequeño para maximizar el área imprimible
        double paperWidth = fromMMToPPI(80); // Ancho del papel en puntos
        double paperHeight = fromMMToPPI(297); // Altura suficiente para el contenido
        paper.setSize(paperWidth, paperHeight);
        paper.setImageableArea(margin, 0, paperWidth - 2 * margin, paperHeight);
        pageFormat.setPaper(paper);
    
        printerJob.setPrintable(new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    
                Font plainFont = new Font("Arial", Font.PLAIN, 8);
                Font boldFont = new Font("Arial", Font.BOLD, 8);
    
                int y = 10;
                String[] lines = reciboTextArea.getText().split("\n");
                for (String line : lines) {
                    int x = 0; // Reset x position for each line
                    String[] parts = line.split("(?<=FACTURA|CON DERECHO A CREDITO FISCAL|Ley N° 453:|Producto|P. Unitario|Cantidad|Total|Fecha|NIT/CI:|Nombre:|NIT:|Cod. Autorizacion:|N° Factura:)");
                    for (String part : parts) {
                        if (part.contains("FACTURA") || part.contains("CON DERECHO A CREDITO FISCAL") || part.contains("Ley N° 453:") || part.contains("Producto") || part.contains("P. Unitario") || part.contains("Cantidad") || part.contains("Total") || part.contains("Fecha") || part.contains("NIT/CI:") || part.contains("Nombre:") || part.contains("NIT:") || part.contains("Cod. Autorizacion:") || part.contains("N° Factura:")) {
                            g2d.setFont(boldFont); // Set font to bold for keywords
                        } else {
                            g2d.setFont(plainFont); // Set font to plain for other text
                        }
                        g2d.drawString(part, x, y);
                        x += g2d.getFontMetrics().stringWidth(part); // Update x position after drawing
                    }
                    y += g2d.getFontMetrics().getHeight(); // Move to the next line
                }
                return PAGE_EXISTS;
            }
        }, pageFormat);
    
        // Mostrar el diálogo de impresión
        if (printerJob.printDialog()) {
            try {
                // Imprimir el recibo
                printerJob.print();
            } catch (PrinterException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al imprimir el recibo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        dispose();
    }

    private double fromMMToPPI(double mm) {
        return mm * 2.83465; // 1 mm = 2.83465 puntos
    }

    private void volverAFacturacionInterfaz() {
        // Limpiar los campos y la tabla de la instancia actual de FacturacionInterfaz
        facturacionInterfaz.limpiarCampos();
        dispose();
    }          
    
}
