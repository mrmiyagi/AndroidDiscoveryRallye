package android.discoveryRallye;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;


public class POIContainer implements IDBPOI {
	
	/* ArrayList is not synchronized */
	private ArrayList<POI> pois;
	private ArrayList<String> poisName;
	private static POIContainer poiCon;
	private Context ctx;
	
	/**
	 * Private Constructor; Singleton pattern 
	 */
	private POIContainer(Context ctx){
		Log.i("SQLite","POIContainer::POIContainer");
		pois = new ArrayList<POI>();
		poisName = new ArrayList<String>();
		this.ctx = ctx;
		fillPois();
	}
	
	/**
	 * Method to get the Instance of the unique POIConatiner
	 * 
	 * @return The reference of the container class
	 */
	public static POIContainer getInstance(Context ctx){
		if (poiCon == null ){
			poiCon = new POIContainer(ctx);
		}
		return poiCon;
	}
	
	
	/**
	 * Add a POI 
	 * 
	 * The POI is added to the SQLite DB and to both lists of the POIContainer
	 * 
	 * @param poi a POI object
	 * @return -1 if the POI was not added, otherwise a value greater or equal 0
	 */
	public long addPOI(POI poi){
		long result = -1;
		
		DiscoveryDbAdapter db = new DiscoveryDbAdapter(ctx);
		db.open();
		
		if (db != null ){
			result = db.insertPoi(poi);
			db.close();
		}else{
			Log.e("DiscoveryRallye","ERROR POIContainer::addPOIdb()");
		}

		/* 
		 * POI does not exist in the database, so add it 
		 * to the lists 
		 */
		if ( result >= 0){
			pois.add(poi);
			poisName.add(poi.getDescription());
		}
		
		return result;
	}
	
	/**
	 * Remove a POI.
	 * 
	 * The POI is deleted form the POI Container and form
	 * the SQLite database.
	 * 
	 * @param name the name of the POI
	 * @param id the id in the ListView (is equal with the id in the ArrayLists)
	 */
	public void removePOI(String name, long id){
		// Delete form the ArrayLists
		pois.remove((int)id);
		poisName.remove((int)id);
		
		// Delete from the Database
		DiscoveryDbAdapter db = new DiscoveryDbAdapter(ctx);
		db.open();
		if (db != null ){
			db.deletePOI(name);
			db.close();
		}else{
			Log.e("DiscoveryRallye","POIContainer::removePOI(long id)");
		}
	}
	
	/**
	 * Getter for the POI at the position index
	 * 
	 * If index less then zero or greater then the total size
	 * of the list, the POI at position zero will be returned
	 * 
	 * @param index the position
	 * @see POI
	 * @return the POI object
	 */
	public POI getPOI(int index){
		index = ( index <0 || index >= pois.size()) ? 0 : index;
		return pois.get(index);
	}
	
	/**
	 * Get method for the ArrayList, that contains all POIs objects
	 * 
	 * @return the ArrayList with all POIs
	 */
	public ArrayList<POI> getALLPOIs(){
		return pois;
	}
	
	/**
	 * Get method for the ArrayList, that contains all names of the POIs
	 * 
	 * @return the ArrayList with all POI names
	 */
	public ArrayList<String> getAllPOIsName(){
		return poisName;
	}
	
	/**
	 * The total number of stored POIs
	 *  
	 * @return number of POIs objects
	 */
	public int numberOfPOIs(){
		return pois.size();
	}
	
	
	// TODO Comment me
	private void fillPois(){
		DiscoveryDbAdapter db = new DiscoveryDbAdapter(ctx);
		
		if ( db != null ){
			db.open();
			
			//TODO Delete me
			db.resetDB();
			
			Cursor c = db.getPOIs();
			/* If c a vaild cursor */
			if ( c != null ){
				// The result set contains values
				if ( c.moveToFirst() ){
					
					// Set the capacity to 10 more than records are in the table
					pois.ensureCapacity(c.getCount()+10);
					
					/* The indices of the columns */
					int locationName = c.getColumnIndex(ATTR_NAME);
					int latitude	 = c.getColumnIndex(ATTR_LAT);
					int longitude	 = c.getColumnIndex(ATTR_LON);
					
					do{
						// retrieve the information
						String name = c.getString(locationName);
						double lat  = c.getDouble(latitude);
						double lon  = c.getDouble(longitude);
						
						// Adding the POI to the ArrayList
						pois.add( new POI(lat, lon, name) );
						poisName.add(name);
						
						// TODO Delete me
						Log.i("SQLite1","Name: " +name + "lat/lon " +
								lat + "/" + lon);
							
					}while(c.moveToNext());
				}
			}
			// Close the DB
			db.close();
		}
	}

}
