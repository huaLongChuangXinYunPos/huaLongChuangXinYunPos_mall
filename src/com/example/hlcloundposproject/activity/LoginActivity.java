package com.example.hlcloundposproject.activity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.hlcloundposproject.Configs;
import com.example.hlcloundposproject.Constants;
import com.example.hlcloundposproject.MainActivity;
import com.example.hlcloundposproject.R;
import com.example.hlcloundposproject.db.MyOpenHelper;
import com.example.hlcloundposproject.entity.User;
import com.example.hlcloundposproject.tasks.TaskCallBack;
import com.example.hlcloundposproject.tasks.TaskResult;
import com.example.hlcloundposproject.tasks.UserLoginTask;
import com.example.hlcloundposproject.utils.MyProgressDialog;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 登录   界面
 */
public class LoginActivity extends Activity implements OnClickListener, TaskCallBack {

	@ViewInject(R.id.account)
	private EditText etAccount;
	@ViewInject(R.id.password)
	private EditText etPassword;
	
	@ViewInject(R.id.login_form)
	private View loginForm;
	
	@ViewInject(R.id.login_progress)
	private View loginProgress;
	
	@ViewInject(R.id.login_status_message)
	private TextView mLoginStatusMessageView;
	
	@ViewInject(R.id.login_btn_sure)
	private Button loginSure;
	
	@ViewInject(R.id.login_btn_exit)
	private Button loginExit;
	
	/**
	 * 尝试登录时的      账户和密码值：
	 * */
	private String account;
	private String password;
	private SQLiteOpenHelper userHelper;
	private SQLiteDatabase userdb;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);
		
		ViewUtils.inject(this);
		
		userHelper = new MyOpenHelper(LoginActivity.this,
				Constants.USER_INFO_DB_NAME);
		
		/**
		 * 初始化编辑事件    和登录事件
		 */
		loginSure.setOnClickListener(this);
		loginExit.setOnClickListener(this);
	}

	//登录和取消的    单击事件：
	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		
			case R.id.login_btn_sure:
				
				// 获取  数据值
				account = etAccount.getText().toString().trim();
				password = etPassword.getText().toString().trim();
				
				// 账户验证：
				if (TextUtils.isEmpty(account)) {
					//  如果为null
					etAccount.setError("账号不准为空");
					etAccount.requestFocus();
					break;
				} 
				
				// 密码验证
				if (TextUtils.isEmpty(password)) {
					etPassword.setError("密码不准为空");
					etPassword.requestFocus();
					break;
				} 
				
				userdb = userHelper.getReadableDatabase();
				
				Cursor cursor = userdb.query(Constants.TABLE_USERS_NAME,
						new String[]{"user","password","name","right","ki","quanXian"},
						"user = '"+ account+"'", null,null, null, null);
				
				MyProgressDialog.showProgress(this,"正在登录...",null);
				/**
				 * 验证存在用户：
				 */
				switch(cursor.getCount()){
					case 0://没有查询到用户   到服务器端查找
						new UserLoginTask(this).execute(String.format(Configs.LOGIN_USER,account));
						break;
					case 1://查询到 一位用户：判断密码  是否一样
						User user = new User();
						while(cursor.moveToNext()){
							if(cursor.getString(cursor.getColumnIndex("password")).equals(password)){
								//获取user 对象：
								user.setKi(cursor.getString(cursor.getColumnIndex("ki")));
								user.setName(cursor.getString(cursor.getColumnIndex("name")));
								user.setPass(cursor.getString(cursor.getColumnIndex("password")));
								user.setQuanxian(cursor.getString(cursor.getColumnIndex("quanXian")));
								user.setRight(cursor.getString(cursor.getColumnIndex("right")));
								user.setUser(cursor.getString(cursor.getColumnIndex("user")));
								
								toMainActivity(user);
								
								finish();
							}else{
								Toast.makeText(this, "请验证输入密码", Toast.LENGTH_SHORT).show();
							}
							MyProgressDialog.stopProgress();
							break;
						}
						
						break;
					default:
						//查询到 多位用户
						Toast.makeText(this, "请联系管理员，当前用户名存在冲突。",Toast.LENGTH_SHORT).show();
						MyProgressDialog.stopProgress();
						break;
					}
				
				break;
				
			case R.id.login_btn_exit:
				finish();
				break;
		}
	}

	//数据   回调完后的   回调方法
	@Override
	public void onTaskFinished(TaskResult result) {
		/**
		 * 判断返回数据  和异步任务的标识：
		 */
		if(result!=null && result.task_id == Configs.USER_LOGIN_TASK_AUTHORITY){
			
			if(result.resultStatus != 0 ){
				JSONArray array = (JSONArray) result.resultData;
					try {
						JSONObject jobj = array.getJSONObject(0);
						User user = User.userLoginParser(jobj);
						/**
						 * 密码一样    进行登录
						 */
						if(user.getPass().equals(password)){
							// 可以登录   界面
							toMainActivity(user);
							
							MyProgressDialog.stopProgress();
							
							/**
							 * 保存信息  到本地 数据库
							 */
							userdb = userHelper.getWritableDatabase();
							// 保存用户信息到本地
							ContentValues values = new ContentValues();
							values.put("ki", user.getKi());
							values.put("name", user.getName());
							values.put("password", user.getPass());
							values.put("quanxian", user.getQuanxian());
							values.put("right", user.getRight());
							values.put("user",user.getUser());
							
							userdb.insert(Constants.TABLE_USERS_NAME, null, values);
						}else{
							MyProgressDialog.stopProgress();
							Toast.makeText(this, "请验证输入密码", Toast.LENGTH_SHORT).show();
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
			}else{
				MyProgressDialog.stopProgress();
				Toast.makeText(this, "请验证用户名或密码", Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * 打开主界面：
	 * @param user
	 */
	private void toMainActivity(User user) {
		Intent intent = new Intent();
		intent.setClass(this, MainActivity.class);
		intent.putExtra("user",user);
		startActivity(intent);
		finish();
	}
	

}
