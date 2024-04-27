import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.FontFactory;
import java.io.File;

public class VistaPreviaRecibo extends JDialog {  // Cambiado de JFrame a JDialog
    private JTextArea reciboTextArea;
    private FacturacionInterfaz facturacionInterfaz;
    private byte[] qrCodeBytes;

    public VistaPreviaRecibo(JFrame owner, String recibo, double dummy, FacturacionInterfaz facturacionInterfaz, byte[] qrCodeBytes) {
        super(owner, "Factura de Venta", true);  // Se agrega el constructor de JDialog con modalidad
        this.facturacionInterfaz = facturacionInterfaz;
        this.qrCodeBytes = qrCodeBytes;
        setSize(450, 850); // Aumentar el tamaño de la ventana
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

        JButton guardarPdfButton = new JButton("Guardar como PDF");
        guardarPdfButton.setFont(new Font("Arial", Font.BOLD, 16));
        guardarPdfButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarComoPDF();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(imprimirButton);
        buttonPanel.add(guardarPdfButton);

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
        
                int y = 10; // Initial y position
                boolean qrDrawn = false;
                String[] lines = reciboTextArea.getText().split("\n");
                for (String line : lines) {
                    // Check if it is the position to insert QR
                    if (line.contains("                             Facturacion NEO v1.0") && !qrDrawn) {
                        // Insert QR before this line
                        if (qrCodeBytes != null) {
                            try {
                                BufferedImage qrImage = ImageIO.read(new ByteArrayInputStream(qrCodeBytes));
                                int qrWidth = qrImage.getWidth();
                                int pageWidth = (int) pageFormat.getImageableWidth();
        
                                // Calculate the x position to center the QR code
                                int qrXPosition = (pageWidth - qrWidth) / 2;
        
                                g2d.drawImage(qrImage, qrXPosition, y, null);
                                y += qrImage.getHeight() + 5; // Update y to draw the next text below the QR
                                qrDrawn = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
        
                    int x = 0; // Reset x position for each line
                    String[] parts = line.split("(?<=FACTURA|CON DERECHO A CREDITO FISCAL|Ley N° 453:|Producto|P. Unitario|Cantidad|Total|Fecha|NIT/CI:|Nombre:|NIT:|Cod. Autorizacion:|N° Factura:|Son:|Metodo de Pago:|Efectivo pagado:|Cambio:)");
                    for (String part : parts) {
                        if (part.matches(".*(FACTURA|CON DERECHO A CREDITO FISCAL|Ley N° 453:|Producto|P. Unitario|Cantidad|Total|Fecha|NIT/CI:|Nombre:|NIT:|Cod. Autorizacion:|N° Factura:|Son:|Metodo de Pago:|Efectivo pagado:|Cambio:).*")) {
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

    private void guardarComoPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar Recibo como PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Documents", "pdf"));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getAbsolutePath().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }
    
            Rectangle pageSize = new Rectangle((float)fromMMToPPI(80), (float)fromMMToPPI(297));
            Document document = new Document(pageSize, 10, 10, 10, 10); // Margenes pequeños
    
            try {
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();
    
                // Define una fuente de tamaño 8 usando la clase completamente calificada para Font
                com.itextpdf.text.Font font = FontFactory.getFont(FontFactory.HELVETICA, 8);
    
                // Agregar el texto del recibo con la fuente ajustada
                Paragraph paragraph = new Paragraph(reciboTextArea.getText(), font);
                document.add(paragraph);
    
                // Agregar el QR si está disponible
                if (qrCodeBytes != null) {
                    Image qrImage = Image.getInstance(qrCodeBytes);
                    qrImage.setAlignment(Image.ALIGN_CENTER);
                    document.add(qrImage);
                }
    
                document.close();
                JOptionPane.showMessageDialog(this, "Recibo guardado como PDF exitosamente.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al guardar el recibo como PDF.", "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
        dispose();
    }

    private void volverAFacturacionInterfaz() {
        // Limpiar los campos y la tabla de la instancia actual de FacturacionInterfaz
        facturacionInterfaz.limpiarCampos();
        dispose();
    }          
    
}
