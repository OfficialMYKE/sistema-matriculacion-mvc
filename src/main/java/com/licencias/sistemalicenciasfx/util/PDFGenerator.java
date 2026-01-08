package com.licencias.sistemalicenciasfx.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.licencias.sistemalicenciasfx.model.entities.Solicitante;

import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PDFGenerator {

    // --- PALETA DE COLORES OFICIAL ---
    private static final BaseColor COLOR_AZUL_FONDO = new BaseColor(20, 45, 110); // Azul Institucional
    private static final BaseColor COLOR_ROJO = new BaseColor(200, 0, 0);
    private static final BaseColor COLOR_GRIS_CLARO = new BaseColor(252, 252, 252); // Casi blanco
    private static final BaseColor COLOR_TEXTO_MAIN = BaseColor.BLACK;
    private static final BaseColor COLOR_LABEL = new BaseColor(60, 60, 60);

    // --- TIPOGRAFÍAS ---
    private static final Font FONT_HEADER = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.WHITE);
    private static final Font FONT_HEADER_SUB = new Font(Font.FontFamily.HELVETICA, 7, Font.NORMAL, new BaseColor(220, 220, 220));
    private static final Font FONT_PAIS = new Font(Font.FontFamily.TIMES_ROMAN, 12, Font.BOLD, BaseColor.BLACK);
    private static final Font FONT_LABEL_NUM = new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD, COLOR_LABEL);
    private static final Font FONT_DATA = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_TEXTO_MAIN);
    private static final Font FONT_DATA_SMALL = new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, COLOR_TEXTO_MAIN);
    private static final Font FONT_TIPO_LICENCIA = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, COLOR_TEXTO_MAIN);

    public static boolean generarLicencia(Solicitante solicitante, String rutaDestino) {
        // Dimensiones Fijas: CR-80 escalado (485x306 puntos)
        Rectangle pageSize = new Rectangle(485, 306);

        // Márgenes ajustados para evitar salto de página
        Document document = new Document(pageSize, 12, 12, 10, 8);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(rutaDestino));
            document.open();

            PdfContentByte canvas = writer.getDirectContent();

            // 1. DIBUJAR FONDO ESTILO TARJETA
            dibujarFondo(canvas, pageSize);

            // -----------------------------------------------------
            // 2. ENCABEZADO (Solo Español)
            // -----------------------------------------------------
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);

            PdfPCell cellHeader = new PdfPCell();
            cellHeader.setBackgroundColor(COLOR_AZUL_FONDO);
            cellHeader.setBorder(Rectangle.NO_BORDER);
            cellHeader.setPaddingTop(6);
            cellHeader.setPaddingBottom(8);
            cellHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellHeader.setVerticalAlignment(Element.ALIGN_MIDDLE);

            // Título Principal
            Paragraph pTitle = new Paragraph("LICENCIA DE CONDUCIR", FONT_HEADER);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            cellHeader.addElement(pTitle);

            // Subtítulo Institucional (Español)
            Paragraph pSub = new Paragraph("AGENCIA NACIONAL DE TRÁNSITO", FONT_HEADER_SUB);
            pSub.setAlignment(Element.ALIGN_CENTER);
            pSub.setSpacingBefore(2);
            cellHeader.addElement(pSub);

            headerTable.addCell(cellHeader);
            document.add(headerTable);

            // -----------------------------------------------------
            // 3. CUERPO: FOTO (Izquierda) vs DATOS (Derecha)
            // -----------------------------------------------------
            PdfPTable bodyTable = new PdfPTable(2);
            bodyTable.setWidthPercentage(100);
            bodyTable.setWidths(new float[]{1.15f, 2.3f}); // Relación Foto / Datos
            bodyTable.setSpacingBefore(8);

            // === COLUMNA IZQUIERDA: FOTO ===
            PdfPCell cellFoto = new PdfPCell();
            cellFoto.setBorder(Rectangle.NO_BORDER);
            try {
                if (solicitante.getFotoUrl() != null && !solicitante.getFotoUrl().isEmpty()) {
                    Image img = Image.getInstance(new URL(solicitante.getFotoUrl()));
                    img.scaleToFit(110, 145);
                    img.setBorder(Rectangle.BOX);
                    img.setBorderWidth(0.5f);
                    img.setBorderColor(BaseColor.GRAY);
                    cellFoto.addElement(img);
                } else {
                    PdfPTable placeholder = new PdfPTable(1);
                    PdfPCell pCell = new PdfPCell(new Phrase("\n\nSIN\nFOTO", FONT_LABEL_NUM));
                    pCell.setFixedHeight(140);
                    pCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    pCell.setBorderColor(BaseColor.LIGHT_GRAY);
                    cellFoto.addElement(pCell);
                }
            } catch (Exception e) {
                cellFoto.addElement(new Phrase("NO FOTO", FONT_LABEL_NUM));
            }
            bodyTable.addCell(cellFoto);

            // === COLUMNA DERECHA: DATOS ===
            PdfPCell cellDatos = new PdfPCell();
            cellDatos.setBorder(Rectangle.NO_BORDER);
            cellDatos.setPaddingLeft(10);

            // A. Título República
            Paragraph pRep = new Paragraph("REPÚBLICA DEL ECUADOR", FONT_PAIS);
            pRep.setAlignment(Element.ALIGN_CENTER);
            pRep.setSpacingAfter(8);
            cellDatos.addElement(pRep);

            // B. Contenedor Datos + Tipo Sangre
            PdfPTable topData = new PdfPTable(2);
            topData.setWidthPercentage(100);
            topData.setWidths(new float[]{3.2f, 1f});

            // B1. Datos Personales
            PdfPCell cInfo = new PdfPCell();
            cInfo.setBorder(Rectangle.NO_BORDER);

            agregarCampoNumerado(cInfo, "1. APELLIDOS:", solicitante.getApellidos().toUpperCase());
            agregarCampoNumerado(cInfo, "2. NOMBRES:", solicitante.getNombres().toUpperCase());
            agregarCampoNumerado(cInfo, "3. LUGAR:", "QUITO - ECUADOR"); // Estático por ahora

            topData.addCell(cInfo);

            // B2. Recuadro Sangre (ACTUALIZADO)
            PdfPCell cSangre = new PdfPCell();
            cSangre.setBorder(Rectangle.BOX);
            cSangre.setBorderWidth(0.5f);
            cSangre.setPaddingBottom(5);

            Paragraph lblSangre = new Paragraph("FACTOR RH", FONT_LABEL_NUM);
            lblSangre.setAlignment(Element.ALIGN_CENTER);
            lblSangre.setFont(new Font(Font.FontFamily.HELVETICA, 6, Font.BOLD, BaseColor.GRAY));

            // --- CAMBIO AQUI: Usamos el getter del solicitante ---
            String txtSangre = solicitante.getTipoSangre() != null ? solicitante.getTipoSangre() : "-";
            Paragraph valSangre = new Paragraph(txtSangre, FONT_DATA);
            // -----------------------------------------------------

            valSangre.setAlignment(Element.ALIGN_CENTER);
            valSangre.setSpacingBefore(2);

            cSangre.addElement(lblSangre);
            cSangre.addElement(valSangre);
            topData.addCell(cSangre);

            cellDatos.addElement(topData);

            // C. Fechas (4a, 4b)
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate emision = LocalDate.now();
            LocalDate vence = emision.plusYears(5);

            PdfPTable fechasTbl = new PdfPTable(2);
            fechasTbl.setWidthPercentage(100);
            fechasTbl.setSpacingBefore(6);

            PdfPCell cFecha1 = new PdfPCell(new Phrase("4a. EMISIÓN: " + emision.format(fmt), FONT_DATA_SMALL));
            cFecha1.setBorder(Rectangle.NO_BORDER);

            PdfPCell cFecha2 = new PdfPCell(new Phrase("4b. VENCE: " + vence.format(fmt), FONT_DATA_SMALL));
            cFecha2.setBorder(Rectangle.NO_BORDER);

            fechasTbl.addCell(cFecha1);
            fechasTbl.addCell(cFecha2);
            cellDatos.addElement(fechasTbl);

            // D. Tipo Licencia y Donante (ACTUALIZADO)
            PdfPTable footerInfoTbl = new PdfPTable(2);
            footerInfoTbl.setWidthPercentage(100);
            footerInfoTbl.setSpacingBefore(8);

            // --- CAMBIO AQUI: Lógica de Donante SI/NO ---
            String textoDonante = solicitante.isEsDonante() ? "SI" : "NO";
            PdfPCell cDonante = new PdfPCell(new Phrase("DONANTE: " + textoDonante, FONT_DATA_SMALL));
            // --------------------------------------------

            cDonante.setBorder(Rectangle.NO_BORDER);
            cDonante.setVerticalAlignment(Element.ALIGN_BOTTOM);

            PdfPCell cTipo = new PdfPCell();
            cTipo.setBorder(Rectangle.NO_BORDER);
            cTipo.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph pTipo = new Paragraph();
            pTipo.add(new Chunk("TIPO: ", FONT_LABEL_NUM));
            pTipo.add(new Chunk(solicitante.getTipoLicencia() + " ", FONT_TIPO_LICENCIA));
            cTipo.addElement(pTipo);

            footerInfoTbl.addCell(cDonante);
            footerInfoTbl.addCell(cTipo);

            cellDatos.addElement(footerInfoTbl);
            bodyTable.addCell(cellDatos);
            document.add(bodyTable);

            // -----------------------------------------------------
            // 4. PIE DE PÁGINA (Código de Barras y Cédula)
            // -----------------------------------------------------

            // Texto Cédula Izquierda
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                    new Phrase("C.I. " + solicitante.getCedula(), FONT_DATA),
                    20, 20, 0);

            // Código de Barras Derecha
            Barcode128 barcode = new Barcode128();
            barcode.setCode(solicitante.getCedula());
            barcode.setCodeType(Barcode128.CODE128);
            barcode.setBarHeight(30f);
            barcode.setX(1.1f);
            barcode.setFont(null); // Sin texto abajo

            Image codeImage = barcode.createImageWithBarcode(canvas, null, null);
            // Posición manual: X=250 (mitad derecha), Y=15 (margen inferior)
            codeImage.setAbsolutePosition(280, 15);
            document.add(codeImage);

            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void dibujarFondo(PdfContentByte canvas, Rectangle pageSize) {
        canvas.saveState();
        canvas.setColorFill(COLOR_GRIS_CLARO);
        canvas.rectangle(0, 0, pageSize.getWidth(), pageSize.getHeight());
        canvas.fill();
        canvas.setColorStroke(BaseColor.BLACK);
        canvas.setLineWidth(0.5f);
        canvas.rectangle(4, 4, pageSize.getWidth()-8, pageSize.getHeight()-8);
        canvas.stroke();
        canvas.restoreState();
    }

    private static void agregarCampoNumerado(PdfPCell cell, String label, String valor) {
        Paragraph p = new Paragraph();
        p.setLeading(10);
        p.add(new Chunk(label + " ", FONT_LABEL_NUM));
        p.add(new Chunk(valor, FONT_DATA));
        p.setSpacingAfter(2);
        cell.addElement(p);
    }
}