package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.feedback.FeedbackRequest;
import com.example.backend_quanlynhahanglau.dto.feedback.FeedbackResponse;
import com.example.backend_quanlynhahanglau.entity.Customer;
import com.example.backend_quanlynhahanglau.entity.CustomerFeedback;
import com.example.backend_quanlynhahanglau.entity.Order;
import com.example.backend_quanlynhahanglau.entity.User;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.CustomerFeedbackRepository;
import com.example.backend_quanlynhahanglau.repository.CustomerRepository;
import com.example.backend_quanlynhahanglau.repository.OrderRepository;
import com.example.backend_quanlynhahanglau.repository.UserRepository;
import com.example.backend_quanlynhahanglau.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerFeedbackService {
    private final CustomerFeedbackRepository feedbackRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getUnresolvedFeedbacks() {
        return feedbackRepository.findByResolvedFalseOrderByCreatedAtDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FeedbackResponse getFeedbackById(Long id) {
        CustomerFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", "id", id));
        return mapToResponse(feedback);
    }

    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Khách hàng", "id", request.getCustomerId()));

        CustomerFeedback feedback = CustomerFeedback.builder()
                .customer(customer)
                .rating(request.getRating())
                .comment(request.getComment())
                .resolved(false)
                .build();

        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng", "id", request.getOrderId()));
            feedback.setOrder(order);
        }

        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    @Transactional
    public FeedbackResponse respondToFeedback(Long id, String response) {
        CustomerFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", "id", id));

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        feedback.setResponse(response);
        feedback.setResolved(true);
        feedback.setResolvedBy(user);
        feedback.setResolvedAt(LocalDateTime.now());

        feedback = feedbackRepository.save(feedback);
        return mapToResponse(feedback);
    }

    @Transactional
    public void deleteFeedback(Long id) {
        CustomerFeedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phản hồi", "id", id));
        feedbackRepository.delete(feedback);
    }

    @Transactional(readOnly = true)
    public Double getAverageRating() {
        Double rating = feedbackRepository.calculateAverageRating();
        return rating != null ? rating : 0.0;
    }

    private FeedbackResponse mapToResponse(CustomerFeedback feedback) {
        return FeedbackResponse.builder()
                .id(feedback.getId())
                .customerName(feedback.getCustomer().getFullName())
                .customerId(feedback.getCustomer().getId())
                .orderNumber(feedback.getOrder() != null ? feedback.getOrder().getOrderNumber() : null)
                .orderId(feedback.getOrder() != null ? feedback.getOrder().getId() : null)
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .resolved(feedback.getResolved())
                .resolvedByName(feedback.getResolvedBy() != null ? feedback.getResolvedBy().getFullName() : null)
                .response(feedback.getResponse())
                .createdAt(feedback.getCreatedAt())
                .resolvedAt(feedback.getResolvedAt())
                .build();
    }
}
