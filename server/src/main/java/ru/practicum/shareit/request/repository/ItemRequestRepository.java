package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.ItemRequest;
import org.springframework.data.domain.Pageable;


import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requestorId != :userId ORDER BY ir.created DESC")
    List<ItemRequest> findAllExceptUserRequests(@Param("userId") Long userId, Pageable pageable);
}
