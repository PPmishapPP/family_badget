package ru.mishapp.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.mishapp.dto.AccountBalance;
import ru.mishapp.dto.ListDto;
import ru.mishapp.entity.Account;
import ru.mishapp.entity.AccountHistory;
import ru.mishapp.entity.PeriodicChange;
import ru.mishapp.entity.PeriodicChangeRule;
import ru.mishapp.repository.AccountHistoryRepository;
import ru.mishapp.repository.AccountRepository;
import ru.mishapp.repository.PeriodicChangeRepository;
import ru.mishapp.services.records.ForecastItem;
import ru.mishapp.services.records.ForecastResult;
import static ru.mishapp.Constants.DAY;
import static ru.mishapp.Constants.RUB;

@Service
@RequiredArgsConstructor
public class ForecastService {
    
    private final PeriodicChangeRepository repository;
    private final AccountRepository accountRepository;
    private final AccountHistoryRepository accountHistoryRepository;
    
    public ForecastResult forecastFor(LocalDate day, Long chatId) {
        List<PeriodicChange> changes = repository.findAllByChatId(chatId);
        List<AccountBalance> accounts = accountRepository.findAllAccountBalanceByChatId(chatId);
        Map<Long, Integer> accountBalance = accounts.stream()
            .collect(Collectors.toMap(AccountBalance::id, AccountBalance::balance));
        
        List<ForecastItem> rulesForecast = new ArrayList<>();
        for (PeriodicChange periodicChange : changes) {
            int ruleSum = 0;
            for (PeriodicChangeRule rule : periodicChange.getRules()) {
                LocalDate nextDay = rule.getNextDay();
                while (!nextDay.isAfter(day)) {
                    accountBalance.computeIfPresent(rule.getTargetAccountId(), (key, value) -> value + rule.getSum());
                    ruleSum += rule.getSum();
                    nextDay = rule.getType().next(nextDay, rule.getPass());
                }
            }
            rulesForecast.add(new ForecastItem(periodicChange.getName(), ruleSum));
        }
        
        List<ForecastItem> accountsForecast = new ArrayList<>();
        for (AccountBalance account : accounts) {
            accountsForecast.add(new ForecastItem(account.name(), accountBalance.get(account.id())));
        }
        
        return new ForecastResult(rulesForecast, accountsForecast);
    }
    
    public ListDto forecastTo(LocalDate to, Account account, Long chatId) {
        Map<LocalDate, List<PeriodicChangeRule>> map = repository.findAllByChatId(chatId).stream()
            .flatMap(periodicChange -> periodicChange.getRules().stream())
            .collect(Collectors.groupingBy(PeriodicChangeRule::getNextDay));
        AccountHistory last = accountHistoryRepository.findLast(account.getId());
        int balance = last.getBalance();
        
        List<String> result = new ArrayList<>();
        result.add(LocalDate.now().format(DAY) + ": " + RUB.format(balance) + "₽");
        for (LocalDate current = LocalDate.now(); !current.isAfter(to); current = current.plusDays(1)) {
            List<PeriodicChangeRule> periodicChangeRules = map.remove(current);
            if (periodicChangeRules != null) {
                for (PeriodicChangeRule rule : periodicChangeRules) {
                    balance = balance + rule.getSum();
                    result.add(String.format(
                        "%s: %s₽ (%s %s)",
                        current.format(DAY),
                        RUB.format(balance),
                        rule.getName(),
                        RUB.format(rule.getSum()))
                    );
                    LocalDate nextDay = rule.getType().next(rule.getNextDay(), rule.getPass());
                    PeriodicChangeRule nextRule = rule.withNextDay(nextDay);
                    map.computeIfAbsent(nextDay, day -> new ArrayList<>()).add(nextRule);
                }
            }
        }
        return new ListDto(result);
    }
}
