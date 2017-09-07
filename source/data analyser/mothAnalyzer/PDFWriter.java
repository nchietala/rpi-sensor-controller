/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mothAnalyzer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceCharacteristicsDictionary;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;

/**
 *
 * @author Nick
 */
public abstract class PDFWriter {

    /**
     * Creates a PDF from the provided text and nodes
     *
     * @param chart
     * @param hist
     * @param dir
     * @param noteText
     * @param stats
     */
    public static void export(Node prog, Node chart, Node hist, String dir, String noteText, String[] stats) {
        PDDocument doc = null;
        PDPage page, page2;
        PDPageContentStream content = null;
        PDImageXObject ximage;

        try {

            //snapshot the nodes
            Image fxProg = prog.snapshot(null, null);
            Image fxChart = chart.snapshot(null, null);
            Image fxHist = hist.snapshot(null, null);
            BufferedImage bufferedProg = SwingFXUtils.fromFXImage(fxProg, null);
            BufferedImage bufferedChart = SwingFXUtils.fromFXImage(fxChart, null);
            BufferedImage bufferedHist = SwingFXUtils.fromFXImage(fxHist, null);

            // create a pdf
            doc = new PDDocument();
            page = new PDPage(new PDRectangle(1100, 850));
            doc.addPage(page);

            //add form to the document
            PDAcroForm form = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(form);

            content = new PDPageContentStream(doc, page);

            // alternate path A => try to create a PDJpeg from a jpegInputStream.
            ximage = JPEGFactory.createFromImage(doc, bufferedChart);
            content.drawImage(ximage, 0, 0);
            ximage = JPEGFactory.createFromImage(doc, bufferedHist);
            content.drawImage(ximage, 575, 700);
            ximage = JPEGFactory.createFromImage(doc, bufferedProg);
            content.drawImage(ximage, 0, 350);

            //JPEGFactory.createFromStream(doc, ImageIO.read(ImageIO.createImageInputStream(hist)));
            //content.
            for (int i = 0; i < stats.length; i++) {
                content.beginText();
                content.setFont(PDType1Font.COURIER, 10);
                content.moveTextPositionByAmount(275, 815 - i * 10);
                content.drawString(stats[i]);
                content.endText();

            }
            
            content.close();

            PDFont font = PDType1Font.HELVETICA;
            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Helv"), font);
            PDAcroForm acroForm = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm);
            acroForm.setDefaultResources(resources);
            String defaultAppearanceString = "/Helv 0 Tf 0 g";
            acroForm.setDefaultAppearance(defaultAppearanceString);
            PDTextField textBox = new PDTextField(acroForm);
            textBox.setPartialName("SampleField");
            defaultAppearanceString = "/Helv 10 Tf 0 0 0 rg";
            textBox.setDefaultAppearance(defaultAppearanceString);
            textBox.setMultiline(true);
            acroForm.getFields().add(textBox);
            PDAnnotationWidget widget = textBox.getWidgets().get(0);
            PDRectangle rect = new PDRectangle(50, 695, 200, 130);
            widget.setRectangle(rect);
            widget.setPage(page);
            PDAppearanceCharacteristicsDictionary fieldAppearance
                    = new PDAppearanceCharacteristicsDictionary(new COSDictionary());
            fieldAppearance.setBackground(
                    new PDColor(
                            new float[]{
                                (float) .9,
                                (float) .9,
                                (float) .9
                            }, PDDeviceRGB.INSTANCE
                    ));
            widget.setAppearanceCharacteristics(fieldAppearance);
            widget.setPrinted(true);
            page.getAnnotations().add(widget);
            textBox.setValue(noteText);

            // save the created image to disk.
            doc.save(dir);

            System.out.println("Exported PDF to: " + dir);
        } catch (IOException ie) {
            System.out.println(ie);
        } finally {
            try {
                if (content != null) {
                    content.close();
                }
                if (doc != null) {
                    doc.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }

    public static void exportAggregate() {

    }
}
