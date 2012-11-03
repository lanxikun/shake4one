package com.oulix.app.shake4one;

import android.content.Context;
import android.telephony.NeighboringCellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import java.util.ArrayList;
import java.util.List;

public class CellIDInfoManager {

    private CdmaCellLocation cdma;
    String current_ci;
    private GsmCellLocation gsm;
    int lac;
    private PhoneStateListener listener;
    private TelephonyManager manager;
    String mcc;
    String mnc;

    public class CellIDInfo {
	public double Latitude = 0.0D;
	public double Longitude = 0.0D;
	public int cellId;
	public int locationAreaCode;
	public String mobileCountryCode;
	public String mobileNetworkCode;
	public String radioType;
    }

    public ArrayList<CellIDInfo> getCellIDInfo(Context paramContext) {
	this.manager = ((TelephonyManager) paramContext
		.getSystemService("phone"));
	this.listener = new PhoneStateListener();
	this.manager.listen(this.listener, 0);
	ArrayList localArrayList = new ArrayList();
	CellIDInfo localCellIDInfo2 = new CellIDInfo();
	int i = this.manager.getNetworkType();
	if ((i != 1) && (i != 2) && (i != 8) && (i != 10) && (i != 9)) {
	    if ((i != 4) && (i != 7) && (i != 5) && (i != 6)) {
		localArrayList = null;
	    } else {
		this.cdma = ((CdmaCellLocation) this.manager.getCellLocation());
		if (this.cdma != null) {
		    CellIDInfo localCellIDInfo1 = new CellIDInfo();
		    localCellIDInfo1.Latitude = (this.cdma
			    .getBaseStationLatitude() / 14400.0D);
		    localCellIDInfo1.Longitude = (this.cdma
			    .getBaseStationLongitude() / 14400.0D);
		    localCellIDInfo1.radioType = "CDMA";
		    localArrayList.add(localCellIDInfo1);
		    localArrayList = localArrayList;
		} else {
		    localArrayList = null;
		}
	    }
	} else {
	    this.gsm = ((GsmCellLocation) this.manager.getCellLocation());
	    if (this.gsm != null) {
		this.lac = this.gsm.getLac();
		this.mcc = this.manager.getNetworkOperator().substring(0, 3);
		this.mnc = this.manager.getNetworkOperator().substring(3, 5);
		localCellIDInfo2.cellId = this.gsm.getCid();
		localCellIDInfo2.mobileCountryCode = this.mcc;
		localCellIDInfo2.mobileNetworkCode = this.mnc;
		localCellIDInfo2.locationAreaCode = this.lac;
		localCellIDInfo2.radioType = "GSM";
		localArrayList.add(localCellIDInfo2);
		List localList = this.manager.getNeighboringCellInfo();
		int j = localList.size();
		for (int k = 0; k < j; k++) {
		    CellIDInfo localCellIDInfo3 = new CellIDInfo();
		    localCellIDInfo3.cellId = ((NeighboringCellInfo) localList
			    .get(k)).getCid();
		    localCellIDInfo3.mobileCountryCode = this.mcc;
		    localCellIDInfo3.mobileCountryCode = this.mnc;
		    localCellIDInfo3.locationAreaCode = this.lac;
		    localCellIDInfo3.radioType = "GSM";
		    localArrayList.add(localCellIDInfo3);
		}
		localArrayList = localArrayList;
	    } else {
		localArrayList = null;
	    }
	}
	return localArrayList;
    }
}