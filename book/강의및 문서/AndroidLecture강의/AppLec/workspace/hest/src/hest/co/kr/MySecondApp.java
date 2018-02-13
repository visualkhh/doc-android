package hest.co.kr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MySecondApp extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_second_app);
		final Button btn_1 = (Button)findViewById(R.id.btn_1);
		btn_1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText ed_1 = (EditText)findViewById(R.id.ed_1);
				String msg = ed_1.getText().toString();
				// ���̾˷α�
				new AlertDialog.Builder(MySecondApp.this)
				.setTitle("���û���")
				.setMessage(msg)
				.setNeutralButton("�ݱ�", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						btn_1.setText("�ݾ���");
					}
				}).show();
				Log.d("okgosu", "##########okgosu@@@@@@@@@");
			}
		});
	}

}
