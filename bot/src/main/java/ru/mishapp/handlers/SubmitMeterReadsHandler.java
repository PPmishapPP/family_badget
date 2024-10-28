package ru.mishapp.handlers;

import lombok.RequiredArgsConstructor;
import ru.mishapp.annotations.TelegramCommand;
import ru.mishapp.annotations.TelegramHandler;
import ru.mishapp.annotations.TelegramParam;
import ru.mishapp.services.SubmitMeterReadsService;

@TelegramHandler(value = "показания", description = "Передать показаня счетчиков")
@RequiredArgsConstructor
public class SubmitMeterReadsHandler {

    private final SubmitMeterReadsService submitMeterReadsService;

    @TelegramCommand("сданы")
    public String create(Long chatId) {
        return submitMeterReadsService.setDatetimeOfSubmitMeterReads(chatId);
    }

    @TelegramCommand("сдам")
    public String reschedule(@TelegramParam("через") String hours, Long chatId) {
        return submitMeterReadsService.postponeMeterSubmit(hours, chatId);
    }
}