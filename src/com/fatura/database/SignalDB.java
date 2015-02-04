package com.fatura.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.location.LocationManager;

import com.fatura.model.SignalPoint;

public class SignalDB extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "faturaDB";

    private static final String TABLE_SIGNAL = "signal";

    private static final String SIGNAL_ID = "id";
    private static final String SIGNAL_LAT = "latitude";
    private static final String SIGNAL_LONG = "longitude";
    private static final String SIGNAL_STRENGTH = "strength";

    private static final String CREATE_TABLE_SIGNAL = "CREATE TABLE "
    		+ TABLE_SIGNAL + "(" +
    		SIGNAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
    		SIGNAL_STRENGTH + " INTEGER," +
    		SIGNAL_LAT + " FLOAT," +
    		SIGNAL_LONG + " FLOAT )";

    public SignalDB (Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		try{
			db.execSQL(CREATE_TABLE_SIGNAL);
		}catch(SQLException e){
			
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SIGNAL);
		onCreate(db);
	}

	public List<SignalPoint> getSignalPoints(){
		SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SIGNAL, null);
        List<SignalPoint> listPoint = new ArrayList<SignalPoint>();

        while(cursor.moveToNext()){
        	SignalPoint p = new SignalPoint();
        	Location loc = new Location(LocationManager.GPS_PROVIDER);

        	loc.setLatitude(cursor.getDouble(cursor.getColumnIndex(SIGNAL_LAT)));
        	loc.setLongitude(cursor.getDouble(cursor.getColumnIndex(SIGNAL_LONG)));
        	p.setLocation(loc);
        	p.setStrength(cursor.getInt(cursor.getColumnIndex(SIGNAL_STRENGTH)));

        	listPoint.add(p);
        }

        cursor.close();
        db.close();

        return listPoint;
    }
	
	public void insertSignalPoint(SignalPoint point) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(SIGNAL_STRENGTH, point.getStrength());
		values.put(SIGNAL_LAT, point.getLocation().getLatitude());
		values.put(SIGNAL_LONG, point.getLocation().getLongitude());

		db.insert(TABLE_SIGNAL, null, values);

		db.close();
	}
}
