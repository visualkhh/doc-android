package hest.co.kr;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MyHandlerDemo extends Activity {
	ProgressBar bar;
	TextView txt;
	boolean isRunning = false;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			bar.incrementProgressBy(5);
			txt.setText((CharSequence) msg.obj);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_handler_demo);
		bar = (ProgressBar) findViewById(R.id.progress);
		txt = (TextView) findViewById(R.id.txt);

		Button btn_start = (Button) findViewById(R.id.btn_start);
		btn_start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startThread(); // 쓰레드 시작 (6단계에서 함수 구현)
			}
		});
		Button btn_stop = (Button) findViewById(R.id.btn_stop);
		btn_stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				isRunning = false;
			}
		});
	}

	private void startThread() {
		bar.setProgress(0);
		Thread mythread = new Thread(new Runnable() {
			int mynum = 0;

			public void run() {
				try {
					while (isRunning) {
						Thread.sleep(1000);
						String obj = "[My Thread Msg] : " + mynum;
						Message msg = handler.obtainMessage(1, obj);
						handler.sendMessage(msg);
						mynum++;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		isRunning = true;
		mythread.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		isRunning = false;
	}
}
