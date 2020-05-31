package com.veyiskuralay.ataunikampus.fragments;


import com.google.android.material.snackbar.Snackbar;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.veyiskuralay.ataunikampus.R;
import com.veyiskuralay.ataunikampus.database.Database;
import com.veyiskuralay.ataunikampus.model.PointItem;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment implements TextToSpeech.OnInitListener { // TextToSpeech sesli uyarı için listener kullanımı için implements ettik

    View rootView;
    Database db;                          // database clasımızdaki fonksiyonları kullanmak için tanımladım.
    ArrayList<PointItem> points_list;    //  database'den gelen kordinatları PointItem modeline göre çekip ArrayList'te tutmak için tanımladım.

    TextView tw_title, tw_distance, tw_lat, tw_altitude;   // Tasarım kısmı layout'ta ki textview'lerin tanımlanması
    ImageView iv_point;                                //  Tasarım kısmı layout'ta ki imageview'in tanımlanması

    private TextToSpeech mTts;                        // Sesli uyarı vermek için  android tarafından sunulan kütüphaneyi kullanacağız. Değişkenimizi tanımlıyoruz.


    DecimalFormat dformat = new DecimalFormat("##.####", new DecimalFormatSymbols(Locale.US)); // Kullanıcın kordinatlarının noktadan sonra 4 basamağını göstermek için dönüşüm formatı
    DecimalFormat dformat_meter = new DecimalFormat("###,###.#", new DecimalFormatSymbols(Locale.US));  // Konuma uzaklığının metre ile gösterilmesi için dönüşüm formatı noktadan sonra 1 basamak

    private static final String[] INITIAL_PERMS = {               // Android 6.0 ve üzeri sürümler için kullanıcıdan konum izini alınması için tanımlandı
            android.Manifest.permission.ACCESS_FINE_LOCATION,       // Hassas konum bilgisi için izin  tanımlanması
            android.Manifest.permission.ACCESS_COARSE_LOCATION      // Normal konum bilgisi için izinin tanımlanması
    };
    private static final int INITIAL_REQUEST = 1337;      // Android 6.0 ve üzeri sürümlerde kullanıcıya sorarak alacağımız izinin kodu


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new Database(getContext());    // Database sınıfımızdan yeni bir Database üretiyoruz. Veritabanı işlemleri için kullandım.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_home, container, false);  // fragment'in layout bağlantısı

        tw_lat = (TextView) rootView.findViewById(R.id.tw_lat);                 // kullanıcın konum bilgilerini göstereceğimiz textview'in bağlantısını
        tw_altitude = (TextView) rootView.findViewById(R.id.tw_altitude);       // kullanıcın yükseklik bilgisini göstereceğimiz textview'in bağlantısını
        tw_title = (TextView) rootView.findViewById(R.id.tw_title);             // yaklaşılan konumun başlığını göstereceğimiz textview'in bağlantısını
        tw_distance = (TextView) rootView.findViewById(R.id.tw_distance);       // yaklaşılan konuma uzaklığını göstereceğimiz textview'in bağlantısını
        iv_point = (ImageView) rootView.findViewById(R.id.iv_point);            // yaklaşılan konumun fotoğrafını göstereceğimiz imageview'in bağlantısını


        mTts = new TextToSpeech(getContext(), this);  // sesli uyarı için yeni bir sınıf türetiyoruz.

        getPoints();  // Veritabanında kayıtlı olan noktaların çekilmesi

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {                 // Android 6.0 ve üzeri konum izinin alınması için sürüm kontrolu yapıyoruz.
            if (!canAccessCoarseLocation() || !canAccessFineLocation()) {     // İzinin daha önce alınıp alınmadığını kontrol ediyoruz.
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);         // İzin alınmadığı için yukarıda tanımladığımız dizideki izinleri izin kodumuz ile kullanıcadan izin istiyoruz.
            }                                                               // Bu işlem sadece Android 6.0 ve üzeri sürümler için geçerlidir. Alt sürümler direk izin almaktadır.
            else {
                getLocation();  // İzin daha önce alınmış ise kullanıcının lokasyon bilgisinin alındığı fonksiyonu çağırıyoruz.
            }
        } else {  // 6.0 ve altı için izin alınması gerekmediğinden kullanıcının lokasyon bilgisinin alındığı fonksiyonu çağırıyoruz.
            getLocation();
        }

        return rootView;
    }

    // Anroid 6.0 ve üzeri için kullanıcının izin vermesinin kontrolunu sağlıyoruz.
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == INITIAL_REQUEST) {

            if (canAccessFineLocation() && canAccessCoarseLocation()) { // Kullanıcı izin vermesi kontrolu
                getLocation();
                Snackbar.make(rootView.findViewById(R.id.cl_homefragment), "İzin başarılı.", Snackbar.LENGTH_LONG)
                        .setAction("Kapat", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                            }
                        }).show();

            } else {  //İzin vermemsi durumu
            }
        }
    }

    // Hassas izin için daha önce kullanıcın izin vermesinin kontorlunu yapıyoruz
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessFineLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    // Normal izin için daha önce kullanıcın izin vermesinin kontorlunu yapıyoruz
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean canAccessCoarseLocation() {
        return (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION));
    }

    // Yukarıdaki izinlerin kontrolunun gerçekleştirmesi
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(getContext(), perm));
    }

    private void getPoints() {  // Veritabanından konumların çekilmesi
        try {
            points_list = db.points(); // yukarıda tanımladığımız array list değerlerin atılması
        } catch (Exception e) {
        }
    }

    // Kullanıcının konum bilgisinin alındığı fonksiyon
    private void getLocation() {

        // Android 6.0 ve üzeri için izin alınmışmı diye kontrol ediyoruz. Hataya düşmemesi için. İzin alınmamış ise return ile fonksiyondan çıkacak
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        // Kullanıcın konumunu almak için oluşturduk
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        // Kullanıcın Veri bağlantısı veya Wifi ile konum isteğinin oluşturulması 5 sn de bir güncellenecek
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 5, new Listener());
        // Kullanıcın GPS ile konum isteğinin oluşturulması 5 sn de bir güncellenecek
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, new Listener());

        // Konumun alınması
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        // Alınan konum boş değil ise handleLatLng ile hesaplama ve ekrana yazdırma yapacağımız fonksiyona gönderiyoruz.
        if (location != null) {
            handleLatLng(location.getLatitude(), location.getLongitude(), location.getAltitude());
        }
        //Kullanıca bilgi veriyoruz.

        Snackbar.make(rootView.findViewById(R.id.cl_homefragment), "GPS koordinatları edinmeye çalışılıyor.\nKonum servisinizin açık olduğundan emin olun.", Snackbar.LENGTH_LONG)
                .setAction("Kapat", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                }).show();
    }


    // Konum servisinin kendi sınıfı
    private class Listener implements LocationListener {
        public void onLocationChanged(Location location) {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
            handleLatLng(latitude, longitude, location.getAltitude());
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    // Uzaklık hesaplanması ve ekrana yazdırma işlemleri
    private void handleLatLng(double latitude, double longitude, double altitude) {

        // Yukarıda tanımladığımız dformat ile noktadan sonra 4 basamak kadar hassasiyet olacak
        tw_lat.setText("(" + dformat.format(latitude) + " , " + dformat.format(longitude) + ")"); // Kullanıcın konumunun bilgisinin tw_lat textview'ine yazdırılması
        tw_altitude.setText("(" + dformat.format(altitude) + ")");     // kullanıcın yükseklik bilgisinin tw_altitude textview'ine yazdırılması

        Location locationA = new Location("A");  // İki nokta arasında farkı bulmak için önce kullanıcın konumun bulunduğu bir location oluşturuyoruz.

        locationA.setLatitude(latitude);    // getLocationdan gelen latitude değerini atıyoruz.

        locationA.setLongitude(longitude);   // getLocationdan gelen longitude değerini atıyoruz.

        try { // hesaplama işlemine başlıyoruz.

            int nearPoint_id = -1;                // gösterilecek id'yi tutmak için tanımladık. -1 olması ile boş olma durumunu kontrol edeceğiz.
            double nearPoint_distance = 5000000.0;  // en yakın mesafenin değerini tutmak için tanımladık
            // 1000.0 değeri kullanıcın buluduğu konumdan veritabanındaki noktalar arasında en fazla nekadar fark ile başlayacağını belirtiyor.
            // yanı kullanıcı 1 km dışında kaldığında birşey bulanamayacak

            for (int i = 0; i < points_list.size(); i++) {  // veritabanından çektiğimiz değerler ile tek tek karşılaştırma yapacağız.

                double lat2 = points_list.get(i).getP_lat(); // veritabanından latitude değerini çekiğ lat2'ye atıyoruz.
                double lng2 = points_list.get(i).getP_lng(); // veritabanından longitude değerini çekiğ lng2'ye atıyoruz.

                Location locationB = new Location("B");  // yukarıda kullanıcının konumu için oluşturduğumuz gibi vt'deki değerler içinde location oluşturuyoruz.

                locationB.setLatitude(lat2);  // vt'den gelen latitude değerini atıyoruz.

                locationB.setLongitude(lng2);   // vt'den gelen longitude değerini atıyoruz.


                double sonuc = locationA.distanceTo(locationB);  // yukarıda tanımladığımız kullanıcın konumu ile veritabanından gelen konumu uzaklığını ölçüyoruz.

                if (sonuc < nearPoint_distance) {  // sonuç ilk tanımladığımız 1000.0 değerinden küçük ise yeni sonuçumuz olacak.
                    // bu değer her yeni hesaplama için en yakın olan değeri tespit edecek
                    nearPoint_distance = sonuc;   // yeni değeri nearPoint_distance atıyoruz.
                    nearPoint_id = i;             // yeni değerimize ait id'yi nearPoint_id atıyoruz.
                }


            }

            if (nearPoint_id != -1) {    // nearPoint_id'nin -1 den farklı olması yakınlarda bir konum buluduğu ve id'sinin alındığı demek

                if (!mTts.isSpeaking()) {
                    speak(points_list.get(nearPoint_id).getP_name() + " birimine uzaklığınız " + dformat_meter.format(nearPoint_distance) + " metre");
                }


                tw_title.setText("" + points_list.get(nearPoint_id).getP_name());     // bulunan konumun id'si ile veritabanından çektiğimiz arraylist'ten ismini yazdırıyoroz.

                // dformat_meter ile hesaplanan uzaklığın metre'ye uygun noktadan sonra 1 basamak hassasiyet ile gösteriyor.
                tw_distance.setText("Uzaklığınız: " + dformat_meter.format(nearPoint_distance) + " Metre"); // uzaklık değerini yazdırıyoruz.

                // Belirlenen konumun fotoğrafının gösterilmesi ---- Veritabanında konumlara ait tuttuğumuz  görselin ismini çekiyoruz ve int değerine dönüştürüyoruz.
                int image_id = getResources().getIdentifier(points_list.get(nearPoint_id).getP_image(), "drawable", getActivity().getPackageName());

                // Glide kütüphanesi ile fotoğrafın gösterilmesini gerçekleştireceğiz
                Glide.with(getContext())  // clasımızın context'ini gönderiyoruz.
                        .load(image_id)     // yukarıda hesapladığımız konuma ait görselin id'sini gönderiyoruz.
                        .crossFade()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)       // glide'nin fotoğrafı cache alarak daha sonra hızlı yüklemesini sağladık.
                        .into(iv_point);                               // tasarım tarafında tanımladığımız, yukarıda bağladığımız imageview'e yüklenmesini belirtiyoruz.
            } else {          // nearPoint_id'nin -1 olması hiç değişmemesi demek buda yakın bir yer bulunamaması demek
                tw_title.setText("Yakın Nokta Bulunamadı");     // yakınlarda nokta bulunmadığını yazdırıyoruz.
                tw_distance.setText("GPS servisinizin açık olduğundan emin olun."); // GPS servisinin kontrol edilmesi için uyarı yazdrıyoruz.

                // yine glide ile boş image olduğunu belirten placeholder_image göreselini göstertiyoruz.
                Glide.with(getContext())
                        .load(R.drawable.placeholder_image)
                        .crossFade()
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(iv_point);
            }

        } catch (Exception e) {
        }
    }

    private void speak(String textToSpeak) {        // sesli uyarı vermek için oluşturduk. sesli olarak okunacak metni string olarak bu fonksiyon ile okutacağız

        mTts.speak(textToSpeak, TextToSpeech.QUEUE_FLUSH, null); // oluşturtuğumuz sınıfa gelen metini sesli olarak kullanıcıya sunmasını gerçekleştirdik

    }


    // Konuşma için kullandığımız kütüphanenin kurulması
    @Override
    public void onInit(int status) {
        // Kullancağımız dilin yukarıda tanımladığımız mTts'ye tanımlanması.

        if (status == TextToSpeech.SUCCESS) {
            int result = mTts.setLanguage(Locale.getDefault());                                               // Dil seçenekleri arasında Türkçe olmadığı için cihazın default olarak dilini kullanmasını gerçekleştirdik.
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Bu dil kütüphanede varmı diye kontrol ediyoruz.
                //Dil yok ise buraya düşecek

            } else {
                //Dil tanımlı ise buraya girecek
                // Ve kullanıcıya hoşgeldiniz mesajı okunacak.
                mTts.speak("Merhaba\n Kampüs App Hoş Geldiniz.", TextToSpeech.QUEUE_FLUSH, null);
            }
        } else {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void onPause() {
        if (mTts != null) {         // Uygulamanın kapatılarak arkaplana alınması halinde ses çalmasını durduruyoruz.
            mTts.stop();
            mTts.shutdown();
        }
        super.onPause();
    }
}
