package com.fatura.database;

import java.util.LinkedList;
import java.util.List;

import com.fatura.model.Call;
import com.fatura.model.PhoneNumber;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CallDB extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "faturaDB";

    private static final String TABLE_CALLS = "calls";

    private static final String CALL_ID = "id";
    private static final String CALL_FROM = "call_from";
    private static final String CALL_TO = "call_to";
    private static final String CALL_DURATION = "duration";
    private static final String CALL_DATE = "date";
    private static final String CALL_ROAMING = "roaming";

    private static final String CREATE_TABLE_CALLS = "CREATE TABLE "
    		+ TABLE_CALLS + "(" +
    		CALL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
    		CALL_FROM + " TEXT," +
    		CALL_TO + " TEXT," +
    		CALL_DURATION + " INTEGER," +
    		CALL_DATE + " INTEGER," +
    		CALL_ROAMING + " BOOLEAN" + ")";

    public CallDB (Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

    @Override
    public void onOpen(SQLiteDatabase db) {
        onCreate(db);
    }
	@Override
	public void onCreate(SQLiteDatabase db) {
		try{
			db.execSQL(CREATE_TABLE_CALLS);
		}catch(SQLException e){
			
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALLS);
		onCreate(db);
	}
	
	public List<Call> getCalls(long begin, long end){
		SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CALLS + " WHERE " + CALL_DATE + " >= " + begin + " AND " + CALL_DATE + " <= " + end, null);
        List<Call> callList = new LinkedList<Call>();

        while(cursor.moveToNext()){
        	Call call = new Call();
        	call.setFrom(new PhoneNumber(cursor.getString(cursor.getColumnIndex(CALL_FROM))));
        	call.setTo(new PhoneNumber(cursor.getString(cursor.getColumnIndex(CALL_TO))));
        	call.setDate(cursor.getLong((cursor.getColumnIndex(CALL_DATE))));
        	call.setDuration(cursor.getInt(cursor.getColumnIndex(CALL_DURATION)));
        	call.setRoaming(cursor.getInt(cursor.getColumnIndex(CALL_ROAMING)) == 1);
        	callList.add(call);
        }

        cursor.close();
        db.close();
        return callList;
    }
	
	public Call getLastCall(){
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery("SELECT * FROM " +  TABLE_CALLS + " order by " + CALL_ID + " DESC LIMIT 1", null);
		Call call = null;

		if(cursor.moveToFirst()){
        	call = new Call();
        	call.setFrom(new PhoneNumber(cursor.getString(cursor.getColumnIndex(CALL_FROM))));
        	call.setTo(new PhoneNumber(cursor.getString(cursor.getColumnIndex(CALL_TO))));
        	call.setDate(cursor.getLong((cursor.getColumnIndex(CALL_DATE))));
        	call.setDuration(cursor.getInt(cursor.getColumnIndex(CALL_DURATION)));
        	call.setRoaming(cursor.getInt(cursor.getColumnIndex(CALL_ROAMING)) == 1);
		}

		return call;
	}
	
	public void insertCall(Call call) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(CALL_FROM, call.getFrom().getFullNumber());
		values.put(CALL_TO, call.getTo().getFullNumber());
		values.put(CALL_DURATION, call.getDuration());
		values.put(CALL_DATE, call.getDate());
		values.put(CALL_ROAMING, call.isRoaming());

		db.insert(TABLE_CALLS, null, values);

		db.close();
	}
}
