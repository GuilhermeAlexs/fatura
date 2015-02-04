package com.fatura.database;

import com.fatura.model.Carrier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CarrierCacheDB extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "faturaDB";

    private static final String TABLE_CACHE = "cache";

    private static final String CACHE_NUMBER = "phone";
    private static final String CACHE_CARRIER = "carrier";

    private static final String CREATE_TABLE_PHONE = "CREATE TABLE "
    		+ TABLE_CACHE + "(" +
    		CACHE_NUMBER + " VARCHAR(20) PRIMARY KEY NOT NULL," +
    		CACHE_CARRIER + " INTEGER," + 
    		"FOREIGN KEY(" + CACHE_CARRIER + ") REFERENCES " + 
    		CarrierLookupDB.TABLE_CARRIER_LOOKUP + "(" + 
    		CarrierLookupDB.CARRIER_LOOKUP_CODE + "))";

    private CarrierLookupDB lookupDB;

    public CarrierCacheDB (Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		lookupDB = new CarrierLookupDB(context);
	}
    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
    }
	@Override
	public void onCreate(SQLiteDatabase db) {
		try{
			db.execSQL(CREATE_TABLE_PHONE);
		}catch(SQLException e){
			
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CACHE);
		onCreate(db);
	}

	public Carrier getCarrier(String phone){
		SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT " + CACHE_CARRIER + " FROM " + TABLE_CACHE + " WHERE " + CACHE_NUMBER + " == \'" + phone + "\'", null);

        Carrier carrier = null;
        
        if(cursor.moveToNext()){
        	int code = cursor.getInt(cursor.getColumnIndex(CACHE_CARRIER));
        	carrier = lookupDB.getCarrier(code);
        }

        cursor.close();
        db.close();

        return carrier;
    }
	
	public void insertCache(String phone, Carrier carrier) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(CACHE_NUMBER, phone);
		values.put(CACHE_CARRIER, carrier.getId());

		db.insert(TABLE_CACHE, null, values);

		db.close();
	}
}
