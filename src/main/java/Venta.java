import java.time.LocalDateTime;
import java.util.List;

public class Venta {
    private String nitCi;
    private String nombreCliente;
    private LocalDateTime fechaHora;
    private List<Producto> productos;
    private double granTotal; // Nuevo campo para almacenar el gran total

    public Venta(String nitCi, String nombreCliente, LocalDateTime fechaHora, List<Producto> productos, double granTotal) {
        this.nitCi = nitCi;
        this.nombreCliente = nombreCliente;
        this.fechaHora = fechaHora;
        this.productos = productos;
        this.granTotal = granTotal; // Establecer el gran total
    }

    public double getGranTotal() {
        return granTotal;
    }

    // Método para establecer el gran total después de la creación de la instancia
    public void setGranTotal(double granTotal) {
        this.granTotal = granTotal;
    }

    public String getNitCi() {
        return nitCi;
    }

    public void setNitCi(String nitCi) {
        this.nitCi = nitCi;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }
}