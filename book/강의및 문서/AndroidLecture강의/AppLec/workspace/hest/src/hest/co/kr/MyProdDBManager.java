package hest.co.kr;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MyProdDBManager extends Activity {
	private MyProdDBHelper openHelper;
	private SQLiteDatabase sdb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_prod_db_manager);
		if (checkDBCount() == 0)
			initMyProdDB();

		// �˻���ư & �˻��� �̺�Ʈ ó��
		final EditText searchEt = (EditText) findViewById(R.id.et_search);
		Button findBtn = (Button) findViewById(R.id.btn_query);
		findBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent("MyProdDBList");
				intent.putExtra("searchCode", searchEt.getText().toString());
				startActivity(intent);
			}
		});

		Button insertBtn = (Button) findViewById(R.id.btn_insert);
		insertBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent("MyProdDBInsert");
				startActivity(intent);
			}
		});

		// Intent parameter ��������
		Intent intent = getIntent();
		final String action = intent.getAction();
		String prod_code = intent.getStringExtra(MyProdDBCons.PROD_CODE);
		String prod_name = intent.getStringExtra(MyProdDBCons.PROD_NAME);
		ContentValues values = new ContentValues();
		values.put(MyProdDBCons.PROD_CODE, prod_code);
		values.put(MyProdDBCons.PROD_NAME, prod_name);
		if (Intent.ACTION_INSERT.equals(action)) {
			sdb = openHelper.getWritableDatabase();
			// �÷����� �� �����ϴ� map ��ü
			sdb.insert(MyProdDBCons.TABLE_NAME, null, values);
			sdb.close();
			Toast.makeText(getApplicationContext(), "�ԷµǾ����ϴ�",
					Toast.LENGTH_SHORT).show();
		}
		String whereClause = MyProdDBCons.PROD_CODE + "= ? ";
		String[] whereArgs = { prod_code };
		if (Intent.ACTION_EDIT.equals(action)) {
			sdb = openHelper.getWritableDatabase();
			sdb.update(MyProdDBCons.TABLE_NAME, values, whereClause, whereArgs);
			Toast.makeText(getApplicationContext(), "�����Ǿ����ϴ�",
					Toast.LENGTH_SHORT).show();
		}
		if (Intent.ACTION_DELETE.equals(action)) {
			sdb = openHelper.getWritableDatabase();
			sdb.delete(MyProdDBCons.TABLE_NAME, whereClause, whereArgs);
			Toast.makeText(getApplicationContext(), "�����Ǿ����ϴ�",
					Toast.LENGTH_SHORT).show();
		}

	}

	private int checkDBCount() {
		// db ���� �غ�
		openHelper = new MyProdDBHelper(this, null, null, 0);
		sdb = openHelper.getReadableDatabase();
		// DB ��ȸ
		Cursor c = sdb.query(MyProdDBCons.TABLE_NAME, null, null, null, null,
				null, null);
		startManagingCursor(c);
		int count = c.getCount();
		c.close();
		sdb.close();
		return count;
	}

	private void initMyProdDB() {
		sdb = openHelper.getWritableDatabase();
		// �÷����� �� �����ϴ� map ��ü
		ContentValues values = new ContentValues();
		String[] items = { "�����", "������", "ī�����", "��¡���", "��¯��" };
		for (int i = 0; i < items.length; i++) {
			values.put(MyProdDBCons.PROD_CODE, "SNK" + i);
			values.put(MyProdDBCons.PROD_NAME, items[i]);
			sdb.insert(MyProdDBCons.TABLE_NAME, null, values);
		}
		sdb.close();
	}
}
