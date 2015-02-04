package com.fatura.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.provider.CallLog;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.fatura.database.CallDB;
import com.fatura.database.CarrierCacheDB;
import com.fatura.database.CarrierLookupDB;

public class CallService extends Service{
	private String WEBSERVICE_URL = "http://portabilidadecelular.com/painel/consulta_numero.php?user=guilhermealexs&pass=tUHhmCwB&seache_number=";
	private final IBinder mBinder = new LocalBinder();
	private CallDB callDB;
	private CarrierCacheDB carrierCacheDB;
	private CarrierLookupDB carrierLookupDB;
	static private TeleListener telListener;
	
	public CallService(){
	}

    @Override
    public void onCreate() {
    	callDB = new CallDB(this);
    	carrierCacheDB = new CarrierCacheDB(this);
    	carrierLookupDB = new CarrierLookupDB(this);
    	Call lastCallFromDB = callDB.getLastCall();
    	Call lastCallFromSys = getLastCall();
  
    	if(lastCallFromDB.getDate() != lastCallFromSys.getDate() &&
    	   lastCallFromDB.getTo() != lastCallFromSys.getTo()){
    		callDB.insertCall(lastCallFromSys);
    	}

    	if(telListener == null){
	    	telListener = new TeleListener();
	
	    	TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
	    	TelephonyMgr.listen(telListener, PhoneStateListener.LISTEN_CALL_STATE);
    	}
    }

    public List<Call> getCallLog(long begin, long end){
    	return callDB.getCalls(begin, end);
    }
    
    private Call getLastCall(){
        Uri contacts = CallLog.Calls.CONTENT_URI;
        Cursor managedCursor = getContentResolver().query(
                contacts, null, null, null, "date DESC");
        int type;
        Call lastCall = null;

        if(managedCursor.moveToNext()){
        	//Log.d("getLastCall()", "EXISTE RESULTADO!");
            type = managedCursor.getInt(managedCursor.getColumnIndex(CallLog.Calls.TYPE));

        	if(type == CallLog.Calls.OUTGOING_TYPE){
        		//Log.d("getLastCall()","OUTGOING_TYPE");
            	String callDate = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DATE));
        		String phNumber = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.NUMBER));
            	String callDuration = managedCursor.getString(managedCursor.getColumnIndex(CallLog.Calls.DURATION));

                managedCursor.close();

                lastCall = new Call();

                lastCall.setDate(Long.parseLong(callDate));
                lastCall.setDuration(Integer.parseInt(callDuration));
            	PhoneNumber phTo = PhoneNumberFactory.createPhoneNumber(phNumber);

            	if(phTo == null){
            		phTo = new PhoneNumber();
            		phTo.setFullNumber(phNumber);
            		phTo.setFormatNotFound(true);
            	}
            	
            	try {
					phTo.setCarrier(getCarrier(phTo));
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

            	if(phTo.isCollectNumber() || phTo.isFreeBusinessNumber() || lastCall.getDuration() == 0)
            		return null;

            	lastCall.setTo(phTo);
            	lastCall.setFrom(Session.getInstance().getUser().getPhoneNumber());
        	}
        }

        return lastCall;
    }

    public Carrier getCarrier(PhoneNumber phone) throws IllegalStateException, IOException{
    	Carrier carrier = carrierCacheDB.getCarrier(phone.getDDD() + phone.getCoreNumber());

    	if(carrier == null){
    		carrier = getCarrierFromWebservice(phone);
    		carrierCacheDB.insertCache(phone.getDDD()+phone.getCoreNumber(), carrier);
    	}

    	return carrier;
    }
    
    public Carrier getCarrierFromWebservice(PhoneNumber phone) throws IllegalStateException, IOException{
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(WEBSERVICE_URL + phone.getDDD() + phone.getCoreNumber()));
        StatusLine statusLine = response.getStatusLine();
        Carrier carrier = null;

        if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            int code = Integer.parseInt(out.toString());

            carrier = carrierLookupDB.getCarrier(code);
        } else{
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }
        
        return carrier;
    }
    
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

    public class LocalBinder extends Binder {
    	public CallService getService() {
            return CallService.this;
        }
    }

    class TeleListener extends PhoneStateListener {
       boolean wasOffHook = false;
       boolean wasRoaming = false;

	   public void onCallStateChanged(int state, String incomingNumber) {
		   super.onCallStateChanged(state, incomingNumber);

		   switch (state) {
		      case TelephonyManager.CALL_STATE_IDLE:
		    	 if(!wasOffHook)
		    		 break;
		    	 //Log.d("LISTENER", "IDLE");
		    	 wasOffHook = false;
		    	 new CarrierRequester().execute(wasRoaming);
		         break;
		      case TelephonyManager.CALL_STATE_OFFHOOK:
		    	 if(wasOffHook)
		    		 break;
		    	 //Log.d("LISTENER", "OFFHOOK");
				 ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				 NetworkInfo ni = cm.getActiveNetworkInfo();

				 if(ni != null)
					 wasRoaming = ni.isRoaming();

		    	 wasOffHook = true;
		         break;
		      default:
		         break;
		   }
	   }
    }
    
    class CarrierRequester extends AsyncTask<Boolean, Integer, Call> {
    	public CarrierRequester(){
    		super();
    	}

        protected Call doInBackground(Boolean... roaming) {
	    	Call lastCall = getLastCall();
	    	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	    	if(lastCall == null){
	    		//Log.d("doInBackground", "lastCall == null!");
	    		return null;
	    	}
	    	lastCall.setRoaming(roaming[0]);
	    	callDB.insertCall(lastCall);

	    	return lastCall;
        }
    }
}
