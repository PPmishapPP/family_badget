package ru.mishapp.repository;

import org.springframework.data.repository.CrudRepository;
import ru.mishapp.entity.SubmitMeterReads;

public interface SubmitMeterReadsRepository extends CrudRepository<SubmitMeterReads, Long> {

    SubmitMeterReads readByChatId(Long chatId);
}
