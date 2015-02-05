package com.fatura.model.plan;

import java.util.List;

import com.fatura.model.Call;
import com.fatura.model.Carrier;
import com.fatura.model.DataUsage;
import com.fatura.model.PhoneNumber;

public class ClaroPreTodaHora implements Plan{

	@Override
	public double price(DataUsage data) {
		return 0;
	}

	private double calculatePrice(double priceUnit, int duration){
	    duration = (int)(duration - 33);
		double price = 0;

		while(duration > 0){
			duration = duration - 6;
			price = price + (priceUnit/10);
		}

		return price;
	}

	@Override
	public List<Call> partial(List<Call> callList) {
		PhoneNumber phTo;
		PhoneNumber phFrom;

		for(Call call : callList){
			phTo = call.getTo();
			phFrom = call.getFrom();

			if((phTo.getCarrier() == null || phTo.getCarrier().equals(Carrier.CLARO)) &&
			   (phTo.getDDD() == phFrom.getDDD() || phTo.getDDD() == 0)){
				call.setPrice(calculatePrice(1.42,(int)call.getDuration()));
			}else if(!phTo.getCarrier().equals(phFrom.getCarrier())){
				call.setPrice(calculatePrice(1.44,(int)call.getDuration()));
			}
		}
		return callList;	
	}

	@Override
	public double price(List<Call> callList) {
		PhoneNumber phTo;
		PhoneNumber phFrom;
		double price = 0;

		for(Call call : callList){
			phTo = call.getTo();
			phFrom = call.getFrom();

			if((phTo.getCarrier() == null || phTo.getCarrier().equals(Carrier.CLARO)) &&
			   (phTo.getDDD() == phFrom.getDDD() || phTo.getDDD() == 0)){
				price = price + calculatePrice(1.42,(int)call.getDuration());
			}else if(!phTo.getCarrier().equals(phFrom.getCarrier())){
				price = price + calculatePrice(1.44,(int)call.getDuration());
			}
		}
		return price;	
	}

}
