package BackAnt.service.calendar;

import BackAnt.controller.calendar.ScheduleController;
import BackAnt.dto.calendar.CalendarDTO;
import BackAnt.dto.calendar.ScheduleDTO;
import BackAnt.entity.calendar.Calendar;
import BackAnt.entity.calendar.Schedule;
import BackAnt.repository.UserRepository;
import BackAnt.repository.calendar.CalendarRepository;
import BackAnt.repository.calendar.ScheduleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class CalendarService {

    private final ModelMapper modelMapper;
    private final CalendarRepository calendarRepository;
    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;


    public List<CalendarDTO> selectCalendar (String uid){

        List<Calendar> calendars = calendarRepository.findAllByUser_Uid(uid);

        log.info("45678"+calendars);

        return calendars.stream()
                .map(calendar -> {
                    CalendarDTO calendarDTO = modelMapper.map(calendar, CalendarDTO.class);
                    calendarDTO.setUser_id(calendar.getUser() != null ? calendar.getUser().getUid() : null);
                    return calendarDTO;
                })
                .toList();
    }

    public void insertCalendar (CalendarDTO calendarDTO) {

        Calendar calendar = Calendar.builder()
                .name(calendarDTO.getName())
                .user(userRepository.findByUid(calendarDTO.getUser_id()).orElseThrow(() -> new EntityNotFoundException("user값이 없습니다.")))
                .view(calendarDTO.getUser_id())
                .build();

        log.info("5555"+calendar);

        calendarRepository.save(calendar);
    }

    public void updateCalendar (int no, String newName) {
        Calendar calendar = calendarRepository.findById(no).orElseThrow(() -> new EntityNotFoundException("이 id의 Calendar가 없습니다."));

        log.info("123123432432"+calendar);
        calendar.updateName(newName);
        log.info("123123432432"+calendar);
        calendarRepository.save(calendar);

    }

    public void deleteCalendar (int no) {
        calendarRepository.deleteById(no);
    }

    public void insertSchedule (ScheduleDTO scheduleDTO){

        String internalAttendees = scheduleDTO.getInternalAttendees().toString() .replace("[", "")
                .replace("]", "");;
        String externalAttendees = scheduleDTO.getExternalAttendees().toString() .replace("[", "")
                .replace("]", "");;

        Schedule schedule = Schedule.builder()
                .title(scheduleDTO.getTitle())
                .calendar(calendarRepository.findById(scheduleDTO.getCalendarId()).orElseThrow(() -> new EntityNotFoundException("이 id의 Calendar가 없습니다.")))
                .content(scheduleDTO.getContent())
                .internalAttendees(internalAttendees)
                .externalAttendees(externalAttendees)
                .location(scheduleDTO.getLocation())
                .start(scheduleDTO.getStart())
                .end(scheduleDTO.getEnd())
                .uid(scheduleDTO.getUid())
                .build();

        scheduleRepository.save(schedule);

    }

    public List<ScheduleDTO> selectSchedule (String uid) {
        List<Schedule> schedules = scheduleRepository.findScheduleByUid(uid);
        log.info("schedule::::::::::"+schedules);

        return schedules.stream()
                .map(schedule -> {
                    ScheduleDTO dto = modelMapper.map(schedule, ScheduleDTO.class); // 기본 매핑
                    dto.setCalendarId(schedule.getCalendar().getCalendarId()); // calendarId 수동 설정
                    return dto;
                })
                .toList();



    }


}
