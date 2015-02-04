package com.fatura.model;

public class Billing {
	private double totalCallPrice;
	private double totalDataUsagePrice;
	
	public Billing() {
	}
	
	public Billing(double totalCallPrice, double totalDataUsagePrice) {
		this.totalCallPrice = totalCallPrice;
		this.totalDataUsagePrice = totalDataUsagePrice;
	}

	public double getTotalCallPrice() {
		return totalCallPrice;
	}

	public void setTotalCallPrice(double totalCallPrice) {
		this.totalCallPrice = totalCallPrice;
	}

	public double getTotalDataUsagePrice() {
		return totalDataUsagePrice;
	}

	public void setTotalDataUsagePrice(double totalDataUsagePrice) {
		this.totalDataUsagePrice = totalDataUsagePrice;
	}
}
