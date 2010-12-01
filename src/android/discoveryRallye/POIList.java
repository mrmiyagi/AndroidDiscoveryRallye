package android.discoveryRallye;

import java.util.ArrayList;

import org.andnav.osm.views.OpenStreetMapView;

import android.app.ListActivity;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class POIList extends ListActivity 
{
	private static ArrayList<POI> pois = new ArrayList<POI>();
	private static ArrayList<String> poisDesription = new ArrayList<String>();
	private OpenStreetMapView openStreetMapView;
	
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//TODO: Test
		addListItem(new POI(51.494995, 7.419649, "FB4" ));
		
		setListAdapter((ListAdapter)  new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, poisDesription));
		
		openStreetMapView = CampusOSM.getOpenStreetMapView();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) 
	{
		POI poi = pois.get(position);
		
		addRouteOverlay(poi);
	}
	
	//TODO: Dieses statische Rumgeschiebe könnte man durch die Datenbank umgehen
	//Dann braucht diese Methode so nicht mehr.
	public static void addListItem(POI poi)
	{
		poisDesription.add(poi.getDescription());
		pois.add(poi);
	}
	
	@SuppressWarnings("unchecked")
	private void addRouteOverlay(POI destination)
    {
		JSONRequest jsonRequest = new JSONRequest(this, openStreetMapView);
		 
		ArrayList<POI> pois = new ArrayList<POI>();
		
		Location retrievedLocation = GeoUtils.retrieveLocation(this);
		POI myLocation = new POI(retrievedLocation.getLatitude(), retrievedLocation.getLongitude(), "My Location");
		
		pois.add(myLocation);
		pois.add(destination);
		 
		jsonRequest.execute(pois);
	}
	
	
}