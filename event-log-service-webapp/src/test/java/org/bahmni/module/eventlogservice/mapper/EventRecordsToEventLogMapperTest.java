package org.bahmni.module.eventlogservice.mapper;

import org.bahmni.module.eventlogservice.mapper.filterEvaluators.AddressHierarchyFilterEvaluator;
import org.bahmni.module.eventlogservice.mapper.filterEvaluators.EncounterFilterEvaluator;
import org.bahmni.module.eventlogservice.mapper.filterEvaluators.PatientFilterEvaluator;
import org.bahmni.module.eventlogservice.model.EventRecords;
import org.bahmni.module.eventlogservice.model.EventLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@PrepareForTest({EventRecordsToEventLogMapper.class})
@RunWith(PowerMockRunner.class)
public class EventRecordsToEventLogMapperTest {
    public static final Date TIMESTAMP = new Date();

    private EventRecordsToEventLogMapper eventRecordsToEventLogMapper;

    @Mock
    private PatientFilterEvaluator patientFilterEvaluator;

    @Mock
    private EncounterFilterEvaluator encounterFilterEvaluator;

    @Mock
    private AddressHierarchyFilterEvaluator addressHierarchyFilterEvaluator;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        eventRecordsToEventLogMapper = new EventRecordsToEventLogMapper(patientFilterEvaluator, encounterFilterEvaluator, addressHierarchyFilterEvaluator, jdbcTemplate);
    }

    @Test
    public void shouldMapEventRecordsToEventLog() throws Exception {
        ArrayList<EventRecords> eventRecords = new ArrayList<EventRecords>();
        EventRecords eventRecord = new EventRecords("uuid", "title", TIMESTAMP, "uri", "object", "category");
        eventRecords.add(eventRecord);

        List<EventLog> eventLogs = eventRecordsToEventLogMapper.map(eventRecords);

        assertNotNull(eventLogs);
        assertEquals(eventRecords.size(), eventLogs.size());
        assertEquals(eventRecord.getUuid(), eventLogs.get(0).getUuid());
        assertEquals(eventRecord.getTimestamp(), eventLogs.get(0).getTimestamp());
        assertEquals(eventRecord.getObject(), eventLogs.get(0).getObject());
        assertEquals(eventRecord.getCategory(), eventLogs.get(0).getCategory());
        assertNull(eventLogs.get(0).getFilter());
    }

    @Test
    public void shouldMapConceptEventRecordsToEventLogIfTheyAreChildrenOfOfflineConceptsSet() throws Exception {
        ArrayList<EventRecords> eventRecords = new ArrayList<EventRecords>();
        String conceptUrl = "/openmrs/ws/rest/v1/concept/cfc68437-4e98-4c98-b7f9-a787c68d82ad?s=byFullySpecifiedName&v=bahmni&name=Magnesium";
        EventRecords eventRecord = new EventRecords("uuid", "title", TIMESTAMP, "uri", conceptUrl, "all-concepts");
        eventRecords.add(eventRecord);

        when(jdbcTemplate.queryForObject(any(String.class), any(EventRecordsToEventLogMapper.ConceptRowMapper.class))).thenReturn(1);

        List<EventLog> eventLogs = eventRecordsToEventLogMapper.map(eventRecords);

        assertNotNull(eventLogs);
        assertEquals(eventRecords.size(), eventLogs.size());
        assertEquals(eventRecord.getUuid(), eventLogs.get(0).getUuid());
        assertEquals(eventRecord.getTimestamp(), eventLogs.get(0).getTimestamp());
        assertEquals(eventRecord.getObject(), eventLogs.get(0).getObject());
        assertEquals("offline-concepts", eventLogs.get(0).getCategory());
        assertNull(eventLogs.get(0).getFilter());
    }

    @Test
    public void shouldNotMapConceptEventRecordsToEventLogIfTheyAreNotChildrenOfOfflineConceptsSet() throws Exception {
        ArrayList<EventRecords> eventRecords = new ArrayList<EventRecords>();
        String conceptUrl = "/openmrs/ws/rest/v1/concept/cfc68437-4e98-4c98-b7f9-a787c68d82ad?s=byFullySpecifiedName&v=bahmni&name=Magnesium";
        EventRecords eventRecord = new EventRecords("uuid", "title", TIMESTAMP, "uri", conceptUrl, "all-concepts");
        eventRecords.add(eventRecord);

        when(jdbcTemplate.queryForObject(any(String.class), any(EventRecordsToEventLogMapper.ConceptRowMapper.class))).thenReturn(0);

        List<EventLog> eventLogs = eventRecordsToEventLogMapper.map(eventRecords);

        assertEquals(0, eventLogs.size());
    }

    @Test
    public void shouldMapOfflineConceptEventRecordsToEventLog() throws Exception {
        ArrayList<EventRecords> eventRecords = new ArrayList<EventRecords>();
        String conceptUrl = "/openmrs/ws/rest/v1/concept/cfc68437-4e98-4c98-b7f9-a787c68d82ad?s=byFullySpecifiedName&v=bahmni&name=Magnesium";
        EventRecords eventRecord = new EventRecords("uuid", "title", TIMESTAMP, "uri", conceptUrl, "offline-concepts");
        eventRecords.add(eventRecord);

        when(jdbcTemplate.queryForObject(any(String.class), any(EventRecordsToEventLogMapper.ConceptRowMapper.class))).thenReturn(1);

        List<EventLog> eventLogs = eventRecordsToEventLogMapper.map(eventRecords);

        assertNotNull(eventLogs);
        assertEquals(eventRecords.size(), eventLogs.size());
        assertEquals(eventRecord.getUuid(), eventLogs.get(0).getUuid());
        assertEquals(eventRecord.getTimestamp(), eventLogs.get(0).getTimestamp());
        assertEquals(eventRecord.getObject(), eventLogs.get(0).getObject());
        assertEquals(eventRecord.getCategory(), eventLogs.get(0).getCategory());
        assertNull(eventLogs.get(0).getFilter());
    }

    @Test
    public void shouldEvaluateFilterForPatientEvents() throws Exception {
        ArrayList<EventRecords> eventRecords = new ArrayList<EventRecords>();
        EventRecords eventRecord = new EventRecords("uuid", "title", TIMESTAMP, "uri", "/openmrs/ws/rest/v1/patient/d95bf6c9-d1c6-41dc-aecf-1c06bd71386c?v=full", "patient");
        eventRecords.add(eventRecord);
        EventLog eventLog = new EventLog("uuid", TIMESTAMP, "/openmrs/ws/rest/v1/patient/d95bf6c9-d1c6-41dc-aecf-1c06bd71386c?v=full", "patient", null);
        PowerMockito.whenNew(EventLog.class).withAnyArguments().thenReturn(eventLog);

        List<EventLog> eventLogs = eventRecordsToEventLogMapper.map(eventRecords);

        verify(patientFilterEvaluator, times(1)).evaluateFilter("d95bf6c9-d1c6-41dc-aecf-1c06bd71386c", eventLog);

        assertNotNull(eventLogs);
        assertEquals(eventRecords.size(), eventLogs.size());
        assertEquals(eventRecord.getUuid(), eventLogs.get(0).getUuid());
        assertEquals(eventRecord.getTimestamp(), eventLogs.get(0).getTimestamp());
        assertEquals(eventRecord.getObject(), eventLogs.get(0).getObject());
        assertEquals(eventRecord.getCategory(), eventLogs.get(0).getCategory());
    }

    @Test
    public void shouldEvaluateFilterForEncounterEvents() throws Exception {
        ArrayList<EventRecords> eventRecords = new ArrayList<EventRecords>();
        EventRecords eventRecord = new EventRecords("uuid", "title", TIMESTAMP, "uri", "/openmrs/ws/rest/v1/encounter/d95bf6c9-d1c6-41dc-aecf-1c06bd71358c?v=full", "Encounter");
        eventRecords.add(eventRecord);
        EventLog eventLog = new EventLog("uuid", TIMESTAMP, "/openmrs/ws/rest/v1/encounter/d95bf6c9-d1c6-41dc-aecf-1c06bd71358c?v=full", "Encounter", null);
        PowerMockito.whenNew(EventLog.class).withAnyArguments().thenReturn(eventLog);

        List<EventLog> eventLogs = eventRecordsToEventLogMapper.map(eventRecords);

        verify(encounterFilterEvaluator, times(1)).evaluateFilter("d95bf6c9-d1c6-41dc-aecf-1c06bd71358c", eventLog);

        assertNotNull(eventLogs);
        assertEquals(eventRecords.size(), eventLogs.size());
        assertEquals(eventRecord.getUuid(), eventLogs.get(0).getUuid());
        assertEquals(eventRecord.getTimestamp(), eventLogs.get(0).getTimestamp());
        assertEquals(eventRecord.getObject(), eventLogs.get(0).getObject());
        assertEquals(eventRecord.getCategory(), eventLogs.get(0).getCategory());
    }

    @Test
    public void shouldEvaluateFilterForAddressHierarchyEvents() throws Exception {
        ArrayList<EventRecords> eventRecords = new ArrayList<EventRecords>();
        EventRecords eventRecord = new EventRecords("uuid", "title", TIMESTAMP, "uri", "/openmrs/ws/rest/v1/address/hierarchy/d95bf6c9-d1c6-41dc-aecf-1c06bd71358c?v=full", "addressHierarchy");
        eventRecords.add(eventRecord);
        EventLog eventLog = new EventLog("uuid", TIMESTAMP, "/openmrs/ws/rest/v1/address/hierarchy/d95bf6c9-d1c6-41dc-aecf-1c06bd71358c?v=full", "addressHierarchy", null);
        PowerMockito.whenNew(EventLog.class).withAnyArguments().thenReturn(eventLog);

        List<EventLog> eventLogs = eventRecordsToEventLogMapper.map(eventRecords);

        verify(addressHierarchyFilterEvaluator, times(1)).evaluateFilter("d95bf6c9-d1c6-41dc-aecf-1c06bd71358c", eventLog);

        assertNotNull(eventLogs);
        assertEquals(eventRecords.size(), eventLogs.size());
        assertEquals(eventRecord.getUuid(), eventLogs.get(0).getUuid());
        assertEquals(eventRecord.getTimestamp(), eventLogs.get(0).getTimestamp());
        assertEquals(eventRecord.getObject(), eventLogs.get(0).getObject());
        assertEquals(eventRecord.getCategory(), eventLogs.get(0).getCategory());
    }
}