package com.example.backend_quanlynhahanglau.service;

import com.example.backend_quanlynhahanglau.dto.room.RoomRequest;
import com.example.backend_quanlynhahanglau.dto.room.RoomResponse;
import com.example.backend_quanlynhahanglau.entity.Room;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import com.example.backend_quanlynhahanglau.exception.DuplicateResourceException;
import com.example.backend_quanlynhahanglau.exception.ResourceNotFoundException;
import com.example.backend_quanlynhahanglau.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", id));
        return mapToResponse(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getAvailableRooms() {
        return roomRepository.findByStatus(TableStatus.AVAILABLE).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new DuplicateResourceException("Phòng", "số phòng", request.getRoomNumber());
        }

        Room room = Room.builder()
                .roomNumber(request.getRoomNumber())
                .name(request.getName())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : TableStatus.AVAILABLE)
                .description(request.getDescription())
                .active(true)
                .build();

        room = roomRepository.save(room);
        return mapToResponse(room);
    }

    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", id));

        if (!room.getRoomNumber().equals(request.getRoomNumber()) 
                && roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new DuplicateResourceException("Phòng", "số phòng", request.getRoomNumber());
        }

        room.setRoomNumber(request.getRoomNumber());
        room.setName(request.getName());
        room.setCapacity(request.getCapacity());
        room.setStatus(request.getStatus());
        room.setDescription(request.getDescription());

        room = roomRepository.save(room);
        return mapToResponse(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", id));
        roomRepository.delete(room);
    }

    @Transactional
    public void updateRoomStatus(Long id, TableStatus status) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Phòng", "id", id));
        room.setStatus(status);
        roomRepository.save(room);
    }

    private RoomResponse mapToResponse(Room room) {
        return RoomResponse.builder()
                .id(room.getId())
                .roomNumber(room.getRoomNumber())
                .name(room.getName())
                .capacity(room.getCapacity())
                .status(room.getStatus())
                .description(room.getDescription())
                .active(room.getActive())
                .build();
    }
}
