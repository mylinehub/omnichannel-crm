package com.mylinehub.crm.exports;

import com.mylinehub.crm.service.CurrentTimeInterface;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfWriter;

import javax.servlet.http.HttpServletResponse;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static com.lowagie.text.Element.ALIGN_CENTER;
import static com.lowagie.text.Element.ALIGN_RIGHT;


public interface PDFFileDesignRepository extends CurrentTimeInterface {

    default void setupFileStyle(HttpServletResponse response, Document document) throws IOException {
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        Paragraph date = new Paragraph("Date of data export: " + getCurrentDateTime(), FontFactory.getFont(FontFactory.TIMES_ROMAN));
        date.setAlignment(ALIGN_RIGHT);
        document.add(date);

        //System.out.println(date.toString());
        
        //Resource resource = new ClassPathResource("/resources/crmimage.png");

        File fn = new File("src/main/resources/crmimage.png");
        //System.out.println(fn.getAbsolutePath());
        
       // System.out.println(resource.toString());
        //File file = resource.getFile();
        //String content = new String(Files.readAllBytes(file.toPath()));
        
        com.lowagie.text.Image jpg = com.lowagie.text.Image.getInstance(fn.getAbsolutePath());
        jpg.setAlignment(ALIGN_CENTER);
        jpg.scaleAbsolute(100f, 30.11f);
        document.add(jpg);
    }

    default Font setupFont(){
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        font.setSize(18);
        font.setColor(Color.BLACK);
        return font;
    }
}
