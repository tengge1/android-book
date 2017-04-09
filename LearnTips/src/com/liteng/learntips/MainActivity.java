package com.liteng.learntips;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.util.EncodingUtils;

import com.uucun.adsdk.UUAppConnect;
import com.uucun.adsdk.UpdatePointListener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends Activity implements OnItemClickListener,
		OnTouchListener, UpdatePointListener {

	private ViewFlipper mViewFlipper;// 视图切换控件
	private static float touchDownX = 0;//触摸开始时X位置
	private static int coins=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {//活动创建，初始化程序
		super.onCreate(savedInstanceState);
		// 初始化目录
		 UUAppConnect.getInstance(this).initSdk();
		LayoutInflater li = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = li.inflate(R.layout.activity_main, null);
		ListView lv = (ListView) view.findViewById(R.id.listView1);
		SimpleAdapter ad = new SimpleAdapter(this, getData(),
				R.layout.listitem, new String[] { "title" },
				new int[] { R.id.textView1 });
		lv.setAdapter(ad);
		lv.setOnItemClickListener(this);
		LinearLayout ll = (LinearLayout) view.findViewById(R.id.linearLayout1);
		ll.setOnTouchListener(this);
		ScrollView sv=(ScrollView)ll.findViewById(R.id.scrollView1);
		sv.setOnTouchListener(this);
		// 设置当前视图
		 //隐藏标题栏  
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
//		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		this.setContentView(view);
		mViewFlipper = (ViewFlipper) this.findViewById(R.id.viewFlipper1);
		//获取当前金币数量
		SharedPreferences sharedPreferences = getSharedPreferences("LearnTips", Context.MODE_PRIVATE);
		String scoins=sharedPreferences.getString("coins", "0");
		coins=Integer.parseInt(scoins);
	}

	private List<Map<String, Object>> getData() {//从contents.txt中读取目录数据
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		String contents = "";
		try {
			Resources resources = this.getResources();
			InputStream is = resources.openRawResource(R.raw.contents);
			byte[] bytes = new byte[is.available()];
			is.read(bytes);
			contents = EncodingUtils.getString(bytes, "gbk");
			is.close();
		} catch (Exception e) {
			Toast toast = Toast.makeText(this, "目录文件丢失！", Toast.LENGTH_LONG);
			toast.show();
			e.printStackTrace();
		}
		for (String s : contents.split("\r\n")) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("title", s);
			list.add(map);
		}
		return list;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {// 创建选项菜单
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {//点击目录列表视图条目
		// TODO Auto-generated method stub
		// 获取打开章的内容
		if(coins<50){
			Toast toast=Toast.makeText(this, "您的余额不足，只需50金币既可光看所有文章。", Toast.LENGTH_SHORT);
			toast.show();
			try{
				UUAppConnect.getInstance(this).showOffers();
			}
			catch(Exception e){
				Toast toast2=Toast.makeText(this, "需要联网才能获取金币。", Toast.LENGTH_SHORT);
				toast2.show();
			}
			return;
			
		}
		int raw_id = -1;
		try {
			Field field = R.raw.class.getField("tip_" + position);
			raw_id = field.getInt(new R.raw());
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			Toast toast = Toast.makeText(this, "抱歉，文件" + "tip_" + position
					+ ".txt丢失！", Toast.LENGTH_LONG);
			toast.show();
			e.printStackTrace();
			return;
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			Toast toast = Toast.makeText(this, "参数错误！", Toast.LENGTH_LONG);
			toast.show();
			e.printStackTrace();
			return;
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			Toast toast = Toast.makeText(this, "权限错误！", Toast.LENGTH_LONG);
			toast.show();
			e.printStackTrace();
			return;
		}
		CharSequence cs = ((TextView) (parent.findViewById(R.id.textView1)))
				.getText();
		cs=((TextView)view).getText();
		try {
			Resources resources = this.getResources();
			InputStream is = resources.openRawResource(raw_id);
			byte[] bytes = new byte[is.available()];
			is.read(bytes);

			((TextView) this.findViewById(R.id.tvTitle)).setText(cs);
			((TextView) this.findViewById(R.id.tvContent))
					.setText(EncodingUtils.getString(bytes, "gbk"));
			mViewFlipper.setInAnimation(this, R.anim.push_left_in);
			mViewFlipper.setOutAnimation(this, R.anim.push_left_out);
			mViewFlipper.showNext();
			is.close();
		} catch (Exception e) {
			Toast toast = Toast.makeText(this, "读取该章内容失败！", Toast.LENGTH_LONG);
			toast.show();
			e.printStackTrace();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {// 选择菜单项
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.id.itemBack:// 返回目录
			mViewFlipper.setInAnimation(this, R.anim.push_right_in);
			mViewFlipper.setOutAnimation(this, R.anim.push_right_out);
			mViewFlipper.showPrevious();
			break;
		case R.id.itemRefresh://刷新金币数量
			UUAppConnect.getInstance(this).getPoints(this);
			break;
		case R.id.itemDonate:// 捐赠作者
			try{
				UUAppConnect.getInstance(this).showOffers();
			}
			catch(Exception e){
				Toast toast2=Toast.makeText(this, "需要联网才能捐助作者。", Toast.LENGTH_SHORT);
				toast2.show();
			}
			break;
		case R.id.itemExit:// 退出程序
			UUAppConnect.getInstance(this).exitSdk();
			System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {// 每次显示菜单前调用
		// TODO Auto-generated method stub
		 if(mViewFlipper.getDisplayedChild()==0){//目录界面
			 menu.removeItem(R.id.itemBack);
			 return true;
		 }
		 else{//内容界面
			 if(null==menu.findItem(R.id.itemBack)){
				 menu.add(Menu.NONE, R.id.itemBack, 0, "返回目录");
			 }
			 return true;
		 }
		//return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {// 触屏操作
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchDownX = event.getX();
			return true;
		case MotionEvent.ACTION_UP:
			if (event.getX() - touchDownX > 100) {
				mViewFlipper.setInAnimation(this, R.anim.push_right_in);
				mViewFlipper.setOutAnimation(this, R.anim.push_right_out);
				mViewFlipper.showPrevious();
				return true;
			}
			break;
		}
		return false;
	}

	@Override
	public void onError(String arg0) {//获取金币出现错误
		// TODO Auto-generated method stub
		Toast toast=Toast.makeText(this, arg0, Toast.LENGTH_LONG);
		toast.show();
	}

	@Override
	public void onSuccess(String arg0, int arg1) {//获取金币正确
		// TODO Auto-generated method stub
		SharedPreferences sharedPreferences = getSharedPreferences("LearnTips", Context.MODE_PRIVATE);
		//创建数据编辑器
		Editor editor = sharedPreferences.edit();
		//传递需要保存的数据
		editor.putString("coins", Integer.toString(arg1));
		//保存数据
		editor.commit();
		Toast toast=Toast.makeText(this, "您的当前"+arg0+"数是"+arg1+"。", Toast.LENGTH_LONG);
		toast.show();
		coins=arg1;
	}
}
