package hest.co.kr;

import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MyHandlerServiceApp extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_handler_demo);
		Button btn_start = (Button) findViewById(R.id.btn_start);
		Button btn_stop = (Button) findViewById(R.id.btn_stop);		
		btn_start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 액티비티 매니저에서 실행중 서비스 목록을 가져옴
				ActivityManager am 
				= (ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE);
				// 실행중 서비스 List 객체
				List<RunningServiceInfo> serviceList 
				= am.getRunningServices(Integer.MAX_VALUE);
				// for문으로 추출
				boolean isRunning = false;
				for(Iterator<RunningServiceInfo> it=serviceList.iterator(); it.hasNext();) {
					RunningServiceInfo info = (RunningServiceInfo)it.next();
					if(info.service.getClassName().equals("hest.co.kr.MyHandlerService")) {
						Toast.makeText(getApplicationContext(), "Service 이미 시작", 
								Toast.LENGTH_SHORT).show();				
						isRunning = true;
						break;
					}
				}
				if(!isRunning) {
					startService(new Intent("MyHandlerService"));
					Toast.makeText(getApplicationContext(), "Service 처음 시작", 
							Toast.LENGTH_SHORT).show();						
				} else {
					Toast.makeText(getApplicationContext(), "Service 이미 시작", 
							Toast.LENGTH_SHORT).show();								
				}
			}
		});
		btn_stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				stopService(new Intent("MyHandlerService"));
				Toast.makeText(getApplicationContext(), "Service 종료", 
						Toast.LENGTH_SHORT).show();				
			}
		});
	}
}




