package com.fatura.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.fatura.model.Carrier;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CarrierLookupDB extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "faturaDB";

    public static final String TABLE_CARRIER_LOOKUP = "carrier_lookup";

    public static final String CARRIER_LOOKUP_CODE = "code";
    public static final String CARRIER_LOOKUP_CARRIER = "carrier";
    public static final String CARRIER_LOOKUP_TYPE = "type";

    private static final String CREATE_TABLE_CARRIER_LOOKUP = "CREATE TABLE " +
    	    TABLE_CARRIER_LOOKUP + "(" +
    		CARRIER_LOOKUP_CODE + " INTEGER PRIMARY KEY NOT NULL," +
    		CARRIER_LOOKUP_CARRIER + " VARCHAR(20)," +
    		CARRIER_LOOKUP_TYPE + " VACHAR(20))";
    
    private Context ctx;
 
    public CarrierLookupDB (Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		ctx = context;
	}
    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
    }
	@Override
	public void onCreate(SQLiteDatabase db) {
		try{
		db.execSQL(CREATE_TABLE_CARRIER_LOOKUP);
		}catch(SQLException e){
			
		}
		fillDatabase(db);
	}
	
	private void fillDatabase(SQLiteDatabase db){
		InputStream in;
		try {
			in = ctx.getAssets().open("codigos.csv");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		    while(true){
		    	String line = reader.readLine();
		    	
		    	if(line == null)
		    		break;
		    	//Log.d("LINE", line);
		    	String [] columns = line.split(",");
		    	Carrier carrier = new Carrier();
		    	carrier.setId(Integer.parseInt(columns[2]));
		    	carrier.setType(columns[1]);
		    	carrier.setName(columns[0]);

		    	insertCarrier(carrier,db);
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CARRIER_LOOKUP);
		onCreate(db);
	}

	public Carrier getCarrier(int code){
		SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CARRIER_LOOKUP + " WHERE " + CARRIER_LOOKUP_CODE + " == " + code, null);
        Carrier carrier = null;

        if(cursor.moveToNext()){
        	carrier = new Carrier();
        	carrier.setId(code);
        	carrier.setName(cursor.getString(cursor.getColumnIndex(CARRIER_LOOKUP_CARRIER)));
        	carrier.setType(cursor.getString(cursor.getColumnIndex(CARRIER_LOOKUP_TYPE)));
        }

        cursor.close();
        db.close();

        return carrier;
    }
	
	public void insertCarrier(Carrier carrier,SQLiteDatabase db){
		ContentValues values = new ContentValues();
		values.put(CARRIER_LOOKUP_CODE, carrier.getId());
		values.put(CARRIER_LOOKUP_CARRIER, carrier.getName());
		values.put(CARRIER_LOOKUP_TYPE, carrier.getType());

		db.insert(TABLE_CARRIER_LOOKUP, null, values);

		db.close();
	}
	
	public void insertCarrier(Carrier carrier) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(CARRIER_LOOKUP_CODE, carrier.getId());
		values.put(CARRIER_LOOKUP_CARRIER, carrier.getName());
		values.put(CARRIER_LOOKUP_TYPE, carrier.getType());

		db.insert(TABLE_CARRIER_LOOKUP, null, values);

		db.close();
	}
}
