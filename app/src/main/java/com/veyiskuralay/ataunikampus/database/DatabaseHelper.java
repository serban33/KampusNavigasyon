package com.veyiskuralay.ataunikampus.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


// Uygulama çalışmadan önce hazırladığımız SQLite veritabanımızı uygulama açıldığında 1 seferlik kopyalamak için oluşturduk.
public class DatabaseHelper extends SQLiteOpenHelper {
    static String DB_PATH;
    //Veritabanı ismini veriyoruz
    static String DB_NAME = "database";

    SQLiteDatabase myDatabase;

    final Context myContext;

    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);

        //DB_PATH = context.getFilesDir().getParent() + "/databases/";

        DB_PATH = "/data/data/com.veyiskuralay.ataunikampus/databases/";

        this.myContext = context;
    }

    //Assest dizinine koyduğumuz sql dosyasını, Android projesi içine oluşturma ve kopyalamna işlemi yapıldı..
    public void CreateDataBase() {
        boolean dbExists = checkDataBase();

        if (!dbExists) {
            this.getReadableDatabase();

            try {
                copyDataBase();
                // Toast.makeText(myContext, "Kopyalandi", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Log.w("hata", "Veritabanı kopyalanamıyor");
                //Toast.makeText(myContext, "Veritabanı kopyalanamıyor", Toast.LENGTH_SHORT).show();
                throw new Error("Veritabanı kopyalanamıyor.");
            }
        } else {
            //Toast.makeText(myContext, "onceden Kopyalandi", Toast.LENGTH_SHORT).show();
        }
    }

    //Sqlite veritabanı dosyasını açıp, veritabanımı kontrol ediyoruz
    boolean checkDataBase() {
        SQLiteDatabase checkDB = null;

        try {
            String myPath = DB_PATH + DB_NAME;

            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
        } catch (Exception ex) {


            //  Toast.makeText(myContext, "Veritabanı açılamadı", Toast.LENGTH_SHORT).show();
        }

        if (checkDB != null)
            checkDB.close();

        return checkDB != null ? true : false;
    }

    void copyDataBase() {
        try {
            InputStream myInput = myContext.getAssets().open(DB_NAME);

            String outFileName = DB_PATH + DB_NAME;

            OutputStream myOutput = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];

            int length;

            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();

            myInput.close();
            myOutput.close();
        } catch (Exception ex) {

            // Toast.makeText(myContext, "Kopya oluşturma hatası.", Toast.LENGTH_SHORT).show();
        }
    }
    //Veritabannı açma işlemi yapıldı

    void openDataBase() {
        String myPath = DB_PATH + DB_NAME;

        myDatabase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }

    @Override
    public synchronized void close() {
        if (myDatabase != null && myDatabase.isOpen())
            myDatabase.close();

        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
