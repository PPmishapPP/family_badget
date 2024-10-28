package ru.mishapp.entity;

import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("submit_meter_reads")
@Getter
@Builder(toBuilder = true)
public class SubmitMeterReads {

    @Id
    private final Long id;

    @Nonnull
    private final Long chatId;

    @Nonnull
    private final LocalDateTime datetimeExpected;

    @PersistenceCreator
    public SubmitMeterReads(
            Long id,
            Long chatId,
            LocalDateTime datetimeExpected
    ) {
        this.id = id;
        this.chatId = chatId;
        this.datetimeExpected = datetimeExpected;
    }

}
