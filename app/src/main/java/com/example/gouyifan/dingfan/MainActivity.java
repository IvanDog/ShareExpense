package com.example.gouyifan.dingfan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private long mExitTime = 0;
    private EditText mTotalPayment;
    private Button mAddBT;
    private Button mComputeBT;
    private Button mClean;
    private LinearLayout mPaymentLinear;
    private int mNum;
    private boolean mFlag;
    private ArrayList<View> viewList = new ArrayList<View>();
    private ArrayList<EditText> originList = new ArrayList<EditText>();
    private ArrayList<EditText> numList = new ArrayList<EditText>();
    private ArrayList<EditText> finalList = new ArrayList<EditText>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        mTotalPayment = (EditText)findViewById(R.id.et_total_payment);
        mAddBT = (Button)findViewById(R.id.bt_add_user);
        mComputeBT = (Button)findViewById(R.id.bt_compute);
        mClean = (Button)findViewById(R.id.bt_clean);
        mPaymentLinear = (LinearLayout)findViewById(R.id.linear_user_payment_info);
        mPaymentLinear.addView(getPaymentView());
        mAddBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPaymentLinear.addView(getPaymentView());
            }
        });
        mComputeBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compute();
            }
        });
        mClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTotalPayment.setText("");
                mPaymentLinear.removeAllViews();
                mNum=0;
                mFlag = false;
                mPaymentLinear.addView(getPaymentView());
                viewList.clear();
                originList.clear();
                numList.clear();
                finalList.clear();
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction("ExitApp");
        registerReceiver(mReceiver, filter);
    }

    public void onResume(){
        super.onResume();
    }

    public void onPause(){
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            if((System.currentTimeMillis() - mExitTime) > 2000){
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                Intent intentFinsh = new Intent();
                intentFinsh.setAction("ExitApp");
                sendBroadcast(intentFinsh);
                exit();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit(){
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction()!=null && intent.getAction().equals("ExitApp")){
                finish();
            }
        }
    };


    private View getPaymentView() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.view_user_payment_info, null);
        EditText oriEt = (EditText)view.findViewById(R.id.et_origin_money);
        EditText numEt = (EditText)view.findViewById(R.id.et_number);
        EditText finalEt = (EditText)view.findViewById(R.id.et_payment_money);
        if(mNum==0 && !mFlag){
            oriEt.setText("原始金额");
            oriEt.setKeyListener(null);
            numEt.setText("数量");
            numEt.setKeyListener(null);
            finalEt.setText("折后金额");
            finalEt.setKeyListener(null);
            mFlag = true;
        }else{
            viewList.add(view);
            originList.add(oriEt);
            numList.add(numEt);
            finalList.add(finalEt);
            mNum ++;
        }
        view.setLayoutParams(lp);
        return view;
    }

    private void compute(){
        double totalOriginPay = 0.0;
        double totalPayment = 0.0;
        double rate = 0.0;
        for(int i=0;i<originList.size();i++){
            if(originList.get(i).getText()!=null && !"".equals(originList.get(i).getText().toString())){
                double originPayment =Double.parseDouble(originList.get(i).getText().toString());
                int num;
                if(numList.get(i).getText()!=null && !"".equals(numList.get(i).getText().toString())) {
                  num = Integer.parseInt(numList.get(i).getText().toString());
                }else{
                  num = 1;
                  numList.get(i).setText("1");
                }
                totalOriginPay+=originPayment*num;
            }else if(originList.get(i)!=null && "".equals(originList.get(i).getText().toString())){
                numList.get(i).getText().clear();
                finalList.get(i).getText().clear();
            }
        }
        if(mTotalPayment.getText()!=null && !"".equals(mTotalPayment.getText().toString())){
            totalPayment = Double.parseDouble(mTotalPayment.getText().toString());
            if(totalOriginPay!=0.0){
                rate = totalPayment/totalOriginPay;
            }else{
                rate = 0.0;
            }
        }else{
            Toast.makeText(mContext,"请输入实付总金额",Toast.LENGTH_SHORT);
        }
        for(int i=0;i<finalList.size();i++){
            if(originList.get(i).getText()!=null && !"".equals(originList.get(i).getText().toString())){
                if("0".equals(numList.get(i).getText().toString())){
                    finalList.get(i).setText("0.0");
                }else{
                    finalList.get(i).setText(String.valueOf(formatDouble(Double.parseDouble(originList.get(i).getText().toString())*rate)));
                }
            }
        }
    }

    public static double formatDouble(double d) {
        BigDecimal bg = new BigDecimal(d).setScale(1, RoundingMode.HALF_UP);
        return bg.doubleValue();
    }

}
