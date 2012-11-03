package com.oulix.app.shake4one;

import com.oulix.app.shake4one.*;
import com.oulix.app.shake4one.ShakeSensor.OnShakeListener;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
//import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.audiofx.BassBoost.Settings;

public class Shake4oneActivity extends Activity {
    /** Called when the activity is first created. */

    private TextView m_txtView;
    private Button m_btnShake;
    private Button m_btnStopShake;
    private Button m_btnGetLocation;

    private ShakeSensor m_shakesensor;
    // private LocationService m_serviceLocation;
    // private Context m_thisContext;

    private TextView latituteField;
    private TextView longitudeField;
    private LocationManager locationManager;
    private String provider;

    // end

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
	    @Override
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
		OnStopShake();
	    }
	});

	m_btnGetLocation.setOnClickListener(new Button.OnClickListener() {
	    public void onClick(View v) {
		OnGetLocation();
	    }
	});

    }

    public void OnBeginShake() {
	m_txtView.setText("Shake started...");
	m_txtView.setBackgroundColor(Color.BLACK);
	m_shakesensor.start();
    }

    public void OnStopShake() {
	m_txtView.setText("Shake stoped!");
	m_txtView.setBackgroundColor(Color.BLACK);
	m_shakesensor.stop();
    }

    public void OnGetLocation() {
	m_txtView.setText("Location got!");
    }

}