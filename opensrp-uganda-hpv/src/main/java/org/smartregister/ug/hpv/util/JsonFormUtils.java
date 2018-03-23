package org.smartregister.ug.hpv.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.clientandeventmodel.Address;
import org.smartregister.clientandeventmodel.Client;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.FormEntityConstants;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.ProfileImage;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.HomeRegisterActivity;
import org.smartregister.ug.hpv.activity.HpvJsonFormActivity;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.helper.ECSyncHelper;
import org.smartregister.ug.hpv.repository.UniqueIdRepository;
import org.smartregister.ug.hpv.sync.HPVClientProcessorForJava;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.DateTimeTypeConverter;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import id.zelory.compressor.Compressor;

/**
 * Created by ndegwamartin on 19/03/2018.
 */
public class JsonFormUtils extends org.smartregister.util.JsonFormUtils {
    private static final String TAG = "JsonFormUtils";

    public static final String MOTHER_DEFAULT_DOB = "01-01-1960";
    private static final String ENCOUNTER = "encounter";
    public static final String RELATIONAL_ID = "relational_id";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String CURRENT_ZEIR_ID = "current_zeir_id";
    public static final String READ_ONLY = "read_only";
    private static final String METADATA = "metadata";
    public static final String ZEIR_ID = "ZEIR_ID";
    private static final String M_ZEIR_ID = "M_ZEIR_ID";
    public static final String encounterType = "Update Birth Registration";
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String LOCATION_HIERARCHY = "locationsHierarchy";
    private static final String MAP = "map";


    public static final SimpleDateFormat dd_MM_yyyy = new SimpleDateFormat("dd-MM-yyyy");
    //public static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").create();
    //2007-03-31T04:00:00.000Z
    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();


    public static void saveForm(Context context, org.smartregister.Context openSrpContext,
                                String jsonString, String providerId) {
        try {
            JSONObject form = new JSONObject(jsonString);
            if (form.getString(ENCOUNTER_TYPE).equals(Constants.EventType.BITRH_REGISTRATION)) {
                saveBirthRegistration(context, openSrpContext, jsonString, providerId, "Child_Photo", "child", "mother");
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private static void saveBirthRegistration(Context context, org.smartregister.Context openSrpContext,
                                              String jsonString, String providerId, String imageKey, String bindType,
                                              String subBindType) {
        if (context == null || openSrpContext == null || StringUtils.isBlank(providerId)
                || StringUtils.isBlank(jsonString)) {
            return;
        }

        org.smartregister.util.Utils.startAsyncTask(
                new SaveBirthRegistrationTask(context, openSrpContext, jsonString, providerId, imageKey, bindType, subBindType), null
        );
    }

    public static void editsave(Context context, org.smartregister.Context openSrpContext, String jsonString, String providerId, String imageKey, String bindType, String subBindType) {
        if (context == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(jsonString)) {
            return;
        }

        try {
            ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);

            JSONObject jsonForm = new JSONObject(jsonString);

            String entityId = getString(jsonForm, ENTITY_ID);
            String relationalId = getString(jsonForm, RELATIONAL_ID);

            if (StringUtils.isBlank(entityId)) {
                entityId = generateRandomUUIDString();
            }

            JSONArray fields = fields(jsonForm);
            if (fields == null) {
                return;
            }

            for (int i = 0; i < fields.length(); i++) {
                String key = fields.getJSONObject(i).getString("key");
                if ("Home_Facility".equals(key)
                        || "Birth_Facility_Name".equals(key)
                        || "Residential_Area".equals(key)) {
                    try {
                        String rawValue = fields.getJSONObject(i).getString("value");
                        JSONArray valueArray = new JSONArray(rawValue);
                        if (valueArray.length() > 0) {
                            String lastLocationName = valueArray.getString(valueArray.length() - 1);
                            String lastLocationId = getOpenMrsLocationId(openSrpContext, lastLocationName);
                            fields.getJSONObject(i).put("value", lastLocationId);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, Log.getStackTraceString(e));
                    }
                } else if ("Mother_Guardian_Date_Birth".equals(key)) {
                    if (TextUtils.isEmpty(fields.getJSONObject(i).optString("value"))) {
                        fields.getJSONObject(i).put("value", MOTHER_DEFAULT_DOB);
                    }
                }
            }

            JSONObject metadata = getJSONObject(jsonForm, METADATA);

            Client baseClient = JsonFormUtils.createBaseClient(fields, entityId);
            Event e = JsonFormUtils.createEvent(openSrpContext, fields, metadata, entityId, encounterType, providerId, bindType);

            Client subFormClient = null;

            JSONObject lookUpJSONObject = getJSONObject(metadata, "look_up");
            String lookUpEntityId = "";
            String lookUpBaseEntityId = "";
            if (lookUpJSONObject != null) {
                lookUpEntityId = getString(lookUpJSONObject, "entity_id");
                lookUpBaseEntityId = getString(lookUpJSONObject, "value");
            }

            if ("mother".equals(lookUpEntityId) && StringUtils.isNotBlank(lookUpBaseEntityId)) {
                Client ss = new Client(lookUpBaseEntityId);
                addRelationship(context, ss, baseClient);
            }

            if (StringUtils.isNotBlank(subBindType)) {
                subFormClient = JsonFormUtils.createSubformClient(context, fields, baseClient, subBindType, relationalId);
            }
            Event se = null;
            if (subFormClient != null && e != null) {
                JSONObject subBindTypeJson = getJSONObject(jsonForm, subBindType);
                if (subBindTypeJson != null) {
                    String subBindTypeEncounter = getString(subBindTypeJson, ENCOUNTER_TYPE);
                    if (StringUtils.isNotBlank(subBindTypeEncounter)) {
                        se = JsonFormUtils.createSubFormEvent(null, metadata, e, subFormClient.getBaseEntityId(), subBindTypeEncounter, providerId, subBindType);
                    }
                }
            }
            if (baseClient != null) {
                mergeAndSaveClient(context, baseClient);

            }
            if (e != null) {

                JSONObject eventJson = new JSONObject(gson.toJson(e));
                ecUpdater.addEvent(e.getBaseEntityId(), eventJson);

            }
            if (subFormClient != null) {
                mergeAndSaveClient(context, subFormClient);

            }
            if (se != null) {
                JSONObject eventJson = new JSONObject(gson.toJson(se));
                ecUpdater.addEvent(se.getBaseEntityId(), eventJson);
            }


            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);
            long lastSyncTimeStamp = allSharedPreferences.fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            HPVClientProcessorForJava.getInstance(context).processClient(ecUpdater.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
            allSharedPreferences.saveLastUpdatedAtDate(lastSyncDate.getTime());

            String imageLocation = getFieldValue(fields, imageKey);
            if (!TextUtils.isEmpty(imageLocation)) {
                saveImage(context, providerId, entityId, imageLocation);
            }

            // Unassign current id
            if (baseClient != null) {
                String newZeirId = baseClient.getIdentifier(ZEIR_ID).replace("-", "");
                String currentZeirId = getString(jsonForm, "current_zeir_id").replace("-", "");
                if (!newZeirId.equals(currentZeirId)) {
                    //ZEIR_ID was changed
                    HpvApplication.getInstance().uniqueIdRepository().open(currentZeirId);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
    }

    private static void mergeAndSaveClient(Context context, Client baseClient) throws Exception {
        ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);

        JSONObject updatedClientJson = new JSONObject(gson.toJson(baseClient));

        JSONObject originalClientJsonObject = ecUpdater.getClient(baseClient.getBaseEntityId());

        JSONObject mergedJson = merge(originalClientJsonObject, updatedClientJson);

        //TODO Save edit log

        //save the updated client (the one updated and generated from the form) as EditClient to keep an edit log of the client doc
        // originalClient.setType("PristineClient");
        //originalClient.setRev(null);
        //cloudantDataHandler.addClient(originalClient);

        ecUpdater.addClient(baseClient.getBaseEntityId(), mergedJson);


    }


    public static void saveImage(Context context, String providerId, String entityId, String imageLocation) {
        if (StringUtils.isBlank(imageLocation)) {
            return;
        }


        File file = new File(imageLocation);

        if (!file.exists()) {
            return;
        }

        Bitmap compressedImageFile = Compressor.getDefault(context).compressToBitmap(file);
        saveStaticImageToDisk(compressedImageFile, providerId, entityId);

    }

    private static void saveStaticImageToDisk(Bitmap image, String providerId, String entityId) {
        if (image == null || StringUtils.isBlank(providerId) || StringUtils.isBlank(entityId)) {
            return;
        }
        OutputStream os = null;
        try {

            if (entityId != null && !entityId.isEmpty()) {
                final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";

                File outputFile = new File(absoluteFileName);
                os = new FileOutputStream(outputFile);
                Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                if (compressFormat != null) {
                    image.compress(compressFormat, 100, os);
                } else {
                    throw new IllegalArgumentException("Failed to save static image, could not retrieve image compression format from name "
                            + absoluteFileName);
                }
                // insert into the db
                ProfileImage profileImage = new ProfileImage();
                profileImage.setImageid(UUID.randomUUID().toString());
                profileImage.setAnmId(providerId);
                profileImage.setEntityID(entityId);
                profileImage.setFilepath(absoluteFileName);
                profileImage.setFilecategory("profilepic");
                profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);
                ImageRepository imageRepo = HpvApplication.getInstance().getContext().imageRepository();
                imageRepo.add(profileImage);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to save static image to disk");
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close static images output stream after attempting to write image");
                }
            }
        }

    }

    private static Client createBaseClient(JSONArray fields, String entityId) {

        String firstName = getFieldValue(fields, FormEntityConstants.Person.first_name);
        String middleName = getFieldValue(fields, FormEntityConstants.Person.middle_name);
        String lastName = getFieldValue(fields, FormEntityConstants.Person.last_name);
        String bd = getFieldValue(fields, FormEntityConstants.Person.birthdate);
        Date birthdate = formatDate(bd, true);
        String dd = getFieldValue(fields, FormEntityConstants.Person.deathdate);
        Date deathdate = formatDate(dd, true);
        String aproxbd = getFieldValue(fields, FormEntityConstants.Person.birthdate_estimated);
        Boolean birthdateApprox = false;
        if (!StringUtils.isEmpty(aproxbd) && NumberUtils.isNumber(aproxbd)) {
            int bde = 0;
            try {
                bde = Integer.parseInt(aproxbd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            birthdateApprox = bde > 0;
        }
        String aproxdd = getFieldValue(fields, FormEntityConstants.Person.deathdate_estimated);
        Boolean deathdateApprox = false;
        if (!StringUtils.isEmpty(aproxdd) && NumberUtils.isNumber(aproxdd)) {
            int dde = 0;
            try {
                dde = Integer.parseInt(aproxdd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            deathdateApprox = dde > 0;
        }
        String gender = getFieldValue(fields, FormEntityConstants.Person.gender);

        List<Address> addresses = new ArrayList<>(extractAddresses(fields).values());

        Client c = (Client) new Client(entityId)
                .withFirstName(firstName)
                .withMiddleName(middleName)
                .withLastName(lastName)
                .withBirthdate((birthdate != null ? birthdate : null), birthdateApprox)
                .withDeathdate(deathdate != null ? deathdate : null, deathdateApprox)
                .withGender(gender).withDateCreated(new Date());

        c.withAddresses(addresses)
                .withAttributes(extractAttributes(fields))
                .withIdentifiers(extractIdentifiers(fields));
        return c;

    }

    private static Event createEvent(org.smartregister.Context openSrpContext,
                                     JSONArray fields, JSONObject metadata, String entityId,
                                     String encounterType, String providerId, String bindType) {

        String encounterDateField = getFieldValue(fields, FormEntityConstants.Encounter.encounter_date);
        String encounterLocation = null;

        Date encounterDate = new Date();
        if (StringUtils.isNotBlank(encounterDateField)) {
            Date dateTime = formatDate(encounterDateField, false);
            if (dateTime != null) {
                encounterDate = dateTime;
            }
        }
        try {
            encounterLocation = metadata.getString("encounter_location");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }

        Event e = (Event) new Event()
                .withBaseEntityId(entityId)//should be different for main and subform
                .withEventDate(encounterDate)
                .withEventType(encounterType)
                .withLocationId(encounterLocation)
                .withProviderId(providerId)
                .withEntityType(bindType)
                .withFormSubmissionId(generateRandomUUIDString())
                .withDateCreated(new Date());

        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            String value = getString(jsonObject, VALUE);
            if (StringUtils.isNotBlank(value)) {
                addObservation(e, jsonObject);
            }
        }

        if (metadata != null) {
            Iterator<?> keys = metadata.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject jsonObject = getJSONObject(metadata, key);
                String value = getString(jsonObject, VALUE);
                if (StringUtils.isNotBlank(value)) {
                    String entityVal = getString(jsonObject, OPENMRS_ENTITY);
                    if (entityVal != null) {
                        if (entityVal.equals(CONCEPT)) {
                            addToJSONObject(jsonObject, KEY, key);
                            addObservation(e, jsonObject);
                        } else if (entityVal.equals(ENCOUNTER)) {
                            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
                            if (entityIdVal.equals(FormEntityConstants.Encounter.encounter_date.name())) {
                                Date eDate = formatDate(value, false);
                                if (eDate != null) {
                                    e.setEventDate(eDate);
                                }
                            }
                        }
                    }
                }
            }
        }

        return e;

    }

    public static void fillIdentifiers(Map<String, String> pids, JSONObject jsonObject) {

        String value = getString(jsonObject, VALUE);
        if (StringUtils.isBlank(value)) {
            return;
        }

        if (StringUtils.isNotBlank(getString(jsonObject, ENTITY_ID))) {
            return;
        }

        String entity = PERSON_INDENTIFIER;
        String entityVal = getString(jsonObject, OPENMRS_ENTITY);

        if (entityVal != null && entityVal.equals(entity)) {
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);

            if (entityIdVal.equals(ZEIR_ID)) {
                value = formatChildUniqueId(value);
            }

            pids.put(entityIdVal, value);
        }

    }

    /**
     * This method formats the child unique id obtained from a JSON Form to something that is useable
     *
     * @param unformattedId The unformatted unique identifier
     * @return A formatted ID or the original id if method is unable to format
     */
    private static String formatChildUniqueId(String unformattedId) {
        if (StringUtils.isNotBlank(unformattedId) && !unformattedId.contains("-")) {
            StringBuilder stringBuilder = new StringBuilder(unformattedId);
            stringBuilder.insert(unformattedId.length() - 1, '-');
            unformattedId = stringBuilder.toString();
        }

        return unformattedId;
    }


    private static Client createSubformClient(Context context, JSONArray fields, Client parent, String bindType, String relationalId) throws ParseException {

        if (StringUtils.isBlank(bindType)) {
            return null;
        }

        String entityId = relationalId == null ? generateRandomUUIDString() : relationalId;
        String firstName = getSubFormFieldValue(fields, FormEntityConstants.Person.first_name, bindType);
        String gender = getSubFormFieldValue(fields, FormEntityConstants.Person.gender, bindType);
        String bb = getSubFormFieldValue(fields, FormEntityConstants.Person.birthdate, bindType);

        Map<String, String> idents = extractIdentifiers(fields, bindType);
        String parentIdentifier = parent.getIdentifier(ZEIR_ID);
        if (StringUtils.isNotBlank(parentIdentifier)) {
            String identifier = parentIdentifier.concat("_").concat(bindType);
            idents.put(M_ZEIR_ID, identifier);
        }

        String middleName = getSubFormFieldValue(fields, FormEntityConstants.Person.middle_name, bindType);
        String lastName = getSubFormFieldValue(fields, FormEntityConstants.Person.last_name, bindType);
        Date birthdate = formatDate(bb, true);
        String dd = getSubFormFieldValue(fields, FormEntityConstants.Person.deathdate, bindType);
        Date deathdate = formatDate(dd, true);
        String aproxbd = getSubFormFieldValue(fields, FormEntityConstants.Person.birthdate_estimated, bindType);
        Boolean birthdateApprox = false;
        if (!StringUtils.isEmpty(aproxbd) && NumberUtils.isNumber(aproxbd)) {
            int bde = 0;
            try {
                bde = Integer.parseInt(aproxbd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            birthdateApprox = bde > 0;
        }
        String aproxdd = getSubFormFieldValue(fields, FormEntityConstants.Person.deathdate_estimated, bindType);
        Boolean deathdateApprox = false;
        if (!StringUtils.isEmpty(aproxdd) && NumberUtils.isNumber(aproxdd)) {
            int dde = 0;
            try {
                dde = Integer.parseInt(aproxdd);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            deathdateApprox = dde > 0;
        }

        List<Address> addresses = new ArrayList<>(extractAddresses(fields, bindType).values());

        Client c = (Client) new Client(entityId)
                .withFirstName(firstName)
                .withMiddleName(middleName)
                .withLastName(lastName)
                .withBirthdate(birthdate, birthdateApprox)
                .withDeathdate(deathdate, deathdateApprox)
                .withGender(gender).withDateCreated(new Date());

        c.withAddresses(addresses)
                .withAttributes(extractAttributes(fields, bindType))
                .withIdentifiers(idents);

        if (addresses.isEmpty()) {
            c.withAddresses(parent.getAddresses());
        }

        addRelationship(context, c, parent);

        return c;
    }

    private static Event createSubFormEvent(JSONArray fields, JSONObject metadata, Event parent, String entityId, String encounterType, String providerId, String bindType) {


        Event e = (Event) new Event()
                .withBaseEntityId(entityId)//should be different for main and subform
                .withEventDate(parent.getEventDate())
                .withEventType(encounterType)
                .withLocationId(parent.getLocationId())
                .withProviderId(providerId)
                .withEntityType(bindType)
                .withFormSubmissionId(generateRandomUUIDString())
                .withDateCreated(new Date());

        if (fields != null && fields.length() != 0)
            for (int i = 0; i < fields.length(); i++) {
                JSONObject jsonObject = getJSONObject(fields, i);
                String value = getString(jsonObject, VALUE);
                if (StringUtils.isNotBlank(value)) {
                    addObservation(e, jsonObject);
                }
            }

        if (metadata != null) {
            Iterator<?> keys = metadata.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                JSONObject jsonObject = getJSONObject(metadata, key);
                String value = getString(jsonObject, VALUE);
                if (StringUtils.isNotBlank(value)) {
                    String entityVal = getString(jsonObject, OPENMRS_ENTITY);
                    if (entityVal != null) {
                        if (entityVal.equals(CONCEPT)) {
                            addToJSONObject(jsonObject, KEY, key);
                            addObservation(e, jsonObject);
                        } else if (entityVal.equals(ENCOUNTER)) {
                            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
                            if (entityIdVal.equals(FormEntityConstants.Encounter.encounter_date.name())) {
                                Date eDate = formatDate(value, false);
                                if (eDate != null) {
                                    e.setEventDate(eDate);
                                }
                            }
                        }
                    }
                }
            }
        }

        return e;

    }

    private static void addRelationship(Context context, Client parent, Client child) {
        try {
            String relationships = AssetHandler.readFileFromAssetsFolder(FormUtils.ecClientRelationships, context);
            JSONArray jsonArray = null;

            jsonArray = new JSONArray(relationships);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject rObject = jsonArray.getJSONObject(i);
                if (rObject.has("field") && getString(rObject, "field").equals(ENTITY_ID)) {
                    child.addRelationship(rObject.getString("client_relationship"), parent.getBaseEntityId());
                } /* else {
                    //TODO how to add other kind of relationships
                  } */
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    public static void fillSubFormIdentifiers(Map<String, String> pids, JSONObject jsonObject, String bindType) {

        String value = getString(jsonObject, VALUE);
        if (StringUtils.isBlank(value)) {
            return;
        }

        String bind = getString(jsonObject, ENTITY_ID);
        if (bind == null || !bind.equals(bindType)) {
            return;
        }

        String entity = PERSON_INDENTIFIER;
        String entityVal = getString(jsonObject, OPENMRS_ENTITY);

        if (entityVal != null && entityVal.equals(entity)) {
            String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);

            if (entityIdVal.equals(ZEIR_ID) && StringUtils.isNotBlank(value) && !value.contains("-")) {
                StringBuilder stringBuilder = new StringBuilder(value);
                stringBuilder.insert(value.length() - 1, '-');
                value = stringBuilder.toString();
            }

            pids.put(entityIdVal, value);
        }
    }

    public static Map<String, String> extractIdentifiers(JSONArray fields) {
        Map<String, String> pids = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillIdentifiers(pids, jsonObject);
        }
        return pids;
    }


    public static Map<String, String> extractIdentifiers(JSONArray fields, String bindType) {
        Map<String, String> pids = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillSubFormIdentifiers(pids, jsonObject, bindType);
        }
        return pids;
    }


    private static JSONArray generateLocationHierarchyTree(org.smartregister.Context context, boolean withOtherOption, ArrayList<String> allowedLevels) {
        JSONArray array = new JSONArray();
        try {
            JSONObject locationData = new JSONObject(context.anmLocationController().get());
            if (locationData.has(LOCATION_HIERARCHY)
                    && locationData.getJSONObject(LOCATION_HIERARCHY).has(MAP)) {
                JSONObject map = locationData.getJSONObject(LOCATION_HIERARCHY).getJSONObject(MAP);
                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    getFormJsonData(array, map.getJSONObject(curKey), allowedLevels);
                }
            }

            array = sortTreeViewQuestionOptions(array);
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        if (withOtherOption) {
            try {
                JSONObject other = new JSONObject();
                other.put("name", "Other");
                other.put("key", "Other");
                other.put("level", "");
                array.put(other);
            } catch (JSONException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
        return array;
    }

    public static JSONArray generateDefaultLocationHierarchy(org.smartregister.Context context, ArrayList<String> allowedLevels) {
        try {
            String defaultLocationUuid = context.allSharedPreferences()
                    .fetchDefaultLocalityId(context.allSharedPreferences().fetchRegisteredANM());
            JSONObject locationData = new JSONObject(context.anmLocationController().get());
            if (locationData.has(LOCATION_HIERARCHY)
                    && locationData.getJSONObject(LOCATION_HIERARCHY).has(MAP)) {
                JSONObject map = locationData.getJSONObject(LOCATION_HIERARCHY).getJSONObject(MAP);
                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    JSONArray curResult = getDefaultLocationHierarchy(defaultLocationUuid, map.getJSONObject(curKey), new JSONArray(), allowedLevels);
                    if (curResult != null) {
                        return curResult;
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    private static JSONArray getDefaultLocationHierarchy(String defaultLocationUuid, JSONObject openMrsLocationData, JSONArray parents, ArrayList<String> allowedLevels) throws JSONException {
        JSONArray levels = openMrsLocationData.getJSONObject("node").getJSONArray("tags");
        for (int i = 0; i < levels.length(); i++) {
            if (allowedLevels.contains(levels.getString(i))) {
                parents.put(openMrsLocationData.getJSONObject("node").getString("name"));
            }
        }

        if (openMrsLocationData.getJSONObject("node").getString("locationId").equals(defaultLocationUuid)) {
            return parents;
        }

        if (openMrsLocationData.has("children")) {
            Iterator<String> childIterator = openMrsLocationData.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                JSONArray curResult = getDefaultLocationHierarchy(defaultLocationUuid, openMrsLocationData.getJSONObject("children").getJSONObject(curChildKey), new JSONArray(parents.toString()), allowedLevels);
                if (curResult != null) return curResult;
            }
        }

        return null;
    }

    private static void getFormJsonData(JSONArray allLocationData, JSONObject openMrsLocationData, ArrayList<String> allowedLevels) throws JSONException {
        JSONObject jsonFormObject = new JSONObject();
        String name = openMrsLocationData.getJSONObject("node").getString("name");
        jsonFormObject.put("name", getOpenMrsReadableName(name));
        jsonFormObject.put("key", name);

        JSONArray levels = openMrsLocationData.getJSONObject("node").getJSONArray("tags");
        jsonFormObject.put("level", "");

        JSONArray children = new JSONArray();
        if (openMrsLocationData.has("children")) {
            Iterator<String> childIterator = openMrsLocationData.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                getFormJsonData(children, openMrsLocationData.getJSONObject("children").getJSONObject(curChildKey), allowedLevels);
            }

            boolean allowed = false;
            for (int i = 0; i < levels.length(); i++) {
                if (allowedLevels.contains(levels.getString(i))) {
                    jsonFormObject.put("nodes", children);
                    allowed = true;
                }
            }

            if (!allowed) {
                for (int i = 0; i < children.length(); i++) {
                    allLocationData.put(children.getJSONObject(i));
                }
            }
        }

        for (int i = 0; i < levels.length(); i++) {
            if (allowedLevels.contains(levels.getString(i))) {
                allLocationData.put(jsonFormObject);
            }
        }
    }

    public static String getOpenMrsReadableName(String name) {
        if (name == null) {
            return "";
        }

        String readableName = new String(name);

        Pattern prefixPattern = Pattern.compile("^[a-z]{2} (.*)$");
        Matcher prefixMatcher = prefixPattern.matcher(readableName);
        if (prefixMatcher.find()) {
            readableName = prefixMatcher.group(1);
        }

        if (readableName.contains(":")) {
            String[] splitName = readableName.split(":");
            readableName = splitName[splitName.length - 1].trim();
        }

        return readableName;
    }

    public static void addChildRegLocHierarchyQuestions(JSONObject form,
                                                        org.smartregister.Context context) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            ArrayList<String> allLevels = new ArrayList<>();
            allLevels.add("Country");
            allLevels.add("Province");
            allLevels.add("District");
            allLevels.add("Health Facility");
            allLevels.add("Zone");
            allLevels.add("Residential Area");

            ArrayList<String> healthFacilities = new ArrayList<>();
            healthFacilities.add("Country");
            healthFacilities.add("Province");
            healthFacilities.add("District");
            healthFacilities.add("Health Facility");

            JSONArray defaultLocation = generateDefaultLocationHierarchy(context, allLevels);
            JSONArray defaultFacility = generateDefaultLocationHierarchy(context, healthFacilities);
            JSONArray upToFacilities = generateLocationHierarchyTree(context, false, healthFacilities);
            JSONArray upToFacilitiesWithOther = generateLocationHierarchyTree(context, true, healthFacilities);
            JSONArray entireTree = generateLocationHierarchyTree(context, true, allLevels);

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString("key").equals("Home_Facility")) {
                    questions.getJSONObject(i).put("tree", new JSONArray(upToFacilities.toString()));
                    if (defaultFacility != null) {
                        questions.getJSONObject(i).put("default", defaultFacility.toString());
                    }
                } else if (questions.getJSONObject(i).getString("key").equals("Birth_Facility_Name")) {
                    questions.getJSONObject(i).put("tree", new JSONArray(upToFacilitiesWithOther.toString()));
                    if (defaultFacility != null) {
                        questions.getJSONObject(i).put("default", defaultFacility.toString());
                    }
                } else if (questions.getJSONObject(i).getString("key").equals("Residential_Area")) {
                    questions.getJSONObject(i).put("tree", new JSONArray(entireTree.toString()));
                    if (defaultLocation != null) {
                        questions.getJSONObject(i).put("default", defaultLocation.toString());
                    }
                }
            }
        } catch (JSONException e) {
            //Log.e(TAG, Log.getStackTraceString(e));
        }
    }


    public static String getOpenMrsLocationId(org.smartregister.Context context,
                                              String locationName) throws JSONException {
        String response = locationName;

        if (locationName != null) {
            JSONObject locationData = new JSONObject(context.anmLocationController().get());
            if (locationData.has(LOCATION_HIERARCHY)
                    && locationData.getJSONObject(LOCATION_HIERARCHY).has(MAP)) {
                JSONObject map = locationData.getJSONObject(LOCATION_HIERARCHY).getJSONObject(MAP);
                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String curKey = keys.next();
                    String curResult = getOpenMrsLocationId(locationName, map.getJSONObject(curKey));

                    if (curResult != null) {
                        response = curResult;
                        break;
                    }
                }
            }
        }

        return response;
    }

    private static String getOpenMrsLocationId(String locationName, JSONObject openMrsLocations)
            throws JSONException {
        String name = openMrsLocations.getJSONObject("node").getString("name");

        if (locationName.equals(name)) {
            return openMrsLocations.getJSONObject("node").getString("locationId");
        }

        if (openMrsLocations.has("children")) {
            Iterator<String> childIterator = openMrsLocations.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                String curResult = getOpenMrsLocationId(locationName,
                        openMrsLocations.getJSONObject("children").getJSONObject(curChildKey));
                if (curResult != null) {
                    return curResult;
                }
            }
        }

        return null;
    }

    /**
     * This method returns the name hierarchy of a location given it's id
     *
     * @param context
     * @param locationId The ID for the location we want the hierarchy for
     * @return The name hierarchy (starting with the top-most parent) for the location or {@code NULL} if location id is not found
     */
    public static JSONArray getOpenMrsLocationHierarchy(org.smartregister.Context context,
                                                        String locationId) {
        JSONArray response = null;

        try {
            if (locationId != null) {
                JSONObject locationData = new JSONObject(context.anmLocationController().get());
                Log.d(TAG, "Location data is " + locationData);
                if (locationData.has(LOCATION_HIERARCHY)
                        && locationData.getJSONObject(LOCATION_HIERARCHY).has(MAP)) {
                    JSONObject map = locationData.getJSONObject(LOCATION_HIERARCHY).getJSONObject(MAP);
                    Iterator<String> keys = map.keys();
                    while (keys.hasNext()) {
                        String curKey = keys.next();
                        JSONArray curResult = getOpenMrsLocationHierarchy(locationId, map.getJSONObject(curKey), new JSONArray());

                        if (curResult != null) {
                            response = curResult;
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "locationData doesn't have locationHierarchy");
                }
            } else {
                Log.e(TAG, "Location id is null");
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return response;
    }

    private static JSONArray getOpenMrsLocationHierarchy(String locationId,
                                                         JSONObject openMrsLocation,
                                                         JSONArray parents) throws JSONException {
        JSONArray hierarchy = new JSONArray(parents.toString());
        hierarchy.put(openMrsLocation.getJSONObject("node").getString("name"));
        String id = openMrsLocation.getJSONObject("node").getString("locationId");
        Log.d(TAG, "Current location id is " + id);
        if (locationId.equals(id)) {
            return hierarchy;
        }

        if (openMrsLocation.has("children")) {
            Iterator<String> childIterator = openMrsLocation.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                JSONArray curResult = getOpenMrsLocationHierarchy(locationId,
                        openMrsLocation.getJSONObject("children").getJSONObject(curChildKey),
                        hierarchy);
                if (curResult != null) return curResult;
            }
        } else {
            Log.d(TAG, id + " does not have children");
        }

        return null;
    }

    public static String getOpenMrsLocationName(org.smartregister.Context context,
                                                String locationId) {
        String response = locationId;
        try {
            if (locationId != null) {
                JSONObject locationData = new JSONObject(context.anmLocationController().get());
                Log.d(TAG, "Location data is " + locationData);
                if (locationData.has(LOCATION_HIERARCHY)
                        && locationData.getJSONObject(LOCATION_HIERARCHY).has(MAP)) {
                    JSONObject map = locationData.getJSONObject(LOCATION_HIERARCHY).getJSONObject(MAP);
                    Iterator<String> keys = map.keys();
                    while (keys.hasNext()) {
                        String curKey = keys.next();
                        String curResult = getOpenMrsLocationName(locationId, map.getJSONObject(curKey));

                        if (curResult != null) {
                            response = curResult;
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "locationData doesn't have locationHierarchy");
                }
            } else {
                Log.e(TAG, "Location id is null");
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return response;
    }

    private static String getOpenMrsLocationName(String locationId, JSONObject openMrsLocations)
            throws JSONException {
        String id = openMrsLocations.getJSONObject("node").getString("locationId");
        Log.d(TAG, "Current location id is " + id);
        if (locationId.equals(id)) {
            return openMrsLocations.getJSONObject("node").getString("name");
        }

        if (openMrsLocations.has("children")) {
            Iterator<String> childIterator = openMrsLocations.getJSONObject("children").keys();
            while (childIterator.hasNext()) {
                String curChildKey = childIterator.next();
                String curResult = getOpenMrsLocationName(locationId,
                        openMrsLocations.getJSONObject("children").getJSONObject(curChildKey));
                if (curResult != null) {
                    return curResult;
                }
            }
        } else {
            Log.d(TAG, id + " does not have children");
        }

        return null;
    }

    /**
     * Starts an instance of JsonFormActivity with the provided form details
     *
     * @param context                     The activity form is being launched from
     * @param openSrpContext              Current OpenSRP context
     * @param jsonFormActivityRequestCode The request code to be used to launch {@link HpvJsonFormActivity}
     * @param formName                    The name of the form to launch
     * @param entityId                    The unique entity id for the form (e.g child's ZEIR id)
     * @param metaData                    The form's meta data
     * @param currentLocationId           OpenMRS id for the current device's location
     * @throws Exception
     */
    public static void startForm(Activity context, org.smartregister.Context openSrpContext,
                                 int jsonFormActivityRequestCode,
                                 String formName, String entityId, String metaData,
                                 String currentLocationId) throws Exception {
        Intent intent = new Intent(context, HpvJsonFormActivity.class);

        JSONObject form = FormUtils.getInstance(context).getFormJson(formName);
        if (form != null) {
            form.getJSONObject("metadata").put("encounter_location", currentLocationId);

            if ("child_enrollment".equals(formName)) {
                if (StringUtils.isBlank(entityId)) {
                    UniqueIdRepository uniqueIdRepo = HpvApplication.getInstance().uniqueIdRepository();
                    entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                    if (entityId.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (StringUtils.isNotBlank(entityId)) {
                    entityId = entityId.replace("-", "");
                }

                JsonFormUtils.addChildRegLocHierarchyQuestions(form, openSrpContext);

                // Inject zeir id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase(JsonFormUtils.ZEIR_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                    }
                }
            } else {
                Log.w(TAG, "Unsupported form requested for launch " + formName);
            }

            intent.putExtra("json", form.toString());
            Log.d(TAG, "form is " + form.toString());
            context.startActivityForResult(intent, jsonFormActivityRequestCode);
        }
    }

    public static Event addMetaData(Context context, Event event, Date start) throws JSONException {
        Map<String, String> metaFields = new HashMap<>();
        metaFields.put("deviceid", "163149AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        metaFields.put("end", "163138AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        metaFields.put("start", "163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        Calendar calendar = Calendar.getInstance();

        String end = DATE_TIME_FORMAT.format(calendar.getTime());

        Obs obs = new Obs();
        obs.setFieldCode("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        obs.setValue(DATE_TIME_FORMAT.format(start));
        obs.setFieldType("concept");
        obs.setFieldDataType("start");
        event.addObs(obs);


        obs.setFieldCode("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        obs.setValue(end);
        obs.setFieldDataType("end");
        event.addObs(obs);

        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        String deviceId = mTelephonyManager.getSimSerialNumber();

        obs.setFieldCode("163137AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        obs.setValue(deviceId);
        obs.setFieldDataType("deviceid");
        event.addObs(obs);
        return event;
    }

    /**
     * This method sorts the options provided for a native form tree view question
     *
     * @return The sorted options
     */
    private static JSONArray sortTreeViewQuestionOptions(JSONArray treeViewOptions) throws JSONException {
        JSONArray sortedTree = new JSONArray();

        HashMap<String, JSONObject> sortMap = new HashMap<>();
        for (int i = 0; i < treeViewOptions.length(); i++) {
            sortMap.put(treeViewOptions.getJSONObject(i).getString("name"), treeViewOptions.getJSONObject(i));
        }

        ArrayList<String> sortedKeys = new ArrayList<>(sortMap.keySet());
        Collections.sort(sortedKeys);

        for (String curOptionName : sortedKeys) {
            JSONObject curOption = sortMap.get(curOptionName);
            if (curOption.has("nodes")) {
                curOption.put("nodes", sortTreeViewQuestionOptions(curOption.getJSONArray("nodes")));
            }

            sortedTree.put(curOption);
        }

        return sortedTree;
    }


    ////////////////////////////////////////////////////////////////
    // Inner classes
    ////////////////////////////////////////////////////////////////


    private static class SaveBirthRegistrationTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private org.smartregister.Context openSrpContext;
        private String jsonString;
        private String providerId;
        private String imageKey;
        private String bindType;
        private String subBindType;

        private SaveBirthRegistrationTask(Context context, org.smartregister.Context openSrpContext,
                                          String jsonString, String providerId, String imageKey, String bindType,
                                          String subBindType) {
            this.context = context;
            this.openSrpContext = openSrpContext;
            this.jsonString = jsonString;
            this.providerId = providerId;
            this.imageKey = imageKey;
            this.bindType = bindType;
            this.subBindType = subBindType;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (context instanceof HomeRegisterActivity) {
                HomeRegisterActivity childSmartRegisterActivity = ((HomeRegisterActivity) context);
                childSmartRegisterActivity.refreshList(FetchStatus.fetched);
                childSmartRegisterActivity.hideProgressDialog();
            }
        }

        @Override
        protected void onPreExecute() {
            if (context instanceof HomeRegisterActivity) {
                ((HomeRegisterActivity) context).showProgressDialog();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                AllSharedPreferences allSharedPreferences = new AllSharedPreferences(preferences);

                JSONObject jsonForm = new JSONObject(jsonString);

                String entityId = getString(jsonForm, ENTITY_ID);
                if (StringUtils.isBlank(entityId)) {
                    entityId = generateRandomUUIDString();
                }

                JSONArray fields = fields(jsonForm);
                if (fields == null) {
                    return null;
                }

                String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
                JSONObject metadata = getJSONObject(jsonForm, METADATA);

                // Replace values for location questions with their corresponding location IDs
                for (int i = 0; i < fields.length(); i++) {
                    String key = fields.getJSONObject(i).getString("key");
                    if ("Home_Facility".equals(key)
                            || "Birth_Facility_Name".equals(key)
                            || "Residential_Area".equals(key)) {
                        try {
                            String rawValue = fields.getJSONObject(i).getString("value");
                            JSONArray valueArray = new JSONArray(rawValue);
                            if (valueArray.length() > 0) {
                                String lastLocationName = valueArray.getString(valueArray.length() - 1);
                                String lastLocationId = getOpenMrsLocationId(openSrpContext, lastLocationName);
                                fields.getJSONObject(i).put("value", lastLocationId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    } else if ("Mother_Guardian_Date_Birth".equals(key)) {
                        if (TextUtils.isEmpty(fields.getJSONObject(i).optString("value"))) {
                            fields.getJSONObject(i).put("value", MOTHER_DEFAULT_DOB);
                        }
                    }
                }

                JSONObject lookUpJSONObject = getJSONObject(metadata, "look_up");
                String lookUpEntityId = "";
                String lookUpBaseEntityId = "";
                if (lookUpJSONObject != null) {
                    lookUpEntityId = getString(lookUpJSONObject, "entity_id");
                    lookUpBaseEntityId = getString(lookUpJSONObject, "value");
                }
                Client baseClient = JsonFormUtils.createBaseClient(fields, entityId);
                Event baseEvent = JsonFormUtils.createEvent(openSrpContext, fields, metadata, entityId, encounterType, providerId, bindType);

                Client subformClient = null;
                Event subformEvent = null;
                if ("mother".equals(lookUpEntityId) && StringUtils.isNotBlank(lookUpBaseEntityId)) {
                    Client motherClient = new Client(lookUpBaseEntityId);
                    addRelationship(context, motherClient, baseClient);
                } else {
                    if (StringUtils.isNotBlank(subBindType)) {
                        subformClient = JsonFormUtils.createSubformClient(context, fields, baseClient, subBindType, null);
                    }

                    if (subformClient != null && baseEvent != null) {
                        JSONObject subBindTypeJson = getJSONObject(jsonForm, subBindType);
                        if (subBindTypeJson != null) {
                            String subBindTypeEncounter = getString(subBindTypeJson, ENCOUNTER_TYPE);
                            if (StringUtils.isNotBlank(subBindTypeEncounter)) {
                                subformEvent = JsonFormUtils.createSubFormEvent(null, metadata, baseEvent, subformClient.getBaseEntityId(), subBindTypeEncounter, providerId, subBindType);
                            }
                        }
                    }
                }

                if (baseClient != null) {
                    JSONObject clientJson = new JSONObject(gson.toJson(baseClient));
                    ecUpdater.addClient(baseClient.getBaseEntityId(), clientJson);
                }

                if (baseEvent != null) {
                    JSONObject eventJson = new JSONObject(gson.toJson(baseEvent));
                    ecUpdater.addEvent(baseEvent.getBaseEntityId(), eventJson);
                }

                if (subformClient != null) {
                    JSONObject clientJson = new JSONObject(gson.toJson(subformClient));
                    ecUpdater.addClient(subformClient.getBaseEntityId(), clientJson);
                }

                if (subformEvent != null) {
                    JSONObject eventJson = new JSONObject(gson.toJson(subformEvent));
                    ecUpdater.addEvent(subformEvent.getBaseEntityId(), eventJson);
                }

                String zeirId = baseClient.getIdentifier(ZEIR_ID);
                //mark zeir id as used
                HpvApplication.getInstance().uniqueIdRepository().close(zeirId);

                String imageLocation = getFieldValue(fields, imageKey);
                saveImage(context, providerId, entityId, imageLocation);

                long lastSyncTimeStamp = allSharedPreferences.fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);
                HPVClientProcessorForJava.getInstance(context).processClient(ecUpdater.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
                allSharedPreferences.saveLastUpdatedAtDate(lastSyncDate.getTime());
            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return null;
        }
    }
}