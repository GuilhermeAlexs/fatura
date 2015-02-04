package com.fatura.model;

import android.location.Location;

public class SignalPoint{
	private Location location;
	private int strength;
	
	public SignalPoint(){
		
	}

	public SignalPoint(Location location, int strength) {
		this.location = location;
		this.strength = strength;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}
}
