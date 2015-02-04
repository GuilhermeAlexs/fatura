package com.fatura.model;

public class Carrier {
	public static String TIM = "TIM";
	public static String VIVO = "VIVO";
	public static String CLARO = "CLARO";
	public static String OI = "OI";
	public static String FIXO = "FIXO";
	public static String MOVEL = "MÓVEL";

	private int id;
	private String name;
	private String type;

	public Carrier(){		
	}

	public Carrier(String name){
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.toUpperCase();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
