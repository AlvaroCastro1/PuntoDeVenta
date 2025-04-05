package dulceria.app;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.image.Bitonal;
import com.github.anastaciocintra.escpos.image.BitonalThreshold;
import com.github.anastaciocintra.escpos.image.CoffeeImage;
import com.github.anastaciocintra.escpos.image.CoffeeImageImpl;
import com.github.anastaciocintra.escpos.image.EscPosImage;
import com.github.anastaciocintra.escpos.image.RasterBitImageWrapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class ImpresoraTicket {

    public static void main(String[] args) {
        try {
            // Corrección 1: Usar nombre de clase directamente
            File imagenFile = new File(ImpresoraTicket.class.getResource("/dulceria/images/dulce.jpg").toURI());
            BufferedImage imagen = ImageIO.read(imagenFile);

            // 1. Definir dimensiones en píxeles (2cm ≈ 80 píxeles a 203 DPI)
            int width = 80;  
            int height = 80;

            // 2. Redimensionar y aplicar algoritmo
            Bitonal algorithm = new BitonalThreshold();
            CoffeeImage coffeeImage = new CoffeeImageImpl(imagen); // Redimensiona
            EscPosImage escposImage = new EscPosImage(coffeeImage, algorithm);

            // 3. Configurar impresión centrada
            RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
            imageWrapper.setJustification(RasterBitImageWrapper.Justification.Center);

            // Corrección 2: Mejor manejo de recursos con try-with-resources
            try (OutputStream os = new FileOutputStream("/dev/usb/lp0");
                 EscPos escpos = new EscPos(os)) {
                
                escpos.write(imageWrapper, escposImage);

                Style center = new Style().setJustification(Style.Justification.Center);
                escpos.writeLF(center, "Dulcería Teddy");
                escpos.writeLF(center, "Gracias por su compra!");

                escpos.feed(5).cut(EscPos.CutMode.FULL);
            }

        } catch (Exception e) {
            System.err.println("Error al imprimir: ");
            e.printStackTrace();
        }
    }
}