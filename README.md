# Getting Started
```
1. Download and setup 
   1.1 Apache tomcat application server from https://tomcat.apache.org/download-80.cgi
   1.2 Postgres database server from https://www.postgresql.org/download/
2. Download https://github.com/huilam/jsoncrudrest/blob/master/releases/jsoncrudrest.war and deploy to tomcat 
3. Create sample application database schema by execute "test/01-create-schema.sql"
4. Make sure "test/jsoncrud.properties" is in classpath
5. Uses REST client such as PostMan or Web Browser to test the REST API
```

# Sample Endpoints
```
1. GET(retrieve list), POST(create)
---------------------------------
- http://127.0.0.1:8080/jsoncrudrest/jsoncrud_cfg
- http://127.0.0.1:8080/jsoncrudrest/jsoncrud_cfg_values

2. GET(retrieve), PUT(update), DELETE(delete)
-------------------------------------------
- http://127.0.0.1:8080/jsoncrudrest/jsoncrud_cfg/{id}

3. Optional Pagination, Filtering, Sorting & Returns support thru query parameters
--------------------------------------------------------------------------
 3.1 Filters
   - Syntax : filters=<json-attrname>.<modifier>.<operator>
     <modifier> : not, ci (case insensitive) 
     <operator> : startwith, endwith, contain, from, to, in
   - example : ?filters=jsonfield1.not.contain:xxx,jsonfield2.from:2017,jsonfield3.ci.startwith=AAA,jsonfield4.in=a;b;c
   
 3.2 Sorting
   - Syntax : sorting=<json-attrname>.<direction>  
     <direction> : asc, desc
   - example : ?sorting=jsonfield1,jsonfield2.asc,jsonfield3.desc
 
 3.3 Pagination
   - example : ?pagination=start:1,fetchsize:3
   
 3.4 Returns
   - example : ?returns=jsonfield1,jsonfield2,jsonfield3
  
 
- Output
  {
    "meta":{
    	"start":1,
    	"total":100,
    	"fetchsize":3,
    	"sorting":"jsonfield1,jsonfield2.asc,jsonfield3.desc"
     }
    "result":[{jsonfield1},{jsonfield2},{jsonfield3}]
  }
  
