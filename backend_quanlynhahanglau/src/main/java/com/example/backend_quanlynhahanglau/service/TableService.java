package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.table.TableRequest;
import com.example.backend_quanlynhahanglau.dto.table.TableResponse;
import com.example.backend_quanlynhahanglau.entity.RestaurantTable;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.enums.TableType;
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
                .filter(table -> table.getIsDeleted() == null || !table.getIsDeleted())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TableResponse getTableById(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));
        
        // Kiểm tra bàn đã bị xóa mềm chưa
        if (table.getIsDeleted() != null && table.getIsDeleted()) {
            throw new ResourceNotFoundException("Bàn", "id", id);
        }
        
        return mapToResponse(table);
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getAvailableTables() {
        return tableRepository.findByStatus(TableStatus.AVAILABLE).stream()
                .filter(table -> table.getIsDeleted() == null || !table.getIsDeleted())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TableResponse> getSuitableTables(Integer numberOfGuests) {
        return tableRepository.findSuitableTable(numberOfGuests).stream()
                .filter(table -> table.getIsDeleted() == null || !table.getIsDeleted())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<TableResponse> getAvailableTablesByTime(LocalDateTime reservationTime, Integer numberOfGuests) {
        // Tính khoảng thời gian kiểm tra: ±3 giờ từ thời gian đặt bàn
        LocalDateTime startTime = reservationTime.minusHours(3).plusMinutes(1);
        LocalDateTime endTime = reservationTime.plusHours(3).minusMinutes(1);

        // Chỉ lấy các bàn có type ONLINE (có thể đặt online), active và chưa bị xóa
        List<RestaurantTable> allTables = tableRepository.findAll().stream()
                .filter(table -> table.getActive() 
                        && table.getType() == TableType.ONLINE
                        && (table.getIsDeleted() == null || !table.getIsDeleted()))
                .collect(Collectors.toList());

        // Lọc các bàn không có conflict với reservation và phù hợp với số lượng khách
        return allTables.stream()
                .filter(table -> {
                    // Kiểm tra số lượng khách nếu có
                    if (numberOfGuests != null && table.getCapacity() < numberOfGuests) {
                        return false;
                    }
                    
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
                .type(request.getType() != null ? request.getType() : TableType.OFFLINE)
                .location(request.getLocation())
                .notes(request.getNotes())
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .active(true)
                .isDeleted(false)
                .build();

        table = tableRepository.save(table);
        return mapToResponse(table);
    }

    @Transactional
    public TableResponse updateTable(Long id, TableRequest request) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));

        // Kiểm tra bàn đã bị xóa mềm chưa
        if (table.getIsDeleted() != null && table.getIsDeleted()) {
            throw new ResourceNotFoundException("Bàn", "id", id);
        }

        if (!table.getTableNumber().equals(request.getTableNumber()) 
                && tableRepository.existsByTableNumber(request.getTableNumber())) {
            throw new DuplicateResourceException("Bàn", "số bàn", request.getTableNumber());
        }

        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        if (request.getType() != null) {
            table.setType(request.getType());
        }
        table.setLocation(request.getLocation());
        table.setNotes(request.getNotes());
        table.setPositionX(request.getPositionX());
        table.setPositionY(request.getPositionY());

        table = tableRepository.save(table);
        return mapToResponse(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));
        
        // Kiểm tra bàn đã bị xóa mềm chưa
        if (table.getIsDeleted() != null && table.getIsDeleted()) {
            throw new ResourceNotFoundException("Bàn", "id", id);
        }
        
        // Xóa mềm: set is_deleted = true
        table.setIsDeleted(true);
        tableRepository.save(table);
    }

    @Transactional
    public void updateTableStatus(Long id, TableStatus status) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));
        
        // Kiểm tra bàn đã bị xóa mềm chưa
        if (table.getIsDeleted() != null && table.getIsDeleted()) {
            throw new ResourceNotFoundException("Bàn", "id", id);
        }
        
        table.setStatus(status);
        tableRepository.save(table);
    }

    @Transactional
    public TableResponse updateTablePosition(Long id, Integer positionX, Integer positionY) {
        RestaurantTable table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bàn", "id", id));
        
        // Kiểm tra bàn đã bị xóa mềm chưa
        if (table.getIsDeleted() != null && table.getIsDeleted()) {
            throw new ResourceNotFoundException("Bàn", "id", id);
        }
        
        table.setPositionX(positionX);
        table.setPositionY(positionY);
        table = tableRepository.save(table);
        return mapToResponse(table);
    }

    @Transactional(readOnly = true)
    public Boolean checkTableAvailability(LocalDateTime reservationTime, Integer numberOfGuests) {
        // Tính khoảng thời gian kiểm tra: ±3 giờ từ thời gian đặt bàn
        LocalDateTime startTime = reservationTime.minusHours(3).plusMinutes(1);
        LocalDateTime endTime = reservationTime.plusHours(3).minusMinutes(1);

        // Chỉ kiểm tra các bàn có type ONLINE (có thể đặt online), active và chưa bị xóa
        List<RestaurantTable> allTables = tableRepository.findAll().stream()
                .filter(table -> table.getActive() 
                        && table.getType() == TableType.ONLINE
                        && (table.getIsDeleted() == null || !table.getIsDeleted()))
                .collect(Collectors.toList());

        // Kiểm tra xem có ít nhất 1 bàn khả dụng không
        return allTables.stream()
                .anyMatch(table -> {
                    // Kiểm tra số lượng khách nếu có
                    if (numberOfGuests != null && table.getCapacity() < numberOfGuests) {
                        return false;
                    }
                    
                    // Kiểm tra xem bàn này có reservation nào trong khoảng thời gian ±3h không
                    List<com.example.backend_quanlynhahanglau.entity.Reservation> conflicts = 
                            reservationRepository.findConflictingReservationsByTable(
                                    table.getId(), startTime, endTime);
                    
                    // Trả về true nếu không có conflict
                    return conflicts.isEmpty();
                });
    }

    private TableResponse mapToResponse(RestaurantTable table) {
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .type(table.getType())
                .location(table.getLocation())
                .active(table.getActive())
                .notes(table.getNotes())
                .positionX(table.getPositionX())
                .positionY(table.getPositionY())
                .build();
    }
}
