package com.example.taek.sqlitesample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Taek on 2017-11-16.
 */

public class dbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "mycontacts.db";
    private static final int DATABASE_VERSION = 1;
    private Context context;
    private String TAG = "dbHelper";

    /**
     * 먼저 SQLiteOpenHelper 클래스를 상속받은 dbHelper클래스가 정의 되어 있다. 데이터베이스 파일 이름은 "myDatabase.db"가 되고
     * 데이터베이스 버전은 1로 되어있다. 만약 데이터베이스가 요청되었는데 데이터베이스가 없으면 onCreate()를 호출하여 데이터베이스 파일을 생성해준다.
     */
    public dbHelper (Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE contact (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, tel TEXT, addr TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS contact");
        onCreate(db);
    }

    public void dropTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS contact");
    }

    public void insert(SQLiteDatabase db, String name, String phone, String addr) {
        db.execSQL("INSERT INTO contact VALUES(null,'" + name + "','" + phone + "','" + addr + "');");

        // 데이터베이스 상태창에 업데이트
        ((MainActivity) context).updateListItem(((MainActivity) context).INSERT_DATA, name, phone, addr);
    }

    public Cursor select(SQLiteDatabase db, String name) {
        if (name == null) {
            return null;
        }

        Cursor cursor = db.rawQuery("SELECT name, tel, addr FROM contact where name='" + name +"';", null);

        return cursor;
    }

    public void selectAll(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT * FROM contact", null);
/*
        Log.d(TAG + " - cursor.getCount(): ", cursor.getCount() + "");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Log.d(TAG + " - getColumnCount():", cursor.getColumnCount() + "");

            String name = cursor.getString(1);
            String phone = cursor.getString(2);
            String addr = cursor.getString(3);

            cursor.moveToNext();

            Log.d(TAG + "- selectAll():", "(" + name + ", " + phone + ", " + addr + ")\n");
        }
 */
        ((MainActivity) context).updateListItem(((MainActivity) context).SELECT_ALL_DATA, cursor);
    }

    public void delete(SQLiteDatabase db, String name) {
        if (name == null) {
            return;
        }

        // 데이터베이스 상태창에 업데이트
        String phone = null, addr = null;
        Cursor cursor = select(db, name);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            phone = cursor.getString(1);
            addr = cursor.getString(2);

            cursor.moveToNext();
        }
        ((MainActivity) context).updateListItem(((MainActivity) context).DELETE_DATA, name, phone, addr);

        db.execSQL("DELETE FROM contact WHERE name = '" + name + "';");
    }

    public void update(SQLiteDatabase db, String name, String phone_old, String addr_old, String phone_new, String addr_new) {
        //db.execSQL("UPDATE contact SET phone=" + phone_new + " WHERE name =" + name + ";");
        //db.execSQL("UPDATE contact SET addr=" + addr_new + " WHERE name =" + name + ";");
        ContentValues values_phone = new ContentValues();
        ContentValues values_addr = new ContentValues();
        values_phone.put("tel", phone_new);
        values_addr.put("addr", addr_new);
        String[] parms = new String[] {name};
        db.update("contact", values_phone, "name=?", parms);
        db.update("contact", values_addr, "name=?", parms);

        ((MainActivity) context).updateListItem(((MainActivity) context).UPDATE_DATA, name, phone_old, addr_old, phone_new, addr_new);
    }
}
