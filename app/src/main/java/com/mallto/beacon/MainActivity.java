package com.mallto.beacon;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mallto.beacon.databinding.ActivityMainBinding;
import com.mallto.map.sdk.MalltoConfig;
import com.mallto.map.sdk.MalltoMap;
import com.mallto.map.sdk.bean.MalltoBeacon;
import com.mallto.map.sdk.callback.FetchSlugCallback;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    public static final boolean DEBUG = true;

    public static final String SERVER_DOMAIN = "https://integration-easy.mall-to.com";


    public static final String MAP_DOMAIN = "https://h5-integration.mall-to.com/integration";
    private BluetoothManager bm;
    private Button bleBtn;
    private EditText etScanInterval;
    private EditText etUserName;
    private ActivityMainBinding binding;


    private String domain = SERVER_DOMAIN;
    private String mapDomain = SERVER_DOMAIN;
    private String uuid = "1000241";
    private String username;

    private final Adapter adapter = new Adapter();
    private View domainBtn;
    private Button scanBtn;
    private Button aoaBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        domain = getSharedPreferences("app", 0).getString("domain", SERVER_DOMAIN);
        binding.tvDomain.setText(domain);
        binding.btnDomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectDomainActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        mapDomain = getSharedPreferences("app", 0).getString("map_domain", MAP_DOMAIN);
        binding.tvMapDomain.setText(mapDomain);
        binding.btnMapDomain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectMapDomainActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        binding.btnDeviceUuid.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UuidListActivity.class);
                startActivity(intent);
            }
        });

        uuid = getSharedPreferences("app", 0).getString("uuid", "1000241");
        binding.etUUID.setText(uuid);

        username = getSharedPreferences("app", 0).getString("username", "001");
        binding.etUserName.setText(username);

        bleBtn = binding.btnBle;
        etScanInterval = binding.etScanInterval;
        etUserName = binding.etUserName;
        bm = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleBtn.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "need BlueTooth Permission", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean enabled = bm.getAdapter().isEnabled();
            if (!enabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "open bluetooth...", Toast.LENGTH_SHORT).show();
                        bm.getAdapter().enable();
                        bleBtn.setText("blueTooth Enabled:true");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "open bluetooth...", Toast.LENGTH_SHORT).show();
                    bm.getAdapter().enable();
                    bleBtn.setText("blueTooth Enabled:true");
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "close bluetooth...", Toast.LENGTH_SHORT).show();
                        bm.getAdapter().disable();
                        bleBtn.setText("blueTooth Enabled:false");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "close bluetooth...", Toast.LENGTH_SHORT).show();
                    bm.getAdapter().disable();
                    bleBtn.setText("blueTooth Enabled:false");
                }
            }
        });

        scanBtn = findViewById(R.id.btn_scan);
        RecyclerView rv = findViewById(R.id.rv);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager((this)));

        scanBtn.setText(R.string.start_iBeacon_scanning);
        scanBtn.setOnClickListener(v -> {
            if (!MalltoMap.isIBeaconScanning()) {
                startIBeaconScanning();
                scanBtn.setText(R.string.stop_iBeacon_scanning);
            } else {
                MalltoMap.stopIBeaconScanning();
                scanBtn.setText(R.string.start_iBeacon_scanning);
            }

        });
        aoaBtn = findViewById(R.id.btn_aoa);
        aoaBtn.setText(R.string.start_AoA);
        aoaBtn.setOnClickListener(v -> {
            if (!MalltoMap.isAoAAdvertising()) {
                startAoA();
                aoaBtn.setText(R.string.stop_AoA);
            } else {
                MalltoMap.stopAoA();
                aoaBtn.setText(R.string.start_AoA);
            }

        });

        binding.btnMap.setOnClickListener(v -> {
            init();
            startActivity(new Intent(this, MapActivity.class));
        });

    }

    private void startIBeaconScanning() {
        init();
        MalltoMap.startIBeaconScanning(true, new MalltoMap.OnIBeaconScanCallback() {

            @Override
            public void onScanResult(List<MalltoBeacon> beacons) {
                adapter.submitList(beacons);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "error:" + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void startAoA() {
        init();
        MalltoMap.startAoA(this, new MalltoMap.onAoACallback() {

            @Override
            public void onSuccess(String data) {
                Log.i("beacon", data);
            }

            @Override
            public void onError(String error) {
                aoaBtn.setText(R.string.start_AoA);
                Toast.makeText(MainActivity.this, "error:" + error, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void init() {
        long scanInterval = 1100L;
        try {
            scanInterval = Long.parseLong(etScanInterval.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }
        // android 29之后无法获取IMEI
        // target android 14+, 后台扫描需要传入通知
        Notification notification = createNotification();

        Set<String> uuidSet = getSharedPreferences("app", 0).getStringSet("uuid_list", new HashSet<>());
        List<String> uuidList = new ArrayList<>(uuidSet);
        // 支持的beacon uuid
//        uuidList.add("FDA50693-A4E2-4FB1-AFCF-C6EB07647827");
        String userName = etUserName.getText().toString().trim();
        String projectUUID = binding.etUUID.getText().toString().trim();
        MalltoMap.init(new MalltoConfig.Builder(domain, projectUUID)
                .setDebug(DEBUG)
                .setMapDomain(MAP_DOMAIN)
                .setUserSlug(userName) // 可选关联第三方系统的用户唯一标识,如email/mobile/user_id 等
                .setFetchDeviceSlugCallback(new FetchSlugCallback() { // init会获取设备标识，异步回调到主线程
                    @Override
                    public void onSuccess(String s) {
                        Toast.makeText(MainActivity.this, "slug:"+ s, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail(String s) {

                    }
                })
                .setScanInterval(scanInterval)
                .setDeviceUUIDList(uuidList)
                .setNotification(notification)
                .setIgnoreCertification(true)
                .build());
    }


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

    @Override
    protected void onResume() {
        super.onResume();
        if (!BeaconScanPermissionsActivity.Companion.allPermissionsGranted(this,
                true)) {
            Intent intent = new Intent(this, BeaconScanPermissionsActivity.class);
            intent.putExtra("backgroundAccessRequested", true);
            startActivity(intent);
        }
        if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            boolean blueToothEnabled = bm.getAdapter().isEnabled();
            bleBtn.setText("blueTooth Enabled:" + blueToothEnabled);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        getSharedPreferences("app", 0).edit()
                .putString("username", username)
                .putString("uuid", uuid)
                .apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                String newDomain = data.getStringExtra("map_domain");
                mapDomain = newDomain;
                binding.tvMapDomain.setText(newDomain);
            }
        }
    }

    static class Adapter extends ListAdapter<MalltoBeacon, Holder> {

        protected Adapter() {
            super(new DiffUtil.ItemCallback<MalltoBeacon>() {
                @Override
                public boolean areItemsTheSame(@NonNull MalltoBeacon oldItem, @NonNull MalltoBeacon newItem) {
                    return false;
                }

                @Override
                public boolean areContentsTheSame(@NonNull MalltoBeacon oldItem, @NonNull MalltoBeacon newItem) {
                    return false;
                }
            });
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            MalltoBeacon item = getItem(position);
            holder.tv1.setText(item.getUuid());
            holder.tv2.setText("major:" + item.getMajor() + ",minor=" + item.getMinor() + ",rssi:" + item.getRssi());
        }
    }

    static class Holder extends RecyclerView.ViewHolder {

        TextView tv1;
        TextView tv2;

        public Holder(@NonNull View itemView) {
            super(itemView);
            tv1 = itemView.findViewById(android.R.id.text1);
            tv2 = itemView.findViewById(android.R.id.text2);
        }
    }

}