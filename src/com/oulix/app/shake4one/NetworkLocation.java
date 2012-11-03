package com.oulix.app.shake4one;

import java.util.Iterator;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class NetworkLocation {

    private static final String TAG = null;
    private static final boolean DEBUG = false;

    public Location requestNetworkLocation(Context mContext) {
	try {
	    TelephonyManager telMan = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
	    String mcc, mnc;
	    int cid, lac;
	    String operator = telMan.getNetworkOperator();
	    boolean isCdma = false;
	    CellLocation cl = telMan.getCellLocation();
	    if (cl instanceof GsmCellLocation) {
		cid = ((GsmCellLocation) cl).getCid();
		lac = ((GsmCellLocation) cl).getLac();
		mcc = operator.substring(0, 3);
		mnc = operator.substring(3);
	    } else if (cl instanceof CdmaCellLocation) {
		cid = ((CdmaCellLocation) cl).getBaseStationId();
		lac = ((CdmaCellLocation) cl).getNetworkId();

		mcc = operator.substring(0, 3);
		int systemId = ((CdmaCellLocation) cl).getSystemId();
		mnc = String.valueOf(systemId);
		isCdma = true;
	    } else {
		return null;
	    }

	    JSONObject tower = new JSONObject();
	    try {
		tower.put("cell_id", cid);
		tower.put("location_area_code", lac);
		tower.put("mobile_country_code", mcc);
		tower.put("mobile_network_code", mnc);
	    } catch (JSONException e) {
		if (DEBUG)
		    Log.e(TAG, "JSONObject put failed", e);
	    }

	    JSONArray jarray = new JSONArray();
	    jarray.put(tower);

	    List<NeighboringCellInfo> ncis = telMan.getNeighboringCellInfo();
	    Iterator<NeighboringCellInfo> iter = ncis.iterator();

	    NeighboringCellInfo nci;
	    JSONObject tmpTower;
	    while (iter.hasNext()) {
		nci = iter.next();
		tmpTower = new JSONObject();
		try {
		    tmpTower.put("cell_id", nci.getCid());
		    tmpTower.put("location_area_code", nci.getLac());
		    tmpTower.put("mobile_country_code", mcc);
		    tmpTower.put("mobile_network_code", mnc);
		} catch (JSONException e) {
		    if (DEBUG)
			Log.e(TAG, "JSONObject put failed", e);
		}
		jarray.put(tmpTower);
	    }

	    JSONObject object;
	    if (!isCdma)
		object = createJSONObject("cell_towers", jarray);
	    else
		object = createCDMAJSONObject("cell_towers", jarray, mcc, mnc);

	    String locinfo = null;
	    int tryCount = 0;
	    while (tryCount < 3 && locinfo == null) {
		tryCount++;
		try {
		    HttpPost httpPost = new HttpPost(
			    "http://www.google.com/loc/json");
		    System.out.println(object.toString());
		    httpPost.setEntity(new StringEntity(object.toString()));

		} catch (Exception e) {
		    try {	
			Thread.sleep(2000);
		    } catch (Exception e1) {
		    }
		}
	    }

	    if (locinfo == null)
		return null;

	    if (DEBUG) {
		Log.d(TAG, locinfo);
	    }

	    Location location = new Location("telephone");
	    JSONObject jso = new JSONObject(locinfo);
	    JSONObject locObj = jso.getJSONObject("location");

	    if (locObj.has("latitude") && locObj.has("longitude")) {
		location.setLatitude(locObj.getDouble("latitude"));
		location.setLongitude(locObj.getDouble("longitude"));
		location.setAccuracy(locObj.has("accuracy") ? (float) locObj
			.getDouble("accuracy") : 0.0f);
		location.setTime(System.currentTimeMillis());
		return location;
	    }
	} catch (Exception e) {
	    if (DEBUG)
		Log.d(TAG,
			"request GsmCellLocation failed,reason:"
				+ e.getMessage());
	}

	return null;

    }

    private JSONObject createJSONObject(String arrayName, JSONArray array) {
	JSONObject object = new JSONObject();
	try {
	    object.put("version", "1.1.0");
	    object.put("host", "maps.google.com");
	    object.put(arrayName, array);
	} catch (JSONException e) {
	    if (DEBUG)
		Log.e(TAG, "JSONObject put failed", e);
	}
	return object;
    }

    private JSONObject createCDMAJSONObject(String arrayName, JSONArray array,
	    String mcc, String mnc) {
	JSONObject object = new JSONObject();
	try {
	    object.put("version", "1.1.0");
	    object.put("host", "maps.google.com");
	    object.put("home_mobile_country_code", mcc);
	    object.put("home_mobile_network_code", mnc);
	    object.put("radio_type", "cdma");
	    object.put("request_address", true);
	    if ("460".equals(mcc))
		object.put("address_language", "zh_CN");
	    else
		object.put("address_language", "en_US");

	    object.put(arrayName, array);
	} catch (JSONException e) {
	    if (DEBUG)
		Log.e(TAG, "JSONObject put failed", e);
	}
	return object;
    }

}
