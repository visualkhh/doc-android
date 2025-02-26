package hest.co.kr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

public class MyProdDBHelper extends SQLiteOpenHelper {

	public MyProdDBHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, MyProdDBCons.DB_NAME, factory, MyProdDBCons.DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + MyProdDBCons.TABLE_NAME+ " ("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ MyProdDBCons.PROD_CODE+ " text, "
				+ MyProdDBCons.PROD_NAME+ " text not null);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE " + MyProdDBCons.TABLE_NAME);
		onCreate(db);
	}

}
