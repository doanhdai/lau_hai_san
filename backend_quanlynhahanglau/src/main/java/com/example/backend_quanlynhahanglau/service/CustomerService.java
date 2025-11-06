package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.customer.CustomerRequest;
import com.example.backend_quanlynhahanglau.dto.customer.CustomerResponse;
import com.example.backend_quanlynhahanglau.dto.reservation.ReservationResponse;
import com.example.backend_quanlynhahanglau.entity.Customer;
import com.example.backend_quanlynhahanglau.entity.Reservation;
import com.example.backend_quanlynhahanglau.exception.DuplicateResourceException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.CustomerRepository;
import com.example.backend_quanlynhahanglau.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", id));
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request) {
        if (customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Khách hàng", "số điện thoại", request.getPhone());
        }

        if (request.getEmail() != null && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Khách hàng", "email", request.getEmail());
        }

        Customer customer = Customer.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .address(request.getAddress())
                .isVip(request.getIsVip() != null ? request.getIsVip() : false)
                .notes(request.getNotes())
                .active(true)
                .blocked(false)
                .build();

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", id));

        // Kiểm tra duplicate phone (nếu khác phone hiện tại)
        if (!customer.getPhone().equals(request.getPhone()) 
                && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Khách hàng", "số điện thoại", request.getPhone());
        }

        // Kiểm tra duplicate email (nếu khác email hiện tại)
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())
                && customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Khách hàng", "email", request.getEmail());
        }

        customer.setFullName(request.getFullName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setAddress(request.getAddress());
        customer.setIsVip(request.getIsVip());
        customer.setNotes(request.getNotes());

        customer = customerRepository.save(customer);
        return mapToResponse(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", id));
        customerRepository.delete(customer);
    }

    @Transactional
    public void blockCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", id));
        customer.setBlocked(true);
        customerRepository.save(customer);
    }

    @Transactional
    public void unblockCustomer(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", id));
        customer.setBlocked(false);
        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getVipCustomers() {
        return customerRepository.findByIsVipTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getCustomerReservations(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", customerId));
        
        return reservationRepository.findByCustomerIdOrderByReservationTimeDesc(customerId).stream()
                .map(this::mapReservationToResponse)
                .collect(Collectors.toList());
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .phone(customer.getPhone())
                .email(customer.getEmail())
                .address(customer.getAddress())
                .isVip(customer.getIsVip())
                .active(customer.getActive())
                .blocked(customer.getBlocked())
                .notes(customer.getNotes())
                .createdAt(customer.getCreatedAt())
                .build();
    }

    private ReservationResponse mapReservationToResponse(Reservation reservation) {
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
