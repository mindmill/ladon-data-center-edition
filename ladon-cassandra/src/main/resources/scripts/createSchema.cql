DROP KEYSPACE IF EXISTS LADON;

CREATE KEYSPACE IF NOT EXISTS LADON WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy',  $datacenterString };
--ALTER KEYSPACE system_auth WITH REPLICATION = {'class' : 'NetworkTopologyStrategy',  '$datacenter' : $replicationfactor };

CREATE TABLE LADON.REPOSITORIES (
repoid text PRIMARY KEY,
acl text,
createdby text,
policy text,
region text,
versioned boolean,
creationdate timestamp
);

CREATE TABLE LADON.CONTENT(
repoid text ,
streamid text,
count bigint,
chunkid text,
PRIMARY KEY ((repoid, streamid), count)
) WITH CLUSTERING ORDER BY (count ASC);


CREATE TABLE LADON.CHUNKS(
chunkid text,
ref text,
content blob static ,
PRIMARY KEY (chunkid, ref)
);

CREATE TABLE LADON.OBJECTS (
repoid text,
versionseriesid text,
changetoken timeuuid,
operation text,
length bigint,
acl text,
meta map<text,text>,
streamid text,
md5 text,
created timestamp,
createdby text,
deleted timestamp,
deletedby text,
PRIMARY KEY (repoid , versionseriesid,  changetoken)
)WITH CLUSTERING ORDER BY (versionseriesid ASC, changetoken DESC);

CREATE INDEX vsid_idx ON LADON.OBJECTS (versionseriesid) ;

CREATE MATERIALIZED VIEW LADON.CHANGETOKEN_VIEW
 AS SELECT repoid, changetoken, versionseriesid, operation
 FROM LADON.OBJECTS
 WHERE repoid IS NOT NULL AND changetoken IS NOT NULL AND versionseriesid IS NOT NULL
 PRIMARY KEY ( repoid, changetoken, versionseriesid);


CREATE TABLE LADON.USERS (
name text PRIMARY KEY,
password text,
enabled boolean,
roles set<text>
);

CREATE TABLE LADON.KEYS(
accesskeyid text PRIMARY KEY,
secretkey text,
userid text,
roles set<text>
);

CREATE TABLE LADON.ROLES (
roleid text,
member text,
PRIMARY KEY (roleid,member)
);
