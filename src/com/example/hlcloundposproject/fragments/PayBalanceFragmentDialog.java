package com.example.hlcloundposproject.fragments;

import com.example.hlcloundposproject.Configs;
import com.example.hlcloundposproject.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * 商品  单号  查询    fragment
 * com.hlrj.hlcloundpos.fragments
 * @Email zhaoq_hero@163.com
 * @author zhaoQiang : 2016-1-26
 */
public class PayBalanceFragmentDialog extends DialogFragment implements OnClickListener, TextWatcher {
	
	@ViewInject(R.id.pay_should_banlance)
	private TextView shouldPay; //应付金额
	
	@ViewInject(R.id.pay_input_banlance)
	private TextView inputPay;//实付金额
	
	@ViewInject(R.id.pay_overplus)
	private TextView overplusPay; //找零金额
	
	@ViewInject(R.id.pay_sure_btn)
	private Button btnSure;
	
	@ViewInject(R.id.pay_exit_btn)
	private Button btnExit;
	
	private static PayBalanceFragmentDialog dialog;
	
	public static PayBalanceFragmentDialog getInstance(String pay) {
		 
		dialog = new PayBalanceFragmentDialog();
		
		Bundle bundle = new Bundle();
		bundle.putString("pay", pay);
		dialog.setArguments(bundle);
		
		return dialog;
	}
	
	private FragmentCallback callback;

	private String[] payStrs;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		callback = (FragmentCallback) getActivity();//  回调给  activity：
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().setTitle("结算");

		View view = inflater.inflate(R.layout.pay_balance_dialog, container,true);
		ViewUtils.inject(this,view);
		
		String pay = getArguments().getString("pay", null);
		payStrs = pay.split("-");
		
		shouldPay.setText(payStrs[0]);
		
		inputPay.addTextChangedListener(this);
		btnExit.setOnClickListener(this);
		btnSure.setOnClickListener(this);

		return view;
	}


	@Override
	public void onClick(View v) {
		switch(v.getId()){
		
			case R.id.pay_sure_btn:  
				//确认按钮      确认  回调  activity
				//清除数据
				String backMoney = 
						shouldPay.getText().toString().trim()+"-"+
						inputPay.getText().toString().trim()+"-"+
						overplusPay.getText().toString().trim()+"-"+
						payStrs[1];
				
				callback.fragmentCallback(backMoney, Configs.GET_PAY_CALCULATE_RESULT_AUTHORITY);

				onDestroyView();
				break;
				
			case R.id.pay_exit_btn:
				//取消  结算界面：
				onDestroyView();
				break;
		}
	}
	
	/**
	 * 监听   输入  金额的变化：
	 */
	@Override
	public void afterTextChanged(Editable arg0) {
		if(arg0.toString()==null||arg0.toString().equals("")){
			overplusPay.setText(0.0+"");
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		//数据  正在改变的  时候   修改   找零金额：
		if(s.toString()!=null&&!s.toString().equals("")){

			float inputMoney = Float.parseFloat(s.toString());
			if(Float.parseFloat(payStrs[0]) < inputMoney){
				overplusPay.setText((inputMoney - Float.parseFloat(payStrs[0]) )+"");
			}else{
				inputPay.setError("当前金额不够");
			}
		}
	}
}
