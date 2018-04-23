package org.smartregister.ug.hpv.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Response;
import org.smartregister.service.HTTPAgent;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.repository.UniqueIdRepository;
import org.smartregister.ug.hpv.util.Constants;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by onamacuser on 18/03/2016.
 */
public class PullUniqueIdsIntentService extends IntentService {
    public static final String ID_URL = "/uniqueids/get";
    public static final String IDENTIFIERS = "identifiers";
    private static final String TAG = PullUniqueIdsIntentService.class.getCanonicalName();
    private UniqueIdRepository uniqueIdRepo;


    public PullUniqueIdsIntentService() {
        super("PullUniqueOpenMRSUniqueIdsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            int numberToGenerate;
            if (uniqueIdRepo.countUnUsedIds() == 0) { // first time pull no ids at all
                numberToGenerate = Constants.OPENMRS_UNIQUE_ID_INITIAL_BATCH_SIZE;
            } else if (uniqueIdRepo.countUnUsedIds() <= 250) { //maintain a minimum of 250 else skip this pull
                numberToGenerate = Constants.OPENMRS_UNIQUE_ID_BATCH_SIZE;
            } else {
                return;
            }
            JSONObject ids = fetchOpenMRSIds(Constants.OPENMRS_UNIQUE_ID_SOURCE, numberToGenerate);
            //JSONObject ids = fetchDummyOpenmrsIds();
            if (ids != null && ids.has(IDENTIFIERS)) {
                parseResponse(ids);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private JSONObject fetchDummyOpenmrsIds() throws JSONException {

        String openmrsIds = "{\"identifiers\":[\"242670-8\",\"242671-6\",\"242672-4\",\"242673-2\",\"242674-0\",\"242675-7\",\"242676-5\",\"242677-3\",\"242678-1\",\"242679-9\",\"242680-7\",\"242681-5\",\"242682-3\",\"242683-1\",\"242684-9\",\"242685-6\",\"242686-4\",\"242687-2\",\"242688-0\",\"242689-8\",\"242690-6\",\"242691-4\",\"242692-2\",\"242693-0\",\"242694-8\",\"242695-5\",\"242696-3\",\"242697-1\",\"242698-9\",\"242699-7\",\"242700-3\",\"242701-1\",\"242702-9\",\"242703-7\",\"242704-5\",\"242705-2\",\"242706-0\",\"242707-8\",\"242708-6\",\"242709-4\",\"242710-2\",\"242711-0\",\"242712-8\",\"242713-6\",\"242714-4\",\"242715-1\",\"242716-9\",\"242717-7\",\"242718-5\",\"242719-3\",\"242720-1\",\"242721-9\",\"242722-7\",\"242723-5\",\"242724-3\",\"242725-0\",\"242726-8\",\"242727-6\",\"242728-4\",\"242729-2\",\"242730-0\",\"242731-8\",\"242732-6\",\"242733-4\",\"242734-2\",\"242735-9\",\"242736-7\",\"242737-5\",\"242738-3\",\"242739-1\",\"242740-9\",\"242741-7\",\"242742-5\",\"242743-3\",\"242744-1\",\"242745-8\",\"242746-6\",\"242747-4\",\"242748-2\",\"242749-0\",\"242750-8\",\"242751-6\",\"242752-4\",\"242753-2\",\"242754-0\",\"242755-7\",\"242756-5\",\"242757-3\",\"242758-1\",\"242759-9\",\"242760-7\",\"242761-5\",\"242762-3\",\"242763-1\",\"242764-9\",\"242765-6\",\"242766-4\",\"242767-2\",\"242768-0\",\"242769-8\",\"242770-6\",\"242771-4\",\"242772-2\",\"242773-0\",\"242774-8\",\"242775-5\",\"242776-3\",\"242777-1\",\"242778-9\",\"242779-7\",\"242780-5\",\"242781-3\",\"242782-1\",\"242783-9\",\"242784-7\",\"242785-4\",\"242786-2\",\"242787-0\",\"242788-8\",\"242789-6\",\"242790-4\",\"242791-2\",\"242792-0\",\"242793-8\",\"242794-6\",\"242795-3\",\"242796-1\",\"242797-9\",\"242798-7\",\"242799-5\",\"242800-1\",\"242801-9\",\"242802-7\",\"242803-5\",\"242804-3\",\"242805-0\",\"242806-8\",\"242807-6\",\"242808-4\",\"242809-2\",\"242810-0\",\"242811-8\",\"242812-6\",\"242813-4\",\"242814-2\",\"242815-9\",\"242816-7\",\"242817-5\",\"242818-3\",\"242819-1\",\"242820-9\",\"242821-7\",\"242822-5\",\"242823-3\",\"242824-1\",\"242825-8\",\"242826-6\",\"242827-4\",\"242828-2\",\"242829-0\",\"242830-8\",\"242831-6\",\"242832-4\",\"242833-2\",\"242834-0\",\"242835-7\",\"242836-5\",\"242837-3\",\"242838-1\",\"242839-9\",\"242840-7\",\"242841-5\",\"242842-3\",\"242843-1\",\"242844-9\",\"242845-6\",\"242846-4\",\"242847-2\",\"242848-0\",\"242849-8\",\"242850-6\",\"242851-4\",\"242852-2\",\"242853-0\",\"242854-8\",\"242855-5\",\"242856-3\",\"242857-1\",\"242858-9\",\"242859-7\",\"242860-5\",\"242861-3\",\"242862-1\",\"242863-9\",\"242864-7\",\"242865-4\",\"242866-2\",\"242867-0\",\"242868-8\",\"242869-6\",\"242870-4\",\"242871-2\",\"242872-0\",\"242873-8\",\"242874-6\",\"242875-3\",\"242876-1\",\"242877-9\",\"242878-7\",\"242879-5\",\"242880-3\",\"242881-1\",\"242882-9\",\"242883-7\",\"242884-5\",\"242885-2\",\"242886-0\",\"242887-8\",\"242888-6\",\"242889-4\",\"242890-2\",\"242891-0\",\"242892-8\",\"242893-6\",\"242894-4\",\"242895-1\",\"242896-9\",\"242897-7\",\"242898-5\",\"242899-3\",\"242900-9\",\"242901-7\",\"242902-5\",\"242903-3\",\"242904-1\",\"242905-8\",\"242906-6\",\"242907-4\",\"242908-2\",\"242909-0\",\"242910-8\",\"242911-6\",\"242912-4\",\"242913-2\",\"242914-0\",\"242915-7\",\"242916-5\",\"242917-3\",\"242918-1\",\"242919-9\"]}";
        return new JSONObject(openmrsIds);
    }

    private JSONObject fetchOpenMRSIds(int source, int numberToGenerate) throws Exception {
        HTTPAgent httpAgent = HpvApplication.getInstance().getContext().getHttpAgent();
        String baseUrl = HpvApplication.getInstance().getContext().
                configuration().dristhiBaseURL();
        String endString = "/";
        if (baseUrl.endsWith(endString)) {
            baseUrl = baseUrl.substring(0, baseUrl.lastIndexOf(endString));
        }

        String url = baseUrl + ID_URL + "?source=" + source + "&numberToGenerate=" + numberToGenerate;
        Log.i(PullUniqueIdsIntentService.class.getName(), "URL: " + url);

        if (httpAgent == null) {
            throw new Exception(ID_URL + " http agent is null");
        }

        Response resp = httpAgent.fetch(url);
        if (resp.isFailure()) {
            throw new Exception(ID_URL + " not returned data");
        }

        return new JSONObject((String) resp.payload());
    }

    /**
     * @param connection object; note: before calling this function,
     *                   ensure that the connection is already be open, and any writes to
     *                   the connection's output stream should have already been completed.
     * @return String containing the body of the connection response or null if the input stream could not be read correctly
     */
    private String readInputStreamToString(HttpURLConnection connection) {
        String result = null;
        StringBuilder sb = new StringBuilder();
        InputStream is = null;

        try {
            is = new BufferedInputStream(connection.getInputStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String inputLine = "";
            while ((inputLine = br.readLine()) != null) {
                sb.append(inputLine);
            }
            result = sb.toString();
        } catch (Exception e) {
            Log.i(TAG, "Error reading InputStream");
            result = null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.i(TAG, "Error closing InputStream");
                }
            }
        }

        return result;
    }

    private void parseResponse(JSONObject idsFromOMRS) throws Exception {
        JSONArray jsonArray = idsFromOMRS.getJSONArray(IDENTIFIERS);
        if (jsonArray != null && jsonArray.length() > 0) {
            List<String> ids = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++) {
                ids.add(jsonArray.getString(i));
            }
            uniqueIdRepo.bulkInserOpenmrsIds(ids);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        uniqueIdRepo = HpvApplication.getInstance().uniqueIdRepository();
        return super.onStartCommand(intent, flags, startId);
    }
}
