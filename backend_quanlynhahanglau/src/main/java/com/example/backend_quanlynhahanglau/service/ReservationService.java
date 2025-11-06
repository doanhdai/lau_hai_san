package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.reservation.PublicReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationRequest;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationResponse;
import com.example.backend_quanlynhahanglau.entity.*;
import com.example.backend_quanlynhahanglau.enums.ReservationStatus;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.exception.BadRequestException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.*;
import com.example.backend_quanlynhahanglau.security.UserDetailsImpl;
import com.example.backend_quanlynhahanglau.util.CodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public ReservationResponse createPublicReservation(PublicReservationRequest request) {
        log.info("Creating public reservation for phone: {}", request.getCustomerPhone());
        
        // Tìm hoặc tạo customer
        Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                .orElseGet(() -> {
                    log.info("Creating new customer for phone: {}", request.getCustomerPhone());
                    String customerCode = generateUniqueCustomerCode();
                    Customer newCustomer = Customer.builder()
                            .customerCode(customerCode)
                            .fullName(request.getCustomerName())
                            .phone(request.getCustomerPhone())
                            .email(request.getCustomerEmail())
                            .isVip(false)
                            .active(true)
                            .blocked(false)
                            .build();
                    return customerRepository.save(newCustomer);
                });

        if (customer.getBlocked()) {
            throw new BadRequestException("Khách hàng này đã bị chặn");
        }

        // Kiểm tra bàn bắt buộc phải có
        if (request.getTableId() == null) {
            throw new BadRequestException("Vui lòng chọn bàn");
        }

        RestaurantTable table = tableRepository.findById(request.getTableId())
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", request.getTableId()));
        
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

        // Tạo reservation
        Reservation reservation = Reservation.builder()
                .customer(customer)
                .table(table)
                .reservationTime(request.getReservationDateTime())
                .numberOfGuests(request.getNumberOfGuests())
                .specialRequests(request.getNotes())
                .notes(request.getNotes())
                .status(ReservationStatus.PENDING)
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

        // Parse status if provided, otherwise use PENDING
        ReservationStatus status = ReservationStatus.PENDING;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = ReservationStatus.valueOf(request.getStatus());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status provided: {}, using PENDING", request.getStatus());
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

        // Bắt buộc phải có tableId hoặc roomId (ít nhất một trong hai)
        if (request.getTableId() == null && request.getRoomId() == null) {
            throw new BadRequestException("Vui lòng chọn bàn hoặc phòng");
        }

        if (request.getTableId() != null) {
            RestaurantTable table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", request.getTableId()));
            
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

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", request.getRoomId()));
            
            if (room.getStatus() != TableStatus.AVAILABLE) {
                throw new BadRequestException("Phòng không khả dụng. Trạng thái hiện tại: " + room.getStatus());
            }
            reservation.setRoom(room);
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
            reservation.setTable(table);
        } else {
            reservation.setTable(null);
        }

        if (request.getRoomId() != null) {
            Room room = roomRepository.findById(request.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", request.getRoomId()));
            reservation.setRoom(room);
        } else {
            reservation.setRoom(null);
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

        // Cập nhật trạng thái bàn/phòng
        if (reservation.getTable() != null) {
            reservation.getTable().setStatus(TableStatus.RESERVED);
            tableRepository.save(reservation.getTable());
        }

        if (reservation.getRoom() != null) {
            reservation.getRoom().setStatus(TableStatus.RESERVED);
            roomRepository.save(reservation.getRoom());
        }

        reservation = reservationRepository.save(reservation);
        return mapToResponse(reservation);
    }

    @Transactional
    public ReservationResponse cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Đặt bàn", "id", id));

        reservation.setStatus(ReservationStatus.CANCELLED);

        // Cập nhật lại trạng thái bàn/phòng
        if (reservation.getTable() != null && reservation.getTable().getStatus() == TableStatus.RESERVED) {
            reservation.getTable().setStatus(TableStatus.AVAILABLE);
            tableRepository.save(reservation.getTable());
        }

        if (reservation.getRoom() != null && reservation.getRoom().getStatus() == TableStatus.RESERVED) {
            reservation.getRoom().setStatus(TableStatus.AVAILABLE);
            roomRepository.save(reservation.getRoom());
        }

        reservation = reservationRepository.save(reservation);
        return mapToResponse(reservation);
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
                .roomName(reservation.getRoom() != null ? reservation.getRoom().getName() : null)
                .roomId(reservation.getRoom() != null ? reservation.getRoom().getId() : null)
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
