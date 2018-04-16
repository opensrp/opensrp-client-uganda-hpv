package org.smartregister.ug.hpv.helper;

import android.util.Log;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.domain.jsonmapping.Location;
import org.smartregister.domain.jsonmapping.util.LocationTree;
import org.smartregister.domain.jsonmapping.util.Tree;
import org.smartregister.domain.jsonmapping.util.TreeNode;
import org.smartregister.ug.hpv.application.HpvApplication;
import org.smartregister.ug.hpv.util.Utils;
import org.smartregister.util.AssetHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by vkaruri on 16/04/2018.
 */

public class LocationHelper {

    private final String TAG = getClass().getName();
    private static LocationHelper instance;
    public static LocationHelper getInstance () {
        if (instance == null) {
            instance = new LocationHelper();
        }
        return instance;
    }

    public String getOpenMrsLocationId(String locationName) {
        if (StringUtils.isBlank(locationName)) {
            return null;
        }

        String response = locationName;
        try {
            LinkedHashMap<String, TreeNode<String, Location>> map = getMap();
            if (!Utils.isEmptyMap(map)) {
                for (Map.Entry<String, TreeNode<String, Location>> entry : map.entrySet()) {
                    String curResult = getOpenMrsLocationId(locationName, entry.getValue());
                    if (StringUtils.isNotBlank(curResult)) {
                        response = curResult;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return response;
    }

    public String getOpenMrsLocationId(String locationName, TreeNode<String, Location> openMrsLocations) {
        try {
            if (openMrsLocations == null) {
                return null;
            }

            Location node = openMrsLocations.getNode();
            if (node == null) {
                return null;
            }

            String name = node.getName();
            if (locationName.equals(name)) {
                return node.getLocationId();
            }

            LinkedHashMap<String, TreeNode<String, Location>> childMap = getChildMap(openMrsLocations);
            if (!Utils.isEmptyMap(childMap)) {
                for (Map.Entry<String, TreeNode<String, Location>> childEntry : childMap.entrySet()) {
                    String curResult = getOpenMrsLocationId(locationName, childEntry.getValue());
                    if (StringUtils.isNotBlank(curResult)) {
                        return curResult;
                    }
                }
            }
        } catch(Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
        return null;
    }

    private LinkedHashMap<String, TreeNode<String, Location>> getMap() {
        String locationData = HpvApplication.getInstance().getContext().anmLocationController().get();
        LocationTree locationTree = AssetHandler.jsonStringToJava(locationData, LocationTree.class);
        if (locationTree != null) {
            return locationTree.getLocationsHierarchy();
        }

        return null;
    }

    private LinkedHashMap<String, TreeNode<String, Location>> getChildMap(TreeNode<String, Location> treeNode) {
        if (treeNode.getChildren() != null) {
            return treeNode.getChildren();
        }
        return null;
    }
}
