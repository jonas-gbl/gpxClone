-- osm2pgsql -c -k -d unwire_location -U postgres -W -H localhost -P 5432 -S default.style cph_data\cph.osm.bz2

ALTER TABLE planet_osm_polygon ADD COLUMN commune_id SERIAL UNIQUE;
ALTER TABLE planet_osm_polygon ADD PRIMARY KEY (commune_id);
ALTER TABLE planet_osm_polygon ADD COLUMN last_update TIMESTAMP(6) DEFAULT (now() AT TIME ZONE 'UTC');
ALTER TABLE planet_osm_polygon ADD COLUMN tickets INTEGER DEFAULT 0;

DROP FUNCTION IF EXISTS random_tickets(n_ticket FLOAT, tstamp TIMESTAMP(6),cid INTEGER) CASCADE;
CREATE OR REPLACE FUNCTION  random_tickets(n_ticket FLOAT, tstamp TIMESTAMP(6),cid INTEGER)
RETURNS INTEGER
AS $random_tickets_body$
DECLARE
	new_tickets_num INTEGER;
BEGIN
	
	IF (now() AT TIME ZONE 'UTC' - tstamp <= (INTERVAL '0 00:01:00')) THEN
		return n_ticket;
	
	ELSE
		new_tickets_num := ceil(100*random());
		UPDATE planet_osm_polygon SET last_update=(now() AT TIME ZONE 'UTC'),tickets=new_tickets_num WHERE commune_id = cid;
		return new_tickets_num;
	END IF;
	
END
$random_tickets_body$ LANGUAGE plpgsql;

DROP VIEW IF EXISTS cph_communes;
CREATE OR REPLACE VIEW cph_communes AS
(
	SELECT commune_id,name,tags,way,random_tickets(tickets,last_update,commune_id) AS tickets FROM planet_osm_polygon
	WHERE boundary='administrative' AND name LIKE '%Kommune'
);

DROP TABLE IF EXISTS cph_municipalities;
CREATE TABLE cph_municipalities AS
(
	SELECT commune_id,name,tags,way,tickets,last_update FROM planet_osm_polygon
	WHERE boundary='administrative' AND name LIKE '%Kommune'
);
