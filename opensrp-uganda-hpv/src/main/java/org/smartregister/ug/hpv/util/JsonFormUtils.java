package org.smartregister.ug.hpv.util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.common.reflect.TypeToken;
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
import org.smartregister.commonregistry.AllCommonsRepository;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.domain.FetchStatus;
import org.smartregister.domain.Photo;
import org.smartregister.domain.ProfileImage;
import org.smartregister.location.helper.LocationHelper;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.repository.BaseRepository;
import org.smartregister.repository.EventClientRepository;
import org.smartregister.repository.ImageRepository;
import org.smartregister.sync.ClientProcessor;
import org.smartregister.ug.hpv.R;
import org.smartregister.ug.hpv.activity.HomeRegisterActivity;
import org.smartregister.ug.hpv.activity.HpvJsonFormActivity;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.domain.FormLocation;
import org.smartregister.ug.hpv.event.JsonFormSaveCompleteEvent;
import org.smartregister.ug.hpv.event.PatientRemovedEvent;
import org.smartregister.ug.hpv.helper.ECSyncHelper;
import org.smartregister.ug.hpv.repository.UniqueIdRepository;
import org.smartregister.ug.hpv.sync.HpvClientProcessorForJava;
import org.smartregister.ug.hpv.view.LocationPickerView;
import org.smartregister.util.AssetHandler;
import org.smartregister.util.DateTimeTypeConverter;
import org.smartregister.util.FormUtils;
import org.smartregister.view.activity.DrishtiApplication;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

import static org.smartregister.util.Utils.getValue;

/**
 * Created by ndegwamartin on 19/03/2018.
 */
public class JsonFormUtils extends org.smartregister.util.JsonFormUtils {
    private static final String TAG = JsonFormUtils.class.getCanonicalName();

    private static final String ENCOUNTER = "encounter";
    public static final String ENCOUNTER_TYPE = "encounter_type";
    public static final String CURRENT_OPENSRP_ID = "current_opensrp_id";
    public static final String READ_ONLY = "read_only";
    private static final String METADATA = "metadata";
    public static final String encounterType = "Update Birth Registration";
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
    public static final String ENCOUNTER_LOCATION = "encounter_location";

    public static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").registerTypeAdapter(DateTime.class, new DateTimeTypeConverter()).create();


    public static void saveForm(Context context, org.smartregister.Context openSrpContext,
                                String jsonString, String providerId) {
        try {
            JSONObject form = new JSONObject(jsonString);
            if (form.getString(ENCOUNTER_TYPE).equals(Constants.EventType.REGISTRATION)) {

                saveRegistration(context, openSrpContext, jsonString, providerId, "photo", DBConstants.PATIENT_TABLE_NAME, FORM_MODE.CREATE);

            } else if (form.getString(ENCOUNTER_TYPE).equals(Constants.EventType.UPDATE_REGISTRATION)) {

                saveRegistration(context, openSrpContext, jsonString, providerId, "photo", DBConstants.PATIENT_TABLE_NAME, FORM_MODE.EDIT);

            } else if (form.getString(ENCOUNTER_TYPE).equals(Constants.EventType.REMOVE)) {

                saveRemovedFromRegister(context, openSrpContext, jsonString, providerId, DBConstants.PATIENT_TABLE_NAME);
            }
        } catch (JSONException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private static void saveRegistration(Context context, org.smartregister.Context openSrpContext,
                                         String jsonString, String providerId, String imageKey, String bindType, boolean isEditMode) {
        if (context == null || openSrpContext == null || StringUtils.isBlank(providerId)
                || StringUtils.isBlank(jsonString)) {
            return;
        }

        org.smartregister.util.Utils.startAsyncTask(
                new SaveRegistrationTask(context, openSrpContext, jsonString, providerId, imageKey, bindType, isEditMode), null
        );
    }


    private static void mergeAndSaveClient(Context context, Client baseClient) throws Exception {
        ECSyncHelper ecUpdater = ECSyncHelper.getInstance(context);

        JSONObject updatedClientJson = new JSONObject(gson.toJson(baseClient));

        JSONObject originalClientJsonObject = ecUpdater.getClient(baseClient.getBaseEntityId());

        JSONObject mergedJson = merge(originalClientJsonObject, updatedClientJson);

        //TODO Save edit log ?

        ecUpdater.addClient(baseClient.getBaseEntityId(), mergedJson);


    }


    public static void saveRemovedFromRegister(Context context, org.smartregister.Context openSrpContext,
                                               String jsonString, String providerId, String bindType) {


        try {
            boolean isDeath = false;

            EventClientRepository db = HpvApplication.getInstance().getEventClientRepository();

            JSONObject jsonForm = new JSONObject(jsonString);


            String entityId = getString(jsonForm, ENTITY_ID);
            JSONArray fields = fields(jsonForm);
            if (fields == null) {
                return;
            }


            String encounterType = getString(jsonForm, ENCOUNTER_TYPE);
            JSONObject metadata = getJSONObject(jsonForm, METADATA);


            String encounterLocation = null;


            try {
                encounterLocation = metadata.getString("encounter_location");
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            Date encounterDate = new Date();

            Event event = (Event) new Event()
                    .withBaseEntityId(entityId) //should be different for main and subform
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
                    addObservation(event, jsonObject);
                    isDeath = "Died".equals(value);
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
                                addObservation(event, jsonObject);

                            } else if (entityVal.equals(ENCOUNTER)) {
                                String entityIdVal = getString(jsonObject, OPENMRS_ENTITY_ID);
                                if (entityIdVal.equals(FormEntityConstants.Encounter.encounter_date.name())) {
                                    Date eDate = formatDate(value, false);
                                    if (eDate != null) {
                                        event.setEventDate(eDate);
                                    }
                                }
                            }
                        }
                    }
                }
            }


            if (event != null) {
                JSONObject eventJson = new JSONObject(JsonFormUtils.gson.toJson(event));

                //Update client to deceased
                JSONObject client = db.getClientByBaseEntityId(eventJson.getString(ClientProcessor.baseEntityIdJSONKey));
                if (isDeath) {
                    client.put("deathdate", Utils.getTodaysDate());
                    client.put("deathdateApprox", false);
                }
                JSONObject attributes = client.getJSONObject("attributes");
                attributes.put(DBConstants.KEY.DATE_REMOVED, Utils.getTodaysDate());
                client.put("attributes", attributes);
                db.addorUpdateClient(entityId, client);

                //Add Remove Event for child to flag for Server delete
                db.addEvent(event.getBaseEntityId(), eventJson);

                //Update Child Entity to include death date
                Event updateChildDetailsEvent = (Event) new Event()
                        .withBaseEntityId(entityId) //should be different for main and subform
                        .withEventDate(encounterDate)
                        .withEventType(JsonFormUtils.encounterType)
                        .withLocationId(encounterLocation)
                        .withProviderId(providerId)
                        .withEntityType(bindType)
                        .withFormSubmissionId(generateRandomUUIDString())
                        .withDateCreated(new Date());
                JsonFormUtils.tagSyncMetadata(updateChildDetailsEvent);
                JSONObject eventJsonUpdateChildEvent = new JSONObject(JsonFormUtils.gson.toJson(updateChildDetailsEvent));

                db.addEvent(entityId, eventJsonUpdateChildEvent); //Add event to flag server update

                //Update REGISTER and FTS Tables
                String tableName = DBConstants.PATIENT_TABLE_NAME;
                AllCommonsRepository allCommonsRepository = openSrpContext.allCommonsRepositoryobjects(tableName);
                if (allCommonsRepository != null) {
                    ContentValues values = new ContentValues();
                    values.put(DBConstants.KEY.DATE_REMOVED, Utils.getTodaysDate());
                    allCommonsRepository.update(tableName, values, entityId);
                    allCommonsRepository.updateSearch(entityId);

                }
            }

        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {

            Utils.postStickyEvent(new PatientRemovedEvent());
        }
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
                .withLastName(lastName)
                .withBirthdate((birthdate != null ? birthdate : null), birthdateApprox)
                .withDeathdate(deathdate != null ? deathdate : null, deathdateApprox)
                .withGender(gender).withDateCreated(new Date());

        c.withRelationships(new HashMap<String, List<String>>()).withAddresses(addresses)
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
                .withBaseEntityId(entityId)
                .withEventDate(encounterDate)
                .withEventType(encounterType)
                .withLocationId(encounterLocation)
                .withProviderId(providerId)
                .withEntityType(bindType)
                .withFormSubmissionId(generateRandomUUIDString())
                .withDateCreated(new Date());

        String currLocation = HpvApplication.getInstance().getContext().allSharedPreferences().fetchCurrentLocality();
        LocationHelper.getInstance().setParentAndChildLocationIds(currLocation);

        e.setChildLocationId(LocationHelper.getInstance().getChildLocationId());

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

            if (entityIdVal.equals(DBConstants.KEY.OPENSRP_ID)) {
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


    public static Map<String, String> extractIdentifiers(JSONArray fields) {
        Map<String, String> pids = new HashMap<>();
        for (int i = 0; i < fields.length(); i++) {
            JSONObject jsonObject = getJSONObject(fields, i);
            fillIdentifiers(pids, jsonObject);
        }
        return pids;
    }


    public static void addAdolescentRegLocHierarchyQuestions(JSONObject form) {
        try {
            JSONArray questions = form.getJSONObject("step1").getJSONArray("fields");
            ArrayList<String> allLevels = new ArrayList<>();
            allLevels.add("Country");
            allLevels.add("District");
            allLevels.add("County");
            allLevels.add("Sub-county");
            allLevels.add("Health Facility");
            allLevels.add("School");


            ArrayList<String> healthFacilities = new ArrayList<>();
            healthFacilities.add("School");


            List<String> defaultFacility = LocationHelper.getInstance().generateDefaultLocationHierarchy(healthFacilities);
            List<org.smartregister.domain.form.FormLocation> upToFacilities = LocationHelper.getInstance().generateLocationHierarchyTree(false, healthFacilities);

            String defaultFacilityString = AssetHandler.javaToJsonString(defaultFacility,
                    new TypeToken<List<String>>() {
                    }.getType());

            String upToFacilitiesString = AssetHandler.javaToJsonString(upToFacilities,
                    new TypeToken<List<FormLocation>>() {
                    }.getType());

            for (int i = 0; i < questions.length(); i++) {
                if (questions.getJSONObject(i).getString(Constants.KEY.KEY).equalsIgnoreCase(Utils.SCHOOL)) {
                    if (StringUtils.isNotBlank(upToFacilitiesString)) {
                        questions.getJSONObject(i).put(Constants.KEY.TREE, new JSONArray(upToFacilitiesString));
                    }
                    if (StringUtils.isNotBlank(defaultFacilityString)) {
                        questions.getJSONObject(i).put(Constants.KEY.DEFAULT, defaultFacilityString);
                    }
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * Starts an instance of JsonFormActivity with the provided form details
     *
     * @param context                     The activity form is being launched from
     * @param openSrpContext              Current OpenSRP context
     * @param jsonFormActivityRequestCode The request code to be used to launch {@link HpvJsonFormActivity}
     * @param formName                    The name of the form to launch
     * @param entityId                    The unique entity id for the form (e.g patients's OPENSRP id)
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

            if (Constants.JSON_FORM.PATIENT_REGISTRATION.equals(formName)) {
                if (StringUtils.isBlank(entityId)) {
                    UniqueIdRepository uniqueIdRepo = HpvApplication.getInstance().getUniqueIdRepository();
                    entityId = uniqueIdRepo.getNextUniqueId() != null ? uniqueIdRepo.getNextUniqueId().getOpenmrsId() : "";
                    if (entityId.isEmpty()) {
                        Toast.makeText(context, context.getString(R.string.no_openmrs_id), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                if (StringUtils.isNotBlank(entityId)) {
                    entityId = entityId.replace("-", "");
                }

                JsonFormUtils.addAdolescentRegLocHierarchyQuestions(form);

                // Inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject.getString(JsonFormUtils.KEY)
                            .equalsIgnoreCase(DBConstants.KEY.OPENSRP_ID)) {
                        jsonObject.remove(JsonFormUtils.VALUE);
                        jsonObject.put(JsonFormUtils.VALUE, entityId);
                    }
                }
            } else if (Constants.JSON_FORM.PATIENT_REMOVAL.equals(formName)) {
                if (StringUtils.isNotBlank(entityId)) {
                    // Inject entity id into the remove form
                    form.remove(JsonFormUtils.ENTITY_ID);
                    form.put(JsonFormUtils.ENTITY_ID, entityId);
                }
            } else {
                Log.w(TAG, "Unsupported form requested for launch " + formName);
            }

            intent.putExtra("json", form.toString());
            Log.d(TAG, "form is " + form.toString());
            context.startActivityForResult(intent, jsonFormActivityRequestCode);
        }
    }

    public static void startFormForEdit(Activity context, int jsonFormActivityRequestCode, String metaData) throws Exception {
        Intent intent = new Intent(context, HpvJsonFormActivity.class);
        intent.putExtra("json", metaData);
        Log.d(TAG, "form is " + metaData);
        context.startActivityForResult(intent, jsonFormActivityRequestCode);

    }

    public static String getAutoPopulatedJsonEditFormString(Context context, CommonPersonObjectClient commonPersonObjectClient) {
        try {
            JSONObject form = FormUtils.getInstance(context).getFormJson(Constants.JSON_FORM.PATIENT_REGISTRATION);
            LocationPickerView lpv = new LocationPickerView(context);
            lpv.init();
            JsonFormUtils.addAdolescentRegLocHierarchyQuestions(form);
            Log.d(TAG, "Form is " + form.toString());
            if (form != null) {
                form.put(JsonFormUtils.ENTITY_ID, commonPersonObjectClient.entityId());
                form.put(JsonFormUtils.ENCOUNTER_TYPE, Constants.EventType.UPDATE_REGISTRATION);
                JSONObject metadata = form.getJSONObject(JsonFormUtils.METADATA);
                String lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lpv.getSelectedItem());
                metadata.put(JsonFormUtils.ENCOUNTER_LOCATION, lastLocationId);

                form.put(JsonFormUtils.CURRENT_OPENSRP_ID, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.OPENSRP_ID, true).replace("-", ""));

                //inject opensrp id into the form
                JSONObject stepOne = form.getJSONObject(JsonFormUtils.STEP1);
                JSONArray jsonArray = stepOne.getJSONArray(JsonFormUtils.FIELDS);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.FIRST_NAME)) {
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.FIRST_NAME, true));
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                    }

                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(Constants.KEY.PHOTO)) {

                        Photo photo = ImageUtils.profilePhotoByClientID(commonPersonObjectClient.entityId());

                        if (StringUtils.isNotBlank(photo.getFilePath())) {

                            jsonObject.put(JsonFormUtils.VALUE, photo.getFilePath());
                            jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        }
                    }

                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.FAMILY_NAME)) {
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), "last_name", true));
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase("Sex")) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.GENDER, true));
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.OPENSRP_ID)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.OPENSRP_ID, true).replace("-", ""));
                    }

                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.CARETAKER_NAME)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, false);
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.CARETAKER_NAME, true));
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.CARETAKER_PHONE)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, false);
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.CARETAKER_PHONE, true));
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.VHT_NAME)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, false);
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.VHT_NAME, true));
                    }
                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.VHT_PHONE)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, false);
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.VHT_PHONE, true));
                    }

                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.CLASS)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        jsonObject.put(JsonFormUtils.VALUE, getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.CLASS, true));
                    }


                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.DOB)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);

                        String dobString = getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.DOB, true);
                        Date dob = Utils.dobStringToDate(dobString);
                        if (dob != null) {
                            jsonObject.put(JsonFormUtils.VALUE, DATE_FORMAT.format(dob));
                        }
                    }

                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.SCHOOL)) {
                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                        String school = getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.SCHOOL, true);
                        jsonObject.put(JsonFormUtils.VALUE, school);
                        jsonObject.toString();
                    }

                    if (jsonObject.getString(JsonFormUtils.KEY).equalsIgnoreCase(DBConstants.KEY.SCHOOL)) {
                        List<String> schoolFacilityHierarchy = new ArrayList<>();
                        String address5 = getValue(commonPersonObjectClient.getColumnmaps(), DBConstants.KEY.SCHOOL_NAME, true);
                        schoolFacilityHierarchy.add(address5);

                        String schoolFacilityHierarchyString = AssetHandler.javaToJsonString(schoolFacilityHierarchy, new TypeToken<List<String>>() {
                        }.getType());
                        if (StringUtils.isNotBlank(schoolFacilityHierarchyString)) {
                            jsonObject.put(JsonFormUtils.VALUE, schoolFacilityHierarchyString);
                        }

                        jsonObject.put(JsonFormUtils.READ_ONLY, true);
                    }

                }

                return form.toString();
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

        return "";
    }

    private static Event tagSyncMetadata(Event event) {
        AllSharedPreferences sharedPreferences = HpvApplication.getInstance().getContext().userService().getAllSharedPreferences();
        event.setLocationId(sharedPreferences.fetchDefaultLocalityId(sharedPreferences.fetchRegisteredANM()));
        event.setTeam(sharedPreferences.fetchDefaultTeam(sharedPreferences.fetchRegisteredANM()));
        event.setTeamId(sharedPreferences.fetchDefaultTeamId(sharedPreferences.fetchRegisteredANM()));
        return event;
    }

////////////////////////////////////////////////////////////////
// Inner classes
////////////////////////////////////////////////////////////////


    private static class SaveRegistrationTask extends AsyncTask<Void, Void, Void> {
        private Context context;
        private org.smartregister.Context openSrpContext;
        private String jsonString;
        private String providerId;
        private String imageKey;
        private String bindType;
        private boolean isEditMode;

        private SaveRegistrationTask(Context context, org.smartregister.Context openSrpContext,
                                     String jsonString, String providerId, String imageKey, String bindType, boolean isEditMode) {
            this.context = context;
            this.openSrpContext = openSrpContext;
            this.jsonString = jsonString;
            this.providerId = providerId;
            this.imageKey = imageKey;
            this.bindType = bindType;
            this.isEditMode = isEditMode;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (context instanceof HomeRegisterActivity) {
                HomeRegisterActivity childSmartRegisterActivity = ((HomeRegisterActivity) context);
                childSmartRegisterActivity.refreshList(FetchStatus.fetched);
                childSmartRegisterActivity.hideProgressDialog();
            }

            Utils.postStickyEvent(new JsonFormSaveCompleteEvent());
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

                String lastLocationName = null;
                String lastLocationId = null;
                // Replace values for location questions with their corresponding location IDs
                for (int i = 0; i < fields.length(); i++) {
                    String key = fields.getJSONObject(i).getString(Constants.KEY.KEY);
                    if (DBConstants.KEY.SCHOOL.equalsIgnoreCase(key)) {
                        try {
                            String rawValue = fields.getJSONObject(i).getString(Constants.KEY.VALUE);
                            JSONArray valueArray = new JSONArray(rawValue);
                            if (valueArray.length() > 0) {
                                lastLocationName = valueArray.getString(valueArray.length() - 1);
                                lastLocationId = LocationHelper.getInstance().getOpenMrsLocationId(lastLocationName);
                                fields.getJSONObject(i).put(Constants.KEY.VALUE, lastLocationId);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    } else if (DBConstants.KEY.SCHOOL_NAME.equalsIgnoreCase(key)) {

                        fields.getJSONObject(i).put(Constants.KEY.VALUE, lastLocationName);
                    } else if (DBConstants.KEY.Location.equalsIgnoreCase(key)) {

                        fields.getJSONObject(i).put(Constants.KEY.VALUE, lastLocationId);
                    } else if (DBConstants.KEY.DOSE_ONE_DATE.equalsIgnoreCase(key)) {
                        try {

                            fields.getJSONObject(i).put(Constants.KEY.VALUE, Utils.getTodaysDate());

                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    } else if (DBConstants.KEY.REMOVE_REASON.equalsIgnoreCase(key)) {
                        try {
                            fields.getJSONObject(i).put(DBConstants.KEY.DATE_REMOVED, Utils.getTodaysDate());

                        } catch (Exception e) {
                            Log.e(TAG, Log.getStackTraceString(e));
                        }
                    }
                }

                JSONObject lastInteractedWith = new JSONObject();
                lastInteractedWith.put(Constants.KEY.KEY, DBConstants.KEY.LAST_INTERACTED_WITH);
                lastInteractedWith.put(Constants.KEY.VALUE, Calendar.getInstance().getTimeInMillis());
                fields.put(lastInteractedWith);

                Client baseClient = JsonFormUtils.createBaseClient(fields, entityId);
                Event baseEvent = JsonFormUtils.createEvent(openSrpContext, fields, metadata, entityId, encounterType, providerId, bindType);


                JsonFormUtils.tagSyncMetadata(baseEvent);

                if (baseClient != null) {
                    JSONObject clientJson = new JSONObject(gson.toJson(baseClient));


                    if (isEditMode) {
                        mergeAndSaveClient(context, baseClient);
                    } else {

                        ecUpdater.addClient(baseClient.getBaseEntityId(), clientJson);
                    }
                }

                if (baseEvent != null) {
                    JSONObject eventJson = new JSONObject(gson.toJson(baseEvent));
                    ecUpdater.addEvent(baseEvent.getBaseEntityId(), eventJson);
                }


                long lastSyncTimeStamp = allSharedPreferences.fetchLastUpdatedAtDate(0);
                Date lastSyncDate = new Date(lastSyncTimeStamp);

                if (isEditMode) {

                    // Unassign current OPENSRP ID
                    if (baseClient != null) {
                        String newOpenSRPId = baseClient.getIdentifier(DBConstants.KEY.OPENSRP_ID).replace("-", "");
                        String currentOpenSRPId = getString(jsonForm, CURRENT_OPENSRP_ID).replace("-", "");
                        if (!newOpenSRPId.equals(currentOpenSRPId)) {
                            //OPENSRP ID was changed
                            HpvApplication.getInstance().getUniqueIdRepository().open(currentOpenSRPId);
                        }
                    }

                } else {

                    String opensrpId = baseClient.getIdentifier(DBConstants.KEY.OPENSRP_ID);
                    //mark OPENSRP ID as used
                    HpvApplication.getInstance().getUniqueIdRepository().close(opensrpId);
                }


                String imageLocation = getFieldValue(fields, imageKey);
                saveImage(context, providerId, entityId, imageLocation);

                HpvClientProcessorForJava.getInstance(context).processClient(ecUpdater.getEvents(lastSyncDate, BaseRepository.TYPE_Unsynced));
                allSharedPreferences.saveLastUpdatedAtDate(lastSyncDate.getTime());

            } catch (Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
            return null;
        }

    }


    public class FORM_MODE {
        public static final boolean EDIT = true;
        public static final boolean CREATE = false;
    }


////////////////////////////////////////////////////////////////
// End Inner classes
////////////////////////////////////////////////////////////////


}
