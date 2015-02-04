package com.fatura.view.fatura.total;

import org.joda.time.DateTime;

import com.fatura.R;
import com.fatura.model.CallService;
import com.fatura.model.Session;
import com.fatura.model.CallService.LocalBinder;
import com.fatura.model.plan.Plan;
import com.fatura.model.plan.TimLibertyPlan;
import com.fatura.view.fatura.calls.CallListAdapter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class TotalView extends Fragment{
	private TextView nameTextView;
    private TextView priceTextView;

    private TextView nameTextView2;
    private TextView priceTextView2;

    private TextView totalPriceTextView;
    
	private CallService callService;
	private Session session;
	private Context ctx;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.total_layout, container, false);
        nameTextView = (TextView) rootView.findViewById(R.id.nameTextView);
        priceTextView = (TextView) rootView.findViewById(R.id.priceTextView);

        nameTextView2 = (TextView) rootView.findViewById(R.id.nameTextView2);
        priceTextView2 = (TextView) rootView.findViewById(R.id.priceTextView2);

        totalPriceTextView = (TextView) rootView.findViewById(R.id.totalPriceTextView);
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

    private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
            LocalBinder binder = (com.fatura.model.CallService.LocalBinder) service;
            callService = binder.getService();

            DateTime currDate = new DateTime();
            long oldBillingDate = currDate.minusMonths(1).withDayOfMonth(session.getPaymentDay()).withTime(0, 0, 0, 0).getMillis();
    		long newBillingDate = currDate.plusMonths(1).withDayOfMonth(session.getPaymentDay()).withTime(0, 0, 0, 0).getMillis();

            Plan plan = new TimLibertyPlan();
            
            Double callPrice = plan.price(callService.getCallLog(oldBillingDate,newBillingDate));
            Double internetPrice = 0.0;

            priceTextView.setText("R$ " + String.format("%.2f", callPrice));
            totalPriceTextView.setText("R$ " + String.format("%.2f", (callPrice+internetPrice)));
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {			
		}
    };
}
