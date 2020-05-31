package com.veyiskuralay.ataunikampus.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.veyiskuralay.ataunikampus.model.PointItem;

import java.util.ArrayList;

public class Database extends SQLiteOpenHelper {

    // Veritabanı işlemlerini gerçekleştirmek için oluşturduk.
    // Veritabanın versiyonu, ismi ve tablomuzdaki sütün isimlerini tanımladık.
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "database";


    private static final String TABLE_NAME = "points";
    private static final String P_ID = "id";
    private static final String P_NAME = "p_name";
    private static final String P_LAT = "p_lat";
    private static final String P_LNG = "p_lng";
    private static final String P_IMAGE = "p_image";

    Context context;


    public Database(Context context) {                // Veritabanın çalıştırılması için tanımlanan fonksiyon
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    public ArrayList<PointItem> points() {            // Veritabanındaki konum bilgilerinin tamamını çektiğimiz fonksiyon


        SQLiteDatabase db = this.getReadableDatabase();            // Veritabanından okuma ile db değişkenli  SQLiteDatabase oluşturuyoruz.
        String selectQuery = "SELECT * FROM " + TABLE_NAME;            // SQL sorgumuzu yazıyoruz. Tablodaki tüm değerlerin çeklmesini sağladık.
        Cursor cursor = db.rawQuery(selectQuery, null);                    // sorguyu Cursor'a atarak çalışmasını sağladık
        ArrayList<PointItem> points_list = new ArrayList<PointItem>();    // Oluşturduğumuz PointItem sınıfı ile bir ArrayList oluşturdul verileri buna yükleyip döndereceğiz.

        if (cursor.moveToFirst()) {   // cursor'u çalıştırıyoruz.
            do {

                for (int i = 0; i < cursor.getColumnCount(); i++) {
                    // PointItem sınıfımıza veritabanından çektiğimiz değerleri tek tek atadık.
                    points_list.add(new PointItem(
                            cursor.getInt(cursor.getColumnIndex("id")), cursor.getString(cursor.getColumnIndex("p_name"))
                            , cursor.getDouble(cursor.getColumnIndex("p_lat")), cursor.getDouble(cursor.getColumnIndex("p_lng")), cursor.getString(cursor.getColumnIndex("p_image"))));
                }

            } while (cursor.moveToNext());  // Veritabanındaki tüm değerlere ulaşmak için sürekli bir sonraki değere geçmesini sağladık.
        }
        db.close();            // Veritabanı işlemi bittiği için kapatıyoruz.

        return points_list;        // oluşan sonuçları dönderiyoruz.
    }


    // Veritabanı güncellemesi için otomatik oluşturulan kendi metodu
    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub

    }

}
