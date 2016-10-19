package com.example.yin.alarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.yin.MyMythod.MyRecord;
import com.example.yin.adapter.SongAdapter;
import com.example.yin.constant.MyConstant;
import com.example.yin.entity.Music;
import com.example.yin.sqlite.MySqlite;

import java.util.ArrayList;

/**
 * 闹钟设置界面
 */
public class AddAlarm extends AppCompatActivity {
    private TimePicker tp;
    private TextView showName,noSong;
    private EditText etRemark;
    private Button choose,ok,longBtn;
    private View view;
    private ListView songList;
    private SongAdapter songAdapter;
    private AlertDialog.Builder builder;
    private MyRecord myRecord;
    private String tpCurHour,tpCurMin,curHour,curMin,date,remark,ringPath;
    private int pos;
    private MySqlite mySqlite;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_alarm);
        init();
        startListener();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            builder=new AlertDialog.Builder(AddAlarm.this);
            builder.setMessage(MyConstant.discardSetting)
                    .setPositiveButton(MyConstant.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(MyConstant.cancel,null)
                    .show();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 初始化
     */
    private void init() {
        showName=(TextView) findViewById(R.id.showSongName);
        etRemark=(EditText) findViewById(R.id._remark);
        choose=(Button) findViewById(R.id.chooseSong);
        ok=(Button) findViewById(R.id.saveSetting);
        longBtn= (Button) findViewById(R.id.record);
        tp= (TimePicker) findViewById(R.id.timePicker);
        pos=-1;
        myRecord=new MyRecord();
        tp.setIs24HourView(true);
        tp.setCurrentHour(8);
        tp.setCurrentMinute(00);
        songAdapter=new SongAdapter((MyConstant.localMusic==null ? new ArrayList<Music>() : MyConstant.localMusic),AddAlarm.this);
        mySqlite=new MySqlite(AddAlarm.this);
    }

    private void startListener(){
        choose.setOnClickListener(listener);
        ok.setOnClickListener(listener);

        longBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                myRecord.startVoice();
                return false;
            }
        });
        longBtn.setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_UP:
                        if(MyConstant.isReadyToRecord){
                            myRecord.stopVoice();
                            Toast.makeText(AddAlarm.this,MyConstant.endRecord,Toast.LENGTH_SHORT).show();
                            Log.i("mylog", "录音结束");
                        }else{
                            Toast.makeText(AddAlarm.this,MyConstant.tooShort,Toast.LENGTH_SHORT).show();
                            Log.i("mylog", "时间太短了");
                        }
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

    };

    /**
     * 监听
     */
    View.OnClickListener listener = new View.OnClickListener(){

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.chooseSong://点我
                    view= LayoutInflater.from(getApplicationContext()).inflate(R.layout.choose_song_dialog,null);
                    songList= (ListView) view.findViewById(R.id.showSongList);
                    noSong= (TextView) view.findViewById(R.id.no_song);
                    if(MyConstant.localMusic==null || MyConstant.localMusic.size()==0){
                        songList.setVisibility(View.GONE);
                    }else{
                        songList.setAdapter(songAdapter);
                        songList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                pos=position;
                                showName.setText(MyConstant.localMusic.get(position).getSong_title());
                            }
                        });
                    }
                    builder=new AlertDialog.Builder(AddAlarm.this);
                    builder.setTitle(MyConstant.chooseSong);
                    builder.setView(view);
                    builder.setNegativeButton(MyConstant.cancel,null);
                    builder.show();
                    break;
                case R.id.saveSetting://确定
                    tpCurHour=tp.getCurrentHour().toString();
                    tpCurMin=tp.getCurrentMinute().toString();
                    curHour=(tpCurHour.length()==1 ? "0"+tpCurHour : tpCurHour);
                    curMin=(tpCurMin.length()==1 ? "0"+tpCurMin : tpCurMin);
                    date=(curHour+":"+curMin);
                    remark=(etRemark.getText().toString()==null || etRemark.getText().toString().trim().equals("") ? null : etRemark.getText().toString());
//                    ringPath=(showName.getText().toString().equals(MyConstant.defaultRing) ? null : showName.getText().toString());
                    ringPath=(pos==-1 ? null : MyConstant.localMusic.get(pos).getSong_url());
                    mySqlite.addAlarm(date,remark,ringPath);
                    Toast.makeText(AddAlarm.this,MyConstant.addAlarmOK,Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }

        }
    };
}
