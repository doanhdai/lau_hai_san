package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.table.TableRequest;
import com.example.backend_quanlynhahanglau.dto.table.TableResponse;
import com.example.backend_quanlynhahanglau.entity.RestaurantTable;
import com.example.backend_quanlynhahanglau.enums.DiningFloor;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.enums.TableType;
import com.example.backend_quanlynhahanglau.exception.BadRequestException;
import com.example.backend_quanlynhahanglau.exception.DuplicateResourceException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.ReservationRepository;
import com.example.backend_quanlynhahanglau.repository.RestaurantTableRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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

        /* COMMENT: Tắt tính toán sức chứa tầng
        // Tính tổng số người đã đặt trong khoảng thời gian này cho mỗi tầng
        Map<DiningFloor, Integer> floorBookedGuests = new HashMap<>();
        for (DiningFloor floor : DiningFloor.values()) {
            Integer totalGuests = reservationRepository.sumGuestsByFloorAndTimeRange(
                    floor, startTime, endTime);
            floorBookedGuests.put(floor, totalGuests == null ? 0 : totalGuests);
        }
        */

        // Chỉ lấy các bàn có type ONLINE (có thể đặt online), active và chưa bị xóa
        List<RestaurantTable> allTables = tableRepository.findAll().stream()
                .filter(table -> table.getActive() 
                        && table.getType() == TableType.ONLINE
                        && table.getStatus() == TableStatus.AVAILABLE
                        && (table.getIsDeleted() == null || !table.getIsDeleted()))
                .collect(Collectors.toList());

        // Lọc các bàn không có conflict với reservation
        // Và kiểm tra sức chứa tầng thay vì sức chứa bàn
        List<TableResponse> result = allTables.stream()
                .filter(table -> {
                    /* COMMENT: Tắt kiểm tra floor null
                    // Kiểm tra floor null
                    if (table.getFloor() == null) {
                        return false;
                    }
                    */
                    
                    /* COMMENT: Tắt kiểm tra sức chứa tầng
                    // Kiểm tra sức chứa tầng: tính số người đã đặt trong tầng này
                    if (table.getFloor() != null) {
                    Integer bookedGuests = floorBookedGuests.get(table.getFloor());
                    if (bookedGuests == null) {
                        bookedGuests = 0;
                    }
                    
                    // Tính sức chứa còn lại của tầng
                        int floorCapacityLimit = table.getFloor().getCapacityLimit();
                        int remainingCapacity = floorCapacityLimit - bookedGuests;
                    
                    // Kiểm tra xem tầng này còn đủ chỗ cho số người đặt không
                    if (numberOfGuests != null && remainingCapacity < numberOfGuests) {
                        return false;
                    }
                    }
                    */
                    
                    // Kiểm tra xem bàn này có reservation nào trong khoảng thời gian ±3h không
                    List<com.example.backend_quanlynhahanglau.entity.Reservation> conflicts = 
                            reservationRepository.findConflictingReservationsByTable(
                                    table.getId(), startTime, endTime);
                    
                    // Nếu có conflict, loại bỏ bàn này
                    return conflicts.isEmpty();
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return result;
    }

    @Transactional
    public TableResponse createTable(TableRequest request) {
        Optional<RestaurantTable> existingTableOpt = tableRepository.findByTableNumber(request.getTableNumber());

        if (existingTableOpt.isPresent() && (existingTableOpt.get().getIsDeleted() == null || !existingTableOpt.get().getIsDeleted())) {
            throw new DuplicateResourceException("Bàn", "số bàn", request.getTableNumber());
        }

        if (existingTableOpt.isPresent() && Boolean.TRUE.equals(existingTableOpt.get().getIsDeleted())) {
            RestaurantTable restoredTable = restoreDeletedTable(existingTableOpt.get(), request);
            return mapToResponse(restoredTable);
        }

        validateFloorCapacity(request.getFloor(), request.getCapacity(), null);

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : TableStatus.AVAILABLE)
                .type(request.getType() != null ? request.getType() : TableType.OFFLINE)
                .floor(request.getFloor() != null ? request.getFloor() : DiningFloor.FLOOR_1)
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
                && tableRepository.existsByTableNumberAndIsDeletedFalse(request.getTableNumber())) {
            throw new DuplicateResourceException("Bàn", "số bàn", request.getTableNumber());
        }

        validateFloorCapacity(request.getFloor(), request.getCapacity(), table.getId());

        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        if (request.getType() != null) {
            table.setType(request.getType());
        }
        table.setFloor(request.getFloor() != null ? request.getFloor() : DiningFloor.FLOOR_1);
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
        // Điều kiện 1: Kiểm tra numberOfGuests
        if (numberOfGuests == null || numberOfGuests <= 0) {
            return false;
        }

        // Load tất cả bàn ONLINE một lần để tối ưu hiệu năng
        List<RestaurantTable> onlineTables = tableRepository.findAll().stream()
                .filter(table -> table.getActive()
                        && table.getType() == TableType.ONLINE
                        && (table.getIsDeleted() == null || !table.getIsDeleted()))
                .collect(Collectors.toList());

        // Kiểm tra sức chứa còn lại của từng tầng
        // Thay vì kiểm tra từng bàn, kiểm tra xem tầng nào còn đủ chỗ cho số người đặt
        /* COMMENT: Tắt kiểm tra sức chứa tầng
        for (DiningFloor floor : DiningFloor.values()) {
            // Tính tổng số người đã đặt trong khoảng thời gian này cho tầng này
            Integer totalGuestsBooked = reservationRepository.sumGuestsByFloorAndTimeRange(
                    floor, startTime, endTime);
            
            if (totalGuestsBooked == null) {
                totalGuestsBooked = 0;
            }
            
            // Kiểm tra xem tầng này còn đủ chỗ không
            // Lấy sức chứa tối đa của tầng từ enum
            int floorCapacityLimit = floor.getCapacityLimit();
            int remainingCapacity = floorCapacityLimit - totalGuestsBooked;
            
            // Điều kiện 2: Kiểm tra sức chứa còn lại
            if (remainingCapacity >= numberOfGuests) {
                // Điều kiện 3: Kiểm tra có bàn ONLINE trong tầng
                boolean hasOnlineTable = onlineTables.stream()
                        .anyMatch(table -> table.getFloor() == floor);
                
                if (hasOnlineTable) {
                    return true; // Có tầng còn đủ chỗ và có bàn ONLINE
                }
            }
        }
        */
        
        // Chỉ kiểm tra xem có bàn ONLINE nào không (bỏ qua kiểm tra sức chứa tầng)
        boolean hasAnyOnlineTable = !onlineTables.isEmpty();
        if (hasAnyOnlineTable) {
            return true;
        }
        
        // Không có tầng nào còn đủ chỗ
        return false;
    }

    private TableResponse mapToResponse(RestaurantTable table) {
        return TableResponse.builder()
                .id(table.getId())
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .type(table.getType())
                .floor(table.getFloor() != null ? table.getFloor() : DiningFloor.FLOOR_1)
                .location(table.getLocation())
                .active(table.getActive())
                .notes(table.getNotes())
                .positionX(table.getPositionX())
                .positionY(table.getPositionY())
                .build();
    }

    private void validateFloorCapacity(DiningFloor floor, Integer tableCapacity, Long excludeId) {
        DiningFloor targetFloor = floor != null ? floor : DiningFloor.FLOOR_1;
        int capacity = tableCapacity != null ? tableCapacity : 0;
        Integer sum = excludeId == null
                ? tableRepository.sumCapacityByFloor(targetFloor)
                : tableRepository.sumCapacityByFloorExcludingId(targetFloor, excludeId);
        int currentSum = sum != null ? sum : 0;

        // Lấy sức chứa tối đa của tầng từ enum
        int floorCapacityLimit = targetFloor.getCapacityLimit();
        int total = currentSum + capacity;
        if (total > floorCapacityLimit) {
            throw new BadRequestException(
                    String.format("%s chỉ chứa tối đa %d người. Số chỗ còn lại: %d",
                            targetFloor.getDisplayName(),
                            floorCapacityLimit,
                            Math.max(0, floorCapacityLimit - currentSum))
            );
        }
    }

    private RestaurantTable restoreDeletedTable(RestaurantTable table, TableRequest request) {
        validateFloorCapacity(request.getFloor(), request.getCapacity(), table.getId());

        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus() != null ? request.getStatus() : TableStatus.AVAILABLE);
        if (request.getType() != null) {
            table.setType(request.getType());
        } else {
            table.setType(TableType.OFFLINE);
        }
        table.setFloor(request.getFloor() != null ? request.getFloor() : DiningFloor.FLOOR_1);
        table.setLocation(request.getLocation());
        table.setNotes(request.getNotes());
        table.setPositionX(request.getPositionX());
        table.setPositionY(request.getPositionY());
        table.setActive(true);
        table.setIsDeleted(false);

        return tableRepository.save(table);
    }
}
