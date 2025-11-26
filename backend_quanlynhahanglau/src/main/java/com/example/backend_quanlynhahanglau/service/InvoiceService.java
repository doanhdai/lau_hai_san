package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.entity.OrderDetail;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final OrderRepository orderRepository;
    
    public byte[] generateInvoicePdf(Long orderId) throws IOException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", orderId));
        
        // For simplicity, generating HTML-based invoice that can be converted to PDF
        String html = generateInvoiceHtml(order);
        
        // Convert HTML to PDF bytes (simplified version - in production use iText or similar library)
        return html.getBytes();
    }
    
    public String generateInvoiceHtml(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'><style>");
        html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
        html.append(".invoice-header { text-align: center; margin-bottom: 30px; }");
        html.append(".invoice-details { margin-bottom: 20px; }");
        html.append("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
        html.append("table th, table td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
        html.append("table th { background-color: #f2f2f2; }");
        html.append(".text-right { text-align: right; }");
        html.append(".total-section { margin-top: 20px; text-align: right; }");
        html.append(".total-line { margin: 5px 0; }");
        html.append(".grand-total { font-size: 1.2em; font-weight: bold; margin-top: 10px; }");
        html.append("</style></head><body>");
        
        // Header
        html.append("<div class='invoice-header'>");
        html.append("<h1>HÓA ĐƠN BÁN HÀNG</h1>");
        html.append("<h2>NHÀ HÀNG LẨU</h2>");
        html.append("<p>Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM</p>");
        html.append("<p>Điện thoại: 0123456789</p>");
        html.append("</div>");
        
        // Invoice details
        html.append("<div class='invoice-details'>");
        html.append("<p><strong>Số hóa đơn:</strong> " + order.getOrderNumber() + "</p>");
        html.append("<p><strong>Ngày:</strong> " + order.getCreatedAt().format(formatter) + "</p>");
        
        if (order.getCustomer() != null) {
            html.append("<p><strong>Khách hàng:</strong> " + order.getCustomer().getFullName() + "</p>");
            if (order.getCustomer().getPhone() != null) {
                html.append("<p><strong>Số điện thoại:</strong> " + order.getCustomer().getPhone() + "</p>");
            }
        }
        
        if (order.getTable() != null) {
            html.append("<p><strong>Bàn:</strong> " + order.getTable().getTableNumber() + "</p>");
        }
        
        if (order.getCreatedBy() != null) {
            html.append("<p><strong>Nhân viên:</strong> " + order.getCreatedBy().getFullName() + "</p>");
        }
        html.append("</div>");
        
        // Items table
        html.append("<table>");
        html.append("<thead><tr>");
        html.append("<th>STT</th>");
        html.append("<th>Tên món</th>");
        html.append("<th class='text-right'>Đơn giá</th>");
        html.append("<th class='text-right'>SL</th>");
        html.append("<th class='text-right'>Thành tiền</th>");
        html.append("</tr></thead>");
        html.append("<tbody>");
        
        int index = 1;
        for (OrderDetail detail : order.getOrderDetails()) {
            html.append("<tr>");
            html.append("<td>" + index++ + "</td>");
            html.append("<td>" + detail.getDish().getName() + "</td>");
            html.append("<td class='text-right'>" + formatCurrency(detail.getPrice()) + "</td>");
            html.append("<td class='text-right'>" + detail.getQuantity() + "</td>");
            html.append("<td class='text-right'>" + formatCurrency(detail.getSubtotal()) + "</td>");
            html.append("</tr>");
        }
        
        html.append("</tbody></table>");
        
        // Total section
        html.append("<div class='total-section'>");
        html.append("<div class='total-line'><strong>Tạm tính:</strong> " + formatCurrency(order.getSubtotal()) + "</div>");
        
        if (order.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            html.append("<div class='total-line'><strong>Giảm giá:</strong> -" + formatCurrency(order.getDiscount()) + "</div>");
        }
        
        html.append("<div class='total-line'><strong>Thuế VAT (10%):</strong> " + formatCurrency(order.getTax()) + "</div>");
        html.append("<div class='grand-total'><strong>TỔNG CỘNG:</strong> " + formatCurrency(order.getTotal()) + "</div>");
        html.append("</div>");
        
        // Footer
        html.append("<div style='margin-top: 50px; text-align: center;'>");
        html.append("<p>Cảm ơn quý khách và hẹn gặp lại!</p>");
        html.append("</div>");
        
        html.append("</body></html>");
        
        return html.toString();
    }
    
    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 đ";
        return String.format("%,d đ", amount.longValue());
    }
}
