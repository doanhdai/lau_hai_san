package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.report.BestSellingDish;
import com.example.backend_quanlynhahanglau.dto.report.DashboardStats;
import com.example.backend_quanlynhahanglau.dto.report.RevenueReport;
import com.example.backend_quanlynhahanglau.entity.Dish;
import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.entity.OrderDetail;
import com.example.backend_quanlynhahanglau.enums.DishStatus;
import com.example.backend_quanlynhahanglau.enums.OrderStatus;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Apache POI for Excel
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// iText for PDF
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository tableRepository;
    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;
    private final CustomerFeedbackRepository feedbackRepository;
    private final PaymentRepository paymentRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        // Customer stats
        Long totalCustomers = customerRepository.count();
        Long vipCustomers = (long) customerRepository.findByIsVipTrue().size();

        // Table stats
        Long totalTables = tableRepository.count();
        Long availableTables = (long) tableRepository.findByStatus(TableStatus.AVAILABLE).size();
        Long occupiedTables = (long) tableRepository.findByStatus(TableStatus.OCCUPIED).size();

        // Dish stats
        Long totalDishes = dishRepository.count();
        Long availableDishes = (long) dishRepository.findByStatus(DishStatus.AVAILABLE).size();

        // Order stats
        Long totalOrders = orderRepository.count();
        Long pendingOrders = (long) orderRepository.findByStatus(OrderStatus.PENDING).size();
        Long completedOrders = (long) orderRepository.findByStatus(OrderStatus.COMPLETED).size();

        // Revenue stats - Lấy từ bảng payments (chỉ tính payments có status COMPLETED)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        BigDecimal todayRevenue = paymentRepository.calculateRevenueByDate(todayStart, todayEnd);
        if (todayRevenue == null) todayRevenue = BigDecimal.ZERO;

        LocalDateTime monthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime monthEnd = YearMonth.now().atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal monthRevenue = paymentRepository.calculateRevenueByDate(monthStart, monthEnd);
        if (monthRevenue == null) monthRevenue = BigDecimal.ZERO;

        LocalDateTime yearStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime yearEnd = LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear()).atTime(LocalTime.MAX);
        BigDecimal yearRevenue = paymentRepository.calculateRevenueByDate(yearStart, yearEnd);
        if (yearRevenue == null) yearRevenue = BigDecimal.ZERO;

        // Table status count
        Map<String, Long> tableStatusCount = new HashMap<>();
        List<Object[]> tableStats = tableRepository.countByStatus();
        for (Object[] stat : tableStats) {
            tableStatusCount.put(stat[0].toString(), ((Number) stat[1]).longValue());
        }

        // Order status count
        Map<String, Long> orderStatusCount = new HashMap<>();
        for (OrderStatus status : OrderStatus.values()) {
            Long count = (long) orderRepository.findByStatus(status).size();
            orderStatusCount.put(status.name(), count);
        }

        // Average rating
        Double averageRating = feedbackRepository.calculateAverageRating();
        if (averageRating == null) averageRating = 0.0;

        return DashboardStats.builder()
                .totalCustomers(totalCustomers)
                .vipCustomers(vipCustomers)
                .totalTables(totalTables)
                .availableTables(availableTables)
                .occupiedTables(occupiedTables)
                .totalDishes(totalDishes)
                .availableDishes(availableDishes)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .todayRevenue(todayRevenue)
                .monthRevenue(monthRevenue)
                .yearRevenue(yearRevenue)
                .tableStatusCount(tableStatusCount)
                .orderStatusCount(orderStatusCount)
                .averageRating(averageRating)
                .build();
    }

    @Transactional(readOnly = true)
    public RevenueReport getRevenueReport(LocalDateTime startDate, LocalDateTime endDate) {
        // Tính doanh thu từ bảng payments (chỉ tính payments có status COMPLETED)
        BigDecimal totalRevenue = paymentRepository.calculateRevenueByDate(startDate, endDate);
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        // Lấy danh sách orders trong khoảng thời gian để tính các thống kê khác
        List<Order> orders = orderRepository.findByDate(startDate, endDate).stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED || 
                           o.getStatus() == OrderStatus.PREPARING ||
                           o.getStatus() == OrderStatus.SERVED ||
                           o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());

        Long totalOrders = (long) orders.size();

        BigDecimal averageOrderValue = totalOrders > 0 
                ? totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal totalDiscount = orders.stream()
                .map(Order::getDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = orders.stream()
                .map(Order::getTax)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return RevenueReport.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .averageOrderValue(averageOrderValue)
                .totalDiscount(totalDiscount)
                .totalTax(totalTax)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BestSellingDish> getBestSellingDishes(Integer limit) {
        // Lấy tất cả đơn đã xác nhận trở đi
        List<Order> completedOrders = orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == OrderStatus.CONFIRMED || 
                           o.getStatus() == OrderStatus.PREPARING ||
                           o.getStatus() == OrderStatus.SERVED ||
                           o.getStatus() == OrderStatus.COMPLETED)
                .collect(Collectors.toList());
        
        Map<Long, BestSellingDishData> dishDataMap = new HashMap<>();

        for (Order order : completedOrders) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Dish dish = detail.getDish();
                Long dishId = dish.getId();
                
                BestSellingDishData data = dishDataMap.getOrDefault(dishId, 
                    new BestSellingDishData(
                        dishId,
                        dish.getName(),
                        dish.getCategory().getName(),
                        0L,
                        dish.getPrice(),
                        BigDecimal.ZERO
                    ));
                
                data.totalQuantitySold += detail.getQuantity();
                data.totalRevenue = data.totalRevenue.add(detail.getSubtotal());
                
                dishDataMap.put(dishId, data);
            }
        }

        return dishDataMap.values().stream()
                .sorted((a, b) -> Long.compare(b.totalQuantitySold, a.totalQuantitySold))
                .limit(limit != null ? limit : 10)
                .map(data -> BestSellingDish.builder()
                        .dishId(data.dishId)
                        .dishName(data.dishName)
                        .categoryName(data.categoryName)
                        .totalQuantitySold(data.totalQuantitySold)
                        .price(data.price)
                        .totalRevenue(data.totalRevenue)
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Integer, BigDecimal> getMonthlyRevenue(Integer year) {
        List<Object[]> monthlyData = paymentRepository.calculateMonthlyRevenue(
                year, com.example.backend_quanlynhahanglau.enums.PaymentStatus.COMPLETED.name());
        Map<Integer, BigDecimal> result = new HashMap<>();
        
        // Initialize all months with 0
        for (int i = 1; i <= 12; i++) {
            result.put(i, BigDecimal.ZERO);
        }
        
        // Fill in actual data
        for (Object[] data : monthlyData) {
            Integer month = ((Number) data[0]).intValue(); // Handle both Integer and Number
            BigDecimal revenue = (BigDecimal) data[1];
            result.put(month, revenue != null ? revenue : BigDecimal.ZERO);
        }
        
        return result;
    }

    // Helper class for aggregating dish data
    private static class BestSellingDishData {
        Long dishId;
        String dishName;
        String categoryName;
        Long totalQuantitySold;
        BigDecimal price;
        BigDecimal totalRevenue;

        BestSellingDishData(Long dishId, String dishName, String categoryName, 
                           Long totalQuantitySold, BigDecimal price, BigDecimal totalRevenue) {
            this.dishId = dishId;
            this.dishName = dishName;
            this.categoryName = categoryName;
            this.totalQuantitySold = totalQuantitySold;
            this.price = price;
            this.totalRevenue = totalRevenue;
        }
    }

    // ==================== EXPORT METHODS ====================

    /**
     * Xuất báo cáo doanh thu ra file Excel
     */
    public byte[] exportRevenueToExcel(RevenueReport report) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Báo cáo Doanh thu");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("BÁO CÁO DOANH THU NHÀ HÀNG LẨU");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));

            rowNum++;

            // Date range
            Row dateRow = sheet.createRow(rowNum++);
            dateRow.createCell(0).setCellValue("Thời gian:");
            dateRow.createCell(1).setCellValue(report.getStartDate().format(DATE_FORMATTER) + " - " + 
                                                report.getEndDate().format(DATE_FORMATTER));

            rowNum++;

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"Chỉ tiêu", "Giá trị"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            addDataRow(sheet, rowNum++, "Tổng doanh thu", report.getTotalRevenue(), dataStyle, currencyStyle);
            addDataRow(sheet, rowNum++, "Số đơn hàng", report.getTotalOrders(), dataStyle, dataStyle);
            addDataRow(sheet, rowNum++, "Giá trị TB/đơn", report.getAverageOrderValue(), dataStyle, currencyStyle);
            addDataRow(sheet, rowNum++, "Tổng giảm giá", report.getTotalDiscount(), dataStyle, currencyStyle);
            addDataRow(sheet, rowNum++, "Tổng thuế", report.getTotalTax(), dataStyle, currencyStyle);

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Xuất danh sách món bán chạy ra Excel
     */
    public byte[] exportBestSellingDishesToExcel(List<BestSellingDish> dishes) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Món bán chạy");
            
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("TOP MÓN ĂN BÁN CHẠY");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            rowNum += 2;

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {"#", "Tên món", "Danh mục", "Số lượng bán", "Đơn giá", "Doanh thu"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rank = 1;
            for (BestSellingDish dish : dishes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rank++);
                row.createCell(1).setCellValue(dish.getDishName());
                row.createCell(2).setCellValue(dish.getCategoryName());
                row.createCell(3).setCellValue(dish.getTotalQuantitySold());
                
                Cell priceCell = row.createCell(4);
                priceCell.setCellValue(dish.getPrice().doubleValue());
                priceCell.setCellStyle(currencyStyle);
                
                Cell revenueCell = row.createCell(5);
                revenueCell.setCellValue(dish.getTotalRevenue().doubleValue());
                revenueCell.setCellStyle(currencyStyle);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 1000);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Xuất báo cáo doanh thu ra PDF
     */
    public byte[] exportRevenueToPdf(RevenueReport report) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        Paragraph title = new Paragraph("BAO CAO DOANH THU NHA HANG LAU")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Date range
        Paragraph dateRange = new Paragraph("Thoi gian: " + 
                report.getStartDate().format(DATE_FORMATTER) + " - " + 
                report.getEndDate().format(DATE_FORMATTER))
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(dateRange);

        // Create table
        float[] columnWidths = {3, 3};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Header
        addPdfHeaderCell(table, "Chi tieu");
        addPdfHeaderCell(table, "Gia tri");

        // Data
        addPdfDataRow(table, "Tong doanh thu", formatCurrency(report.getTotalRevenue()));
        addPdfDataRow(table, "So don hang", report.getTotalOrders().toString());
        addPdfDataRow(table, "Gia tri TB/don", formatCurrency(report.getAverageOrderValue()));
        addPdfDataRow(table, "Tong giam gia", formatCurrency(report.getTotalDiscount()));
        addPdfDataRow(table, "Tong thue", formatCurrency(report.getTotalTax()));

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("\nBao cao tao luc: " + 
                LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(30);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    /**
     * Xuất danh sách món bán chạy ra PDF
     */
    public byte[] exportBestSellingDishesToPdf(List<BestSellingDish> dishes) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Title
        Paragraph title = new Paragraph("TOP MON AN BAN CHAY")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30);
        document.add(title);

        // Create table
        float[] columnWidths = {1, 3, 2, 2, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        // Headers
        addPdfHeaderCell(table, "#");
        addPdfHeaderCell(table, "Ten mon");
        addPdfHeaderCell(table, "Danh muc");
        addPdfHeaderCell(table, "So luong");
        addPdfHeaderCell(table, "Don gia");
        addPdfHeaderCell(table, "Doanh thu");

        // Data
        int rank = 1;
        for (BestSellingDish dish : dishes) {
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(String.valueOf(rank++))));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dish.getDishName())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dish.getCategoryName())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(dish.getTotalQuantitySold().toString())));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(dish.getPrice()))));
            table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(formatCurrency(dish.getTotalRevenue()))));
        }

        document.add(table);

        // Footer
        Paragraph footer = new Paragraph("\nBao cao tao luc: " + 
                LocalDateTime.now().format(DATE_FORMATTER))
                .setFontSize(10)
                .setItalic()
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(30);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    // ==================== HELPER METHODS ====================

    // Excel helpers
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(IndexedColors.DARK_RED.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0 \"VND\""));
        return style;
    }

    private void addDataRow(Sheet sheet, int rowNum, String label, Object value, 
                           CellStyle labelStyle, CellStyle valueStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(labelStyle);
        
        Cell valueCell = row.createCell(1);
        if (value instanceof BigDecimal) {
            valueCell.setCellValue(((BigDecimal) value).doubleValue());
        } else if (value instanceof Long) {
            valueCell.setCellValue(((Long) value).doubleValue());
        } else {
            valueCell.setCellValue(value.toString());
        }
        valueCell.setCellStyle(valueStyle);
    }

    // PDF helpers
    private void addPdfHeaderCell(Table table, String text) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(new DeviceRgb(220, 38, 38))
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(8);
        table.addHeaderCell(cell);
    }

    private void addPdfDataRow(Table table, String label, String value) {
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(label)).setPadding(5));
        table.addCell(new com.itextpdf.layout.element.Cell().add(new Paragraph(value)).setPadding(5));
    }

    private String formatCurrency(BigDecimal amount) {
        return CURRENCY_FORMAT.format(amount);
    }
}
