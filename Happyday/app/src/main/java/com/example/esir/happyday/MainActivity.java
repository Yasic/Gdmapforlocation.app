package com.example.esir.happyday;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.renderscript.Script;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.amap.api.location.AMapLocalDayWeatherForecast;
import com.amap.api.location.AMapLocalWeatherForecast;
import com.amap.api.location.AMapLocalWeatherListener;
import com.amap.api.location.AMapLocalWeatherLive;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapUtils;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.Circle;
import com.amap.api.maps2d.model.CircleOptions;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.PolylineOptions;

import java.util.Calendar;
import java.util.List;


public class MainActivity extends Activity implements
        AMapLocationListener, LocationSource,AMapLocalWeatherListener{
    private OnLocationChangedListener mListener;
    private LocationManagerProxy mAMapLocationManager;
    private MapView mapView;
    private AMap aMap;
    private LocationManagerProxy mLocationManagerProxy;
    private Double geoLat,geoLng,geoLatp,geoLngp;
    private int flag,lingo,time,distance;
    private TextView testtext;
    private Circle circle;
    private Marker marker,markerbegin;
    private AMapUtils amaputils;
    private Button locbutton;
    private Toast toast;
    private String address,weather,winddir,windpower,humidity,datatime;
    private String todayweather1,todaydate,todaytemp,todaynighttemp,todaywindpower1,todaywindpower2,todayweather2;
    private String tomorrowweather1,tomorrowdate,tomorrowtemp,tomorrownighttemp,tomorrowwindpower2,tomorrowwindpower1,tomorrowweather2;
    private String afterweather1,afterdate,aftertemp,afternighttemp,afterwindpower1,afterwindpower2,afterweather2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);// 必须要写
        time = 60;
        lingo = 1;
        distance = 10;
        flag = 0;//判断是否模糊定位
        init();
    }
    /**
     * 初始化AMap对象
     */
    private void init() {
        if (aMap == null) {
            aMap = mapView.getMap();
            //aMap.setLocationSource(this);
            //aMap.setMyLocationEnabled(true);
            //aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
            mLocationManagerProxy = LocationManagerProxy.getInstance(this);
            mLocationManagerProxy.requestLocationData(
                    LocationProviderProxy.AMapNetwork, time * 1000, 10, this);//定位方式、定位最短时间、定位最短距离、定位监听者
            //mLocationManagerProxy.setGpsEnable(false);
        }
    }



    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if(amapLocation != null && amapLocation.getAMapException().getErrorCode() == 0) {
            //获取位置信息
            mLocationManagerProxy.requestWeatherUpdates(LocationManagerProxy.WEATHER_TYPE_LIVE, this);
            mLocationManagerProxy.requestWeatherUpdates(LocationManagerProxy.WEATHER_TYPE_FORECAST,this);
            geoLat = amapLocation.getLatitude();
            geoLng = amapLocation.getLongitude();
            address = amapLocation.getAddress();
            if (flag == 0) {
                if(lingo == 1){
                    aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(geoLat,geoLng),16,0,0)), 1000, null);
                    markerbegin = aMap.addMarker(new MarkerOptions().position(new LatLng(geoLat,geoLng)).title("起点"));
                }
                if (lingo != 1) {
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(geoLat,geoLng)), 1000, null);
                    aMap.addPolyline(new PolylineOptions().add(marker.getPosition(), new LatLng(geoLat, geoLng)).color(0xff3366CC).width(4));
                    marker.remove();
                    circle.remove();
                }
            marker = aMap.addMarker(new MarkerOptions().position(new LatLng(geoLat, geoLng)).title(lingo + "." + amapLocation.getAddress()));
                circle = aMap.addCircle(new CircleOptions().center(new LatLng(geoLat, geoLng))
                        .strokeColor(0xcc3300ff).radius(20).fillColor(0x223300ff).strokeWidth(2));
            lingo++;
            }
            if(flag == 1){
                if(lingo == 1){
                    aMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(geoLat, geoLng),16,0,0)), 1000, null);
                    markerbegin = aMap.addMarker(new MarkerOptions().position(new LatLng(geoLat,geoLng)).title("起点"));
                }
                if(lingo != 1 && amaputils.calculateLineDistance(marker.getPosition(), new LatLng(geoLat, geoLng)) > 10){
                    aMap.animateCamera(CameraUpdateFactory.changeLatLng(new LatLng(geoLat, geoLng)), 1000, null);
                    toast.makeText(getApplicationContext(),"got it!",Toast.LENGTH_LONG).show();
                    aMap.addPolyline(new PolylineOptions().add(marker.getPosition(), new LatLng(geoLat, geoLng)).color(0xff3366CC).width(4));
                    marker.destroy();
                    circle.setVisible(false);
                    marker = aMap.addMarker(new MarkerOptions().position(new LatLng(geoLat, geoLng)).title(lingo + "." + amapLocation.getAddress()));
                    circle = aMap.addCircle(new CircleOptions().center(new LatLng(geoLat, geoLng))
                            .strokeColor(0xcc3300ff).radius(20).fillColor(0x223300ff).strokeWidth(2));
                }
                lingo++;
            }
            //mListener.onLocationChanged(amapLocation);//获取小蓝点
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            // 监控返回键
            new AlertDialog.Builder(this)
                    //.setIcon(R.drawable.gong1)
                    .setTitle("确认要退出吗？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    System.exit(0);
                                }
                            })
                    .setNegativeButton("取消",null)
                    .create()
                    .show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.actionbutton1) {
            if(flag == 0){
                flag = 1;
                item.setTitle("模糊定位已开");
            }
            else if(flag == 1){
                flag = 0;
                item.setTitle("模糊定位已关");
            }
            return true;
        }
        if (id == R.id.actionbutton2) {
            LayoutInflater layoutinflater = LayoutInflater.from(getApplicationContext());
            final View newdialog = layoutinflater.inflate(R.layout.timedialog, null);
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("设置模糊距离（米）").setView(newdialog).
                    setPositiveButton("yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText edittext = (EditText)newdialog.findViewById(R.id.timeedit);
                            //edittext.setText(distance);
                            if( edittext.getText()!=null){
                                distance = Integer.parseInt(edittext.getText().toString());
                            }
                            else {
                                toast.makeText(getApplicationContext(),"数字无效",Toast.LENGTH_LONG).show();
                            }
                        }
                    }).setNegativeButton("cancel", null);
            Dialog dialog = builder.show();
            return true;
        }
        if(id == R.id.actionsubbutton1){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("定位信息")
                    .setMessage("纬度：" + geoLat + "\n" + "经度：" + geoLng + "\n" + "地址：" + address + "\n" + "模糊定位距离：" + distance + "米");
            Dialog dialog = builder.show();
            return true;
        }
        if(id == R.id.actionbutton4){
            AlertDialog.Builder builder = new AlertDialog
                    .Builder(this)
                    .setTitle("帮助说明")
                    .setMessage("1.30s一次定位，在地图上表示出运动路线" + "\n" + "2.模糊定位是指忽略与前次定位距离小于模糊距离的定位信息"
                            + "\n" + "3.模糊定位开关关闭时设定模糊距离无效" + "\n" + "4.定位的电量消耗大、不用时请及时关闭app");
            Dialog dialog = builder.show();
        }
        if(id == R.id.actionbutton5){
            new AlertDialog.Builder(this)
                    .setTitle("确认要退出吗？")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    System.exit(0);
                                }
                            })
                    .setNegativeButton("取消",null)
                    .create()
                    .show();
        }
        if(id == R.id.actionsubbutton2){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("天气信息")
                    .setMessage("天气：" + weather + "\n" + "风向：" + winddir + "\n" + "风力：" + windpower + "级" + "\n" + "空气湿度：" +
                            humidity + "\n" + "数据发布时间" + datatime);
            Dialog dialog = builder.show();
        }
        if(id == R.id.actionsubbutton3){
            //todo
        }
        if(id == R.id.actiontodaybutton){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("今天")
                    .setMessage("时间："+todaydate+"\n"+"白天天气："+todayweather1+"\n"+"夜间天气："+todayweather2+"\n"+"白天气温："+todaytemp+"\n"
                    +"夜间气温："+todaynighttemp);
            Dialog dialog = builder.show();
        }
        if(id == R.id.actiontomorrowbutton){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("明天")
                    .setMessage("时间："+tomorrowdate+"\n"+"白天天气："+tomorrowweather1+"\n"+"夜间天气："+tomorrowweather2+"\n"+"白天气温："+tomorrowtemp+"\n"
                            +"夜间气温："+tomorrownighttemp);
            Dialog dialog = builder.show();
        }
        if(id == R.id.actionafterbutton){
            AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle("后天")
                    .setMessage("时间："+afterdate+"\n"+"白天天气："+afterweather1+"\n"+"夜间天气："+afterweather2+"\n"+"白天气温："+aftertemp+"\n"
                            +"夜间气温："+afternighttemp);
            Dialog dialog = builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {
        mListener = null;
        if (mAMapLocationManager != null) {
            mAMapLocationManager.removeUpdates(this);
            mAMapLocationManager.destroy();
        }
        mAMapLocationManager = null;
    }

    @Override
    public void onWeatherLiveSearched(AMapLocalWeatherLive aMapLocalWeatherLive) {
        if(aMapLocalWeatherLive!=null && aMapLocalWeatherLive.getAMapException().getErrorCode() == 0){
            weather = aMapLocalWeatherLive.getWeather();//天气情况
            winddir = aMapLocalWeatherLive.getWindDir();//风向
            windpower = aMapLocalWeatherLive.getWindPower();//风力
            humidity = aMapLocalWeatherLive.getHumidity();//空气湿度
            datatime = aMapLocalWeatherLive.getReportTime();//数据发布时间
        }else{
            // 获取天气预报失败
            //Toast.makeText(this,"获取天气预报失败:", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWeatherForecaseSearched(AMapLocalWeatherForecast aMapLocalWeatherForecast) {
        if(aMapLocalWeatherForecast != null && aMapLocalWeatherForecast.getAMapException().getErrorCode() == 0){
            List<AMapLocalDayWeatherForecast> allforcasts = aMapLocalWeatherForecast.getWeatherForecast();
            for(int i = 0 ;i < allforcasts.size();i++){
                AMapLocalDayWeatherForecast forcast = allforcasts.get(i);
                switch (i){
                    case 0:
                        todaydate = forcast.getDate();
                        todayweather1 = forcast.getDayWeather();
                        todayweather2 = forcast.getNightWeather();
                        todaytemp = forcast.getDayTemp();
                        todaynighttemp = forcast.getNightTemp();
                        todaywindpower1 = forcast.getDayWindPower();
                        todaywindpower2 = forcast.getNightWindPower();
                        break;
                    case 1:
                        tomorrowdate = forcast.getDate();
                        tomorrowweather1 = forcast.getDayWeather();
                        tomorrowweather2 = forcast.getNightWeather();
                        tomorrowtemp = forcast.getDayTemp();
                        tomorrownighttemp = forcast.getNightTemp();
                        tomorrowwindpower1 = forcast.getDayWindPower();
                        tomorrowwindpower2 = forcast.getNightWindPower();
                        break;
                    case 2:
                        afterweather1 = forcast.getDayWeather();
                        afterweather2 = forcast.getNightWeather();
                        afterdate = forcast.getDate();
                        aftertemp = forcast.getDayTemp();
                        afternighttemp = forcast.getNightTemp();
                        afterwindpower1 = forcast.getDayWindPower();
                        afterwindpower2 = forcast.getNightWindPower();
                }
            }
        }
    }
}
