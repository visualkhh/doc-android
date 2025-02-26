package hest.co.kr;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MyXMLPrefView extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_xml_pref_view);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		TextView txt_pref = (TextView)findViewById(R.id.txt_pref);
		String value = sp.getString("my_ed", "설정안됨");
		//value = new Boolean(sp.getBoolean("my_pref_cb", false)).toString();
		txt_pref.setText("my_ed 설정값: " + value);		
		
		LinearLayout mylayout = (LinearLayout)findViewById(R.id.my_layout);
		value = sp.getString("list_preference", "#CCCCCC");
		int color = Color.parseColor(value);
		mylayout.setBackgroundColor(color);		
		
	}
}



