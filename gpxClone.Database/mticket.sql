DROP TABLE IF EXISTS ticket;

CREATE TABLE ticket (
  id SERIAL NOT NULL,
  application_id INTEGER default NULL,
  serial_code varchar(30) default NULL,
  alpha_shortcode text,
  control_code varchar(50) default NULL,
  buy_time TIMESTAMP default NULL,
  start_time TIMESTAMP default NULL,
  expire_time TIMESTAMP default NULL,
  price INTEGER default NULL,
  price_in_tokens INTEGER default 0,
  status INTEGER default NULL,
  batch_flag INTEGER default NULL,
  last_status_time TIMESTAMP default NULL,
  smsc_info_id INTEGER default NULL,
  account_id INTEGER default NULL,
  test INTEGER default 0,
  tariff varchar(30) default NULL,
  type varchar(30) default NULL,
  pending INTEGER default NULL,
  locked varchar(70) default NULL,
  ts timestamp NOT NULL default now(),
  latest_ticket_state_id INTEGER default NULL,
  payment_type_id INTEGER default 1,
  order_channel_id INTEGER default 1,
  ticket_kinship INTEGER default 0,
  parent_ticket_id INTEGER default NULL,
  ticket_state INTEGER default NULL,
  transaction_state INTEGER default NULL,
  ACTION INTEGER default NULL,
  error_code INTEGER default NULL,
  previous_ticket_id INTEGER default NULL,
  next_ticket_id INTEGER default NULL,
  information_field varchar(55) default NULL,
  ticket_variant INTEGER default 0,
  vat varchar(6) default NULL,
  point_of_sale GEOMETRY(POINT,900913) default NULL,
  PRIMARY KEY  (id)
);

CREATE OR REPLACE VIEW cph_ticket_stats AS (
  SELECT municipality_id,name,COUNT(id)/ST_Area(way) AS tickets, way AS geometry
  FROM
    -- JOIN START --
      ticket
    JOIN
      cph_municipalities
    ON 
      ST_WITHIN(point_of_sale,way)  AND (now() AT TIME ZONE 'UTC' - buy_time <= (INTERVAL '0 00:05:00'))
    -- JOIN END  --
  GROUP BY municipality_id
);

CREATE OR REPLACE VIEW geoserver_tickets AS (
  SELECT * 
  FROM 
    ticket
  WHERE 
      (now() AT TIME ZONE 'UTC' - buy_time <= (INTERVAL '0 00:05:00'))
);

INSERT INTO ticket(id,buy_time,point_of_sale) values(DEFAULT,now() AT TIME ZONE 'UTC',ST_SET_SRID(ST_POINT(1381486.95522, 7504331.68749),900913));

-- Bounding Box
-- 1.327.697,625 7.460.647 1.427.373,375 7.541.238,12
-- 11,931344911256 55,507790345628 12,817879958234759 55,913615450602464

CREATE OR REPLACE FUNCTION generate_random_tickets (n INTEGER DEFAULT 10)
        RETURNS void
        AS $generate_random_tickets_BODY$
DECLARE
        i INTEGER := 0;
        j INTEGER := 1;
        maxiter INTEGER := 1000;
        x0 DOUBLE PRECISION;
        dx DOUBLE PRECISION;
        y0 DOUBLE PRECISION;
        dy DOUBLE PRECISION;
        xp DOUBLE PRECISION;
        yp DOUBLE PRECISION;
        cph_point Geometry;
        cph_geom Geometry;
BEGIN
        SELECT INTO cph_geom ST_Collect(way) FROM cph_municipalities;
        -- find envelope
          x0 = ST_XMin(cph_geom);
          dx = (ST_XMax(cph_geom) - x0);
          y0 = ST_YMin(cph_geom);
          dy = (ST_YMax(cph_geom) - y0);
        FOR j IN 1..n LOOP
          WHILE i < maxiter LOOP
                  i = i + 1;
                  xp = x0 + dx * random();
                  yp = y0 + dy * random();
                  cph_point = ST_SetSRID( ST_MakePoint( xp, yp ), ST_SRID(cph_geom) );
                  EXIT WHEN ST_Within( cph_point, cph_geom );
          END LOOP;

          INSERT INTO ticket(id,buy_time,point_of_sale) values(DEFAULT,now() AT TIME ZONE 'UTC',cph_point);
        END LOOP;
        
        IF i >= maxiter THEN
                RAISE EXCEPTION 'RandomPoint: number of interations exceeded %', maxiter;
        END IF; 
END; 
$generate_random_tickets_BODY$ LANGUAGE plpgsql;