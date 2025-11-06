/**
 * Utility to print/export invoice to PDF
 */

export function printInvoice(order) {
  const printWindow = window.open('', '_blank', 'width=800,height=600')
  
  if (!printWindow) {
    alert('Vui lòng cho phép popup để xuất hóa đơn')
    return
  }

  const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(value || 0)
  }

  const formatDate = (dateString) => {
    if (!dateString) return '-'
    const date = new Date(dateString)
    return date.toLocaleString('vi-VN')
  }

  const getStatusLabel = (status) => {
    const labels = {
      'PENDING': 'Chờ xử lý',
      'CONFIRMED': 'Đã xác nhận',
      'PREPARING': 'Đang chuẩn bị',
      'SERVED': 'Đã phục vụ',
      'COMPLETED': 'Hoàn thành',
      'CANCELLED': 'Đã hủy'
    }
    return labels[status] || status
  }

  const htmlContent = `
    <!DOCTYPE html>
    <html lang="vi">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>Hóa đơn - ${order.orderNumber}</title>
      <style>
        * {
          margin: 0;
          padding: 0;
          box-sizing: border-box;
        }
        
        body {
          font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
          padding: 40px;
          max-width: 800px;
          margin: 0 auto;
          background: #fff;
        }
        
        .invoice-header {
          text-align: center;
          margin-bottom: 40px;
          padding-bottom: 20px;
          border-bottom: 3px solid #ef4444;
        }
        
        .restaurant-name {
          font-size: 32px;
          font-weight: bold;
          color: #ef4444;
          margin-bottom: 8px;
        }
        
        .restaurant-info {
          color: #666;
          font-size: 14px;
          line-height: 1.6;
        }
        
        .invoice-title {
          font-size: 28px;
          font-weight: bold;
          text-align: center;
          margin: 30px 0;
          color: #333;
        }
        
        .invoice-info {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 20px;
          margin-bottom: 30px;
        }
        
        .info-block {
          background: #f9fafb;
          padding: 15px;
          border-radius: 8px;
        }
        
        .info-label {
          font-size: 12px;
          color: #666;
          text-transform: uppercase;
          margin-bottom: 5px;
        }
        
        .info-value {
          font-size: 16px;
          font-weight: 600;
          color: #111;
        }
        
        table {
          width: 100%;
          border-collapse: collapse;
          margin: 30px 0;
        }
        
        thead {
          background: #ef4444;
          color: white;
        }
        
        th {
          padding: 12px;
          text-align: left;
          font-weight: 600;
          font-size: 14px;
        }
        
        td {
          padding: 12px;
          border-bottom: 1px solid #e5e7eb;
        }
        
        tbody tr:hover {
          background: #f9fafb;
        }
        
        .text-right {
          text-align: right;
        }
        
        .text-center {
          text-align: center;
        }
        
        .totals {
          margin-top: 30px;
          padding: 20px;
          background: #f9fafb;
          border-radius: 8px;
        }
        
        .total-row {
          display: flex;
          justify-content: space-between;
          padding: 8px 0;
          font-size: 16px;
        }
        
        .total-row.grand-total {
          border-top: 2px solid #ef4444;
          margin-top: 10px;
          padding-top: 15px;
          font-size: 20px;
          font-weight: bold;
          color: #ef4444;
        }
        
        .notes {
          margin-top: 30px;
          padding: 15px;
          background: #fef3c7;
          border-left: 4px solid #f59e0b;
          border-radius: 4px;
        }
        
        .footer {
          margin-top: 50px;
          padding-top: 20px;
          border-top: 2px solid #e5e7eb;
          text-align: center;
          color: #666;
          font-size: 14px;
        }
        
        .signature-section {
          display: grid;
          grid-template-columns: 1fr 1fr;
          gap: 40px;
          margin-top: 50px;
          text-align: center;
        }
        
        .signature-block {
          padding: 20px;
        }
        
        .signature-title {
          font-weight: 600;
          margin-bottom: 60px;
          color: #333;
        }
        
        .signature-line {
          border-top: 1px solid #333;
          padding-top: 10px;
          font-style: italic;
          color: #666;
        }
        
        @media print {
          body {
            padding: 20px;
          }
          
          .no-print {
            display: none;
          }
        }
        
        .print-button {
          position: fixed;
          top: 20px;
          right: 20px;
          background: #ef4444;
          color: white;
          padding: 12px 24px;
          border: none;
          border-radius: 8px;
          font-size: 16px;
          font-weight: 600;
          cursor: pointer;
          box-shadow: 0 4px 6px rgba(0,0,0,0.1);
          transition: all 0.3s;
        }
        
        .print-button:hover {
          background: #dc2626;
          box-shadow: 0 6px 8px rgba(0,0,0,0.15);
        }
      </style>
    </head>
    <body>
      <button class="print-button no-print" onclick="window.print()">🖨️ In hóa đơn</button>
      
      <div class="invoice-header">
        <div class="restaurant-name">NHÀ HÀNG LẨU</div>
        <div class="restaurant-info">
          Địa chỉ: 123 Đường ABC, Quận XYZ, TP.HCM<br>
          Điện thoại: 028 1234 5678 | Email: contact@nhahanglau.com
        </div>
      </div>
      
      <div class="invoice-title">HÓA ĐƠN THANH TOÁN</div>
      
      <div class="invoice-info">
        <div class="info-block">
          <div class="info-label">Số hóa đơn</div>
          <div class="info-value">${order.orderNumber}</div>
        </div>
        <div class="info-block">
          <div class="info-label">Ngày tạo</div>
          <div class="info-value">${formatDate(order.createdAt)}</div>
        </div>
        ${order.customerName ? `
        <div class="info-block">
          <div class="info-label">Khách hàng</div>
          <div class="info-value">${order.customerName}</div>
        </div>
        ` : ''}
        ${order.tableNumber || order.roomName ? `
        <div class="info-block">
          <div class="info-label">Vị trí</div>
          <div class="info-value">${order.tableNumber ? `Bàn ${order.tableNumber}` : `Phòng ${order.roomName}`}</div>
        </div>
        ` : ''}
        <div class="info-block">
          <div class="info-label">Trạng thái</div>
          <div class="info-value">${getStatusLabel(order.status)}</div>
        </div>
        ${order.createdByName ? `
        <div class="info-block">
          <div class="info-label">Người tạo</div>
          <div class="info-value">${order.createdByName}</div>
        </div>
        ` : ''}
      </div>
      
      <table>
        <thead>
          <tr>
            <th>STT</th>
            <th>Tên món</th>
            <th class="text-center">Số lượng</th>
            <th class="text-right">Đơn giá</th>
            <th class="text-right">Thành tiền</th>
          </tr>
        </thead>
        <tbody>
          ${order.items.map((item, index) => `
            <tr>
              <td>${index + 1}</td>
              <td>
                ${item.dishName}
                ${item.notes ? `<br><small style="color: #666; font-style: italic;">${item.notes}</small>` : ''}
              </td>
              <td class="text-center">${item.quantity}</td>
              <td class="text-right">${formatCurrency(item.price)}</td>
              <td class="text-right"><strong>${formatCurrency(item.subtotal)}</strong></td>
            </tr>
          `).join('')}
        </tbody>
      </table>
      
      <div class="totals">
        <div class="total-row">
          <span>Tạm tính:</span>
          <span>${formatCurrency(order.subtotal)}</span>
        </div>
        <div class="total-row">
          <span>Thuế VAT (10%):</span>
          <span>${formatCurrency(order.tax)}</span>
        </div>
        ${order.discount > 0 ? `
        <div class="total-row" style="color: #10b981;">
          <span>Giảm giá:</span>
          <span>-${formatCurrency(order.discount)}</span>
        </div>
        ` : ''}
        <div class="total-row grand-total">
          <span>TỔNG CỘNG:</span>
          <span>${formatCurrency(order.total)}</span>
        </div>
      </div>
      
      ${order.notes ? `
      <div class="notes">
        <strong>Ghi chú:</strong> ${order.notes}
      </div>
      ` : ''}
      
      <div class="signature-section">
        <div class="signature-block">
          <div class="signature-title">Khách hàng</div>
          <div class="signature-line">(Ký và ghi rõ họ tên)</div>
        </div>
        <div class="signature-block">
          <div class="signature-title">Nhân viên thu ngân</div>
          <div class="signature-line">(Ký và ghi rõ họ tên)</div>
        </div>
      </div>
      
      <div class="footer">
        <p><strong>Cảm ơn quý khách đã sử dụng dịch vụ!</strong></p>
        <p style="margin-top: 10px;">Hóa đơn được in lúc: ${formatDate(new Date())}</p>
      </div>
    </body>
    </html>
  `

  printWindow.document.write(htmlContent)
  printWindow.document.close()
  
  // Wait for content to load before focusing
  printWindow.onload = function() {
    printWindow.focus()
  }
}
