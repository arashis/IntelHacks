package com.example.arashis.iokey;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.IntegerRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private DatabaseReference mDatabase;
    private TextView  t;
    private String status;
    private String latitude;
    private String longtitude;
    private String homeLatitude;
    private String homeLongtitude;

    private int[] rangevalue = {1000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        setTitle("");
        ((TextView) findViewById(R.id.toolbar_title_sub)).setText("IoK");
        t = (TextView)findViewById(R.id.status);

        ListView myListView = (ListView) findViewById(R.id.myListView);

        // ListViewに表示
        final KeyAdapter ka = new KeyAdapter(this);
        myListView.setAdapter(ka);


        mDatabase = FirebaseDatabase.getInstance().getReference("Status");

        // Attach a listener to read the data at our posts reference
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status = dataSnapshot.getValue(String.class);
                t.setText(status);

                Date date = new Date();
                SimpleDateFormat sdf1 = new SimpleDateFormat("M/d  HH:mm:ss");
                ka.additem(status,sdf1.format(date));
                ka.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
            });

        // 確認用、後で消す
//        TextView s = (TextView) findViewById(R.id.status);
//        s.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Date date = new Date();
//                SimpleDateFormat sdf1 = new SimpleDateFormat("M/d  H:m:s");
//                ka.additem(status,sdf1.format(date));
//                ka.notifyDataSetChanged();
//            }
//        });

        // Fine か Coarseのいずれかのパーミッションが得られているかチェックする
        // 本来なら、Android6.0以上かそうでないかで実装を分ける必要がある
        if (ActivityCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            /** fine location のリクエストコード（値は他のパーミッションと被らなければ、なんでも良い）*/
            final int requestCode = 1;

            // いずれも得られていない場合はパーミッションのリクエストを要求する、パーミッションを許可したタイミングではGPSが取得できないっぽいので、一旦アプリを落とす必要がある
            ActivityCompat.requestPermissions(this, new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, requestCode );
            return;
        }

        // 位置情報を管理している LocationManager のインスタンスを生成する
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String locationProvider = null;

        // GPSが利用可能になっているかどうかをチェック
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationProvider = LocationManager.GPS_PROVIDER;
        }
        // GPSプロバイダーが有効になっていない場合は基地局情報が利用可能になっているかをチェック
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationProvider = LocationManager.NETWORK_PROVIDER;
        }
        // いずれも利用可能でない場合は、GPSを設定する画面に遷移する
        else {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            return;
        }

        /** 位置情報の通知するための最小時間間隔（ミリ秒） */
        final long minTime = 3000;
        /** 位置情報を通知するための最小距離間隔（メートル）*/
        final long minDistance = 1;

        // 利用可能なロケーションプロバイダによる位置情報の取得の開始
        // FIXME 本来であれば、リスナが複数回登録されないようにチェックする必要がある
        locationManager.requestLocationUpdates(locationProvider, minTime, minDistance, this);
        // 最新の位置情報
        Location location = locationManager.getLastKnownLocation(locationProvider);

        if (location != null) {
            latitude = String.valueOf(location.getLatitude());
            longtitude = String.valueOf(location.getLongitude());
            Log.d("d",latitude + longtitude);
        }
    }

    //位置情報が通知されるたびにコールバックされるメソッド
    @Override
    public void onLocationChanged(Location location){
        latitude = String.valueOf(location.getLatitude());
        longtitude = String.valueOf(location.getLongitude());
        Log.d("d",latitude + longtitude);
        if(!status.equals(null)) {
            if (status.equals("Open")) {
                if (Math.abs(Float.valueOf(latitude) - Float.valueOf(homeLatitude)) >= 0.000008983148616 * Float.valueOf(rangevalue[0]) || Math.abs(Float.valueOf(longtitude) - Float.valueOf(homeLongtitude)) >= 0.000010966382364 * Float.valueOf(rangevalue[0])) {
                    sendNotification();
                }
            }
        }
    }

    //ロケーションプロバイダが利用不可能になるとコールバックされるメソッド
    @Override
    public void onProviderDisabled(String provider) {
    //ロケーションプロバイダーが使われなくなったらリムーブする必要がある
    }

    //ロケーションプロバイダが利用可能になるとコールバックされるメソッド
    @Override
    public void onProviderEnabled(String provider) {
    //プロバイダが利用可能になったら呼ばれる
    }

    //ロケーションステータスが変わるとコールバックされるメソッド
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    // 利用可能なプロバイダの利用状態が変化したときに呼ばれる
    }

    //notification
    private int REQUEST_CODE_MAIN_ACTIVITY = 1000;
    private int NOTIFICATION_CLICK = 2000;
    private void sendNotification() {
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                MainActivity.this, REQUEST_CODE_MAIN_ACTIVITY, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.icon);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext());
        builder.setContentIntent(contentIntent);
        builder.setTicker("My house key is open");
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("My house key is open");
        builder.setContentText("My house key is open");
        builder.setLargeIcon(largeIcon);
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_SOUND
                | Notification.DEFAULT_VIBRATE
                | Notification.DEFAULT_LIGHTS);
        builder.setAutoCancel(true);
        NotificationManager manager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_CLICK, builder.build());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                homeLatitude =latitude;
                homeLongtitude = longtitude;
                final CoordinatorLayout layout = (CoordinatorLayout) findViewById(R.id.root);
                Snackbar.make(layout, "set here as home location", Snackbar.LENGTH_SHORT)
                        .setAction("", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("Snackbar.onClick", "UNDO Clicked");
                            }
                        })
                        .show();
                return true;
            case R.id.action_settingsrange:

                final View dialog_setrange = getLayoutInflater().inflate(R.layout.dialog_setrange, null);
                final AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                        .setView(dialog_setrange)
                        .show();

                final TextView trange = (TextView) dialog_setrange.findViewById(R.id.text_range);
                final SeekBar srange = (SeekBar) dialog_setrange.findViewById(R.id.seekbar_range);
                rangevalue[0] = 10;


                srange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // TODO Auto-generated method stub
                        progress = ((int)Math.round(progress/10 ))*10;
                        seekBar.setProgress(progress);
                        rangevalue[0] = srange.getProgress() + 10;
                        trange.setText(Integer.toString(rangevalue[0]) + "m");
                    }
                });

                dialog_setrange.findViewById(R.id.open_camera).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setRangeNotify();
                        dialog.dismiss();
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    public void setRangeNotify(){
        final CoordinatorLayout layout2 = (CoordinatorLayout) findViewById(R.id.root);

        Snackbar.make(layout2, "set notification range " + Integer.toString(rangevalue[0]) + "m" , Snackbar.LENGTH_SHORT)
                        .setAction("", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d("Snackbar.onClick", "UNDO Clicked");
                            }
                        })
                        .show();
    }
}