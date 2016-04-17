package com.wencai.app;

import java.util.Timer;
import java.util.TimerTask;

import com.wencai.adapter.MainTabPagerAdapter;
import com.wencai.backup.PullDataTask;
import com.wencai.controller.MainTabController;
import com.wencai.controller.TopicController;
import com.wencai.gidance.GuidanceActivity;
import com.wencai.login.LoginActivity;
import com.wencai.project.ProjectConfig;
import com.wencai.util.CallBack;
import com.wencai.util.FileUtil;
import com.wencai.util.UiUtil;
import com.wencai.widget.IconPageIndicator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

public class MainTabActivity extends FragmentActivity
implements MoreListFragment.Callbacks, ClassicsListFragment.Callbacks {
	static int FLAGE_GAI = 1;// 引导页
	static int FLAGE_LOGIN = 2;// 登录

	private ViewPager main_tab_pager;// viewpager
	private IconPageIndicator main_tab_icon_indicator;
	private MainTabPagerAdapter mtpa;
	private MainTabController mtc;
	private TextView tv_title;
	private FileUtil fu;

	// for exit
	private static Timer tExit;
	private static TimerTask task;
	private static Boolean isExit = false;
	private static Boolean hasTask = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_tab);
		
		SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
		String uid = sp.getString("uid", "anonymous");
		//设置用户名
		LoginActivity.uid=uid;
		
		
		// 引导页
		
		isFirst();

		mtc = new MainTabController(this); 

		main_tab_pager = (ViewPager) findViewById(R.id.main_tab_pager);
		main_tab_icon_indicator = (IconPageIndicator) findViewById(R.id.main_tab_icon_indicator);
		main_tab_icon_indicator.setBackgroundResource(R.drawable.zx);

		mtpa = mtc.getPagerAdapter(getSupportFragmentManager());
		main_tab_pager.setAdapter(mtpa);

		main_tab_icon_indicator.setViewPager(main_tab_pager);

		int page = getIntent().getIntExtra("page", -1);

		if (page < 0) {
			switchPage(0);
		} else {
			switchPage(page);
		}

		 
	}

	private void switchPage(int position) {

		tv_title = (TextView) findViewById(R.id.tv_title);
		main_tab_pager.setCurrentItem(position);

		tv_title.setText(mtpa.getTitles().get(position));
		main_tab_pager.setOnPageChangeListener(getOnPageChangeListener());
	}

	@Override
	// for more
	public void onMoreItemSelected(int position) {
		Intent intent = null;

		switch (position) {
		case 0:
			//用户协议
		case 1:
			//问题反馈
		case 2:
			intent = new Intent(this, MoreDetailsActivity.class);
			intent.putExtra("position", position);
			break;
		case 3:
			intent = new Intent(this, ShareSettingActivity.class);
			break;
		case 4:
		{
			
			
			
			new AlertDialog.Builder(MainTabActivity.this).setTitle("系统提示")//设置对话框标题  
			  
		     .setMessage("你确定要重新设置数据库吗？")//设置显示的内容  
		  
		     .setPositiveButton("确定",new DialogInterface.OnClickListener() {//添加确定按钮  
		  
		          
		  
		         @Override  
		  
		         public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件  
		  
		             // TODO Auto-generated method stub  
		        	 	getSharedPreferences("first", MODE_PRIVATE).edit()
						.putBoolean("isFirst", false).commit();
		        		MainTabActivity.this.getSharedPreferences("tiku", Activity.MODE_PRIVATE).edit()
						.putString("tiku",  "").commit();
		        		startActivity(new Intent(MainTabActivity.this,WelcomeActivity.class));
		        		MainTabActivity.this.finish();
		        		//Toast.makeText(MainTabActivity.this, "重设成功，请手动重新启动", Toast.LENGTH_LONG).show();
		             
		  
		         }  
		  
		     }).setNegativeButton("取消",new DialogInterface.OnClickListener() {//添加返回按钮  
		  
		          
		  
		         @Override  
		  
		         public void onClick(DialogInterface dialog, int which) {//响应事件  
		  
		             // TODO Auto-generated method stub  
		  
		             Log.i("alertdialog"," 请保存数据！");  
		  
		         }  
		  
		     }).show();//在按键响应事件中显示此对话框  
			
			
		}
		default:
			break;
		}
		if (intent != null) {
			startActivity(intent);
		}
	}

	@Override
	public void onClassicsIdSelected(int classicsId) {
		Intent intent = new Intent(this, ClassicsActivity.class);
		intent.putExtra("questionId", classicsId);
		if(login()){
			startActivity(intent);
		} 

	}

	public void shotView(View view) {
		fu = new FileUtil(this);
		Bitmap bm = fu.shotAndSave(fu.getPic_path());

		Intent toShare = new Intent(this, ShareFriendActivity.class);
		startActivity(toShare);
		// 保存完毕，及时回收
		if (!bm.isRecycled()) {
			bm.recycle();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// for exit
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			
			if(isExit==false){
				UiUtil.showToastShort(MainTabActivity.this, R.string.main_exit_prompt);
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						isExit=true;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						isExit=false;
					}
				}){}.start();
			}else{
				
				if(!LoginActivity.uid.equals("anonymous")){
//				new PullDataTask(this,new CallBack(){
//
//					@Override
//					public String done() {
//						// TODO Auto-generated method stub
//					//	UiUtil.showToastShort(MainTabActivity.this, "数据已同步");
//						System.exit(0);
//						return null;
//					}
//					
//				}).execute();
					this.finish();
					System.exit(0);
				}else{
					this.finish();
					System.exit(0);
				}
				 
			}
		}
		return false;
	}

	// 顺序练习
	public void toSequence(View v) {
		Intent intent = new Intent(this, TopicActivity.class);
		intent.putExtra("mode", TopicController.MODE_SEQUENCE);
		startActivity(intent);
	}

	// 随机练习
	public void toRandom(View v) {
		Intent intent = new Intent(this, TopicActivity.class);
		intent.putExtra("mode", TopicController.MODE_RANDOM);
		startActivity(intent);
	}

	// 练习
	public void toPracticeTest(View v) {
		Intent intent = new Intent(this, TopicActivity.class);
		intent.putExtra("mode", TopicController.MODE_PRACTICE_TEST);
		startActivity(intent);
	}

	// 专项目练习
	public void toChapters(View v) {
		if (ProjectConfig.TOPIC_MODE_CHAPTERS_SUPPORT) {
			// Intent intent = new Intent(this, TopicActivity.class);
			// intent.putExtra("mode", TopicController.MODE_CHAPTERS);
			// startActivity(intent);
			Intent intent = new Intent(this, ChapterSelectActivity.class);

			startActivity(intent);

		} else {
			UiUtil.showToastShort(this, R.string.please_wait);
		}
	}

	// 未做题
	public void toIntensify(View v) {
		if (ProjectConfig.TOPIC_MODE_INTENSIFY_SUPPORT) {
			Intent intent = new Intent(this, TopicActivity.class);
			intent.putExtra("mode", TopicController.MODE_INTENSIFY);
			startActivity(intent);
		} else {
			UiUtil.showToastShort(this, R.string.please_wait);
		}
	}

	// 统计
	public void toStatistics(View v) {
		Intent intent = new Intent(this, StatisticsActivity.class);
		startActivity(intent);
	}

	// 错题集
	public void toWrongTopic(View v) {
		if (mtc.checkWrongDataExist()) {
			Intent intent = new Intent(this, TopicActivity.class);
			intent.putExtra("mode", TopicController.MODE_WRONG_TOPIC);
			startActivity(intent);
		} else {
			UiUtil.showToastShort(this, R.string.data_not_exist);
		}

	}
	//收藏
	public void toCollect(View v) {
		if (mtc.checkCollectedDataExist()) {
			Intent intent = new Intent(this, TopicActivity.class);
			intent.putExtra("mode", TopicController.MODE_COLLECT);
			startActivity(intent);
		} else {
			UiUtil.showToastShort(this, R.string.data_not_exist);
		}

	}
	//考试记录
	public void toRecord(View v) {
		Intent intent = new Intent(MainTabActivity.this, RecordActivity.class);
		startActivity(intent);
	}

	private OnPageChangeListener getOnPageChangeListener() {
		return (new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				tv_title.setText(mtpa.getTitles().get(position));
				main_tab_icon_indicator.setCurrentItem(position);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
			}
		});
	}

	private void setDatabase() {
		SharedPreferences sp = getSharedPreferences("db", MODE_PRIVATE);
		String dbName = sp.getString("dbName", "data.db");// 获取设定的数据库

	}
	
	boolean login() {
		SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
		String uid = sp.getString("uid", "anonymous");
		LoginActivity.uid=uid;
		if (uid.equals("anonymous")||uid==""||uid==null) {
			// 还没有登录
			startActivityForResult(new Intent(this, LoginActivity.class), FLAGE_LOGIN);
			return false;
		}else{
			return true;
		}

	}

	private boolean isFirst() {

		SharedPreferences sp = getSharedPreferences("first", MODE_PRIVATE);
		boolean isFirst = sp.getBoolean("isFirst", true);
		if (isFirst) {

			overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
			startActivityForResult(new Intent(this, GuidanceActivity.class), FLAGE_GAI);
		}else{

		}
		return isFirst;
	}

	@Override
	protected void onActivityResult(int request, int result, Intent it) {
		// TODO Auto-generated method stub

		if (request == FLAGE_LOGIN) {
			if (result == RESULT_OK) {

				String uid = it.getStringExtra("uid");
				if (uid != null) {
					// 登录成功
					getSharedPreferences("user", MODE_PRIVATE).edit().putString("uid", uid).commit();
					LoginActivity.uid=uid;
				}

			} else if (result == RESULT_CANCELED) {
				// 取消登录
				Log.i("user", "login cancer");
				if(LoginActivity.uid.equals("anonymous")){
					Toast.makeText(this, "请先登录，亲~", Toast.LENGTH_SHORT).show();
					getSharedPreferences("user", MODE_PRIVATE).edit().putString("uid", "anonymous").commit();

				}
			}
		}
		if (request == FLAGE_GAI) {

		}

	}


}
