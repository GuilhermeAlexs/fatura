package com.fatura.view.signalmap;

import java.util.List;

import com.fatura.R;
import com.fatura.database.SignalDB;
import com.fatura.model.SignalPoint;
import com.fatura.service.ChangeLocationListener;
import com.fatura.service.MapService;
import com.fatura.service.MapService.LocalBinder;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public class SignalMap extends FragmentActivity implements OnMapReadyCallback, ChangeLocationListener {
	private MapService mapService;
	private GoogleMap googleMap;
	
	private final double INIT_LAT = -15.7801;
	private final double INIT_LONG = -47.9292;

	private SupportMapFragment fragment;
	private SignalDB db;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fragment = new SupportMapFragment();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment).commit();

        
        googleMap = null;
        this.getActionBar().setTitle("Mapa de Sinal");
        db = new SignalDB(this);
    }

	@Override
	public void onMapReady(GoogleMap map) {
        googleMap = map;
        List<SignalPoint> mapSignals = db.getSignalPoints();
        CameraUpdate centerView;
        CameraUpdate zoomView;

        if(mapSignals != null && mapSignals.size() > 0){
        	Location lastLoc = mapSignals.get(mapSignals.size() - 1).getLocation();
        	centerView = CameraUpdateFactory.newLatLng(new LatLng(lastLoc.getLatitude(), lastLoc.getLongitude()));
        	zoomView = CameraUpdateFactory.zoomTo(18);
        }else{
        	centerView = CameraUpdateFactory.newLatLng(new LatLng(INIT_LAT, INIT_LONG));
        	zoomView = CameraUpdateFactory.zoomTo(18);
        }

        googleMap.moveCamera(centerView);
        googleMap.animateCamera(zoomView);

    	List<SignalPoint> signalMapService = mapService.getSignalMap();
    	drawAreas(signalMapService);
	}
	
    @Override
    public void onStart(){
    	super.onStart();
    	Intent intent = new Intent(this, MapService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
    	super.onStop();
    	try{
    		unbindService(mConnection);
    	}catch(Exception e){
    		return;
    	}
    }
    
    private void drawAreas(List<SignalPoint> map){  	
    	googleMap.clear();
    	int color;

    	if(map == null || map.size() == 0)
    		return;

    	//SignalPoint point = map.get(map.size() - 1);
    	for(SignalPoint point: map){
    		getActionBar().setTitle("Potencia: " + point.getStrength());
    		if(point.getStrength() <= 10)
    			color = Color.argb(40, 255, 0, 0);
    		else if(point.getStrength() <= 20){
    			color = Color.argb(40, 168, 168, 0);
    		}else if(point.getStrength() <= 31){
    			color = Color.argb(40, 0, 255, 0);
    		}else{
    			return; //99
    		}

	    	CircleOptions cOpt = new CircleOptions()
	        .center(new LatLng(point.getLocation().getLatitude(), point.getLocation().getLongitude()))
	        .radius(15)
	        .strokeWidth(0)
	        .fillColor(color);
	    	googleMap.addCircle(cOpt);
    	}
    }

    private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (com.fatura.service.MapService.LocalBinder) service;
            mapService = binder.getService();
            mapService.setLocationChangeListener(SignalMap.this);
            
            fragment.getMapAsync(SignalMap.this);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {			
		}
    };

	@Override
	public void onChangeLocation(List<SignalPoint> map) {
		//this.getActionBar().setTitle("LOCATION CHANGED!");

		Location loc = map.get(map.size() - 1).getLocation();

		if(googleMap != null){
            CameraUpdate centerView =
                    CameraUpdateFactory.newLatLng(new LatLng(loc.getLatitude(),
                                                             loc.getLongitude()));
            CameraUpdate zoomView = CameraUpdateFactory.zoomTo(18);
            googleMap.moveCamera(centerView);
            googleMap.animateCamera(zoomView);
			drawAreas(map);
		}
	}
}
