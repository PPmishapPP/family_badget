package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.repository.SubmitMeterReadsRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubmitMeterReadsReminderService {

    private final SubmitMeterReadsRepository submitMeterReadsRepository;

    private static final LocalTime EIGHT_BM = LocalTime.of(8, 0);
    private static final LocalTime TEN_AM = LocalTime.of(22, 0);

    private static final String DEMAND_TO_SEND_METER_READS = "Необходимо сдать показания счетчиков";

    public Map<Long, String> getChatIdWithReminderMessages() {
        Map<Long, String> map = new HashMap<>();

        LocalTime timeNow = LocalTime.now();
        if (timeNow.isAfter(EIGHT_BM) && timeNow.isBefore(TEN_AM)) {
            submitMeterReadsRepository.findAll().forEach(meterRead -> {
                LocalDateTime dbMeterReadDatetime = meterRead.getDatetimeExpected();

                Long chatId = meterRead.getChatId();

                LocalDateTime localDatetime = LocalDateTime.now();
                if (localDatetime.isAfter(dbMeterReadDatetime)) {
                    map.put(chatId, DEMAND_TO_SEND_METER_READS);
                }
            });
        }

        return map;
    }
}
