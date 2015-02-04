package com.fatura.service;

import java.util.List;

import com.fatura.model.SignalPoint;

public interface ChangeLocationListener {
	public void onChangeLocation(List<SignalPoint> map);
}
