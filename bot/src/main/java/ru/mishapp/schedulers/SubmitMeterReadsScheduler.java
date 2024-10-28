package ru.mishapp.schedulers;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.IBot;
import ru.mishapp.services.SubmitMeterReadsReminderService;

@Service
@RequiredArgsConstructor
public class SubmitMeterReadsScheduler {

    private final IBot iBot;

    private final SubmitMeterReadsReminderService submitMeterReadsReminderService;

    @Scheduled(cron = "${schedule.submit-meter-reminder}")
    @Transactional
    public void execute() {
        submitMeterReadsReminderService.getChatIdWithReminderMessages()
                .forEach((chatId, message) -> iBot.sendMessage(message, chatId));
    }
}
