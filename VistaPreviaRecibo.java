import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.print.*;

public class VistaPreviaRecibo extends JFrame {
    private JTextArea reciboTextArea;

    public VistaPreviaRecibo(String recibo) {
        setTitle("Vista Previa del Recibo");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        reciboTextArea = new JTextArea(recibo);
        reciboTextArea.setEditable(false);

        JButton imprimirButton = new JButton("Imprimir");
        imprimirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                imprimirRecibo();
            }
        });

        JButton guardarPDFButton = new JButton("Guardar como PDF");
        guardarPDFButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                guardarComoPDF();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(imprimirButton);
        buttonPanel.add(guardarPDFButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(new JScrollPane(reciboTextArea), BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void imprimirRecibo() {
        // Crear un objeto PrinterJob
        PrinterJob printerJob = PrinterJob.getPrinterJob();
    
        // Crear un Printable que represente el contenido del recibo
        Printable printable = new Printable() {
            @Override
            public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }
    
                // Definir el área imprimible
                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
    
                // Dibujar el contenido del recibo en el área imprimible
                reciboTextArea.printAll(graphics);
    
                return Printable.PAGE_EXISTS;
            }
        };
    
        // Asignar el Printable al PrinterJob
        printerJob.setPrintable(printable);
    
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
    }

    // Método para guardar el recibo como PDF
    private void guardarComoPDF() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar como PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivo PDF", "pdf"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = fileChooser.getSelectedFile().getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) {
                    filePath += ".pdf"; // Agregar la extensión .pdf si no está presente
                }
                FileWriter writer = new FileWriter(filePath);
                writer.write(reciboTextArea.getText());
                writer.close();
                JOptionPane.showMessageDialog(this, "El recibo se ha guardado como PDF correctamente.", "Guardar PDF", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error al guardar el recibo como PDF.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
