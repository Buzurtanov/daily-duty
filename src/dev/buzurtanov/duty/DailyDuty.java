package dev.buzurtanov.duty;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
public class DailyDuty {
    private static final String[] ATTENDANTS = {"Kilgore Trout", "Eliot Rosewater", "Billy Pilgrim", "Howard Campbell"};
    private static final Map<Integer, Set<LocalDate>> dutyMap = new HashMap<>();
    private static final Set<LocalDate> weekEnds = new TreeSet<>();
    private static volatile AtomicInteger currentId = new AtomicInteger(0);
    private static final Map<Integer, String> userIds = new HashMap<>();
    private static final List<LocalDate> HOLIDAYS = Arrays.asList(
            LocalDate.of(2021, 02, 23),
            LocalDate.of(2021, 03, 8),
            LocalDate.of(2021, 05, 3),
            LocalDate.of(2021, 05, 10)
    );
    static {
        for (int i = 1; i <= ATTENDANTS.length; i++) {
            dutyMap.put(i, new TreeSet<>());
        }
    }
    private static void randomGenerate() {
        Random rGen = new Random();
        List<Integer> randoms = new ArrayList<>();

        while (randoms.size() != ATTENDANTS.length) {
            int random = rGen.nextInt(ATTENDANTS.length);

            if (!randoms.contains(random)) {
                randoms.add(random);
            }
        }

        int i = 0;
        for (int a : randoms) {
            userIds.put(++i, ATTENDANTS[a]);
        }

        System.out.println(userIds + "\n");
    }

    public static void main(String[] args) {
        // раскидываем псевдослучайно сотрудников
        randomGenerate();

        // идем по рабочим дням и раскидываем по сотрудникам в прямом порядке
        LocalDate now = LocalDate.now();
        for (LocalDate date = now; date.isBefore(LocalDate.of(now.getYear()+1, 01, 01)); date = date.plusDays(1)) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY) {
                weekEnds.add(date);
            } else {
                dutyMap.get(nextId()).add(date);
            }
        }

        // идем по выходным и раскидываем по сотрудникам в обратном порядке
        currentId.set(ATTENDANTS.length);

        for (LocalDate date : weekEnds) {
            int id = currentId.get();

            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                id = previousId();
            }

            dutyMap.get(id).add(date);
        }

        for (Map.Entry<Integer, Set<LocalDate>> entry : dutyMap.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println();

        printResult();
    }
    private static void printResult() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");

        for (Map.Entry<Integer, Set<LocalDate>> entry : dutyMap.entrySet()) {
            System.out.println(userIds.get(entry.getKey()) + ":");
            Set<LocalDate> dates = entry.getValue();

            Set<Map.Entry<Month, List<LocalDate>>> datesByMonth = dates.stream().collect(Collectors.groupingBy(item -> item.getMonth())).entrySet();
            Comparator<Map.Entry<Month, List<LocalDate>>> byKeyMonth = (o1, o2) -> o1.getKey().getValue() - o2.getKey().getValue();
            Supplier<TreeSet<Map.Entry<Month, List<LocalDate>>>> supplier = () -> new TreeSet<Map.Entry<Month, List<LocalDate>>>(byKeyMonth);

            TreeSet<Map.Entry<Month, List<LocalDate>>> treeSet = datesByMonth.stream().collect(Collectors.toCollection(supplier));

            for (Map.Entry<Month, List<LocalDate>> entryWithGroupByMonth : treeSet) {
                List<LocalDate> listDates = entryWithGroupByMonth.getValue();
                StringBuilder sb = new StringBuilder();
                for (LocalDate localDate : listDates ) {
                    sb.append(formatter.format(localDate)).append(", ");
                }
                sb.replace(sb.lastIndexOf(","), sb.lastIndexOf(",")+1, "");

                System.out.println("   " + entryWithGroupByMonth.getKey() + ": " + sb.toString());
            }

            System.out.println();
        }
    }

    // start from 1 to USERS.length
    private static int nextId() {
        if (currentId.get() >= ATTENDANTS.length) {
            currentId.set(1);
            return currentId.get();
        }
        return currentId.incrementAndGet();
    }
    private static int previousId() {
        if (currentId.get() == 1) {
            currentId.set(ATTENDANTS.length);
            return currentId.get();
        }
        return currentId.decrementAndGet();
    }
    private static boolean isHoliday(LocalDate date) {
        return HOLIDAYS.contains(date);
    }
}
