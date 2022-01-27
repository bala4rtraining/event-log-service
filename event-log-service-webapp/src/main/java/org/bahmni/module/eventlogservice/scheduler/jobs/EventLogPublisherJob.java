package org.bahmni.module.eventlogservice.scheduler.jobs;

import org.apache.log4j.Logger;
import org.bahmni.module.eventlogservice.fetcher.EventLogFetcher;
import org.bahmni.module.eventlogservice.model.EventLog;
import org.bahmni.module.eventlogservice.repository.EventLogRepository;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@DisallowConcurrentExecution
@Component("eventLogPublisherJob")
@ConditionalOnExpression("'${enable.scheduling}'=='true'")
public class EventLogPublisherJob implements Job {
    private EventLogRepository eventLogRepository;
    private EventLogFetcher eventLogFetcher;

    private static Logger logger = Logger.getLogger(EventLogPublisherJob.class);

    @Autowired
    public EventLogPublisherJob(EventLogRepository eventLogRepository, EventLogFetcher eventLogFetcher) {
        this.eventLogRepository = eventLogRepository;
        this.eventLogFetcher = eventLogFetcher;
    }

    @Override
    public void process() throws InterruptedException {
        EventLog eventLog = eventLogRepository.findFirstByOrderByIdDesc();
        List<EventLog> eventLogs = new ArrayList<EventLog>();
        String lastReadEventUuid = eventLog != null ? eventLog.getParentUuid(): "";
        logger.debug("Reading events which happened after event with uuid: " + lastReadEventUuid);
        try {
            eventLogs.addAll(eventLogFetcher.fetchEventLogsAfter(lastReadEventUuid));
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
        logger.debug("Found " + eventLogs.size() + " events.");
        Map<String,EventLog> eventRecordUuidsWithNewFilter = new HashMap<String, EventLog>();
            for(EventLog event : eventLogs){
            if(event.getCategory().equals("patient")){
                  EventLog recentPatientEvent = eventLogRepository.findTop1ByCategoryAndObjectOrderByIdDesc(event.getCategory(),event.getObject());
                if(requiredNewFilter(event, recentPatientEvent))
                    eventRecordUuidsWithNewFilter.put(event.getParentUuid(),event);
            }
        }

        try {
            if(!eventRecordUuidsWithNewFilter.keySet().isEmpty()) {
                List<EventLog> eventsWithNewFilter = eventLogFetcher.fetchEventLogsForFilterChange(eventRecordUuidsWithNewFilter.keySet());
                for(EventLog event : eventsWithNewFilter){
                    event.setFilter(eventRecordUuidsWithNewFilter.get(event.getParentUuid()).getFilter());
                    eventLogs.add(eventLogs.indexOf(eventRecordUuidsWithNewFilter.get(event.getParentUuid()))+1, event);
                }
            }
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException(e);
        }
        eventLogRepository.save(eventLogs);
        logger.debug("Copied " + eventLogs.size() + " events to events_log table");
    }

    private boolean requiredNewFilter(EventLog event, EventLog recentPatientEvent) {
        if (recentPatientEvent == null)
            return false;
        if (recentPatientEvent.getFilter() == null || event.getFilter() == null)
            return true;
        return !recentPatientEvent.getFilter().equals(event.getFilter());
    }
}
