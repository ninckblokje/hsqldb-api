# hsqldb-api

Some management operation for HSQLDB (exposed using REST API's), to manage an HSQLDB datasource.

## Operations

### Checkpoint

Note: This operation hangs!

URL: `PUT /hsqldb-api/system/hsqldb/checkpoint`
Query:
````sql
checkpoint
````

### Count used / unused LOBS's

URL: `GET /hsqldb-api/system/hsqldb/lob/count`
Query:
````sql
select LOB_USAGE_COUNT, count(*), sum(LOB_LENGTH)
  from SYSTEM_LOBS.LOB_IDS
 group by LOB_USAGE_COUNT
````

### Delete empty LOB allocations

URL: `DELETE /hsqldb-api/system/hsqldb/lob/unused`
Query:
````sql
call SYSTEM_LOBS.DELETE_UNUSED_LOBS(?,?)
````

### Merge empty blocks

URL: `PUT /hsqldb-api/system/hsqldb/lob/blocks/mergeEmpty`
Query:
````sql
call SYSTEM_LOBS.MERGE_EMPTY_BLOCKS()
````

## Commandline

It is also possible to execute there queries using the HSQLDB commandline:

- Download the HSQLDB distribution
- Create a file called `tomee-HSQLDB.rc`:

````
# tomee-HSQLDB.rc
 
urlid tomee
url jdbc:hsqldb:file:data/hsqldb/hsqldb
username sa
password

urlid tomee-readonly
url jdbc:hsqldb:file:data/hsqldb/hsqldb;readonly=true
username sa
password
````

- Create a connection using one of the following two commands:

````
java -jar $HSQLDB_HOME/lib/sqltool.jar --rcFile=tomee-HSQLDB.rc tomee
java -jar $HSQLDB_HOME/lib/sqltool.jar --rcFile=tomee-HSQLDB.rc tomee-readonly
````

## Links

- http://www.hsqldb.org/doc/2.0/guide/management-chapt.html#mtc_large_objects
