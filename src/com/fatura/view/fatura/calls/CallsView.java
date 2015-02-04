package com.fatura.view.fatura.calls;

import org.joda.time.DateTime;

import com.fatura.R;
import com.fatura.model.CallService;
import com.fatura.model.Session;
import com.fatura.model.plan.Plan;
import com.fatura.model.plan.TimLibertyPlan;
import com.fatura.model.CallService.LocalBinder;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class CallsView extends Fragment {
	private CallListAdapter adapter;
	private CallService callService;
	private Context ctx;
	private Session session;
	private ListView listView;
    private Button prevBtn;;
    private Button nextBtn;
    private TextView refDateLbl;

	private int ref = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fatura_call_list, container, false);
        listView = (ListView) rootView.findViewById(R.id.faturaCallList_listViewCalls);
        prevBtn = (Button) rootView.findViewById(R.id.prevBtn);
        nextBtn = (Button) rootView.findViewById(R.id.nextBtn);
        
        refDateLbl = (TextView) rootView.findViewById(R.id.refDateLbl);
       
        nextBtn.setEnabled(false);

        prevBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ref++;
				updateList();
				
				if(ref > 1)
					nextBtn.setEnabled(true);
			}
        });

        nextBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				ref--;
				
				if(ref <= 1){
					nextBtn.setEnabled(false);
					ref = 1;
				}
				updateList();
				
			}        	
        });

		ctx = this.getActivity().getApplicationContext();
        session = Session.getInstance();

        return rootView;
    }

    @Override
    public void onStart(){
    	super.onStart();
    	Intent intent = new Intent(ctx, CallService.class);
        ctx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop(){
    	super.onStop();
    	ctx.unbindService(mConnection);
    }
    
    private void updateList(){
        Plan plan = new TimLibertyPlan();

        DateTime currDate = new DateTime();
        DateTime oldBillingDate;

        if(ref == 1 && currDate.getDayOfMonth() >= session.getPaymentDay()){
            oldBillingDate = currDate.withDayOfMonth(session.getPaymentDay()).withTime(0, 0, 0, 0);
        }else{
        	oldBillingDate = currDate.minusMonths(ref).withDayOfMonth(session.getPaymentDay()).withTime(0, 0, 0, 0);
        }

        DateTime newBillingDate = oldBillingDate.plusMonths(1).withDayOfMonth(session.getPaymentDay()).withTime(0, 0, 0, 0);

        adapter = new CallListAdapter(ctx, plan.partial(callService.getCallLog(oldBillingDate.getMillis(),newBillingDate.getMillis())));
        listView.setAdapter(adapter);

        refDateLbl.setText(oldBillingDate.getMonthOfYear() + "/" + oldBillingDate.getYear());
    }

    private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			//Log.d("ServiceConnection", "Preenchendo lista...");
            LocalBinder binder = (com.fatura.model.CallService.LocalBinder) service;
            callService = binder.getService();
            updateList();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {			
		}
    };
}