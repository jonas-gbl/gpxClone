
function initMap(){
	var options = {
		projection: new OpenLayers.Projection("EPSG:900913"),
		displayProjection: new OpenLayers.Projection("EPSG:4326"),
		units: 'degrees',
		theme: null
	};

	//OpenLayers.Util.onImageLoadErrorColor = 'transparent';
	map = new OpenLayers.Map('map',options);
	
	osm_layer = new OpenLayers.Layer.OSM("Simple OSM Map");
	wms_osm =	new OpenLayers.Layer.WMS("OSM WMS Layer","http://129.206.228.72/cached/osm?",
            {layers: "osm_auto:all",format: "image/png"});
	wms_layer = new OpenLayers.Layer.WMS("Tracks","http://localhost:9090/geoserver/gpxClone/wms",
            {layers: "gpxClone:Tracks",transparent: "true",format: "image/png"},{isBaseLayer: false});
	wms_local = new OpenLayers.Layer.WMS("Local Tracks","trails/wms",
            {layers: "gpxClone:Tracks",transparent: "true",format: "image/png"},{isBaseLayer: false});
	wms_baselayer = new OpenLayers.Layer.WMS("Tracks Base layer","http://localhost:9090/geoserver/gpxClone/wms",
            {layers: "gpxClone:projTracks",format: "image/png"});
	
	
	//map.addLayer(wms_layer);
    //map.addLayer(wms_local);
	//map.addLayer(wms_osm);
	map.addLayer(osm_layer);
		
	map.addControl( new OpenLayers.Control.LayerSwitcher() );
	map.setCenter(
		new OpenLayers.LonLat(23.1507915,38.9543983).transform(
			new OpenLayers.Projection("EPSG:4326"),map.getProjectionObject()),
		12);     
}

function RecenterMap(responseXML) {
	var latitude = $("place", responseXML).attr("lat");
	var longitude = $("place", responseXML).attr("lon");
	
	
	var proj = new OpenLayers.Projection("EPSG:4326");
	var point = new OpenLayers.LonLat(longitude, latitude);
	map.setCenter(point.transform(proj, map.getProjectionObject()));
}


var ajaxNominatimOptions =
	{
		dataType: 'xml',
		success: RecenterMap
	};

$(document).ready(function(){
	initMap();
	
	$("#map_container").height($(window).height()-200)
	$(window).resize(function() {
		$("#map_container").height($(window).height()-200)
	});
	
	$('#nominatim').ajaxForm(ajaxNominatimOptions);
}); 