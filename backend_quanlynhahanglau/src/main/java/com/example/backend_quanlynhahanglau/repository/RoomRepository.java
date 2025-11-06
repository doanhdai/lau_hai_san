package com.example.backend_quanlynhahanglau.repository;

import com.example.backend_quanlynhahanglau.entity.Room;
import com.example.backend_quanlynhahanglau.enums.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByRoomNumber(String roomNumber);
    List<Room> findByStatus(TableStatus status);
    List<Room> findByActiveTrue();
    Boolean existsByRoomNumber(String roomNumber);
}
