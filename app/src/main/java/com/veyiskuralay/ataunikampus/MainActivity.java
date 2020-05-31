package com.veyiskuralay.ataunikampus;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.material.navigation.NavigationView;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.veyiskuralay.ataunikampus.database.DatabaseHelper;
import com.veyiskuralay.ataunikampus.fragments.AboutFragment;
import com.veyiskuralay.ataunikampus.fragments.HomeFragment;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<LocationSettingsResult> {

    Fragment fragment = null;               // Yandan açılır menü'yü  fragment yapısı ile kullanacağız. ekranda gösterilecek fragmenti tanımlıyoruz.
    NavigationView navigationView;          // Yan menü sınıfın tanımlanması
    DrawerLayout drawer;                    // Yan menüye ait otomatik oluşturalan sınıflar
    Toolbar toolbar;                        // uygulama ismimizin ve yan menü simgesinin yer aldığı barımızı tanımladık.


    // Konum servini uygulamadan çıkmadan kullanıcı onayı ile açmak için oluşturduk.
    // Kullanılan yapı Google tarafından hazır sunulmaktadır.
    // https://developer.android.com/training/location/change-location-settings.html
    protected GoogleApiClient mGoogleApiClient;
    protected LocationRequest locationRequest;
    int REQUEST_CHECK_SETTINGS = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Daha önce oluşturduğumuz veritabanın kopyalanması için DatabaseHelper sınıfımızı çağırıyoruz.
        // Bir kere kopyalama işlemi yaptıktan sonra tekrar çalışmayacak.
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.CreateDataBase();
        } catch (Exception ex) {
        }

        setContentView(R.layout.activity_main);             // tasarım kısmı layout'un bağlanması
        toolbar = (Toolbar) findViewById(R.id.toolbar);     // toolbarın bağlantısı
        setSupportActionBar(toolbar);                       // toolbarın bu sınıf için geçerli olan toolbar olduğunun belirtilmesi
        toolbar.setContentInsetsAbsolute(-5, -5);            // sol taraftaki menü simgesinden boşluğun fazlığının giderilmesi için sayfaya ait isim bilgisini biraz sola çektik.

        // Soldan açılan menü için Google tarafından sunulan hazır yapı ile oluşturduk.
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Ekranda gösterilecek sayfayı (Fragmenti) belirtmek hazırladığımız fonksiyona ilk açaçağı sayfa olarak Ana Sayfa değlerini gönderiyoruz.
        displayView(R.id.nav_anasayfa, "Ana Sayfa");

        gpsOnControl();     // Kullanıcın Gps'nin açık olup olamadığının kontrol ettiğimiz fonksiyonu çağırıyoruz.
    }

    @Override
    public void onBackPressed() {   // Geri tuşuna basıl dığı durumda çalışacak.

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);  // yan menü açıl ise kapatılmasını sağıyoruz.
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();          // Yan menü açık değil ise uygulamanın kapanmasını gerçekleşiyor.
        }
    }


    // Yan menü için kullanılan hazır yapı
    // yan menüde seçim yapılması halinde çalışacak.
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        displayView(item.getItemId(), item.getTitle().toString());       // yan menüde seçilen değeri fonksiyonumuza gönderek o sayfanın açılmasını sağlıyoruz.
        drawer.closeDrawer(GravityCompat.START);        // tıklamadan sonra yan menüyü kapatıyoruz.
        return true;
    }

    public void displayView(int position, String getTitle) {        // Ana ekranda fragment yapısını çalışması için kendi yazdığımız fonksiyon.


        String title = getTitle;        // Toolbarda gösterilecek olan sayfa isminin tutulması için tanımladık
        switch (position) {

            case R.id.nav_anasayfa:             // Anasayfanın seçilmesi halide çalışcak alan
                fragment = new HomeFragment();  // Fragment değerimize HomeFragment'i atıyoruz. Ana ekranda bunu gösterimi gerçekleştireceğiz.
                break;


            case R.id.nav_hakkinda:               // Hakkında seçilmesi halince çalışacak alan
                fragment = new AboutFragment(); // Fragment değerimize AboutFragment'i atıyoruz. Konum ekle sayfasının gösterimi gerçekleştireceğiz.
                break;

            case R.id.nav_cikis:                                           // Çıkış seçeneği tıklandığında çalışacak alan
                title = getSupportActionBar().getTitle().toString();      // başlık bilgisinin silmemesi için daha önce yazan başlığı alarak bu değerin tekrar yazmasını sağladık.

                // Android 5.0 ve üzeri için uygulamayı son kullanılan uygulamalardan silmesini sağlayarak kapatılmasını gerçekleştiriyoruz.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAndRemoveTask();
                } else        // Android 5.0 ve altı için böyle bir özellik olmadığı için uygulama normak kapatılıyor. (Son kullanılan uygulamalarda kalacak.)
                    finish();
                break;


            default:
                break;
        }


        if (fragment != null) {  // Yüklenecek fragment'in boş olup olmadığını kontrol ediyoruz.

            getSupportActionBar().setTitle(title);      // toolbarımızdaki başlık alanına başlığı yazdırıyoruz.

            FragmentManager fragmentManager = getSupportFragmentManager();                          // fragmenti göstermek için oluşturduk.
            fragmentManager.beginTransaction().replace(R.id.container_body, fragment).commit();     // fragmentin ana ekranda gösterilmesini sağladık.

        }
    }


    // Gps'in kapalı olması halide kullanıcaya uygulamadan çıkmayarak açabilmesi için bir uyarı pencerisi gösteriyoruz.
    // Bu özellik Google tarafından sunulmaktadır.
    private void gpsOnControl() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);

    }

    // Gps'in bağlanması durumu
    @Override
    public void onConnected(Bundle bundle) {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(
                        mGoogleApiClient,
                        builder.build()
                );

        result.setResultCallback(this);

    }

    @Override
    public void onConnectionSuspended(int i) {


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    // Konum açma için sonuç
    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {

        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:

                // NO need to show the dialog;

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  Location settings are not satisfied. Show the user a dialog

                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show
                }
                break;

            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }

    }

    // Kullanıcın Gps'i açmasi için sunduğumuz uyarı pencerisine verdiği cevabı kontrol edeceğiz.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {

            if (resultCode == RESULT_OK) {  // Konumunu açılması onaylaması durumu

                // GPS etkin olduğunu kullancıya bildiriyoruz.
                Toast.makeText(getApplicationContext(), "GPS etkin", Toast.LENGTH_LONG).show();
            } else {
                // GPS devre dışı olduğunu kullanıcıya bildiriyoruz.
                Toast.makeText(getApplicationContext(), "GPS devredışı", Toast.LENGTH_LONG).show();
            }

        }
    }

    // Uygulamanın arka plana alınıp tekrar açılması durumu konum servisinin açık olup olmadığını kontrol etmesi için  mGoogleApiClient bağlıyoruz.
    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }


    // Uygulamanın arka plana alınması durumu mGoogleApiClient bağı ise durduruyoruz.
    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }
}
