package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.reservation.PublicReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationResponse;
import com.example.backend_quanlynhahanglau.entity.*;
import com.example.backend_quanlynhahanglau.enums.DishStatus;
import com.example.backend_quanlynhahanglau.enums.OrderStatus;
import com.example.backend_quanlynhahanglau.enums.ReservationStatus;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.enums.TableType;
import com.example.backend_quanlynhahanglau.exception.BadRequestException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.*;
import com.example.backend_quanlynhahanglau.security.UserDetailsImpl;
import com.example.backend_quanlynhahanglau.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final RestaurantTableRepository tableRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final WebSocketNotificationService webSocketNotificationService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final DishRepository dishRepository;

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));
        return mapToResponse(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByDate(LocalDateTime startDate, LocalDateTime endDate) {
        return reservationRepository.findByDate(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> searchByCustomerName(String customerName) {
        return reservationRepository.searchByCustomerName(customerName).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByCustomerId(Long customerId) {
        // Kiểm tra customer có tồn tại không
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Khách hàng", "id", customerId);
        }
        
        // Lấy lịch sử đặt bàn của customer, sắp xếp theo thời gian đặt bàn giảm dần
        return reservationRepository.findByCustomerIdOrderByReservationTimeDesc(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getReservationsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Người dùng", "id", userId);
        }
        
        List<Customer> customers = customerRepository.findByUserId(userId);
        
        if (customers.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Không tìm thấy thông tin khách hàng cho người dùng này. Vui lòng liên kết tài khoản với thông tin khách hàng.");
        }
        
        // Lấy tất cả reservation của tất cả Customer, sắp xếp theo thời gian đặt bàn giảm dần
        return customers.stream()
                .flatMap(customer -> reservationRepository.findByCustomerIdOrderByReservationTimeDesc(customer.getId()).stream())
                .sorted((r1, r2) -> r2.getReservationTime().compareTo(r1.getReservationTime()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReservationResponse createPublicReservation(PublicReservationRequest request) {
        log.info("Creating public reservation for phone: {}", request.getCustomerPhone());
        
        // Lấy userId: ưu tiên từ request, nếu không có thì lấy từ SecurityContext (nếu user đã đăng nhập)
        Long userId = request.getUserId();
        if (userId == null) {
            userId = getCurrentUserId();
        }
        
        final Long finalUserId = userId;
        
        // Tìm hoặc tạo customer
        Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> {
                    log.info("Creating new customer for phone: {}", request.getCustomerPhone());
                    String customerCode = generateUniqueCustomerCode();
                    Customer.CustomerBuilder customerBuilder = Customer.builder()
                            .customerCode(customerCode)
                            .fullName(request.getCustomerName())
                            .phone(request.getCustomerPhone())
                            .email(request.getCustomerEmail())
                            .isVip(false)
                            .active(true)
                            .blocked(false);
                    
                    if (finalUserId != null) {
                        User user = userRepository.findById(finalUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", finalUserId));
                        customerBuilder.user(user);
                        log.info("Linking new customer with user {}", finalUserId);
                    }
                    
                    return customerRepository.save(customerBuilder.build());
                });

        boolean customerUpdated = false;
        if (request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank()) {
            String newEmail = request.getCustomerEmail().trim();
            if (!newEmail.equals(customer.getEmail())) {
                customer.setEmail(newEmail);
                customerUpdated = true;
            }
        }

        // Nếu customer đã tồn tại nhưng chưa có user và có userId, cập nhật liên kết
        if (customer.getUser() == null && finalUserId != null) {
            User user = userRepository.findById(finalUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", finalUserId));
            customer.setUser(user);
            customerUpdated = true;
            log.info("Linked existing customer {} with user {}", customer.getId(), finalUserId);
        }
        
        // Lưu customer sau khi cập nhật (chỉ save nếu có thay đổi)
        if (customerUpdated) {
            customer = customerRepository.save(customer);
        }

        if (customer.getBlocked()) {
            throw new BadRequestException("Khách hàng này đã bị chặn");
        }

        // Table có thể để trống, admin/manager sẽ cập nhật sau
        RestaurantTable table = null;
        if (request.getTableId() != null) {
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", request.getTableId()));
            
            // Kiểm tra bàn đã bị xóa mềm chưa
            if (table.getIsDeleted() != null && table.getIsDeleted()) {
                throw new ResourceNotFoundException("Bàn", "id", request.getTableId());
            }
            
            // Chỉ cho phép đặt bàn online các bàn có type ONLINE
            if (table.getType() != TableType.ONLINE) {
                throw new BadRequestException("Bàn này không thể đặt online. Chỉ các bàn có loại ONLINE mới có thể đặt online. Loại hiện tại: " + table.getType());
            }

            // Kiểm tra conflict với các reservation khác trong khoảng ±3 giờ (chỉ khi có table)
            LocalDateTime reservationTime = request.getReservationDateTime();
            LocalDateTime startTime = reservationTime.minusHours(3).plusMinutes(1);
            LocalDateTime endTime = reservationTime.plusHours(3).minusMinutes(1);
            
            List<Reservation> conflictingReservations = reservationRepository
                    .findConflictingReservationsByTable(table.getId(), startTime, endTime);
            
            if (!conflictingReservations.isEmpty()) {
                Reservation conflict = conflictingReservations.get(0);
                throw new BadRequestException(
                        String.format("Bàn %s đã được đặt vào lúc %s. Không thể đặt trong khoảng ±3 giờ (từ %s đến %s)",
                                table.getTableNumber(),
                                conflict.getReservationTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                endTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            }
        }

        // Tạo reservation (có thể không có table)
        // Lưu tên khách hàng từ form đặt bàn vào reservation (riêng cho đơn này)
        String reservationCustomerName = request.getCustomerName() != null && !request.getCustomerName().isBlank() 
                ? request.getCustomerName().trim() 
                : null;
        
        Reservation savedReservation = Reservation.builder()
                .customer(customer)
                .customerName(reservationCustomerName) 
                .table(table)
                .reservationTime(request.getReservationDateTime())
                .numberOfGuests(request.getNumberOfGuests())
                .specialRequests(request.getNotes())
                .notes(request.getNotes())
                .status(ReservationStatus.CONFIRMED)
                .emailSent(false)
                .build();

        savedReservation = reservationRepository.save(savedReservation);
        log.info("Public reservation created successfully with ID: {}", savedReservation.getId());
        
        // Nếu có items, tạo Order cùng với Reservation
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            try {
                createOrderFromReservation(savedReservation, request.getItems());
                log.info("Order created successfully for reservation ID: {}", savedReservation.getId());
            } catch (Exception e) {
                log.error("Error creating order for reservation ID: {}", savedReservation.getId(), e);
            }
        }
        
        // Refresh reservation để đảm bảo customer được load với dữ liệu mới nhất
        final Long reservationId = savedReservation.getId();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", reservationId));
        
        log.info("Reservation {} mapped with customer name: {}", reservation.getId(), 
                reservation.getCustomer() != null ? reservation.getCustomer().getFullName() : "null");
        
        ReservationResponse response = mapToResponse(reservation);
        
        // Send WebSocket notification for new reservation
        webSocketNotificationService.notifyNewReservation(response);
        
        return response;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        Customer customer;
        
        // Check if customerId is provided or need to create/find customer by phone
        if (request.getCustomerId() != null) {
            customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", request.getCustomerId()));
        } else {
            // Find or create customer by phone
            if (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank()) {
                throw new BadRequestException("Vui lòng cung cấp số điện thoại khách hàng");
            }
            
            customer = customerRepository.findByPhone(request.getCustomerPhone())
                    .orElseGet(() -> {
                        log.info("Creating new customer for phone: {}", request.getCustomerPhone());
                        String customerCode = generateUniqueCustomerCode();
                        Customer newCustomer = Customer.builder()
                                .customerCode(customerCode)
                                .fullName(request.getCustomerName() != null ? request.getCustomerName() : "Khách hàng")
                                .phone(request.getCustomerPhone())
                                .email(request.getCustomerEmail())
                                .isVip(false)
                                .active(true)
                                .blocked(false)
                                .build();
                        return customerRepository.save(newCustomer);
                    });
        }

        if (customer.getBlocked()) {
            throw new BadRequestException("Khách hàng này đã bị chặn");
        }

        // Parse status if provided, otherwise use CONFIRMED
        ReservationStatus status = ReservationStatus.CONFIRMED;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = ReservationStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}, using CONFIRMED", request.getStatus());
            }
        }

        Reservation reservation = Reservation.builder()
                .customer(customer)
                .reservationTime(request.getReservationDateTime())
                .numberOfGuests(request.getNumberOfGuests())
                .specialRequests(request.getSpecialRequests())
                .notes(request.getNotes())
                .status(status)
                .emailSent(false)
                .build();

        // Bắt buộc phải có tableId
        if (request.getTableId() == null) {
            throw new BadRequestException("Vui lòng chọn bàn");
        }

        if (request.getTableId() != null) {
            RestaurantTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", request.getTableId()));
            
            // Kiểm tra bàn đã bị xóa mềm chưa
            if (table.getIsDeleted() != null && table.getIsDeleted()) {
                throw new ResourceNotFoundException("Bàn", "id", request.getTableId());
            }
            
            if (table.getStatus() != TableStatus.AVAILABLE) {
                throw new BadRequestException("Bàn không khả dụng. Trạng thái hiện tại: " + table.getStatus());
            }
            
            // Kiểm tra conflict với các reservation khác trong khoảng ±3 giờ
            LocalDateTime reservationTime = request.getReservationDateTime();
            LocalDateTime startTime = reservationTime.minusHours(3).plusMinutes(1);
            LocalDateTime endTime = reservationTime.plusHours(3).minusMinutes(1);
            
            List<Reservation> conflictingReservations = reservationRepository
                    .findConflictingReservationsByTable(table.getId(), startTime, endTime);
            
            if (!conflictingReservations.isEmpty()) {
                Reservation conflict = conflictingReservations.get(0);
                throw new BadRequestException(
                        String.format("Bàn %s đã được đặt vào lúc %s. Không thể đặt trong khoảng ±3 giờ (từ %s đến %s)",
                                table.getTableNumber(),
                                conflict.getReservationTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                                endTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            }
            
            reservation.setTable(table);
        }

        reservation = reservationRepository.save(reservation);
        log.info("Reservation created successfully with ID: {}", reservation.getId());
        
        ReservationResponse response = mapToResponse(reservation);
        
        // Send WebSocket notification for new reservation
        webSocketNotificationService.notifyNewReservation(response);
        
        return response;
    }

    @Transactional
    public ReservationResponse updateReservation(Long id, ReservationRequest request) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));

        // Update customer if provided
        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", request.getCustomerId()));
            reservation.setCustomer(customer);
        } else if (request.getCustomerPhone() != null) {
            // Find or create customer by phone
            Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                    .orElseGet(() -> {
                        log.info("Creating new customer for phone: {}", request.getCustomerPhone());
                        String customerCode = generateUniqueCustomerCode();
                        Customer newCustomer = Customer.builder()
                                .customerCode(customerCode)
                                .fullName(request.getCustomerName() != null ? request.getCustomerName() : "Khách hàng")
                                .phone(request.getCustomerPhone())
                                .email(request.getCustomerEmail())
                                .isVip(false)
                                .active(true)
                                .blocked(false)
                                .build();
                        return customerRepository.save(newCustomer);
                    });
            reservation.setCustomer(customer);
        }

        reservation.setReservationTime(request.getReservationDateTime());
        reservation.setNumberOfGuests(request.getNumberOfGuests());
        reservation.setSpecialRequests(request.getSpecialRequests());
        reservation.setNotes(request.getNotes());

        // Update status if provided
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                reservation.setStatus(ReservationStatus.valueOf(request.getStatus()));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}, keeping current status", request.getStatus());
            }
        }

        if (request.getTableId() != null) {
            RestaurantTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", request.getTableId()));
            
            // Kiểm tra bàn đã bị xóa mềm chưa
            if (table.getIsDeleted() != null && table.getIsDeleted()) {
                throw new ResourceNotFoundException("Bàn", "id", request.getTableId());
            }
            
            reservation.setTable(table);
        } else {
            reservation.setTable(null);
        }

        reservation = reservationRepository.save(reservation);
        log.info("Reservation updated successfully with ID: {}", reservation.getId());
        return mapToResponse(reservation);
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));
        reservationRepository.delete(reservation);
    }

    @Transactional
    public ReservationResponse confirmReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setConfirmedBy(user);
        reservation.setConfirmedAt(LocalDateTime.now());

        // Cập nhật trạng thái bàn
        if (reservation.getTable() != null) {
            reservation.getTable().setStatus(TableStatus.RESERVED);
            tableRepository.save(reservation.getTable());
        }

        reservation = reservationRepository.save(reservation);
        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse checkInReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setConfirmedBy(user);

        // Gán nhân viên phụ trách vào Reservation (lưu vĩnh viễn để tra cứu)
        // Nếu user là STAFF, tự động gán làm nhân viên phụ trách
        // Admin/Manager check-in sẽ không tự động gán, họ có thể gán sau
        if (user.getRoles().stream().anyMatch(role -> role.getName().name().equals("ROLE_STAFF"))) {
            reservation.setAssignedStaff(user);
            log.info("Assigned staff {} to reservation {} after check-in", user.getFullName(), reservation.getId());
        }

        reservation = reservationRepository.save(reservation);
        
        // Nếu reservation có bàn, gán nhân viên phụ trách cho bàn đó (tạm thời để hiển thị real-time)
        if (reservation.getTable() != null && reservation.getAssignedStaff() != null) {
            RestaurantTable table = reservation.getTable();
            table.setAssignedStaff(reservation.getAssignedStaff());
            tableRepository.save(table);
            log.info("Assigned staff {} to table {} after check-in", reservation.getAssignedStaff().getFullName(), table.getTableNumber());
        }
        
        // Cập nhật status của tất cả orders liên quan thành CONFIRMED
        LocalDateTime checkInTime = LocalDateTime.now();
        List<Order> orders = orderRepository.findByReservationId(id);
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.COMPLETED) {
                OrderStatus oldStatus = order.getStatus();
                order.setStatus(OrderStatus.CONFIRMED);
                // Lưu thời gian checkin để tính thời gian cảnh báo món từ lúc này
                order.setConfirmedAt(checkInTime);
                orderRepository.save(order);
                log.info("Updated order ID: {} status from {} to CONFIRMED and set confirmedAt to {} after check-in reservation ID: {}", 
                        order.getId(), oldStatus, checkInTime, id);
            }
        }
        
        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));

        reservation.setStatus(ReservationStatus.CANCELLED);

        // Cập nhật lại trạng thái bàn
        if (reservation.getTable() != null && reservation.getTable().getStatus() == TableStatus.RESERVED) {
            reservation.getTable().setStatus(TableStatus.AVAILABLE);
            tableRepository.save(reservation.getTable());
        }

        reservation = reservationRepository.save(reservation);
        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse updateReservationTable(Long id, Long tableId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));

        // Lưu reservation ID để dùng trong lambda
        final Long reservationId = reservation.getId();

        // Kiểm tra reservation chưa bị hủy
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Không thể cập nhật bàn cho đặt bàn đã bị hủy");
        }

        // Nếu tableId là null, xóa bàn khỏi reservation
        if (tableId == null) {
            // Cập nhật lại trạng thái bàn cũ nếu có
            if (reservation.getTable() != null && reservation.getTable().getStatus() == TableStatus.RESERVED) {
                reservation.getTable().setStatus(TableStatus.AVAILABLE);
                tableRepository.save(reservation.getTable());
            }
            reservation.setTable(null);
            Reservation savedReservation = reservationRepository.save(reservation);
            log.info("Removed table from reservation ID: {}", savedReservation.getId());
            return mapToResponse(savedReservation);
        }

        // Tìm bàn mới
        RestaurantTable newTable = tableRepository.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", tableId));

        // Kiểm tra bàn đã bị xóa mềm chưa
        if (newTable.getIsDeleted() != null && newTable.getIsDeleted()) {
            throw new ResourceNotFoundException("Bàn", "id", tableId);
        }

        // Kiểm tra bàn có active không
        if (newTable.getActive() == null || !newTable.getActive()) {
            throw new BadRequestException("Bàn không khả dụng");
        }


        // Kiểm tra conflict với các reservation khác (trừ chính reservation hiện tại)
        LocalDateTime reservationTime = reservation.getReservationTime();
        LocalDateTime startTime = reservationTime.minusHours(3).plusMinutes(1);
        LocalDateTime endTime = reservationTime.plusHours(3).minusMinutes(1);
        
        List<Reservation> conflictingReservations = reservationRepository
                .findConflictingReservationsByTable(newTable.getId(), startTime, endTime);
        
        // Loại bỏ chính reservation hiện tại khỏi danh sách conflict
        conflictingReservations = conflictingReservations.stream()
                .filter(r -> !r.getId().equals(reservationId))
                .collect(Collectors.toList());
        
        if (!conflictingReservations.isEmpty()) {
            Reservation conflict = conflictingReservations.get(0);
            throw new BadRequestException(
                    String.format("Bàn %s đã được đặt vào lúc %s. Không thể đặt trong khoảng ±3 giờ (từ %s đến %s)",
                            newTable.getTableNumber(),
                            conflict.getReservationTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            endTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }

        // Cập nhật lại trạng thái bàn cũ nếu có và khác với bàn mới
        if (reservation.getTable() != null && !reservation.getTable().getId().equals(tableId)) {
            if (reservation.getTable().getStatus() == TableStatus.RESERVED) {
                reservation.getTable().setStatus(TableStatus.AVAILABLE);
                tableRepository.save(reservation.getTable());
            }
        }

        // Gán bàn mới
        reservation.setTable(newTable);
        

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Updated table for reservation ID: {} to table ID: {}", savedReservation.getId(), tableId);
        
        // Gửi email thông báo bàn đã được chọn cho khách hàng
        try {
            emailService.sendTableAssignedEmail(savedReservation);
            // Đánh dấu đã gửi email (chỉ khi gửi thành công)
            savedReservation.setEmailSent(true);
            reservationRepository.save(savedReservation);
        } catch (Exception e) {
            log.error("Không thể gửi email cho reservation ID: {}", savedReservation.getId(), e);
            // Không throw exception để không ảnh hưởng đến flow chính
        }
        
        return mapToResponse(savedReservation);
    }

    @Transactional
    public ReservationResponse cancelPublicReservation(Long id, Long userId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));

        // Nếu có userId, kiểm tra xem reservation có thuộc về user này không
        if (userId != null) {
            // Tìm Customer theo User ID
            List<Customer> customers = customerRepository.findByUserId(userId);
            if (customers.isEmpty()) {
                throw new BadRequestException("Không tìm thấy thông tin khách hàng cho người dùng này");
            }
            
            // Kiểm tra xem reservation có thuộc về một trong các Customer của User không
            Long reservationCustomerId = reservation.getCustomer().getId();
            boolean belongsToUser = customers.stream()
                    .anyMatch(customer -> customer.getId().equals(reservationCustomerId));
            
            if (!belongsToUser) {
                throw new BadRequestException("Bạn không có quyền hủy đặt bàn này");
            }
        }

        // Kiểm tra trạng thái reservation
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new BadRequestException("Đặt bàn này đã được hủy rồi");
        }

        if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
            // Nếu đã confirm, có thể cần thông báo cho nhà hàng
            log.warn("User {} is cancelling a confirmed reservation {}", userId, id);
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        // Cập nhật lại trạng thái bàn
        if (reservation.getTable() != null && reservation.getTable().getStatus() == TableStatus.RESERVED) {
            reservation.getTable().setStatus(TableStatus.AVAILABLE);
            tableRepository.save(reservation.getTable());
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        log.info("Public reservation {} cancelled by user {}", id, userId);
        return mapToResponse(savedReservation);
    }

    /**
     * Đổi bàn cho khách đã check-in
     * Cập nhật reservation, orders và trạng thái bàn
     */
    @Transactional
    public ReservationResponse transferTable(Long reservationId, Long newTableId) {
        // Tìm reservation
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", reservationId));

        // Kiểm tra reservation đã check-in chưa
        if (reservation.getStatus() != ReservationStatus.CHECKED_IN) {
            throw new BadRequestException("Chỉ có thể đổi bàn cho khách đã check-in");
        }

        // Kiểm tra reservation có bàn hiện tại không
        if (reservation.getTable() == null) {
            throw new BadRequestException("Đặt bàn này chưa có bàn được gán");
        }

        Long oldTableId = reservation.getTable().getId();

        // Kiểm tra bàn mới có khác bàn cũ không
        if (oldTableId.equals(newTableId)) {
            throw new BadRequestException("Bàn mới phải khác bàn hiện tại");
        }

        // Tìm bàn mới
        RestaurantTable newTable = tableRepository.findById(newTableId)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", newTableId));

        // Kiểm tra bàn đã bị xóa mềm chưa
        if (newTable.getIsDeleted() != null && newTable.getIsDeleted()) {
            throw new ResourceNotFoundException("Bàn", "id", newTableId);
        }

        // Kiểm tra bàn có active không
        if (newTable.getActive() == null || !newTable.getActive()) {
            throw new BadRequestException("Bàn không khả dụng");
        }

        // Kiểm tra bàn mới có trống không (chỉ cho phép đổi sang bàn AVAILABLE)
        if (newTable.getStatus() != TableStatus.AVAILABLE) {
            throw new BadRequestException("Chỉ có thể đổi sang bàn trống (AVAILABLE)");
        }

        // Kiểm tra sức chứa bàn mới có đủ không
        if (newTable.getCapacity() < reservation.getNumberOfGuests()) {
            throw new BadRequestException(
                    String.format("Bàn %s chỉ có %d chỗ, không đủ cho %d khách",
                            newTable.getTableNumber(), newTable.getCapacity(), reservation.getNumberOfGuests()));
        }

        // Lấy bàn cũ
        RestaurantTable oldTable = reservation.getTable();

        // Cập nhật reservation với bàn mới
        reservation.setTable(newTable);
        reservation = reservationRepository.save(reservation);
        log.info("Updated reservation ID: {} table from {} to {}", reservationId, oldTableId, newTableId);

        // Cập nhật nhân viên phụ trách vào bàn mới (nếu reservation có nhân viên phụ trách)
        if (reservation.getAssignedStaff() != null) {
            newTable.setAssignedStaff(reservation.getAssignedStaff());
            tableRepository.save(newTable);
            log.info("Assigned staff {} to new table {} after transfer", reservation.getAssignedStaff().getFullName(), newTable.getTableNumber());
        }

        // Cập nhật tất cả orders liên quan đến reservation này
        List<Order> orders = orderRepository.findByReservationId(reservationId);
        for (Order order : orders) {
            // Chỉ cập nhật orders chưa thanh toán (PENDING, CONFIRMED, PREPARING, SERVED)
            if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.COMPLETED) {
                order.setTable(newTable);
                orderRepository.save(order);
                log.info("Updated order ID: {} table from {} to {}", order.getId(), oldTableId, newTableId);
            }
        }

        // Cập nhật trạng thái bàn mới thành OCCUPIED
        newTable.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(newTable);
        log.info("Updated new table ID: {} status to OCCUPIED", newTableId);

        // Cập nhật trạng thái bàn cũ thành AVAILABLE và xóa nhân viên phụ trách
        oldTable.setStatus(TableStatus.AVAILABLE);
        oldTable.setAssignedStaff(null); // Xóa nhân viên phụ trách khỏi bàn cũ
        tableRepository.save(oldTable);
        log.info("Updated old table ID: {} status to AVAILABLE and removed assigned staff", oldTableId);

        return mapToResponse(reservation);
    }

    /**
     * Lấy userId từ SecurityContext nếu user đã đăng nhập
     * @return userId nếu user đã đăng nhập, null nếu chưa đăng nhập
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() 
                    && authentication.getPrincipal() instanceof UserDetailsImpl) {
                UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                return userDetails.getId();
            }
        } catch (Exception e) {
            log.debug("Could not get current user from SecurityContext: {}", e.getMessage());
        }
        return null;
    }

    private String generateUniqueCustomerCode() {
        String code;
        do {
            code = CodeGenerator.generateCustomerCode();
        } while (customerRepository.existsByCustomerCode(code));
        return code;
    }

    /**
     * Tạo Order từ Reservation với danh sách món đã chọn
     * Method này được gọi khi khách hàng đặt món cùng với đặt bàn
     */
    private void createOrderFromReservation(Reservation reservation, List<com.example.backend_quanlynhahanglau.dto.order.OrderItemRequest> items) {
        log.info("Creating order from reservation ID: {} with {} items", reservation.getId(), items.size());
        
        // Tạo Order
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PENDING)
                .createdBy(null) // Public reservation không có user
                .customer(reservation.getCustomer())
                .reservation(reservation)
                .table(reservation.getTable()) // Có thể null nếu chưa gán bàn
                .subtotal(BigDecimal.ZERO)
                .discount(BigDecimal.ZERO)
                .tax(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .notes(reservation.getNotes())
                .orderDetails(new java.util.ArrayList<>())
                .build();
        
        // Save order first to get ID
        order = orderRepository.save(order);
        
        // Add order details
        BigDecimal subtotal = BigDecimal.ZERO;
        for (com.example.backend_quanlynhahanglau.dto.order.OrderItemRequest itemRequest : items) {
            Dish dish = dishRepository.findById(itemRequest.getDishId())
                    .orElseThrow(() -> new ResourceNotFoundException("Món ăn", "id", itemRequest.getDishId()));
            
            if (!dish.getActive()) {
                throw new BadRequestException("Món ăn " + dish.getName() + " không còn hoạt động");
            }
            
            if (dish.getStatus() == DishStatus.DISCONTINUED) {
                throw new BadRequestException("Món ăn " + dish.getName() + " đã dừng kinh doanh");
            }
            
            // Tạo từng order_detail riêng cho mỗi đơn vị món (quantity = 1 cho mỗi record)
            for (int i = 0; i < itemRequest.getQuantity(); i++) {
                BigDecimal itemSubtotal = dish.getPrice();
                
                OrderDetail detail = OrderDetail.builder()
                        .order(order)
                        .dish(dish)
                        .quantity(1) // Luôn là 1 cho mỗi record
                        .price(dish.getPrice())
                        .subtotal(itemSubtotal)
                        .notes(itemRequest.getNotes())
                        .build();
                
                order.getOrderDetails().add(detail);
                orderDetailRepository.save(detail);
                subtotal = subtotal.add(itemSubtotal);
            }
        }
        
        // Calculate totals
        order.setSubtotal(subtotal);
        
        // Calculate tax (10%)
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10));
        order.setTax(tax);
        
        // Calculate total
        BigDecimal total = subtotal.add(tax).subtract(order.getDiscount());
        order.setTotal(total);
        
        // Save order with totals
        orderRepository.save(order);
        
        log.info("Order created successfully with ID: {} for reservation ID: {}", order.getId(), reservation.getId());
    }
    
    private String generateOrderNumber() {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + timestamp;
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        // Ưu tiên lấy tên từ reservation.customerName (tên từ form đặt bàn)
        // Nếu không có thì lấy từ customer.fullName
        String customerName = reservation.getCustomerName();
        if (customerName == null || customerName.isBlank()) {
            customerName = reservation.getCustomer() != null ? reservation.getCustomer().getFullName() : null;
        }
        
        return ReservationResponse.builder()
                .id(reservation.getId())
                .customerName(customerName)
                .customerPhone(reservation.getCustomer() != null ? reservation.getCustomer().getPhone() : null)
                .customerId(reservation.getCustomer() != null ? reservation.getCustomer().getId() : null)
                .tableNumber(reservation.getTable() != null ? reservation.getTable().getTableNumber() : null)
                .tableId(reservation.getTable() != null ? reservation.getTable().getId() : null)
                .reservationTime(reservation.getReservationTime())
                .numberOfGuests(reservation.getNumberOfGuests())
                .status(reservation.getStatus())
                .specialRequests(reservation.getSpecialRequests())
                .notes(reservation.getNotes())
                .emailSent(reservation.getEmailSent())
                .confirmedByName(reservation.getConfirmedBy() != null ? reservation.getConfirmedBy().getFullName() : null)
                .confirmedAt(reservation.getConfirmedAt())
                .depositAmount(reservation.getDepositAmount())
                .assignedStaffId(reservation.getAssignedStaff() != null ? reservation.getAssignedStaff().getId() : null)
                .assignedStaffName(reservation.getAssignedStaff() != null ? reservation.getAssignedStaff().getFullName() : null)
                .createdAt(reservation.getCreatedAt())
                .build();
    }

    /**
     * Thanh toán cọc 20% cho reservation có đặt món trước
     */
    @Transactional
    public ReservationResponse payDeposit(Long reservationId, BigDecimal depositAmount) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", reservationId));

        // Kiểm tra reservation đã có cọc chưa
        if (reservation.getDepositAmount() != null && reservation.getDepositAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("Đặt bàn này đã được thanh toán cọc");
        }

        // Kiểm tra reservation có order không (phải có món mới cần cọc)
        List<Order> orders = orderRepository.findByReservationId(reservationId);
        if (orders.isEmpty()) {
            throw new BadRequestException("Đặt bàn này không có món, không cần thanh toán cọc");
        }

        // Tính tổng đơn món từ orderDetails (vì total có thể chưa được tính)
        BigDecimal totalOrderAmount = BigDecimal.ZERO;
        for (Order order : orders) {
            if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.COMPLETED) {
                // Nếu order có total và > 0, dùng total
                if (order.getTotal() != null && order.getTotal().compareTo(BigDecimal.ZERO) > 0) {
                    totalOrderAmount = totalOrderAmount.add(order.getTotal());
                } else {
                    // Nếu không, tính từ orderDetails
                    if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                        BigDecimal orderSubtotal = order.getOrderDetails().stream()
                                .map(detail -> detail.getSubtotal() != null ? detail.getSubtotal() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
                        // Tính tax (10%)
                        BigDecimal tax = orderSubtotal.multiply(BigDecimal.valueOf(0.10));
                        // Tính total (subtotal + tax - discount)
                        BigDecimal orderTotal = orderSubtotal.add(tax)
                                .subtract(order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO);
                        totalOrderAmount = totalOrderAmount.add(orderTotal);
                    }
                }
            }
        }

        if (totalOrderAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Tổng đơn món không hợp lệ. Vui lòng kiểm tra lại đơn hàng.");
        }

        log.info("Reservation {} - Total order amount: {}, Deposit amount: {}", 
                reservationId, totalOrderAmount, depositAmount);

        // Tính 20% tổng đơn
        BigDecimal expectedDeposit = totalOrderAmount.multiply(BigDecimal.valueOf(0.20));

        // Kiểm tra số tiền cọc có đúng không (cho phép sai số nhỏ)
        BigDecimal difference = depositAmount.subtract(expectedDeposit).abs();
        if (difference.compareTo(BigDecimal.valueOf(0.01)) > 0) {
            throw new BadRequestException(
                    String.format("Số tiền cọc không đúng. Yêu cầu: %s VNĐ (20%% của %s VNĐ)",
                            formatCurrency(expectedDeposit), formatCurrency(totalOrderAmount)));
        }

        // Lưu số tiền cọc
        reservation.setDepositAmount(depositAmount);
        reservation = reservationRepository.save(reservation);

        log.info("Deposit paid for reservation ID: {}, amount: {}", reservationId, depositAmount);

        // Gửi email thông báo thanh toán cọc thành công
        try {
            emailService.sendDepositPaymentEmail(reservation, depositAmount, totalOrderAmount);
        } catch (Exception e) {
            log.error("Error sending deposit payment email for reservation ID: {}", reservationId, e);
            // Không throw exception để không ảnh hưởng đến flow chính
        }

        return mapToResponse(reservation);
    }

    /**
     * Tạo reservation với thanh toán cọc trong một transaction
     * Chỉ dùng khi có món ăn và cần thanh toán cọc
     */
    @Transactional
    public ReservationResponse createPublicReservationWithDeposit(
            com.example.backend_quanlynhahanglau.dto.reservation.PublicReservationWithDepositRequest request) {
        log.info("Creating public reservation with deposit for phone: {}", request.getCustomerPhone());
        
        // Tạo reservation request từ request có deposit
        PublicReservationRequest reservationRequest = PublicReservationRequest.builder()
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .customerEmail(request.getCustomerEmail())
                .reservationDateTime(request.getReservationDateTime())
                .numberOfGuests(request.getNumberOfGuests())
                .tableId(request.getTableId())
                .notes(request.getNotes())
                .userId(request.getUserId())
                .items(request.getItems())
                .build();
        
        // Tạo reservation và order
        ReservationResponse reservationResponse = createPublicReservation(reservationRequest);
        
        // Thanh toán cọc
        ReservationResponse finalResponse = payDeposit(reservationResponse.getId(), request.getDepositAmount());
        
        log.info("Public reservation with deposit created successfully with ID: {}", finalResponse.getId());
        
        return finalResponse;
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format("%,.0f", amount.doubleValue());
    }
}
