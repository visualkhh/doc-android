package hest.co.kr;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;

public class MyTab extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_tab);
		final TabHost tabs = (TabHost) findViewById(R.id.tabhost);
		tabs.setup();
		// ù��° ��ȭ�� �߰�
		TabHost.TabSpec spec = tabs.newTabSpec("tag1");
		spec.setContent(R.id.AnalogClock01);
		spec.setIndicator("�ð�");
		tabs.addTab(spec);
		// �ι�° ��ȭ�� �߰�
		spec = tabs.newTabSpec("tag2");
		spec.setContent(R.id.TabButton01);
		spec.setIndicator("��ư");
		tabs.addTab(spec);
		// ������ȭ�� ����
		tabs.setCurrentTab(0);
		Button btn = (Button) findViewById(R.id.TabButton01);
		btn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// ���� �ϳ��� �߰�
				TabHost.TabSpec spec = tabs.newTabSpec("tag1");
				spec.setContent(new TabContentFactory() {
					public View createTabContent(String tag) {
						return (new DatePicker(MyTab.this));
					}
				});
				spec.setIndicator("��¥");
				tabs.addTab(spec);
				tabs.setCurrentTab(tabs.getChildCount());
			}
		});

	}
}
