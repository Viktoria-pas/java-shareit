package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status);
    List<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(
            Long userId, Long itemId, BookingStatus bookingStatus, LocalDateTime time);

    Optional<Booking> findFirstByItemIdAndStartBeforeAndStatusOrderByStartDesc(
            Long itemId, LocalDateTime now, BookingStatus status);

    Optional<Booking> findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
            Long itemId, LocalDateTime now, BookingStatus status);

    boolean existsByBookerIdAndItemIdAndEndBefore(
            Long bookerId, Long itemId, LocalDateTime now);
}
