package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.table.TableRequest;
import com.example.backend_quanlynhahanglau.dto.table.TableResponse;
import com.example.backend_quanlynhahanglau.entity.RestaurantTable;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.exception.DuplicateResourceException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.ReservationRepository;
import com.example.backend_quanlynhahanglau.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {
    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;

    @Transactional(readOnly = true)
    public List<TableResponse> getAllTables() {
        return tableRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TableResponse getTableById(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));
        return mapToResponse(table);
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getAvailableTables() {
        return tableRepository.findByStatus(TableStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getSuitableTables(Integer numberOfGuests) {
        return tableRepository.findSuitableTable(numberOfGuests).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách bàn khả dụng theo thời gian, loại bỏ các bàn đã có reservation trong khoảng ±3 giờ
     * Ví dụ: Nếu bàn 1 đã được đặt lúc 7h, thì từ 4h01 đến 9h59 bàn 1 sẽ không được hiển thị
     * @param reservationTime Thời gian đặt bàn
     * @param numberOfGuests Số lượng khách (optional, để lọc bàn phù hợp)
     * @return Danh sách bàn khả dụng
     */
    @Transactional(readOnly = true)
    public List<TableResponse> getAvailableTablesByTime(LocalDateTime reservationTime, Integer numberOfGuests) {
        // Tính khoảng thời gian kiểm tra: ±3 giờ từ thời gian đặt bàn
        // Ví dụ: đặt lúc 7h thì kiểm tra từ 4h01 đến 9h59
        LocalDateTime startTime = reservationTime.minusHours(3).plusMinutes(1);
        LocalDateTime endTime = reservationTime.plusHours(3).minusMinutes(1);

        // Lấy tất cả bàn có trạng thái AVAILABLE và active
        List<RestaurantTable> allTables = tableRepository.findAll().stream()
                .filter(table -> table.getActive() && table.getStatus() == TableStatus.AVAILABLE)
                .collect(Collectors.toList());

        // Lọc các bàn không có conflict với reservation và phù hợp với số lượng khách
        return allTables.stream()
                .filter(table -> {
                    // Kiểm tra số lượng khách nếu có
                    if (numberOfGuests != null && table.getCapacity() < numberOfGuests) {
                        return false;
                    }
                    
                    // Kiểm tra xem bàn này có reservation nào trong khoảng thời gian ±3h không
                    List<com.example.backend_quanlynhahanglau.entity.Reservation> conflicts = 
                            reservationRepository.findConflictingReservationsByTable(
                                    table.getId(), startTime, endTime);
                    
                    // Nếu có conflict, loại bỏ bàn này
                    return conflicts.isEmpty();
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TableResponse createTable(TableRequest request) {
        if (tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new DuplicateResourceException("Bàn", "số bàn", request.getTableNumber());
        }

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : TableStatus.AVAILABLE)
                .location(request.getLocation())
                .notes(request.getNotes())
                .active(true)
                .build();

        table = tableRepository.save(table);
        return mapToResponse(table);
    }

    @Transactional
    public TableResponse updateTable(Long id, TableRequest request) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));

        if (!table.getTableNumber().equals(request.getTableNumber()) 
                && tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new DuplicateResourceException("Bàn", "số bàn", request.getTableNumber());
        }

        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        table.setLocation(request.getLocation());
        table.setNotes(request.getNotes());

        table = tableRepository.save(table);
        return mapToResponse(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));
        tableRepository.delete(table);
    }

    @Transactional
    public void updateTableStatus(Long id, TableStatus status) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));
        table.setStatus(status);
        tableRepository.save(table);
    }

    private TableResponse mapToResponse(RestaurantTable table) {
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .location(table.getLocation())
                .active(table.getActive())
                .notes(table.getNotes())
                .build();
    }
}
