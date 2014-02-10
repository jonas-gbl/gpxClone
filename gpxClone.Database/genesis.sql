DROP TABLE IF EXISTS "Users" CASCADE;
DROP SEQUENCE IF EXISTS users_surrogate_key_seq;
CREATE SEQUENCE users_surrogate_key_seq START 1;
CREATE TABLE "Users" (
	"pId" integer NOT NULL,
	"Username" character varying(255) NOT NULL UNIQUE,
	"Email" character varying(128) NOT NULL,
	"Password" character varying(255) NOT NULL,
	"ActivationKey" character varying(255),
	"PasswordQuestion" character varying(255),
	"PasswordAnswer" character varying(255),
	"IsApproved" boolean DEFAULT FALSE, 
	"LastActivityDate" timestamptz,
	"LastLoginDate" timestamptz,
	"LastPasswordChangedDate" timestamptz,
	"CreationDate" timestamptz DEFAULT now(), 
	"IsOnLine" boolean DEFAULT FALSE,
	"IsLockedOut" boolean DEFAULT TRUE,
	CONSTRAINT users_pkey PRIMARY KEY ("pId")
);
CREATE INDEX users_email_index ON "Users" ("Email");
ALTER SEQUENCE users_surrogate_key_seq OWNED BY "Users"."pId";

DROP TABLE IF EXISTS "Roles" CASCADE;
CREATE TABLE "Roles" (
	"Rolename" character varying(255) NOT NULL,
	"Description" character varying(255) NULL,
	CONSTRAINT roles_pkey PRIMARY KEY ("Rolename")
);

DROP TABLE IF EXISTS "UsersInRoles" CASCADE;
CREATE TABLE "UsersInRoles" (
	"pId" integer NOT NULL,
	"Rolename" character varying(255) NOT NULL,
	CONSTRAINT usersinroles_pkey PRIMARY KEY ("pId", "Rolename"),
	CONSTRAINT usersinroles_username_fkey FOREIGN KEY ("pId") REFERENCES "Users" ("pId") ON DELETE CASCADE,
	CONSTRAINT usersinroles_rolename_fkey FOREIGN KEY ("Rolename") REFERENCES "Roles" ("Rolename") ON DELETE CASCADE
);

DROP TABLE IF EXISTS "UserPreferences" CASCADE;
CREATE TABLE "UserPreferences" (
	"pId" integer NOT NULL,
	"profilePolicy" character varying(255) NOT NULL,
	"gender" character varying(255) NOT NULL,
	"richEmailFormat" boolean DEFAULT false,
	"marketingOptIn" boolean DEFAULT false,
	"photoImageType" character varying(255),
	"profilePhoto" bytea,
	"birthYear" integer,
	"displayName" character varying(255),
	"description" character varying(255),
	"country" character varying(255),
	"region" character varying(255),
	CONSTRAINT userPrefs_pkey PRIMARY KEY ("pId"),
	CONSTRAINT userPrefs_username_fkey FOREIGN KEY ("pId") REFERENCES "Users" ("pId") ON DELETE CASCADE
);


DROP TABLE IF EXISTS "TrackTypes" CASCADE;
DROP SEQUENCE IF EXISTS tracktypes_surrogate_key_seq;
CREATE SEQUENCE tracktypes_surrogate_key_seq START 1;
CREATE TABLE "TrackTypes" (
	"typeID" integer NOT NULL,
	"Typename" character varying(255) NOT NULL,
	CONSTRAINT tracktypes_pkey PRIMARY KEY ("typeID")
);
ALTER SEQUENCE tracktypes_surrogate_key_seq OWNED BY "TrackTypes"."typeID";



DROP TABLE IF EXISTS "Tracks" CASCADE;
DROP SEQUENCE IF EXISTS tracks_surrogate_key_seq;
CREATE SEQUENCE tracks_surrogate_key_seq START 1;
CREATE TABLE "Tracks" (
	"trackID" integer NOT NULL,
	"owner" integer NOT NULL,
	"type" integer NOT NULL,
	"trace" geometry(GEOMETRY,4326),
	CONSTRAINT tracks_pkey PRIMARY KEY ("trackID"),
	CONSTRAINT tracks_owner_fkey FOREIGN KEY ("owner") REFERENCES "Users" ("pId") ON DELETE CASCADE,
	CONSTRAINT tracks_type_fkey FOREIGN KEY ("type") REFERENCES "TrackTypes" ("typeID") ON DELETE CASCADE
);
ALTER SEQUENCE tracks_surrogate_key_seq OWNED BY "Tracks"."trackID";

DROP TABLE IF EXISTS "Waypoints" CASCADE;
DROP SEQUENCE IF EXISTS waypoints_surrogate_key_seq;
CREATE SEQUENCE waypoints_surrogate_key_seq START 1;
CREATE TABLE "Tracks" (
	"pointID" integer NOT NULL,
	"point" geometry(GEOMETRY,4326),
	CONSTRAINT tracks_pkey PRIMARY KEY ("pointID")
);
ALTER SEQUENCE waypoints_surrogate_key_seq OWNED BY "Tracks"."trackID";

DROP VIEW IF EXISTS "projTracks";
CREATE OR REPLACE VIEW public."projTracks" AS 
	SELECT "Tracks"."trackID", "Tracks".owner, "Tracks".type, "Tracks"."public",
		st_transform("Tracks".trace, 900913)::geometry(Geometry,900913) AS trace
	FROM "Tracks";
