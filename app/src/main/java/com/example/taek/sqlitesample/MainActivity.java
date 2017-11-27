package com.example.taek.sqlitesample;

import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public final static int INSERT_DATA = 1;
    public final static int DELETE_DATA = 2;
    public final static int SELECT_ALL_DATA = 3;
    public final static int UPDATE_DATA = 4;

    dbHelper helper;
    SQLiteDatabase db;
    EditText edit_name, edit_phone, edit_addr;
    public ListView listView_log;
    public ArrayList<String> items = new ArrayList<>();
    public ArrayAdapter<String> adapter;

    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helper = new dbHelper(this);
        try {
            // 데이터베이스 객체를 얻기 위해 getWritableDatabase()를 호출
            db = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = helper.getReadableDatabase();
        }

        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_phone = (EditText) findViewById(R.id.edit_phone);
        edit_addr = (EditText) findViewById(R.id.edit_address);
        listView_log = (ListView) findViewById(R.id.listview_log);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
        listView_log.setAdapter(adapter);
        listView_log.setOnItemClickListener(mItemClickListener);

        helper.selectAll(db);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_add: {
                String name = edit_name.getText().toString();
                String phone = edit_phone.getText().toString();
                String addr = edit_addr.getText().toString();

                helper.insert(db, name, phone, addr);
                clearEditText();
                break;
            }

            case R.id.button_remove: {
                String name = edit_name.getText().toString();

                helper.delete(db, name);
                clearEditText();
                break;
            }

            case R.id.button_select: {
                final String name = edit_name.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Cursor cursor = helper.select(db, name);

                        Log.d(TAG + " - cursor.getCount(): ", cursor.getCount() + "");
                        cursor.moveToFirst();

                        while (!cursor.isAfterLast()) {
                            Log.d(TAG + " - getColumnCount():", cursor.getColumnCount() + "");
                            StringBuilder strBuilder = new StringBuilder();
                            for(String str : cursor.getColumnNames()) {
                                strBuilder.append(str);
                            }
                            Log.d(TAG + " - getColumnName():", strBuilder.toString());
                            Log.d(TAG + "Columns: ", "(" + cursor.getString(0) + ", " + cursor.getString(1) + ", " + cursor.getString(2) + ")");
                            final String phone = cursor.getString(1);
                            final String addr = cursor.getString(2);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    edit_phone.setText(phone);
                                    edit_addr.setText(addr);
                                }
                            });

                            cursor.moveToNext();
                        }
                    }
                }).start();
                break;
            }
        }
    }

    // EditText 텍스트 없애기
    public void clearEditText() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                edit_name.setText("");
                edit_phone.setText("");
                edit_addr.setText("");
            }
        });
    }

    // 데이터베이스 상태정보
    public void updateListItem(final int command, final String name, final String phone, final String addr) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (command) {
                    case INSERT_DATA: {
                        items.add("(" + name + "," + phone + "," + addr + ")");
                        break;
                    }

                    case DELETE_DATA: {
                        int index = -1;
                        for (String str : items) {
                            if (str.equals("(" + name + "," + phone + "," + addr + ")")) {
                                index = items.indexOf(str);
                            }
                        }
                        if (index == -1) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    toast("삭제할 데이터가 존재하지 않습니다.");
                                }
                            });
                        } else {
                            items.remove(index);
                        }
                        break;
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        // listView_log.setSelection(adapter.getCount() - 1);
                    }
                });
            }
        }).start();
    }

    public void updateListItem(final int command, final Cursor cursor) {
        if (command == SELECT_ALL_DATA) {
            Log.d(TAG + " - cursor.getCount(): ", cursor.getCount() + "");
            StringBuilder stringBuilder = new StringBuilder();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Log.d(TAG + " - getColumnCount():", cursor.getColumnCount() + "");

                String name = cursor.getString(1);
                String phone = cursor.getString(2);
                String addr = cursor.getString(3);

                stringBuilder.append("(" + name + "," + phone + "," + addr + ")\n");
                items.add("(" + name + "," + phone + "," + addr + ")");

                cursor.moveToNext();

            }
            Log.d(TAG + "- selectAll():", stringBuilder.toString());
        }
    }

    public void updateListItem(final int command, final String name, final String phone_old, final String addr_old, final String phone_new, final String addr_new) {
        if (command == UPDATE_DATA) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateListItem(INSERT_DATA, name, phone_new, addr_new);
                    updateListItem(DELETE_DATA, name, phone_old, addr_old);
                }
            });
        }
    }

    public void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            final String str = items.get(position);
            final String[] arr_str = str.split(",");
            final String name = arr_str[0].substring(1);
            final String phone = arr_str[1];
            final String addr = arr_str[2].replace(")", "");

            LayoutInflater inflater = getLayoutInflater();

            final View dialogView = inflater.inflate(R.layout.dialog_update_data, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Edit contact");
            builder.setIcon(R.drawable.ic_mode_edit_black_24dp);
            builder.setView(dialogView);

            builder.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    EditText edit_phone = dialogView.findViewById(R.id.dialog_update_phone);
                    EditText edit_address = dialogView.findViewById(R.id.dialog_update_address);

                    if (edit_phone.getText().toString() != null && edit_address.getText().toString() != null) {
                        helper.update(db, name, phone, addr, edit_phone.getText().toString(), edit_address.getText().toString());
                    }

                    toast("정보가 수정되었습니다.");
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // do not anything
                }
            });

            AlertDialog dialog = builder.create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
    };

    public void onDestroy() {
        super.onDestroy();
        // helper.dropTable(db);
        db.close();
    }

}
