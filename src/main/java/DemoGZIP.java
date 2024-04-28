import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class DemoGZIP {

    private final byte[] BUFFER = new byte[1024];

    /**
     * @param archivo Archivo a comprimir
     *      El archivo se comprime con el mismo nombre del archivo origen seguido de la extension *.zip
     * @return boolean 
     *      TRUE tuvo exito
     *      FALSE no se pudo comprimir
     */
    public boolean comprimir(File archivo) {                
        try (GZIPOutputStream  out = new GZIPOutputStream (new FileOutputStream(archivo.getAbsolutePath() + ".zip"));
                FileInputStream in = new FileInputStream(archivo)) {
            int len;
            while ((len = in.read(BUFFER)) != -1) {
                out.write(BUFFER, 0, len);
            }
            out.close();
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
            return false;
        }
        return true;
    }
}