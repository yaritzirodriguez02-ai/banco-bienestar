package com.yrs.bancobienestar.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.Color;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.yrs.bancobienestar.Modelo.MovimientosEntity;
import com.yrs.bancobienestar.Modelo.SolicitudCreditoEntity;
import com.yrs.bancobienestar.Modelo.UsuarioEntity;

@Service
public class PdfService {

    // 1. PDF Historial de Movimientos (Cliente)
    public ByteArrayInputStream generarEstadoCuentaPdf(UsuarioEntity cliente, List<MovimientosEntity> movimientos) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Encabezado
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(34, 21, 51));
            Paragraph titulo = new Paragraph("BANCO BIENESTAR - ESTADO DE CUENTA", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph(" ")); // Espacio

            // Datos del Cliente
            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            document.add(new Paragraph("Cliente: " + cliente.getNombre(), subTitleFont));
            document.add(new Paragraph("Usuario: " + cliente.getUserName(), subTitleFont));
            if (cliente.getTelefono() != null) document.add(new Paragraph("Teléfono: " + cliente.getTelefono()));
            if (cliente.getDomicilio() != null) document.add(new Paragraph("Domicilio: " + cliente.getDomicilio()));
            document.add(new Paragraph(" "));

            // Tabla de Movimientos
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 2, 3, 2});

            // Headers
            String[] headers = {"ID", "Fecha", "Tipo", "Descripción", "Monto"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                cell.setBackgroundColor(new Color(34, 21, 51));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            for (MovimientosEntity mov : movimientos) {
                table.addCell(mov.getId().toString());
                table.addCell(mov.getFecha() != null ? mov.getFecha().toString().substring(0, 10) : "-");
                table.addCell(mov.getTipo());
                table.addCell(mov.getDescripcion());
                table.addCell("$" + String.format("%.2f", mov.getMonto()));
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // 2. PDF Voucher / Oficio de Solicitud de Crédito (Con Firma y Datos)
    public ByteArrayInputStream generarVoucherCreditoPdf(SolicitudCreditoEntity credito) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Encabezado
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(34, 21, 51));
            Paragraph titulo = new Paragraph("BANCO BIENESTAR\nOFICIO DE CRÉDITO BANCARIO", titleFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("\n===========================================================\n"));

            // Cuerpo del Voucher
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            document.add(new Paragraph("Folio de Solicitud: #000" + credito.getId(), bodyFont));
            document.add(new Paragraph("Fecha de Solicitud: " + (credito.getFecha() != null ? credito.getFecha().toString() : "N/A"), bodyFont));
            document.add(new Paragraph("Cliente Acreditado: " + credito.getUsuario().getNombre(), bodyFont));
            document.add(new Paragraph("Nombre de Usuario: " + credito.getUsuario().getUserName(), bodyFont));
            document.add(new Paragraph("Monto Solicitado: $" + String.format("%.2f", credito.getMontoSolicitado()), bodyFont));
            document.add(new Paragraph("Estado Actual: " + credito.getEstado(), bodyFont));
            document.add(new Paragraph("\nPor medio del presente documento se valida la solicitud formal del crédito bancario bajo las cláusulas del Banco Bienestar.\n\n"));

            // Firma Digital
            if (credito.getFirmaBase64() != null && credito.getFirmaBase64().contains(",")) {
                document.add(new Paragraph("FIRMA DIGITAL DEL CLIENTE:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                String base64Image = credito.getFirmaBase64().split(",")[1];
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
                Image firmaImg = Image.getInstance(imageBytes);
                firmaImg.scaleToFit(180, 80);
                firmaImg.setAlignment(Element.ALIGN_CENTER);
                document.add(firmaImg);
            }

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    // 3. PDF Reporte Consolidado para el Ejecutivo
    public ByteArrayInputStream generarReporteEjecutivoPdf(List<UsuarioEntity> clientes) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Paragraph titulo = new Paragraph("REPORTE EJECUTIVO DE CARTERA BANCARIA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(34, 21, 51)));
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            
            String[] headers = {"ID", "Cliente", "Usuario", "Saldo Disponible"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                cell.setBackgroundColor(new Color(34, 21, 51));
                table.addCell(cell);
            }

            for (UsuarioEntity u : clientes) {
                table.addCell(u.getId().toString());
                table.addCell(u.getNombre());
                table.addCell(u.getUserName());
                Double saldo = (u.getCuentas() != null && !u.getCuentas().isEmpty()) ? u.getCuentas().get(0).getSaldo() : 0.0;
                table.addCell("$" + String.format("%.2f", saldo));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
