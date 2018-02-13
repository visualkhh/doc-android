package hest.co.kr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MyIntentReceiver extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_intent_receiver);
		// Intent Parameter ����
		String input = getIntent().getStringExtra("mysend");
		if (input != null) {
			EditText edit = (EditText) findViewById(R.id.result_text);
			edit.setText(input);
		}
		// ��ư �̺�Ʈ ó��
		Button finish = (Button) findViewById(R.id.finish_button);
		finish.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				callFinish();
			}
		});
	}

	private void callFinish() {
		EditText edit = (EditText) findViewById(R.id.result_text);
		String result = edit.getText().toString();
		if (result.length() != 0) {
			Intent i = new Intent();
			i.putExtra("ACT_RESULT", result);
			// ��� �ڵ�(RESULT_OK)�� �Բ� ����Ʈ�� ���� �������� �ǵ���
			setResult(RESULT_OK, i);
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}
}
