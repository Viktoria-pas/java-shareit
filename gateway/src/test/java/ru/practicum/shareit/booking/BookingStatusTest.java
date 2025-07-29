package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BookingStatusTest {

    @Test
    void from_ValidUpperCaseStatus_ShouldReturnCorrectStatus() {

        String status = "WAITING";

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void from_ValidLowerCaseStatus_ShouldReturnCorrectStatus() {

        String status = "approved";

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void from_ValidMixedCaseStatus_ShouldReturnCorrectStatus() {

        String status = "ReJeCTeD";

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(BookingStatus.REJECTED);
    }

    @ParameterizedTest
    @EnumSource(BookingStatus.class)
    void from_AllValidStatuses_ShouldReturnCorrectStatus(BookingStatus expectedStatus) {

        String status = expectedStatus.name();

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expectedStatus);
    }

    @ParameterizedTest
    @ValueSource(strings = {"waiting", "WAITING", "Waiting", "wAiTiNg"})
    void from_WaitingInDifferentCases_ShouldReturnWaiting(String status) {

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(BookingStatus.WAITING);
    }

    @ParameterizedTest
    @ValueSource(strings = {"approved", "APPROVED", "Approved", "aPpRoVeD"})
    void from_ApprovedInDifferentCases_ShouldReturnApproved(String status) {

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(BookingStatus.APPROVED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"rejected", "REJECTED", "Rejected", "rEjEcTeD"})
    void from_RejectedInDifferentCases_ShouldReturnRejected(String status) {

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(BookingStatus.REJECTED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"canceled", "CANCELED", "Canceled", "cAnCeLeD"})
    void from_CanceledInDifferentCases_ShouldReturnCanceled(String status) {

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(BookingStatus.CANCELED);
    }

    @Test
    void from_InvalidStatus_ShouldReturnEmpty() {

        String status = "INVALID_STATUS";

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isEmpty();
    }

    @Test
    void from_NullStatus_ShouldReturnEmpty() {

        String status = null;

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isEmpty();
    }

    @Test
    void from_EmptyStatus_ShouldReturnEmpty() {

        String status = "";

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isEmpty();
    }

    @Test
    void from_WhitespaceStatus_ShouldReturnEmpty() {

        String status = "   ";

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING", "CONFIRMED", "COMPLETED", "UNKNOWN", "123", "waiting123", "approved_status"})
    void from_InvalidStatuses_ShouldReturnEmpty(String status) {

        Optional<BookingStatus> result = BookingStatus.from(status);

        assertThat(result).isEmpty();
    }

    @Test
    void enumValues_ShouldContainAllExpectedStatuses() {

        BookingStatus[] statuses = BookingStatus.values();

        assertThat(statuses).containsExactlyInAnyOrder(
                BookingStatus.WAITING,
                BookingStatus.APPROVED,
                BookingStatus.REJECTED,
                BookingStatus.CANCELED
        );
    }

    @Test
    void enumNames_ShouldBeCorrect() {

        assertThat(BookingStatus.WAITING.name()).isEqualTo("WAITING");
        assertThat(BookingStatus.APPROVED.name()).isEqualTo("APPROVED");
        assertThat(BookingStatus.REJECTED.name()).isEqualTo("REJECTED");
        assertThat(BookingStatus.CANCELED.name()).isEqualTo("CANCELED");
    }

    @Test
    void valueOf_ShouldWorkForValidStatuses() {
        assertThat(BookingStatus.valueOf("WAITING")).isEqualTo(BookingStatus.WAITING);
        assertThat(BookingStatus.valueOf("APPROVED")).isEqualTo(BookingStatus.APPROVED);
        assertThat(BookingStatus.valueOf("REJECTED")).isEqualTo(BookingStatus.REJECTED);
        assertThat(BookingStatus.valueOf("CANCELED")).isEqualTo(BookingStatus.CANCELED);
    }
}
