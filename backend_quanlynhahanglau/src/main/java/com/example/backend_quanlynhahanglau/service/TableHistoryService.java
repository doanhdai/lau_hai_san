package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.history.TableHistoryResponse;
import com.example.backend_quanlynhahanglau.entity.TableHistory;
import com.example.backend_quanlynhahanglau.repository.TableHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableHistoryService {
    private final TableHistoryRepository historyRepository;

    @Transactional(readOnly = true)
    public List<TableHistoryResponse> getAllHistory() {
        return historyRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TableHistoryResponse> getHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return historyRepository.findByDateRange(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TableHistoryResponse> getHistoryByTableId(Long tableId) {
        return historyRepository.findAll().stream()
                .filter(h -> h.getTable() != null && h.getTable().getId().equals(tableId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private TableHistoryResponse mapToResponse(TableHistory history) {
        return TableHistoryResponse.builder()
                .id(history.getId())
                .tableNumber(history.getTable() != null ? history.getTable().getTableNumber() : null)
                .tableId(history.getTable() != null ? history.getTable().getId() : null)
                .roomName(history.getRoom() != null ? history.getRoom().getName() : null)
                .roomId(history.getRoom() != null ? history.getRoom().getId() : null)
                .customerName(history.getCustomer() != null ? history.getCustomer().getFullName() : null)
                .customerId(history.getCustomer() != null ? history.getCustomer().getId() : null)
                .orderNumber(history.getOrder() != null ? history.getOrder().getOrderNumber() : null)
                .orderId(history.getOrder() != null ? history.getOrder().getId() : null)
                .status(history.getStatus())
                .startTime(history.getStartTime())
                .endTime(history.getEndTime())
                .notes(history.getNotes())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
