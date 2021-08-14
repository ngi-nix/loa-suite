package org.linkedopenactors.code.osmadapter;


import java.util.HashMap;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OsmEntry {

//	{
//		"1": {
//			"type": "node",
//			"id": 1498389349,
//			"lat": 48.7190489,
//			"lon": 9.114674,
//			"tags": {
//				"amenity": "restaurant",
//				"check_date:opening_hours": "2021-07-24",
//				"cuisine": "indian",
//				"diet:vegan": "yes",
//				"diet:vegetarian": "yes",
//				"email": "namaste-india@email.de",
//				"fax": "+49 711 91 266 204",
//				"name": "Namaste India",
//				"opening_hours": "Mo-Su,PH 17:30-23:00, Su-Fr,PH 11:30-14:30",
//				"outdoor_seating": "yes",
//				"phone": "+49 711 91 266 202",
//				"website": "http://www.namaste-india-restaurant.com",
//				"wheelchair": "limited"
//			}
//		}
//	}



	  private Double lat;

	  private Double lon;

	  private HashMap<String, String> tags;

	  private String id;




}
