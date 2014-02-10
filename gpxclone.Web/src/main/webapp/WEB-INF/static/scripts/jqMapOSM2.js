
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
	wms_ktimatologio =	new OpenLayers.Layer.WMS("Ktimatologio WMS Layer","http://gis.ktimanet.gr/wms/wmsopen/wmsserver.aspx",
            {layers: "KTBASEMAP",format: "image/png"});
	wms_layer = new OpenLayers.Layer.WMS("Tracks","http://localhost:9090/geoserver/gpxClone/wms",
            {layers: "gpxClone:Tracks",transparent: "true",format: "image/png"},{isBaseLayer: false});
	wms_local = new OpenLayers.Layer.WMS("Local Tracks","trails/wms",
            {layers: "gpxClone:Tracks",transparent: "true",format: "image/png"},{isBaseLayer: false});
	wms_baselayer = new OpenLayers.Layer.WMS("Tracks Base layer","http://localhost:9090/geoserver/gpxClone/wms",
            {layers: "gpxClone:projTracks",format: "image/png"});
	
	
	//map.addLayer(wms_layer);
        map.addLayer(wms_local);
	//map.addLayer(wms_osm);
	map.addLayer(osm_layer);
	map.addLayer(wms_ktimatologio);
		
	map.addControl( new OpenLayers.Control.LayerSwitcher() );
	map.addControl( new OpenLayers.Control.MousePosition({div:document.getElementById('position')}) );
	map.addControl( new OpenLayers.Control.Scale(document.getElementById('scale'),null) );
	map.setCenter(
		new OpenLayers.LonLat(23.1507915,38.9543983).transform(
			new OpenLayers.Projection("EPSG:4326"),map.getProjectionObject()),
		12);


	map.events.register('click', map, function (e) {
		document.getElementById('feature_info').innerHTML = "Loading... please wait...";
		
		var wms_params = {
			REQUEST: "GetFeatureInfo",
			EXCEPTIONS: "application/vnd.ogc.se_xml",
			BBOX: map.getExtent().toBBOX(),
			SERVICE: "WMS",
			INFO_FORMAT: 'text/html',
			QUERY_LAYERS: map.layers[0].params.LAYERS,
			FEATURE_COUNT: 50,
			Layers: 'gpxClone:Tracks',
			WIDTH: map.size.w,
			HEIGHT: map.size.h,
			format: 'image/png',
			styles: map.layers[0].params.STYLES,
			srs: map.layers[0].params.SRS};
			
		// handle the wms 1.3 vs wms 1.1 madness
		if(map.layers[0].params.VERSION == "1.3.0") {
			wms_params.version = "1.3.0";
			wms_params.j = parseInt(e.xy.x);
			wms_params.i = parseInt(e.xy.y);
		} else {
			wms_params.version = "1.1.1";
			wms_params.x = parseInt(e.xy.x);
			wms_params.y = parseInt(e.xy.y);
		}
			
		var get_config = {
			url: "http://localhost:8080/gpxclone/trails/wms",
			params: wms_params,
			callback: setHTML
		};
		

			
		OpenLayers.Request.GET(get_config);
		OpenLayers.Event.stop(e);
		
	});
}

// sets the HTML provided into the nodelist element
function setHTML(response){
	document.getElementById('feature_info').innerHTML = response.responseText;
};


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
	
	$("#map").height($(window).height()-200);
	$(window).resize(function() {
		$("#map").height($(window).height()-200);
	});
	
	$('#nominatim').ajaxForm(ajaxNominatimOptions);
}); 