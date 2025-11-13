package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.order.*;
import com.example.backend_quanlynhahanglau.entity.*;
import com.example.backend_quanlynhahanglau.enums.OrderStatus;
import com.example.backend_quanlynhahanglau.enums.ReservationStatus;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.exception.BadRequestException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.*;
import com.example.backend_quanlynhahanglau.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final DishRepository dishRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByDate(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .createdBy(user)
                .subtotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .notes(request.getNotes())
                .orderDetails(new ArrayList<>())
                .build();

        Reservation reservation = null;
        
        // Xử lý reservation (đặt bàn trực tuyến)
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", request.getReservationId()));
            
            // Kiểm tra trạng thái reservation
            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                throw new BadRequestException("Không thể tạo đơn từ đặt bàn đã bị hủy");
            }
            
            if (reservation.getStatus() == ReservationStatus.COMPLETED) {
                throw new BadRequestException("Đặt bàn này đã được sử dụng để tạo đơn");
            }
            
            // Kiểm tra nếu có tableId trong request thì báo lỗi
            if (request.getTableId() != null) {
                throw new BadRequestException("Khi tạo đơn từ đặt bàn, không được chọn bàn lại. Bàn sẽ được lấy tự động từ đặt bàn.");
            }
            
            // Tự động lấy customer từ reservation
            order.setCustomer(reservation.getCustomer());
            
            // Tự động lấy table từ reservation (bắt buộc phải có)
            if (reservation.getTable() != null) {
                RestaurantTable table = reservation.getTable();
                // Kiểm tra bàn có khả dụng không
                if (table.getActive() == null || !table.getActive()) {
                    throw new BadRequestException("Bàn không khả dụng");
                }
                // Nếu bàn đang RESERVED hoặc AVAILABLE, có thể sử dụng cho reservation này
                if (table.getStatus() == TableStatus.AVAILABLE || table.getStatus() == TableStatus.RESERVED) {
                    table.setStatus(TableStatus.OCCUPIED);
                    tableRepository.save(table);
                    order.setTable(table);
                } else if (table.getStatus() == TableStatus.OCCUPIED) {
                    // Kiểm tra xem bàn này có đang được sử dụng bởi reservation khác không
                    throw new BadRequestException("Bàn đang được sử dụng");
                }
            } else {
                throw new BadRequestException("Đặt bàn này chưa có bàn được gán");
            }
            
            order.setReservation(reservation);
            
            // Cập nhật trạng thái reservation thành CONFIRMED nếu đang PENDING
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
                reservation.setConfirmedBy(user);
                reservation.setConfirmedAt(LocalDateTime.now());
                reservationRepository.save(reservation);
            }
        } else {
            // Nếu không có reservation, xử lý customer và table như bình thường
            
            // Set customer
            if (request.getCustomerId() != null) {
                Customer customer = customerRepository.findById(request.getCustomerId())
                        .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", request.getCustomerId()));
                if (customer.getBlocked()) {
                    throw new BadRequestException("Khách hàng này đã bị chặn");
                }
                order.setCustomer(customer);
            }

            // Set table
            if (request.getTableId() != null) {
                RestaurantTable table = tableRepository.findById(request.getTableId())
                        .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", request.getTableId()));
                
                if (table.getStatus() != TableStatus.AVAILABLE && table.getStatus() != TableStatus.RESERVED) {
                    throw new BadRequestException("Bàn không khả dụng. Trạng thái hiện tại: " + table.getStatus());
                }
                
                table.setStatus(TableStatus.OCCUPIED);
                tableRepository.save(table);
                order.setTable(table);
            }
        }

        // Kiểm tra có ít nhất customer hoặc table
        if (order.getCustomer() == null && order.getTable() == null) {
            throw new BadRequestException("Đơn hàng phải có ít nhất khách hàng hoặc bàn");
        }

        // Save order first to get ID
        order = orderRepository.save(order);

        // Add order details
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Dish dish = dishRepository.findById(itemRequest.getDishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", itemRequest.getDishId()));
            
            if (!dish.getActive()) {
                throw new BadRequestException("Món ăn " + dish.getName() + " không còn hoạt động");
            }

            BigDecimal itemSubtotal = dish.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(itemRequest.getQuantity())
                    .price(dish.getPrice())
                    .subtotal(itemSubtotal)
                    .notes(itemRequest.getNotes())
                    .build();

            order.getOrderDetails().add(detail);
            subtotal = subtotal.add(itemSubtotal);
        }

        // Calculate totals
        order.setSubtotal(subtotal);
        
        // Calculate tax (10%)
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10));
        order.setTax(tax);
        
        // Calculate total
        BigDecimal total = subtotal.add(tax).subtract(order.getDiscount());
        order.setTotal(total);

        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));

        order.setStatus(status);

        if (status == OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
            
            // Cập nhật lại trạng thái bàn
            if (order.getTable() != null) {
                order.getTable().setStatus(TableStatus.AVAILABLE);
                tableRepository.save(order.getTable());
            }
            
            // Cập nhật trạng thái reservation thành COMPLETED nếu có
            if (order.getReservation() != null && order.getReservation().getStatus() != ReservationStatus.COMPLETED) {
                order.getReservation().setStatus(ReservationStatus.COMPLETED);
                reservationRepository.save(order.getReservation());
            }
        }

        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse addItemToOrder(Long orderId, OrderItemRequest itemRequest) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", orderId));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Không thể thêm món vào đơn hàng đã hoàn thành hoặc đã hủy");
        }

        Dish dish = dishRepository.findById(itemRequest.getDishId())
                .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", itemRequest.getDishId()));
        
        if (!dish.getActive()) {
            throw new BadRequestException("Món ăn " + dish.getName() + " không còn hoạt động");
        }

        BigDecimal itemSubtotal = dish.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
        
        OrderDetail detail = OrderDetail.builder()
                .order(order)
                .dish(dish)
                .quantity(itemRequest.getQuantity())
                .price(dish.getPrice())
                .subtotal(itemSubtotal)
                .notes(itemRequest.getNotes())
                .build();

        order.getOrderDetails().add(detail);
        orderDetailRepository.save(detail);

        // Recalculate totals
        recalculateOrderTotals(order);
        
        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", id));
        
        // Cập nhật lại trạng thái bàn
        if (order.getTable() != null && order.getTable().getStatus() == TableStatus.OCCUPIED) {
            order.getTable().setStatus(TableStatus.AVAILABLE);
            tableRepository.save(order.getTable());
        }
        
        orderRepository.delete(order);
    }

    @Transactional
    public OrderResponse createCounterOrder(com.example.backend_quanlynhahanglau.dto.order.CounterOrderRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .createdBy(user)
                .subtotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .notes(request.getNotes())
                .orderDetails(new ArrayList<>())
                .build();

        // Create or find customer if provided
        if (request.getCustomerName() != null && !request.getCustomerName().trim().isEmpty()) {
            // Try to find existing customer by phone
            Customer customer = null;
            if (request.getCustomerPhone() != null && !request.getCustomerPhone().trim().isEmpty()) {
                customer = customerRepository.findByPhone(request.getCustomerPhone()).orElse(null);
            }
            
            // If not found, create a walk-in customer entry
            if (customer == null) {
                customer = Customer.builder()
                        .fullName(request.getCustomerName())
                        .phone(request.getCustomerPhone())
                        .isVip(false)
                        .blocked(false)
                        .active(true)
                        .build();
                customer = customerRepository.save(customer);
            } else {
                // Kiểm tra customer có bị chặn không
                if (customer.getBlocked()) {
                    throw new BadRequestException("Khách hàng này đã bị chặn");
                }
            }
            
            order.setCustomer(customer);
        }

        // Hỗ trợ gán bàn cho khách đặt trực tiếp tại nhà hàng (nếu có)
        if (request.getTableId() != null) {
            RestaurantTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", request.getTableId()));
            
            if (table.getStatus() != TableStatus.AVAILABLE && table.getStatus() != TableStatus.RESERVED) {
                throw new BadRequestException("Bàn không khả dụng. Trạng thái hiện tại: " + table.getStatus());
            }
            
            table.setStatus(TableStatus.OCCUPIED);
            tableRepository.save(table);
            order.setTable(table);
        }

        // Save order first to get ID
        order = orderRepository.save(order);

        // Add order details
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItemRequest itemRequest : request.getItems()) {
            Dish dish = dishRepository.findById(itemRequest.getDishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", itemRequest.getDishId()));
            
            if (!dish.getActive()) {
                throw new BadRequestException("Món ăn " + dish.getName() + " không còn hoạt động");
            }

            BigDecimal itemSubtotal = dish.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .dish(dish)
                    .quantity(itemRequest.getQuantity())
                    .price(dish.getPrice())
                    .subtotal(itemSubtotal)
                    .notes(itemRequest.getNotes())
                    .build();

            order.getOrderDetails().add(detail);
            subtotal = subtotal.add(itemSubtotal);
        }

        // Calculate totals
        order.setSubtotal(subtotal);
        
        // Calculate tax (10%)
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10));
        order.setTax(tax);
        
        // Calculate total
        BigDecimal total = subtotal.add(tax).subtract(order.getDiscount());
        order.setTotal(total);

        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private void recalculateOrderTotals(Order order) {
        BigDecimal subtotal = order.getOrderDetails().stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);
        
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10));
        order.setTax(tax);
        
        BigDecimal total = subtotal.add(tax).subtract(order.getDiscount());
        order.setTotal(total);
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + timestamp;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderDetails().stream()
                .map(detail -> OrderItemResponse.builder()
                        .id(detail.getId())
                        .dishName(detail.getDish().getName())
                        .dishId(detail.getDish().getId())
                        .quantity(detail.getQuantity())
                        .price(detail.getPrice())
                        .subtotal(detail.getSubtotal())
                        .notes(detail.getNotes())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .tableNumber(order.getTable() != null ? order.getTable().getTableNumber() : null)
                .tableId(order.getTable() != null ? order.getTable().getId() : null)
                .reservationId(order.getReservation() != null ? order.getReservation().getId() : null)
                .items(items)
                .subtotal(order.getSubtotal())
                .discount(order.getDiscount())
                .tax(order.getTax())
                .total(order.getTotal())
                .status(order.getStatus())
                .notes(order.getNotes())
                .createdByName(order.getCreatedBy() != null ? order.getCreatedBy().getFullName() : null)
                .createdAt(order.getCreatedAt())
                .completedAt(order.getCompletedAt())
                .build();
    }
}
