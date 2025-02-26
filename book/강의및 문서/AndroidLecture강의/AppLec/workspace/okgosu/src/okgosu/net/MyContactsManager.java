package okgosu.net;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MyContactsManager extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String[] from = {ContactsContract.Contacts.DISPLAY_NAME, 
				ContactsContract.Contacts._ID};
		Cursor cursor = getContentResolver()
			.query(ContactsContract.Contacts.CONTENT_URI, from, null, null, null);
		startManagingCursor(cursor);
		int[] to = {android.R.id.text1, android.R.id.text2};
		//SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, 
		//		android.R.layout.simple_list_item_2, cursor, from, to);
		MyAdapter adapter = new MyAdapter(this, cursor, from, to);
		setListAdapter(adapter);
		
	}
	
	class MyAdapter extends SimpleCursorAdapter {
		public MyAdapter(Context context, Cursor c, String[] from,
				int[] to) {
			super(context, R.layout.my_row, c, from, to);
		}
		
		Cursor c = getCursor();
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = getLayoutInflater().inflate(R.layout.my_row, null);
			TextView label = (TextView)row.findViewById(R.id.text1);
			int columnIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
			label.setText(c.getString(0));
			c.moveToNext();
			return row;
		}
	}
}









