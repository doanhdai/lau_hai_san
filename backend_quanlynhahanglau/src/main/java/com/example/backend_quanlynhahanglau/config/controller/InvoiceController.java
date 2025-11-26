package com.example.backend_quanlynhahanglau.config.controller;

import com.example.backend_quanlynhahanglau.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class InvoiceController {
    private final InvoiceService invoiceService;
    
    @GetMapping("/{orderId}/html")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<String> getInvoiceHtml(@PathVariable Long orderId) {
        String html = invoiceService.generateInvoiceHtml(
            new com.example.backend_quanlynhahanglau.entity.Order()
        );
        
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }
    
    @GetMapping("/{orderId}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'STAFF')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long orderId) throws IOException {
        byte[] pdfBytes = invoiceService.generateInvoicePdf(orderId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "invoice-" + orderId + ".html");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
