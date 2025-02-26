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

		// 검색버튼 & 검색어 이벤트 처리
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

		// Intent parameter 가져오기
		Intent intent = getIntent();
		final String action = intent.getAction();
		String prod_code = intent.getStringExtra(MyProdDBCons.PROD_CODE);
		String prod_name = intent.getStringExtra(MyProdDBCons.PROD_NAME);
		ContentValues values = new ContentValues();
		values.put(MyProdDBCons.PROD_CODE, prod_code);
		values.put(MyProdDBCons.PROD_NAME, prod_name);
		if (Intent.ACTION_INSERT.equals(action)) {
			sdb = openHelper.getWritableDatabase();
			// 컬럼별로 값 설정하는 map 객체
			sdb.insert(MyProdDBCons.TABLE_NAME, null, values);
			sdb.close();
			Toast.makeText(getApplicationContext(), "입력되었습니다",
					Toast.LENGTH_SHORT).show();
		}
		String whereClause = MyProdDBCons.PROD_CODE + "= ? ";
		String[] whereArgs = { prod_code };
		if (Intent.ACTION_EDIT.equals(action)) {
			sdb = openHelper.getWritableDatabase();
			sdb.update(MyProdDBCons.TABLE_NAME, values, whereClause, whereArgs);
			Toast.makeText(getApplicationContext(), "수정되었습니다",
					Toast.LENGTH_SHORT).show();
		}
		if (Intent.ACTION_DELETE.equals(action)) {
			sdb = openHelper.getWritableDatabase();
			sdb.delete(MyProdDBCons.TABLE_NAME, whereClause, whereArgs);
			Toast.makeText(getApplicationContext(), "삭제되었습니다",
					Toast.LENGTH_SHORT).show();
		}

	}

	private int checkDBCount() {
		// db 연결 준비
		openHelper = new MyProdDBHelper(this, null, null, 0);
		sdb = openHelper.getReadableDatabase();
		// DB 조회
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
		// 컬럼별로 값 설정하는 map 객체
		ContentValues values = new ContentValues();
		String[] items = { "새우깡", "맛동산", "카라멜콘", "오징어땅콩", "꿀짱구" };
		for (int i = 0; i < items.length; i++) {
			values.put(MyProdDBCons.PROD_CODE, "SNK" + i);
			values.put(MyProdDBCons.PROD_NAME, items[i]);
			sdb.insert(MyProdDBCons.TABLE_NAME, null, values);
		}
		sdb.close();
	}
}
