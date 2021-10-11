package com.example.myapplication;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    boolean onOff=false;
    double happyindex[]=new double[3];
    ImageView ivConnect=null;
    Button btnFirst;
    Button btnSecond;
    TextView tvHomeResult=null;
    ImageView ivHomeResult=null;
    final private int wheres[]={0,1,3,2,150,151,152,101,102,100};
    final int seekBarsID[]={R.id.sb1,R.id.sb2,R.id.sb3,R.id.sb4,R.id.sb5,R.id.sb6,R.id.sb7,R.id.sb8,R.id.sb9};
    //글의색 농도변화와 이미지 텍스트뷰의 유동변화
    TextView tvInfo[]=null;
    TextView tvUpdate[]=null;
    Button submitButtons[]=null;
    ImageView ivs[]=null;
    SeekBar seekBars[]=new SeekBar[seekBarsID.length];
    BluetoothAdapter bluetoothAdapter;
    int mPairedDEviceCount=0;
    Set<BluetoothDevice> mDevices;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket=null;
    OutputStream mOutputStream=null;
    InputStream mInputStream=null;
    Thread mWorkerThread=null;
    byte readBuffer[];
    byte j=100;
    int readBufferPosition;
    static final int REQUEST_ENABLE_BT=10;
    String msg[]={"블루투스를 지원하지 않습니다","블루투스 연결을 거부합니다.","연결할 블루투스 장치가 하나도 없습니다"
            ,"블루투스 장치 선택","취소를 선택했습니다","현재 장치와 연결이 안됩니다."};



    Integer tbIds[]={R.id.tb1,R.id.tb2,R.id.tb3,R.id.tb4,R.id.tb5,R.id.tb6,R.id.tb7};
    Integer textIds[]={R.id.tv1,R.id.tv2,R.id.tv3,R.id.tv4,R.id.tv5,R.id.tv6,R.id.tv7};
    ToggleButton toggleButton[]=new ToggleButton[tbIds.length];
    LinearLayout linearLayout[]=new LinearLayout[textIds.length];
    float startX, startY = 0;
    int recogSize=4;
    protected void setColor(int i)
    {
        int x=(Math.floor(i/3)==0)? 0: (Math.floor(i/3)==1)? 1:2;
        ImageView iv=ivs[x];
        int a=0;
        for (int j=0;j<3;j++)
        {
            a+=seekBars[x*3+j].getProgress()<<(8*(2-j));
        }
        a+=255*256*256*256;
        iv.setColorFilter(a,PorterDuff.Mode.SRC_IN);
    }

    protected void requestData(int id)
    {
        sendData((byte)(id));
        int x=(id>=150)? 3:1;
        for (int i=0;i<x;i++)
        {
            sendData((byte)255);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvHomeResult=(TextView)findViewById(R.id.tvHomeResult);
        ivHomeResult=(ImageView)findViewById(R.id.ivHomeResult);
        btnFirst=(Button)findViewById(R.id.btnFirst);
        btnSecond=(Button)findViewById(R.id.btnSecond);
        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<4;i++)
                {
                    requestData(wheres[i]);
                }
            }
        });
        btnSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                for(int i=4;i<7;i++)
                {
                    requestData(wheres[i]);
                }
            }
        });

        getSupportActionBar().setTitle("IOT 홈프로젝트");
        ivConnect=(ImageView) findViewById(R.id.ivConnect);
        ivConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!onOff)
                {
                    checkBluetooth();
                }
                else
                {
                    androidx.appcompat.app.AlertDialog.Builder alert=new AlertDialog.Builder(MainActivity.this);
                    alert.setTitle("정말 종료하시겠습니까?");
                    final BluetoothAdapter bl=bluetoothAdapter;
                    alert.setPositiveButton("종료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bl.disable();
                        }
                    });
                            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                }
                            });
                    alert.show();
                    ivConnect.setImageResource(R.drawable.bluetooth_grayicon);
                    onOff=!onOff;
                }
            }
        });
        ivs=new ImageView[]{(ImageView)findViewById(R.id.iv1),(ImageView)findViewById(R.id.iv2),(ImageView)findViewById(R.id.iv3)};
        submitButtons=new Button[]{(Button)findViewById(R.id.btn1),(Button)findViewById(R.id.btn2),(Button)findViewById(R.id.btn3)};
        tvInfo=new TextView[]{(TextView)findViewById(R.id.tvInfo1),(TextView)findViewById(R.id.tvInfo2),
                (TextView)findViewById(R.id.tvInfo3),(TextView)findViewById(R.id.tvInfo4),(TextView)findViewById(R.id.tvinfo5)
                ,(TextView)findViewById(R.id.tvinfo6),(TextView)findViewById(R.id.tvinfo7)};
        tvUpdate=new TextView[]{(TextView)findViewById(R.id.tvUpdate1),(TextView)findViewById(R.id.tvUpdate2),(TextView)findViewById(R.id.tvUpdate3)
                ,(TextView)findViewById(R.id.tvUpdate4),(TextView)findViewById(R.id.tvUpdate5),(TextView)findViewById(R.id.tvUpdate6),
                (TextView)findViewById(R.id.tvUpdate7),(TextView)findViewById(R.id.tvUpdate8),(TextView)findViewById(R.id.tvUpdate9),(TextView)findViewById(R.id.tvUpdate10)};
        for (int i=0;i<seekBarsID.length;i++)
        {
            final int now=i;
            seekBars[i]=(SeekBar)findViewById(seekBarsID[i]);
            seekBars[i].setMax(255);
            seekBars[i].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    setColor(now);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

        }
        setAllFrames(new FrameLayout[]{(FrameLayout)(findViewById(R.id.frMain))});
        for (int i=0;i<tbIds.length;i++)
        {

            toggleButton[i]=(ToggleButton)findViewById(tbIds[i]);
            linearLayout[i]=(LinearLayout) findViewById(textIds[i]);
            setToggleButonsShow(toggleButton[i],linearLayout[i]);
        }
        for (int i=0;i<submitButtons.length;i++)
        {
            final int result=i;
            submitButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    byte id=(result==0)? (byte)(150): (result==1)? (byte)(151):(byte)(152);
                    //한사이클의 RGB정보를 보내주고 받는쪽에서의 처리를통해 싱크화
                    sendData(id);
                    sendData((byte)seekBars[result*3].getProgress());
                    sendData((byte)seekBars[result*3+1].getProgress());
                    sendData((byte)seekBars[result*3+2].getProgress());
                }
            });
        }
    }
    //스와이프 설정
    private void setAllFrames(FrameLayout f[]) {
        for (FrameLayout s : f) {
            s.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            startX = event.getX();
                            startY = event.getY();
                            break;
                        case MotionEvent.ACTION_UP:
                            float length = v.getWidth();
                            float size = startX - event.getX();
                            boolean toRight = (startX - event.getX() < 0);
                            boolean motion_size = (Math.abs(size) > length /recogSize);
                            //참이면이전 섹션 거짓이면 다음섹션
                            if (motion_size) {
                                onSwipeChangeToNext((FrameLayout) v, toRight);
                            }
                            break;
                    }
                    return true;
                }
            });
        }
    }
    private void setToggleButonsShow(ToggleButton t,final LinearLayout tv)
    {
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToggleButton t=(ToggleButton)v;
                if(t.isChecked())
                {
                    tv.setVisibility(View.VISIBLE);
                }
                else
                {
                    tv.setVisibility(View.GONE);
                }
            }
        });
    }
    private void onSwipeChangeToNext(FrameLayout f, boolean toRight) {
        int num = f.getChildCount();
        for (int i = 0; i < num; i++) {
            if (f.getChildAt(i).getVisibility() == View.VISIBLE) {
                f.getChildAt(i).setVisibility(View.GONE);
                int j = (toRight) ? (i != num - 1) ? i + 1 : 0 : (i != 0) ? i - 1 : num - 1;
                f.getChildAt(j).setVisibility(View.VISIBLE);
                break;
            }
        }
    }
    void checkBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast(msg[0]);
        } else
        {
            if(!bluetoothAdapter.isEnabled()) {
                Intent enableBTIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBTIntent,REQUEST_ENABLE_BT);
            }
            else
            {
                selectDevice();
            }
        }
    }
    void showToast(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }

    void selectDevice() {
        mDevices = bluetoothAdapter.getBondedDevices();
        if ((mPairedDEviceCount = mDevices.size()) == 0) {
            showToast(msg[2]);
        } else
        {
            androidx.appcompat.app.AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle(msg[3]);
            List<String> listitems=new ArrayList<>();
            for(BluetoothDevice device:mDevices)
            {
                listitems.add(device.getName());
            }
            listitems.add("취소");
            final CharSequence items[]=listitems.toArray(new CharSequence[listitems.size()]);
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which==mPairedDEviceCount)
                    {
                        showToast(msg[4]);
                    }
                    else
                    {
                        connectToSelectedDevice(items[which].toString());
                    }
                }
            });
            builder.show();
        }
    }
    void processingDatas(int a,byte...b)
    {

        int where=0;
        for (int i=0;i<wheres.length;i++)
        {
            if(wheres[i]==a)
            {
                where=i;
                break;
            }
        }
        Time time=new Time(System.currentTimeMillis());
        String inside="";
        String texts[]=new String[]{"주차장 상태:","문 상태:","가스밸브 상태:","RFID 상태:","RGB1:","RGB2:","RGB3:","온도:","습도:","미세먼지:"};
        String insdeTexts[][]=new String[][]{{"닫혀있습니다","열려있습니다"},{"밸브잠김","밸브열림(긴급)"}};
        int x=(a<150)? R.id.tvFirstFloorResult:R.id.tvSecondFloorResult;
        if(where>=4&&where<=6) {
            for (int i = 0; i < 3; i++) {
                Log.e("where:",(where-4)+"");
                Log.e("byte-"+i+":",""+((b[i]<0)? b[i]+256:b[i]));
                seekBars[(where - 4) * 3 + i].setProgress((b[i] < 0) ? b[i] + 256 : b[i]);
            }
        }
        else if(where<4&&where>=0)
        {
            inside=(where!=2)? (b[0]==0)? insdeTexts[0][0]:insdeTexts[0][1] :(b[0]==0)?insdeTexts[1][0] :insdeTexts[1][1];
        }
        else if(where>=7&&where<=9)
        {
            String max="(max)";
            String min="(min)";
            int value=(b[0]<0)? b[0]+256:b[0];
            double result=(where==7)? value*0.2-10:(where==8)? value*0.4:value;
            happyindex[where-7]=result;
            //온도 습도 미세먼지의 판단기준과 색깔변화
            inside+= ((float)(int)(result*10)/10);
            inside+=(value>=250||value==0)? (value>=250)? max :min :"";
        }
        if(a<150)
        {TextView tv=(where>=7)? tvInfo[where-3]:tvInfo[where];
            tv.setText(texts[where]+inside);
            if(where>=7)
            {
                int baseData=checkCondition(happyindex[where-7],where-6);
                int colorData=(int)Math.floor(baseData/1000);
                float alpha=baseData%100;
                Log.e("colorData",colorData+"");
                int insert=(int)((alpha)-50)*3;
                int inserts[]={insert,insert<<8,insert<<16};
                switch (colorData)
                {
                    case 10:
                        tv.setTextColor(0xff0000ee-inserts[0]);
                        break;
                    case 8:
                        tv.setTextColor(0xff00ee00-inserts[1]);
                        break;
                    case 5:
                        tv.setTextColor(0xffeeee00-inserts[1]-inserts[2]);
                        break;
                    case 2:
                        tv.setTextColor(0xffee0000);
                        break;
                }


                Log.e("alpha:",((alpha==0)? 100:alpha)*(2.55f)+"");
            }
        }
        boolean casebycase=true;
        int result=0;
        for (int i=0;i<happyindex.length;i++)
        {
            result+=(checkCondition(happyindex[i],i+1)/1000)*10;
            if(happyindex[i]==0)
            {
                casebycase=false;
            }
        }
        if(casebycase)
        {
            boolean best=(result>=240);
            boolean normal=(result>=210);
            if(best)
            {
                tvHomeResult.setText(result+"점:최상의 집상태");
                tvHomeResult.setTextColor(Color.CYAN);
                ivHomeResult.setImageResource(R.drawable.good);
            }
            else if(normal)
            {
                tvHomeResult.setText(result+"점:보통의 집상태");
                tvHomeResult.setTextColor(Color.GREEN);
                ivHomeResult.setImageResource(R.drawable.nomal);
            }
            else
            {
                tvHomeResult.setText(result+"점:최악의 집상태");
                tvHomeResult.setTextColor(Color.RED);
                ivHomeResult.setImageResource(R.drawable.bad);
            }
        }
        tvUpdate[where].setText(texts[where]+time+"에 업데이트됨");
        TextView tv=(TextView)findViewById(x);
        tv.setText(time+"에 업데이트됨");
    }
    //1 온도 2 습도 3미세먼지
    private int checkCondition(double i,int signal)
    {

        int result=0;
        int score=0;
        //만약 베이스가변하면 구문을 변경작성해야함 베이스가 50이라는기준으로 100-50은 50 이 100분율의 기준이 되는것을
        //기준으로 작성한구문
        int value=(int)(i*10);
        int base=50;
        int blue_score=10000,green_score=8000,yellow_score=5000,red_score=2000;
        //opacity를 base+백분율로 지급,opacity를 받는것과 점수를 받는것을 인트로지급
        if(signal==1)
        {
            boolean blue=(i<=25)&&(i>=20);
            boolean green=(i<=30)&&(i>=15);
            boolean yellow=(i<=35)&&(i>=10);
            if(blue)
            {
                result+=blue_score;
                result+=base;
                result+=+value-200;
            }
            else if(green)
            {
                result+=green_score;
                result+=base;
                result+=(i>=25)? value-250:200-value;
            }
            else if(yellow)
            {
                result+=yellow_score;
                result+=base;
                result+=(i>=30)? value-300:150-value;
            }
            else //red실행구문 opacity의 등분없이 하한값에 도달시 지정된색깔발현
            {
                result+=red_score;
                result+=base*2;
            }
        }
        else if(signal==2)
        {
            boolean blue=(i<=70)&&(i>=60);
            boolean green=(i<=75)&&(i>=55);
            boolean yellow=(i<=80)&&(i>=50);
            if(blue)
            {
                result+=blue_score;
                result+=base;
                result+=(int)(value-600)*0.5;
            }
            else if(green)
            {
                result+=green_score;
                result+=base;
                result+=(i>=70)? (int)(value-700):(int)(550-value);
            }
            else if(yellow)
            {
                result+=yellow_score;
                result+=base;
                result+=(i>=75)? (int)(value-750):(int)(500-value);
            }
            else //red실행구문 opacity의 등분없이 하한값에 도달시 지정된색깔발현
            {
                result+=red_score;
                result+=base*2;
            }
        }
        else if(signal==3)
        {

            boolean blue=(i<=25);
            boolean green=(i<=50);
            boolean yellow=(i<=75);
            if(blue)
            {
                result+=blue_score;
                result+=base;

            }
            else if(green)
            {
                result+=green_score;
                result+=base;
                result+=(int)((500-value)*0.2);
            }
            else if(yellow)
            {
                result+=yellow_score;
                result+=base;
                result+=(int)((750-value)*0.2);
            }
            else //red실행구문 opacity의 등분없이 하한값에 도달시 지정된색깔발현
            {
                result+=red_score;
                result+=base*2;
            }
        }
        else
        {
            result=-1;
        }
        return result;
    }
    void setTextAndTime(LinearLayout L,String x)
    {
        TextView tv1=(TextView)L.getChildAt(0);
        tv1.setText(x);
        TextView tv2=(TextView)L.getChildAt(1);
        tv2.setText(new Time(System.currentTimeMillis())+"에 업데이트됨.");
    }
    void beginListenForData()
    {
        final Handler handler=new Handler();
        mWorkerThread=new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        while(mInputStream.available()>0) {

                            byte readAndParse[] = null;
                            int first =mInputStream.read();
                            Log.e("id값",first+"");
                            readAndParse=(first<150)? new byte[1]:new byte[3];
                            mInputStream.read(readAndParse);
                            processingDatas(first, readAndParse);
                        }
                    } catch (Exception e)
                    {
                        Log.e("ERROR_RECIEVE",e.getMessage());
                    }

                }
            }
        });
        mWorkerThread.start();
    }
    void sendData(byte a)
    {
        try
        {
            mOutputStream.write(a);
        }
        catch(Exception e)
        {
        }
    }
    void connectToSelectedDevice(String selecedDeviceName)
    {
        mRemoteDevice=getDeviceFromBondedList(selecedDeviceName);
        UUID uuid=UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try{
            mSocket=mRemoteDevice.createRfcommSocketToServiceRecord(uuid);
            mSocket.connect();
            mOutputStream=mSocket.getOutputStream();
            mInputStream=mSocket.getInputStream();
            beginListenForData();
            ivConnect.setImageResource(R.drawable.bluetooth_icon);
            onOff=!onOff;
            //**!!ivBT.setImageResource(R.drawable.bluetooth_icon);
        }
        catch(Exception e)
        {
            showToast(msg[5]);
            //**!! ivBT.setImageResource(R.drawable.bluetooth_grayicon);
        }
    }
    BluetoothDevice getDeviceFromBondedList(String name)
    {
        BluetoothDevice selectedDevice=null;
        for(BluetoothDevice device:mDevices)
        {
            if(name.equals(device.getName()))
            {
                selectedDevice=device;
                break;
            }
        }
        return selectedDevice;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
                if (resultCode==RESULT_OK)
                {
                    selectDevice();
                }
                else if(requestCode==RESULT_CANCELED)
                {
                    showToast(msg[1]);
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onDestroy() {
        try
        {
            mWorkerThread.interrupt();
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
        }
        catch (Exception e)
        {}
        super.onDestroy();
    }
}