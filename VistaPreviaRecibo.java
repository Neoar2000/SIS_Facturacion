import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
        dispose();
    }

    private void volverAFacturacionInterfaz() {
        FacturacionInterfaz nuevaFacturacionInterfaz = new FacturacionInterfaz();
        nuevaFacturacionInterfaz.setVisible(true);
    }    
    
}
