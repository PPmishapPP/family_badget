package ru.mishapp.schedulers;


import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mishapp.IBot;
import ru.mishapp.entity.DutyUser;
import ru.mishapp.entity.User;
import ru.mishapp.repository.DutyRepository;
import ru.mishapp.repository.UserRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DutyScheduler {
    
    private final DutyRepository dutyRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();
    private final IBot bot;
    
    @Scheduled(cron = "${schedule.duty}")
    @Transactional
    public void execute() {
        dutyRepository.findAll().forEach(duty -> {
            
            LocalDate lastMessages = duty.getLastMessages();
            int days = (int) Duration.between(lastMessages.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays();
            
            if (days > 30) {
                List<DutyUser> users = duty.getUsers();
                Long nextUserId = users.get(random.nextInt(users.size())).getId().getId();
                if (nextUserId != null) {
                    userRepository.findById(nextUserId).ifPresent(user -> {
                        
                        dutyRepository.updateNext(duty.getId(), nextUserId);
                        int dayOfMonth = LocalDate.now().getDayOfMonth();
                        String message = getMessage(user, dayOfMonth);
                        bot.sendMessage(message, duty.getChatId());
                    });
                }
            }
        });
    }
    
    private static String getMessage(User user, int dayOfMonth) {
        List<String> messages = List.of(
            "Напоминаю всем, что нужно мыть посуду за собой. Особенно тебе, %s",
            "%s, а ты мыл(а) вчера посуду за собой? Сегодня не забудь!",
            "Аля, напоминаю: если ты только что приготовила яичницу и сковородка слишком горячая, чтобы её мыть, то нужно её охладить холодной водой и помыть сразу",
            "Аля, если ты готовишься мыть гору посуды и видишь, что какая-то тарелка особо грязная, то можно её замочить сразу и помыть в конце. Другие сценарии \"замачивания\" запрещены - надо мыть сразу."
        );
        
        
        return String.format(
            messages.get(dayOfMonth % messages.size()),
            user.getName()
        );
    }
}
