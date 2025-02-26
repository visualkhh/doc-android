package hest.co.kr;

import android.app.ListActivity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MyCPDemo extends ListActivity {
	TextView mCallbackText;
	Button call_btn;
	private static String[] MY_PROJECTION = new String[] { MyProdDBCons._ID,
			MyProdDBCons.PROD_CODE, MyProdDBCons.PROD_NAME };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_cp_demo);
		mCallbackText = (TextView) findViewById(R.id.callback);
		mCallbackText.setText("ContentProvider 예제");
		call_btn = (Button) findViewById(R.id.call);
		call_btn.setOnClickListener(mCalllListener);
	}

	private OnClickListener mCalllListener = new OnClickListener() {
		public void onClick(View v) {
			String urlString = "content://okgosu.net.provider.MyProd/myprods/2";
			//ContentValues values = new ContentValues();
			//values.put("prod_name", "!!!!!!!양파링!!!!!!!!!!!!!!!");
			//getContentResolver().update(Uri.parse(urlString), values, null, null);
			
			getContentResolver().delete(Uri.parse(urlString), null, null);
			
//			ContentValues values = new ContentValues();
//			values.put("prod_name", "양파링");
//			values.put("prod_code", "SNK99");
//			getContentResolver().insert(Uri.parse(urlString), values);
			
//			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
//			startActivity(intent);
			 Cursor c = getContentResolver().query(MyProdDBCons.CONTENT_URI,
			 MY_PROJECTION, null, null, null);
			 startManagingCursor(c);
			 ListAdapter adapter = new SimpleCursorAdapter(MyCPDemo.this,
			 android.R.layout.simple_list_item_2, c, new String[] {
			 MyProdDBCons.PROD_CODE, MyProdDBCons.PROD_NAME },
			 new int[] { android.R.id.text1, android.R.id.text2 });
			 setListAdapter(adapter);
		}
	};
}
