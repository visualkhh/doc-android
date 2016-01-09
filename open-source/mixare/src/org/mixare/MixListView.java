/*
 * Copyright (C) 2010- Peer internet solutions
 * 
 * This file is part of mixare.
 * 
 * This program is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License 
 * for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with 
 * this program. If not, see <http://www.gnu.org/licenses/>
 */
package org.mixare;

import java.util.Vector;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MixListView extends ListActivity{
	
	private static int list=0;
	
	private static Vector<String> listViewMenu = null;
	private static Vector<String> selectedItemURL= null;
	private static Vector<String> dataSourceMenu= null;
	private static MixContext context = null;
	private static DataView dataView = null;
	private static String selectedDataSource="Wikipedia";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		context = MixView.ctx;
		dataView = MixView.view;
		
		switch(list){
		case 1:
			dataSourceMenu = new Vector();
			dataSourceMenu.add("Wikipedia");
			dataSourceMenu.add("Twitter");
			dataSourceMenu.add("Buzz");
			dataSourceMenu.add("OpenStreetMap");
			dataSourceMenu.add("KDT");
//			dataSourceMenu.add("own URL");
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,dataSourceMenu));
			getListView().setTextFilterEnabled(true);
			break;
		
		case 2:
			selectedItemURL = new Vector();
			listViewMenu = new Vector();
			/*add all marker items to a title and a URL Vector*/
			for(int i = 0; i<dataView.jLayer.markers.size();i++){
				Marker ma = new Marker();
				ma = dataView.jLayer.markers.get(i);
				listViewMenu.add(ma.getText());
					/*the website for the corresponding title*/
					if(ma.getURL()!=null)
						selectedItemURL.add(ma.getURL());
					/*if no website is available for a specific title*/
					else
						selectedItemURL.add("");
			}
			setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,listViewMenu));
			getListView().setTextFilterEnabled(true);
			break;
			
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		switch(list){
			/*Data Sources*/
			case 1:
				clickOnDataSource(position);		
				break;
			
			/*List View*/
			case 2:
				clickOnListView(position);
				break;
		}
		
	}
	public void clickOnListView(int position){
		/*if no website is available for this item*/
		if(selectedItemURL.get(position)==""){				
			Toast.makeText( this, getString(dataView.NO_WEBINFO_AVAILABLE), Toast.LENGTH_LONG ).show();			
		}
		else{
			String url = selectedItemURL.get(position);
			try {
				if (url != null && url.startsWith("webpage")) {
					String newUrl = MixUtils.parseAction(url);
					dataView.ctx.loadWebPage(newUrl, this);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void clickOnDataSource(int position){
		switch(position){
			/*WIKIPEDIA*/
			case 0:
				setDataSource("Wikipedia");
				Toast.makeText( this , getString(dataView.DATA_SOURCE_CHANGE_WIKIPEDIA), Toast.LENGTH_LONG ).show();
				break;
			
			/*TWITTER*/
			case 1:		
				setDataSource("Twitter");
				Toast.makeText( this ,getString(dataView.DATA_SOURCE_CHANGE_TWITTER), Toast.LENGTH_LONG ).show();	
				break;

			/*BUZZ*/
			case 2:
				setDataSource("Buzz");
				Toast.makeText( this ,getString(dataView.DATA_SOURCE_CHANGE_BUZZ), Toast.LENGTH_LONG).show();
				break;
				
			/*OSM*/
			case 3:
				setDataSource("OpenStreetMap");
				Toast.makeText( this ,getString(dataView.DATA_SOURCE_CHANGE_OSM), Toast.LENGTH_LONG).show();
				break;
			/*KDT*/
			case 4:
				setDataSource("KDT");
				Toast.makeText( this ,getString(dataView.DATA_SOURCE_CHANGE_KDT), Toast.LENGTH_LONG).show();
				break;
				
			/*Own URL*/
//			case 3:
//				setDataSource("OwnURL");
//				Toast.makeText( this ,"sdfwer3rh", Toast.LENGTH_LONG ).show();	
//				break;
			
		}
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
			int base = Menu.FIRST;
			
			/*define menu items*/
			MenuItem item1 = menu.add(base, base, base, getString(dataView.MENU_ITEM_3)); 
			MenuItem item2 = menu.add(base, base+1, base+1, getString(dataView.MENU_CAM_MODE));

			/*assign icons to the menu items*/
			item1.setIcon(android.R.drawable.ic_menu_mapmode);
			item2.setIcon(android.R.drawable.ic_menu_camera);
			
			return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId()){
			/*Map View*/
			case 1:
				createMixMap();
				finish();
				break;
			/*back to Camera View*/
			case 2:
				finish();
				break;
		}
		return true;
	}
	
	public void createMixMap(){
		Intent intent2 = new Intent(MixListView.this, MixMap.class); 
		startActivityForResult(intent2, 20);
	}
	
	public static void setDataSource(String source){
		selectedDataSource = source;
	}
	
	public static String getDataSource(){
		return selectedDataSource;
	}
	
	public static void setList(int l){
		list = l;
	}
}




