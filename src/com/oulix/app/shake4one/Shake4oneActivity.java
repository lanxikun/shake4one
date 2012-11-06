package com.oulix.app.shake4one;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Location;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.CellLocation;
import android.telephony.NeighboringCellInfo;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.oulix.app.shake4one.ShakeSensor.OnShakeListener;

public class Shake4oneActivity extends Activity {
	/** Called when the activity is first created. */

	private TextView m_txtView;
	private Button m_btnShake;
	private Button m_btnStopShake;
	private Button m_btnGetLocation;

	private ShakeSensor m_shakesensor;
	private Handler m_Handler;

	Location location;

	private static final String TAG = "<TAG>";
	private static final boolean DEBUG = true;
	protected static final int HTTPFINISHED = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		m_txtView = (TextView) findViewById(R.id.id_textview);
		m_btnShake = (Button) findViewById(R.id.id_btnShake);
		m_btnStopShake = (Button) findViewById(R.id.id_btnStopShake);
		m_btnGetLocation = (Button) findViewById(R.id.id_btnGetLocation);

		// setting shake listener
		m_shakesensor = new ShakeSensor(this);
		m_shakesensor.registerOnShakeListener(new OnShakeListener() {
			public void onShake() {
				m_txtView.setText("shakeing detected.... !!!");
				m_txtView.setBackgroundColor(Color.RED);
			}
		});

		m_btnShake.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				OnBeginShake();
			}
		});

		m_btnStopShake.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				try {
					OnStopShake();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		m_btnGetLocation.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				OnGetLocation();
			}
		});

		m_Handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Shake4oneActivity.HTTPFINISHED:
					m_txtView.setText("http finished.");
					break;
				}
				super.handleMessage(msg);
			}
		};

	}

	public void OnBeginShake() {
		m_txtView.setText("Shake started...");
		m_txtView.setBackgroundColor(Color.BLACK);
		m_shakesensor.start();
	}

	public void OnStopShake() throws IOException {
		// m_txtView.setText("Shake stoped!");
		m_txtView.setBackgroundColor(Color.GREEN);
		m_shakesensor.stop();

		Log.v("<JOU>", "entering ...");
		// return;
		// requestNetworkLocation();

		Thread mThread = new Thread() {
			public void run() {
				super.run();
				try {
					httpTest("http://wap.crossmo.com/t.php");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		mThread.start();

	}

	private void httpTest(String strUrl) throws IOException {
		// TODO Auto-generated method stub
		showTrace(strUrl);

		try {
			URL url = new URL(strUrl);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();

			BufferedReader buffer = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			String inputLine = null;

			StringBuilder resultData = new StringBuilder();
			showTrace("stringbuilder..");
			while (((inputLine = buffer.readLine()) != null)) {
				resultData.append(inputLine);
				Log.v("[inputLine]:", inputLine);
			}

			urlConn.disconnect();
			showTrace(resultData.toString());

			// send message to inform main thread
			Message m = new Message();
			m.what = Shake4oneActivity.HTTPFINISHED;
			this.m_Handler.sendMessage(m);

		} catch (Exception e) {
			Log.v("url response", "false");
			e.printStackTrace();
		}
	}

	public void OnGetLocation() {
		m_txtView.setText("Location got!");

		String str;

		TelephonyManager telMan = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
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
			str = "GSM: cid=" + cid + " lac=" + lac + " mcc=" + mcc + " mnc="
					+ mnc;

			System.out.println(str);
		} else if (cl instanceof CdmaCellLocation) {
			cid = ((CdmaCellLocation) cl).getBaseStationId();
			lac = ((CdmaCellLocation) cl).getNetworkId();

			mcc = operator.substring(0, 3);
			int systemId = ((CdmaCellLocation) cl).getSystemId();
			mnc = String.valueOf(systemId);
			isCdma = true;
			str = "CDMA: cid=" + cid + " lac=" + lac + " mcc=" + mcc + " mnc="
					+ mnc;
			System.out.println(str);

		} else {
			return;
		}

		m_txtView.setText(str);

	}

	public static void showTrace(String msg) {
		Log.v("showTrace",
				"\n" + msg + "\n" + "\tTrace: " + "\n <File> "
						+ new Throwable().getStackTrace()[1].getFileName()
						+ "\n <Class> "
						+ new Throwable().getStackTrace()[1].getClassName()
						+ "\n <Method> "
						+ new Throwable().getStackTrace()[1].getMethodName()
						+ "\n <Line> "
						+ new Throwable().getStackTrace()[1].getLineNumber());
	}

	public Location requestNetworkLocation() {

		Log.v("<JOU>", "requestNetworkLocation");
		try {
			showTrace("JOU");

			TelephonyManager telMan = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
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
				showTrace("is GSM Location");
			} else if (cl instanceof CdmaCellLocation) {
				cid = ((CdmaCellLocation) cl).getBaseStationId();
				lac = ((CdmaCellLocation) cl).getNetworkId();

				mcc = operator.substring(0, 3);
				int systemId = ((CdmaCellLocation) cl).getSystemId();
				mnc = String.valueOf(systemId);
				isCdma = true;
				showTrace("is CDMA Location");
			} else {
				return null;
			}

			showTrace("begin fetch location by HTTP Post..");

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
							"http://wap.crossmo.com/t.php");
					// "http://www.google.com/loc/json");
					showTrace(object.toString());
					httpPost.setEntity(new StringEntity(object.toString()));

					HttpClient httpclient = new DefaultHttpClient();

					HttpResponse response = httpclient.execute(httpPost);
					HttpEntity resEntity = response.getEntity();
					showTrace(resEntity.toString());
					locinfo = resEntity.toString();
					showTrace(locinfo);

				} catch (Exception e) {
					try {
						Thread.sleep(2000);
					} catch (Exception e1) {
					}
				}
			}

			if (locinfo == null)
				return null;

			if (DEBUG)
				Log.d(TAG, locinfo);

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