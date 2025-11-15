package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.reservation.PublicReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationResponse;
import com.example.backend_quanlynhahanglau.entity.*;
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

import java.time.LocalDateTime;
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
        // Kiểm tra User có tồn tại không
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Người dùng", "id", userId);
        }
        
        // Tìm tất cả Customer theo User ID (có thể có nhiều Customer cho 1 User)
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
        
        // Tạo biến final để sử dụng trong lambda
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
                    
                    // Liên kết với User nếu có userId
                    if (finalUserId != null) {
                        User user = userRepository.findById(finalUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", finalUserId));
                        customerBuilder.user(user);
                        log.info("Linking new customer with user {}", finalUserId);
                    }
                    
                    return customerRepository.save(customerBuilder.build());
                });

        // Nếu customer đã tồn tại nhưng chưa có user và có userId, cập nhật liên kết
        if (customer.getUser() == null && finalUserId != null) {
            User user = userRepository.findById(finalUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng", "id", finalUserId));
            customer.setUser(user);
            customer = customerRepository.save(customer);
            log.info("Linked existing customer {} with user {}", customer.getId(), finalUserId);
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
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .table(table)
                .reservationTime(request.getReservationDateTime())
                .numberOfGuests(request.getNumberOfGuests())
                .specialRequests(request.getNotes())
                .notes(request.getNotes())
                .status(ReservationStatus.CONFIRMED)
                .emailSent(false)
                .build();

        reservation = reservationRepository.save(reservation);
        log.info("Public reservation created successfully with ID: {}", reservation.getId());
        
        return mapToResponse(reservation);
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
        return mapToResponse(reservation);
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

        reservation = reservationRepository.save(reservation);
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

    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .customerName(reservation.getCustomer().getFullName())
                .customerPhone(reservation.getCustomer().getPhone())
                .customerId(reservation.getCustomer().getId())
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
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
