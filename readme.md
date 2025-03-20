# MalltoMap sdk demo

## demo 下载

[https://github.com/never615/mallto_map_sdk_android/raw/refs/heads/master/app/apk/demo-debug.apk](https://github.com/never615/mallto_map_sdk_android/raw/refs/heads/master/app/apk/demo-debug.apk)

## How to use

### 1.gradle 文件中添加依赖

```gradle
implementation("org.altbeacon:android-beacon-library:2.20.6")
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("com.google.code.gson:gson:2.11.0")
// sdk
implementation(files("./libs/mallto-map-release.aar"))
```

### 2.init

```java

MalltoMap.init(new MalltoConfig.Builder(appId, appSecret, SERVER_DOMAIN, PROJECT_UUID)
        .setDebug(DEBUG) //是否打印日志
        .setUserSlug("001") // 可选关联第三方系统的用户唯一标识,如email/mobile/user_id 等
        .setFetchDeviceSlugCallback(new FetchSlugCallback() { // init会获取设备短标识，异步回调到主线程
            @Override
            public void onSuccess(String slug) {
                Toast.makeText(MainActivity.this, "slug" + slug, Toast.LENGTH_SHORT).show();
            }
        
            @Override
            public void onFail(String reason) {
        
            }
        })
        .setNotification(notification) // 扫描iBeacon需要开启服务，需要传入Notification
        .setMapDomain(MAP_DOMAIN) // 设置地图域名
        .setScanInterval(1100L)  // 设置扫描频率，1100ms一次
        .build());


private Notification createNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel chan = new NotificationChannel("beaconSDK",
                "beaconSDK", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
    }
    return new NotificationCompat.Builder(this, "beaconSDK")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("scanning for beacons")
            .build();
}
```

### 3.开启/关闭 iBeacon 扫描

```java
// 使用初始化接口 setDeviceUUIDList 和 setScanInterval 传入的uuidList和scanInterval参数开始扫描iBeacon
// 1. 是否上传扫描结果
// 2. 扫描结果回调
MalltoMap.startIBeaconScanning(true, new BeaconSDK.Callback() {
    @Override
    public void onScanResult(List<MalltoBeacon> beacons) {
        adapter.submitList(beacons);
    }

    @Override
    public void onError(String error) {
        Toast.makeText(MainActivity.this, "error:" + error, Toast.LENGTH_SHORT).show();
    }
});

或
MalltoMap.startIBeaconScanning(scanInterval,  //scanInterval 扫描
                               true,  //是否上传扫描结果
                               callback) //扫描结果回调

停止扫描
MalltoMap.stopIBeaconScanning();
```

### 4.开启/关闭 AoA 广播

```java
MalltoMap.startAoA(this, new MalltoMap.Callback.AoA() {

    @Override
    public void onSuccess(String data) {
        Log.i("beacon", data);
    }

    @Override
    public void onError(String error) {
        Toast.makeText(MainActivity.this, "error:" + error, Toast.LENGTH_SHORT).show();
    }
});

MalltoMap.stopAoA();
```

### 5.地图显示

```java
MapView mapView = MalltoMap.mapView(this);

ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
main.addView(mapView, lp);
```
