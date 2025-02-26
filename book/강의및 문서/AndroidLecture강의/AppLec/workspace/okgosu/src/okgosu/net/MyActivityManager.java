package okgosu.net;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class MyActivityManager extends ListActivity {
	List<ApplicationInfo> applist;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PackageManager manager = getPackageManager();
        applist = manager.getInstalledApplications(0);
        setListAdapter(new MyAdapter(this, R.layout.my_row, applist));
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	ApplicationInfo app = (ApplicationInfo)applist.get(position);
		Intent intent = new Intent(getApplicationContext(), app.getClass());
		startActivity(intent);
    }
    class MyAdapter extends ArrayAdapter {
    	Activity context;
    	List<ApplicationInfo> items = new ArrayList<ApplicationInfo>();
		public MyAdapter(Context context, int resource, List objects) {
			super(context, resource, objects);
			this.context = (Activity)context;
			this.items = objects;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = context.getLayoutInflater().inflate(R.layout.my_row, null);
			ImageView img = (ImageView)row.findViewById(R.id.img);
			final ApplicationInfo app = (ApplicationInfo)items.get(position);
			img.setImageDrawable(app.loadIcon(getPackageManager()));
			TextView label = (TextView)row.findViewById(R.id.text1);
			// 패키지
			//label.setText(app.packageName);
			// 라벨
			label.setText(app.loadLabel(getPackageManager()));
			return row;
		}
    }
}








