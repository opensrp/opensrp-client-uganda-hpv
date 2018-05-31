package org.smartregister.ug.hpv.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.clientandeventmodel.DateUtil;
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.domain.db.Client;
import org.smartregister.domain.db.Event;
import org.smartregister.domain.db.EventClient;
import org.smartregister.domain.jsonmapping.ClientClassification;
import org.smartregister.domain.jsonmapping.ClientField;
import org.smartregister.domain.jsonmapping.Column;
import org.smartregister.domain.jsonmapping.Table;
import org.smartregister.immunization.domain.Vaccine;
import org.smartregister.immunization.repository.VaccineRepository;
import org.smartregister.immunization.service.intent.VaccineIntentService;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.DetailsRepository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.helper.ECSyncHelper;
import org.smartregister.ug.hpv.util.Constants;
import org.smartregister.ug.hpv.util.DBConstants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.smartregister.ug.hpv.util.Utils.addVaccine;
import static org.smartregister.ug.hpv.util.Utils.updateEcPatient;

/**
 * Created by ndegwamartin on 15/03/2018.
 */

public class HpvClientProcessorForJava extends ClientProcessorForJava {

    private static final String TAG = HpvClientProcessorForJava.class.getCanonicalName();
    private static HpvClientProcessorForJava instance;
    private final String HPV_VACCINATION = "HPV Vaccination";

    public HpvClientProcessorForJava(Context context) {
        super(context);
    }

    public static HpvClientProcessorForJava getInstance(Context context) {
        if (instance == null) {
            instance = new HpvClientProcessorForJava(context);
        }

        return instance;
    }


    @Override
    public void processClient(List<EventClient> eventClients) throws Exception {

        ClientClassification clientClassification = assetJsonToJava("ec_client_classification.json", ClientClassification.class);
        Table vaccineTable = assetJsonToJava("ec_client_vaccine.json", Table.class);

        if (!eventClients.isEmpty()) {
            List<Event> unsyncEvents = new ArrayList<>();
            for (EventClient eventClient : eventClients) {
                Event event = eventClient.getEvent();
                if (event == null) {
                    return;
                }

                String eventType = event.getEventType();
                if (eventType == null) {
                    continue;
                }

                if (eventType.equals(HPV_VACCINATION) || eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT)) {
                    if (vaccineTable == null) {
                        continue;
                    }

                    processVaccine(eventClient, vaccineTable, eventType.equals(VaccineIntentService.EVENT_TYPE_OUT_OF_CATCHMENT));
                } else if (eventType.equals(Constants.EventType.REMOVE)) {
                    unsyncEvents.add(event);
                } else if (eventType.equals(Constants.EventType.REGISTRATION) || eventType.equals(Constants.EventType.UPDATE_REGISTRATION)) {
                    if (clientClassification == null) {
                        continue;
                    }

                    Client client = eventClient.getClient();
                    //iterate through the events
                    if (client != null) {
                        processEvent(event, client, clientClassification);

                    }
                }
            }

            // Unsync events that are should not be in this device
            if (!unsyncEvents.isEmpty()) {
                unSync(unsyncEvents);
            }
        }
    }


    private Boolean processVaccine(EventClient vaccine, Table vaccineTable, boolean outOfCatchment) throws Exception {

        try {
            if (vaccine == null || vaccine.getEvent() == null) {
                return false;
            }

            if (vaccineTable == null) {
                return false;
            }

            Log.d(TAG, "Starting processVaccine table: " + vaccineTable.name);

            ContentValues contentValues = processCaseModel(vaccine, vaccineTable);

            // save the values to db
            if (contentValues != null && contentValues.size() > 0) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date date = simpleDateFormat.parse(contentValues.getAsString(VaccineRepository.DATE));

                VaccineRepository vaccineRepository = HpvApplication.getInstance().vaccineRepository();
                Vaccine vaccineObj = new Vaccine();
                vaccineObj.setBaseEntityId(contentValues.getAsString(VaccineRepository.BASE_ENTITY_ID));
                vaccineObj.setName(contentValues.getAsString(VaccineRepository.NAME));
                if (contentValues.containsKey(VaccineRepository.CALCULATION)) {
                    vaccineObj.setCalculation(parseInt(contentValues.getAsString(VaccineRepository.CALCULATION)));
                }
                vaccineObj.setDate(date);
                vaccineObj.setAnmId(contentValues.getAsString(VaccineRepository.ANMID));
                vaccineObj.setLocationId(contentValues.getAsString(VaccineRepository.LOCATION_ID));
                vaccineObj.setChildLocationId(contentValues.getAsString(VaccineRepository.CHILD_LOCATION_ID));
                vaccineObj.setTeamId(contentValues.getAsString(VaccineRepository.TEAM_ID));
                vaccineObj.setTeam(contentValues.getAsString(VaccineRepository.TEAM));
                vaccineObj.setSyncStatus(VaccineRepository.TYPE_Synced);
                vaccineObj.setFormSubmissionId(vaccine.getEvent().getFormSubmissionId());
                vaccineObj.setEventId(vaccine.getEvent().getEventId());
                vaccineObj.setOutOfCatchment(outOfCatchment ? 1 : 0);

                String createdAtString = contentValues.getAsString(VaccineRepository.CREATED_AT);
                Date createdAt = getDate(createdAtString);
                vaccineObj.setCreatedAt(createdAt);

                addVaccine(vaccineRepository, vaccineObj);
                Log.d(TAG, "Ending processVaccine table: " + vaccineTable.name);

                // update patient record
                String baseEntityId = vaccineObj.getBaseEntityId();
                String vaccineName = vaccineObj.getName();
                Date vaccineDate = vaccineObj.getDate();
                String locationId = vaccineObj.getChildLocationId();

                updateEcPatient(baseEntityId, vaccineName, vaccineDate, locationId);
            }

            return true;
        } catch (Exception e) {
            Log.e(TAG, "Process Vaccine Error", e);
            return null;
        }
    }

    private Integer parseInt(String string) {
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }

    private ContentValues processCaseModel(EventClient eventClient, Table table) {
        try {
            List<Column> columns = table.columns;
            ContentValues contentValues = new ContentValues();

            for (Column column : columns) {
                processCaseModel(eventClient.getEvent(), eventClient.getClient(), column, contentValues);
            }

            return contentValues;
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return null;
    }

    private Date getDate(String eventDateStr) {
        Date date = null;
        if (StringUtils.isNotBlank(eventDateStr)) {
            try {
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
                date = dateFormat.parse(eventDateStr);
            } catch (ParseException e) {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
                    date = dateFormat.parse(eventDateStr);
                } catch (ParseException pe) {
                    try {
                        date = DateUtil.parseDate(eventDateStr);
                    } catch (ParseException pee) {
                        Log.e(TAG, pee.toString(), pee);
                    }
                }
            }
        }
        return date;
    }


    private boolean unSync(ECSyncHelper ecSyncHelper, DetailsRepository detailsRepository, List<Table> bindObjects, Event event, String registeredAnm) {
        try {
            String baseEntityId = event.getBaseEntityId();
            String providerId = event.getProviderId();

            if (providerId.equals(registeredAnm)) {
                boolean eventDeleted = ecSyncHelper.deleteEventsByBaseEntityId(baseEntityId);
                boolean clientDeleted = ecSyncHelper.deleteClient(baseEntityId);
                Log.d(getClass().getName(), "EVENT_DELETED: " + eventDeleted);
                Log.d(getClass().getName(), "ClIENT_DELETED: " + clientDeleted);

                boolean detailsDeleted = detailsRepository.deleteDetails(baseEntityId);
                Log.d(getClass().getName(), "DETAILS_DELETED: " + detailsDeleted);

                for (Table bindObject : bindObjects) {
                    String tableName = bindObject.name;

                    boolean caseDeleted = deleteCase(tableName, baseEntityId);
                    Log.d(getClass().getName(), "CASE_DELETED: " + caseDeleted);
                }

                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
        return false;
    }

    private boolean unSync(List<Event> events) {
        try {

            if (events == null || events.isEmpty()) {
                return false;
            }

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            String registeredAnm = allSharedPreferences.fetchRegisteredANM();

            ClientField clientField = assetJsonToJava("ec_client_fields.json", ClientField.class);
            if (clientField == null) {
                return false;
            }

            List<Table> bindObjects = clientField.bindobjects;
            DetailsRepository detailsRepository = HpvApplication.getInstance().getContext().detailsRepository();
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(getContext());

            for (Event event : events) {
                unSync(ecUpdater, detailsRepository, bindObjects, event, registeredAnm);
            }

            return true;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }

        return false;
    }


    @Override
    public String[] getOpenmrsGenIds() {
        return new String[]{DBConstants.KEY.OPENSRP_ID};
    }

    @Override
    public void updateFTSsearch(String tableName, String entityId, ContentValues contentValues) {

        Log.i(TAG, "Starting updateFTSsearch table: " + tableName);

        AllCommonsRepository allCommonsRepository = org.smartregister.CoreLibrary.getInstance().context().
                allCommonsRepositoryobjects(tableName);

        if (allCommonsRepository != null) {
            allCommonsRepository.updateSearch(entityId);
        }

        Log.i(TAG, "Finished updateFTSsearch table: " + tableName);
    }
}
