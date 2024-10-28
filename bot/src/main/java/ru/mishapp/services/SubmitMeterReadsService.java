package ru.mishapp.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.entity.SubmitMeterReads;
import ru.mishapp.repository.SubmitMeterReadsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SubmitMeterReadsService {

    private final SubmitMeterReadsRepository submitMeterReadsRepository;

    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public static final String MESSAGE_METERS_SUBMITTED = "Показания сданы." +
            "\nВремя сдачи показаний перенесено на %s";
    public static final String MESSAGE_NUMBER_NOT_FOUND_IN_STRING = "Число не найдено в строке.";

    @Value("${datetime.day}")
    public int dayForReschedule;

    @Value("${datetime.hour}")
    public int hourForReschedule;

    @Value("${datetime.minute}")
    public int minuteForReschedule;

    @Transactional
    public String setDatetimeOfSubmitMeterReads(Long chatId) {
        String nextMeterSubmitTerm = rescheduleMetersSubmitToNextMonth(chatId);

        return String.format(MESSAGE_METERS_SUBMITTED, nextMeterSubmitTerm);
    }

    @Transactional
    public String postponeMeterSubmit(String hours, Long chatId) {
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(hours);

        int hoursInt;

        if (matcher.find()) {
            hoursInt = Integer.parseInt(matcher.group(1));
        } else {
            return MESSAGE_NUMBER_NOT_FOUND_IN_STRING;
        }

        return putNextMetersSubmitTermIntoDB(chatId, LocalDateTime.now().plusHours(hoursInt));
    }

    private String rescheduleMetersSubmitToNextMonth(Long chatId) {
        LocalDateTime nowDatetime = LocalDateTime.now();
        LocalDateTime rescheduledDatetime = LocalDateTime.of(
                nowDatetime.getYear(),
                nowDatetime.getMonth().plus(1),
                dayForReschedule,
                hourForReschedule,
                minuteForReschedule
        );
        return putNextMetersSubmitTermIntoDB(chatId, rescheduledDatetime);
    }

    private String putNextMetersSubmitTermIntoDB(
            Long chatId,
            LocalDateTime datetime
    ) {
        SubmitMeterReads submitMeterReads = submitMeterReadsRepository.readByChatId(chatId);
        submitMeterReads = submitMeterReads.toBuilder()
                .datetimeExpected(datetime)
                .build();

        submitMeterReads = submitMeterReadsRepository.save(submitMeterReads);

        return submitMeterReads.getDatetimeExpected().format(formatter);
    }

}
