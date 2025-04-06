package dulceria.app;

import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.image.*;

import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.PrinterName;

import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ImpresoraTicket {

    public static void imprimirTicket(String contenido) throws Exception {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            imprimirEnWindows(contenido);
        } else {
            imprimirEnLinux(contenido);
        }
    }

    private static void imprimirEnLinux(String contenido) throws Exception {
        try (OutputStream os = new FileOutputStream("/dev/usb/lp0");
             EscPos escpos = new EscPos(os)) {

            imprimirContenido(escpos, contenido);
        }
    }

    private static void imprimirEnWindows(String contenido) throws Exception {
        PrintService printService = obtenerImpresoraPorNombre("POS-80"); // Cambia el nombre si es necesario
        if (printService == null) {
            throw new RuntimeException("No se encontró la impresora POS-58");
        }

        try (OutputStream os = new FileOutputStream("ticket.raw")) {
            EscPos escpos = new EscPos(os);
            imprimirContenido(escpos, contenido);
            escpos.close();
        }

        FileInputStream fis = new FileInputStream("ticket.raw");
        Doc doc = new SimpleDoc(fis, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
        DocPrintJob job = printService.createPrintJob();
        job.print(doc, null);
        fis.close();
    }

    private static void imprimirContenido(EscPos escpos, String contenido) throws Exception {
        Style center = new Style().setJustification(Style.Justification.Center);

        imprimirLogo(escpos);

        escpos.writeLF(center, "Dulcería Teddy");
        escpos.writeLF("----------------------------------------------");
        escpos.writeLF(center, "Niños Heroes 5");
        escpos.writeLF(center, "San Pedro de la Laguna");
        escpos.writeLF(center, "55609 Zumpango de Ocampo, Méx.");
        escpos.writeLF(center, "Tel: 555-1234-567");
        escpos.writeLF("----------------------------------------------");

        escpos.writeLF(center, "Fecha: " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        escpos.writeLF(center, "Atendió: " + App.getUsuarioAutenticado().getNombre());
        escpos.writeLF("----------------------------------------------");

        for (String linea : contenido.split("\n")) {
            escpos.writeLF(linea);
        }

        escpos.feed(5).cut(EscPos.CutMode.FULL);
    }

    private static void imprimirLogo(EscPos escpos) {
        try {
            File imagenFile = new File(ImpresoraTicket.class.getResource("/dulceria/images/logo_2x2.jpg").toURI());
            BufferedImage imagen = ImageIO.read(imagenFile);

            Bitonal algorithm = new BitonalThreshold();
            CoffeeImage coffeeImage = new CoffeeImageImpl(imagen);
            EscPosImage escposImage = new EscPosImage(coffeeImage, algorithm);

            RasterBitImageWrapper imageWrapper = new RasterBitImageWrapper();
            imageWrapper.setJustification(RasterBitImageWrapper.Justification.Center);

            escpos.write(imageWrapper, escposImage);
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo, continuando sin él...");
        }
    }

    private static PrintService obtenerImpresoraPorNombre(String nombre) {
        PrintService[] servicios = PrintServiceLookup.lookupPrintServices(null, null);
        for (PrintService servicio : servicios) {
            if (servicio.getName().equalsIgnoreCase(nombre)) {
                return servicio;
            }
        }
        return null;
    }
}
