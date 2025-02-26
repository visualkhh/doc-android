package hest.co.kr;

import android.net.Uri;
import android.provider.BaseColumns;

public class MyProdDBCons implements BaseColumns {
	//DB 정보
	public static final String DB_NAME = "my_prod_sqlite.db";
	public static final int DB_VERSION = 1;
	//TABLE 정보
	public static final String TABLE_NAME ="prod";
	//COLUMN 정보
	public static final String PROD_CODE ="prod_code";
	public static final String PROD_NAME ="prod_name";

	
	public static final String AUTHORITY= "okgosu.net.provider.MyProd";
	public static final Uri CONTENT_URI= Uri.parse("content://" + AUTHORITY+ "/myprods");
	public static final String CONTENT_TYPE= "vnd.android.cursor.dir/vnd.okgosu.net.MyProd";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.okgosu.net.MyProd";
	
	// 상품리스트 조회 URI  content://okgosu.net.provider.MyProd/myprods 
	
}
