
function initMap(){
	var options = {
		projection: new OpenLayers.Projection("EPSG:900913"),
		displayProjection: new OpenLayers.Projection("EPSG:4326"),
		units: 'degrees',
		theme: null
	};

	map = new OpenLayers.Map('map',options);
	
	osm_layer = new OpenLayers.Layer.OSM("Simple OSM Map");
	cph_ticket_stats_wms = new OpenLayers.Layer.WMS("mTickets Stats","http://localhost:9090/geoserver/Unwire/wms",
            {layers: "Unwire:cph_ticket_stats",transparent: "true",format: "image/png"},{isBaseLayer: false});
	cph_tickets_wms = new OpenLayers.Layer.WMS("mTickets","http://localhost:9090/geoserver/Unwire/wms",
            {layers: "Unwire:geoserver_tickets",transparent: "true",format: "image/png"},{isBaseLayer: false});

	map.addLayer(osm_layer);
	map.addLayer(cph_ticket_stats_wms);
	map.addLayer(cph_tickets_wms);
		
	map.addControl( new OpenLayers.Control.LayerSwitcher() );
	map.addControl( new OpenLayers.Control.MousePosition({div:document.getElementById('position')}) );
	map.addControl( new OpenLayers.Control.Scale(document.getElementById('scale'),null) );
	map.setCenter(
		new OpenLayers.LonLat(12.5700724,55.6867243).transform(
			new OpenLayers.Projection("EPSG:4326"),map.getProjectionObject()),
		12);
	map.zoomToScale(500000,false);


	map.events.register('click', map, function (e) {
		document.getElementById('feature_info').innerHTML = "Loading... please wait...";
		
		var wms_params = {
			REQUEST: "GetFeatureInfo",
			EXCEPTIONS: "application/vnd.ogc.se_xml",
			BBOX: map.getExtent().toBBOX(),
			SERVICE: "WMS",
			INFO_FORMAT: 'text/html',
			QUERY_LAYERS: map.layers[1].params.LAYERS,
			FEATURE_COUNT: 50,
			Layers: 'Unwire:cph_ticket_stats',
			WIDTH: map.size.w,
			HEIGHT: map.size.h,
			format: 'image/png',
			styles: map.layers[1].params.STYLES,
			srs: map.layers[1].params.SRS};
			
		// handle the wms 1.3 vs wms 1.1 madness
		if(map.layers[1].params.VERSION == "1.3.0") {
			wms_params.version = "1.3.0";
			wms_params.j = parseInt(e.xy.x);
			wms_params.i = parseInt(e.xy.y);
		} else {
			wms_params.version = "1.1.1";
			wms_params.x = parseInt(e.xy.x);
			wms_params.y = parseInt(e.xy.y);
		}
			
		var get_config = {
			url: "http://localhost:9090/geoserver/Unwire/wms",
			params: wms_params,
			callback: setHTML
		};
		

			
		OpenLayers.Request.GET(get_config);
		OpenLayers.Event.stop(e);
		
	});
}

// sets the HTML provided into the nodelist element
function setHTML(response){
	h_before=$("#title_row").outerHeight( true );
	document.getElementById('feature_info').innerHTML = response.responseText;
	h_after=$("#title_row").outerHeight( true );
	h_diff = h_after - h_before;
	$("#map").height($(window).height()-200-h_diff);
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