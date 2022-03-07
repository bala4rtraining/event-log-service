package org.bahmni.module.eventlogservice.fetcher;

import org.bahmni.module.eventlogservice.model.EventLog;
import org.bahmni.module.eventlogservice.web.helper.OpenMRSProperties;
import org.bahmni.module.eventlogservice.web.helper.OpenMRSWebClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@ContextConfiguration

@PrepareForTest({EventLogFetcher.class})
@RunWith(PowerMockRunner.class)
public class EventLogFetcherTest {
    @InjectMocks
    private EventLogFetcher eventLogFetcher;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private OpenMRSProperties openMRSProperties;

    @Mock
    private OpenMRSWebClient openMRSWebClient;


    @Before
    public void setUp() throws Exception {
        initMocks(this);

    }

    @Test
    public void shouldMapEventRecordsToEventLog() throws Exception {
        eventLogFetcher.setBahmniEventLogURL("bahmniEventLogURL/");

        String lastReadEventUuid = "uuid";
        EventLog eventLog = new EventLog("uuid1",new Date(),"patientURL","Patient","202020", null);
        EventLog[] eventLogs = new EventLog[]{eventLog};
        when(openMRSWebClient.get(any(URI.class))).thenReturn(new ObjectMapper().writeValueAsString(eventLogs));
        List<EventLog> actualEventLogList = eventLogFetcher.fetchEventLogsAfter(lastReadEventUuid);
        Assert.assertEquals(eventLogs.length,actualEventLogList.size());

    }

    @Test
    public void shouldFetchEventLogsWhenLastReadUuidIsNull() throws Exception {
        eventLogFetcher.setBahmniEventLogURL("bahmniEventLogURL/");
        List<EventLog> eventLogs = new ArrayList<EventLog>();
        EventLog eventLog = new EventLog("uuid1",new Date(),"patientURL","Patient","202020", null);
        eventLogs.add(eventLog);
        when(openMRSWebClient.get(any(URI.class))).thenReturn(new ObjectMapper().writeValueAsString(eventLogs));
        List<EventLog> actualEventLogList = eventLogFetcher.fetchEventLogsAfter(null);
        Assert.assertEquals(eventLogs.size(),actualEventLogList.size());

    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowExceptionWhenUriIsInvalid() throws Exception {
        eventLogFetcher.setBahmniEventLogURL(null);
        List<EventLog> eventLogs = new ArrayList<EventLog>();
        EventLog eventLog = new EventLog("`invalid``",new Date(),"patientURL","Patient","202020", null);
        eventLogs.add(eventLog);
        when(openMRSWebClient.get(any(URI.class))).thenReturn(new ObjectMapper().writeValueAsString(eventLogs));
        eventLogFetcher.fetchEventLogsAfter(eventLog.getUuid());
    }

    @Test(expected=RuntimeException.class)
    public void shouldThrowRuntimeExceptionWhenObjectMapperThrowsException() throws Exception {
        eventLogFetcher.setBahmniEventLogURL("bahmniEventLogURL/");
        List<EventLog> eventLogs = new ArrayList<EventLog>();
        EventLog eventLog = new EventLog("uuid",new Date(),"patientURL","Patient","202020", null);
        eventLogs.add(eventLog);
        when(openMRSWebClient.get(any(URI.class))).thenThrow(new IOException());
        eventLogFetcher.fetchEventLogsAfter(eventLog.getUuid());
    }

    @Test
    public void shouldFetchEventLogsFromForPatientsWithFilterChange() throws Exception {
        eventLogFetcher.setBahmniEventLogURL("http://192.168.33.10/openmrs");
        Set<String> eventRecordUuids = new HashSet<String>();
        eventRecordUuids.add("UUID1");
        eventRecordUuids.add("UUID2");
        eventRecordUuids.add("UUID3");

        List<EventLog> eventLogs = new ArrayList<EventLog>();
        EventLog eventLog = new EventLog("uuid",new Date(),"patientURL","Encounter","202020", "uuid1");
        eventLogs.add(eventLog);

        when(openMRSWebClient.get(any(URI.class))).thenReturn(new ObjectMapper().writeValueAsString(eventLogs.toArray()));
        List<EventLog> eventLogsForFilterChange = eventLogFetcher.fetchEventLogsForFilterChange(eventRecordUuids);

        Assert.assertEquals(eventLogs.size(),eventLogsForFilterChange.size());
        Assert.assertEquals(eventLogs.get(0).getUuid(),eventLogsForFilterChange.get(0).getUuid());
    }

}