package com.example.bluetoothsdkapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Button ADD_BLUETOOTH;
    private Button Scene_1;
    private Button Scene_2;
    private Button Scene_3;
    private Button Scene_4;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private SeekBar Yaw;
    private TextView Yaw_view;
    private TextView Pitch_view;
    private SeekBar Pitch;
    private SeekBar light;
    private TextView light_view;

    private View bluetooth_view;
    private AlertDialog mAlertDialog;
    private ListView listView;
    private Switch LED_Switch;
    private TextView WorkView;
    public ArrayAdapter adapter;
    //定义一个列表，存蓝牙设备的地址。
    public ArrayList<String> deviceName = new ArrayList<>();
    //定义一个列表，存蓝牙设备的地址。
    private List<BluetoothDevice> arrayList = new ArrayList<>();

    private BluetoothGatt mBluetoothGatt;

    public byte[] msg_buffer = new byte[5];

    //服务和特征值
    private UUID write_UUID_service;
    private UUID write_UUID_chara;
    private UUID read_UUID_service;
    private UUID read_UUID_chara;
    private UUID notify_UUID_service;
    private UUID notify_UUID_chara;
    private UUID indicate_UUID_service;
    private UUID indicate_UUID_chara;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothDevice selectDevice;
    // 获取到选中设备的客户端串口，全局变量，否则连接在方法执行完就结束了
    private BluetoothSocket clientSocket;
    // 获取到向设备写的输出流，全局变量，否则连接在方法执行完就结束了
    private OutputStream os;
    // 连接对象的名称
    private final String Name = "HC05";
    // 服务端利用线程不断接受客户端信息
    private AcceptThread thread;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        init_bluetooth();
    }

    private void initView() {
        ADD_BLUETOOTH = (Button) findViewById(R.id.ADD_BLUETOOTH);
        ADD_BLUETOOTH.setOnClickListener(this);
        Scene_1 = (Button) findViewById(R.id.Scene_1);
        Scene_1.setOnClickListener(this);
        Scene_2 = (Button) findViewById(R.id.Scene_2);
        Scene_2.setOnClickListener(this);
        Scene_3 = (Button) findViewById(R.id.Scene_3);
        Scene_3.setOnClickListener(this);
        Scene_4 = (Button) findViewById(R.id.Scene_4);
        Scene_4.setOnClickListener(this);
        LED_Switch = (Switch) findViewById(R.id.LED_Switch);
        LED_Switch.setOnClickListener(this);
        LED_Switch.setOnCheckedChangeListener(this);
        WorkView = (TextView) findViewById(R.id.WorkView);
        WorkView.setOnClickListener(this);

        Yaw_view = (TextView) findViewById(R.id.Yaw_view);
        Yaw_view.setOnClickListener(this);
        Yaw_view.setText("Yaw投射角度：");
        Yaw = (SeekBar) findViewById(R.id.Yaw);
        Yaw.setOnClickListener(this);
        Yaw.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*拖动条停止拖动时调用 */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            /*拖动条开始拖动时调用*/
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            /* 拖动条进度改变时调用*/
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Yaw_view.setText("Yaw投射角度：" + progress + "°");
                msg_buffer[0]=0x01;
                msg_buffer[1]=(byte)(((progress-30)>>0*8)&0xff);
                msg_buffer[2]=(byte)(((progress-30)>>1*8)&0xff);
                msg_buffer[3]=(byte)(((progress-30)>>2*8)&0xff);
                msg_buffer[4]=(byte)(((progress-30)>>3*8)&0xff);
                Write(msg_buffer);
            }
        });

        Pitch_view = (TextView) findViewById(R.id.Pitch_view);
        Pitch_view.setOnClickListener(this);
        Pitch_view.setText("Pitch投射角度：");
        Pitch = (SeekBar) findViewById(R.id.Pitch);
        Pitch.setOnClickListener(this);
        Pitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*拖动条停止拖动时调用 */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            /*拖动条开始拖动时调用*/
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            /* 拖动条进度改变时调用*/
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Pitch_view.setText("Pitch投射角度：" + (progress-30) + "°");
                msg_buffer[0]=0x02;
                msg_buffer[1]=(byte)(((progress-30)>>0*8)&0xff);
                msg_buffer[2]=(byte)(((progress-30)>>1*8)&0xff);
                msg_buffer[3]=(byte)(((progress-30)>>2*8)&0xff);
                msg_buffer[4]=(byte)(((progress-30)>>3*8)&0xff);
                Write(msg_buffer);
            }
        });

        light_view = (TextView) findViewById(R.id.light_view);
        light_view.setOnClickListener(this);
        light_view.setText("亮度：");
        light = (SeekBar) findViewById(R.id.light);
        light.setOnClickListener(this);
        light.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            /*拖动条停止拖动时调用 */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            /*拖动条开始拖动时调用*/
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            /* 拖动条进度改变时调用*/
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                light_view.setText("亮度：" + progress );
                msg_buffer[0]=0x03;
                msg_buffer[1]=(byte)(((progress-30)>>0*8)&0xff);
                msg_buffer[2]=(byte)(((progress-30)>>1*8)&0xff);
                msg_buffer[3]=(byte)(((progress-30)>>2*8)&0xff);
                msg_buffer[4]=(byte)(((progress-30)>>3*8)&0xff);
                Write(msg_buffer);
            }
        });

        bluetooth_view = getLayoutInflater().inflate(R.layout.activity_bluetooth_link, null);
        listView = bluetooth_view.findViewById(R.id.blue_list);
        mAlertDialog = new AlertDialog.Builder(this).setTitle("蓝牙设备列表\n")
                .setIcon(R.mipmap.ic_launcher)
                .setView(bluetooth_view)
                .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface paramAnonymousDialogInterface,
                                        int paramAnonymousInt) {
                        mBluetoothAdapter.cancelDiscovery();//取消蓝牙扫描
                    }
                }).create();

    }

    /*
    继承监听器的接口并实现onCheckedChanged方法
    * */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.LED_Switch:
                if(isChecked){
                    //text1.setText("开");
                    msg_buffer[0]=0x05;
                    Write(msg_buffer);
                }else {
                    //text1.setText("关");
                    msg_buffer[0]=0x04;
                    Write(msg_buffer);
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ADD_BLUETOOTH:
                Add_bluetooth();
                break;
            case R.id.Scene_1:
                Work_Scene_1();
                break;
            case R.id.Scene_2:
                Work_Scene_2();
                break;
            case R.id.Scene_3:
                Work_Scene_3();
                break;
            case R.id.Scene_4:
                Work_Scene_4();
                break;
        }
    }


    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case BluetoothDevice.ACTION_FOUND:  //找到一个设备之后响应
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //添加名字和地址到list中
                    if (device.getName() != null && arrayList.indexOf(device) == -1) {//防止重复添加
                        deviceName.add("\n" + "设备名：" + device.getName() + "\n" + "设备地址：" + device.getAddress() + "\n");
                        arrayList.add(device);//将搜索到的蓝牙地址添加到列表
                        adapter.notifyDataSetChanged();//更新
                    }
                    break;

                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int state = bluetoothDevice.getBondState();
                    if (state == BluetoothDevice.BOND_BONDED) {
                        Log.e("###", "连接成功");
                    }
                    break;
            }
        }
    };

    public void init_bluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//获取蓝牙适配器
        if (mBluetoothAdapter == null) {//表示手机不支持蓝牙
            finish();
            return;
        }
        if (mBluetoothAdapter.getState() == mBluetoothAdapter.STATE_OFF) {  //打开蓝牙
            mBluetoothAdapter.enable();
        }
        IntentFilter intentFilter = new IntentFilter();//注册广播接收信号
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(bluetoothReceiver, intentFilter);//注册，当一个设备被发现时调用bluetoothReceiver
        Toast.makeText(getApplicationContext(), "蓝牙配置初始化成功!", Toast.LENGTH_SHORT).show();
    }

    public void Add_bluetooth() {
        mAlertDialog.show();//以弹窗的形式显示蓝牙列表
        if (mBluetoothAdapter.isDiscovering()) {
            //判断蓝牙是否正在扫描，如果是调用取消扫描方法；如果不是，则开始扫描
            mBluetoothAdapter.cancelDiscovery();
        } else {
            mBluetoothAdapter.startDiscovery();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.cancelDiscovery();
                }
            }, 8000);
        }
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, deviceName);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 判断当前是否还是正在搜索周边设备，如果是则暂停搜索
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                // 如果选择设备为空则代表还没有选择设备
                if (selectDevice == null) {
                    selectDevice = arrayList.get(position);
                }
                if (selectDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                    try {
                        Method method = BluetoothDevice.class.getMethod("createBond");//配对
                        Log.d("TAG", "开始配对");
                        method.invoke(selectDevice);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }//配对完成
                }
                // 实例接收客户端传过来的数据线程
                thread = new AcceptThread();
                // 接收线程开始
                thread.start();
                try {
                    // 判断客户端接口是否为空
                    if (clientSocket == null) {
                        // 获取到客户端接口
                        clientSocket = selectDevice.createRfcommSocketToServiceRecord(MY_UUID);
                        // 向服务端发送连接
                        clientSocket.connect();
                        Toast.makeText(getApplicationContext(), "蓝牙连接成功!", Toast.LENGTH_SHORT).show();
                        // 获取到输出流，向外写数据
                        os = clientSocket.getOutputStream();
                        //关闭弹窗
                        mAlertDialog.dismiss();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "蓝牙连接失败!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * 传输数据
     *
     * @param message
     */
    public void Write(byte[] message) {
        try {
            if (os != null) {
                //String msgString = new String(msgArray);//把Char型转成String发送
                //os.write(msgString.getBytes("utf-8"));
                os.write(message);
            }
            Log.e("OS", "write:" + message);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "写数据失败!", Toast.LENGTH_SHORT).show();
        }
//        //关闭流
//        try {
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    // 服务端，需要监听客户端的线程类
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    // 线程服务类
    public class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;
        private BluetoothSocket socket;
        // 输入 输出流
        private OutputStream os;
        private InputStream is;

        public AcceptThread() {
            try {
                serverSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(Name, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            // 截获客户端的蓝牙消息
            try {
                // 接收其客户端的接口
                socket = serverSocket.accept();
                // 获取到输入流
                is = socket.getInputStream();
                // 获取到输出流
                os = socket.getOutputStream();
                // 循环来接收数据
                while (true) {
                    // 创建一个128字节的缓冲
                    byte[] buffer = new byte[128];
                    // 每次读取128字节，并保存其读取的角标
                    int count = is.read(buffer);
                    // 创建Message类，向handler发送数据
                    Message msg = new Message();
                    // 发送一个String的数据，让他向上转型为obj类型
                    msg.obj = new String(buffer, 0, count, "utf-8");
                    // 发送数据
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void Work_Scene_1() {
        WorkView.setText("当前工作状态为：\n" + "全日餐厅早餐模式\n" +
                "灯具亮度：50%\n" + "灯具色温：4000K\n" + "投射角度：" + light.getProgress() + "°");
        msg_buffer[0]=0x0a;
        Write(msg_buffer);
    }

    public void Work_Scene_2() {
        WorkView.setText("当前工作状态为：\n" + "全日餐厅午餐餐模式\n" +
                "灯具亮度：50%\n" + "灯具色温：5000K\n" + "投射角度：30°");
        msg_buffer[0]=0x0b;
        Write(msg_buffer);
    }

    public void Work_Scene_3() {
        WorkView.setText("当前工作状态为：\n" + "全日餐厅晚餐餐模式\n" +
                "灯具亮度：50%\n" + "灯具色温：2700K\n" + "静态追踪模式");
        msg_buffer[0]=0x0c;
        Write(msg_buffer);
    }

    public void Work_Scene_4() {
        WorkView.setText("当前工作状态为：\n" + "宴会厅会议模式\n" +
                "灯具亮度：50%\n" + "灯具色温：2700K\n" + "动态追踪模式");
        msg_buffer[0]=0x0d;
        Write(msg_buffer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();//解除注册
        unregisterReceiver(bluetoothReceiver);
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.disable();
    }
}


