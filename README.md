# Getting Started
```
1. Download and setup 
   1.1 Apache tomcat application server from https://tomcat.apache.org/download-80.cgi
   1.2 Postgres database server from https://www.postgresql.org/download/
2. Deploy 'jsoncrudrest.war' to tomcat 
3. Create sample application database schema by execute "test/create-test-schema.sql"
4. Make sure "test/jsoncrud.properties" is in classpath
5. Uses REST client such as PostMan to test the REST 
```

# Sample Endpoints
```
1. GET(retrieve list), POST(create)
---------------------------------
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_users
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_roles
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_userroles
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_userattrs


2. GET(retrieve), PUT(update), DELETE(delete)
-------------------------------------------
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_users/{id}
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_roles/{id}
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_userroles/{id}
- http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_userattrs/{id}


3. Pagination, Filtering & Sorting support using query parameters
------------------------------------------------------------------
 3.1 Filters
   - Syntax : filters=<json-attrname>.<modifier>.<operator>
     <modifier> : not, ci (case insensitive) 
     <operator> : startwith, endwith, contain, from, to
   - example : ?filters=jsonfield1.not.contain:xxx,jsonfield2.from:2017,jsonfield3.ci.startwith=AAA
   
 3.2 Sorting
   - Syntax : sorting=<json-attrname>.<direction>  
     <direction> : asc, desc
   - example : ?sorting=jsonfield1,jsonfield2.asc,jsonfield3.desc
 
 3.3 Pagination
   - example : ?pagination=start:1,fetchsize:3
  
  
- Output (Pagination support)
  {
    "meta":{
    	"start":1,
    	"total":100,
    	"fetchsize":3,
    	"sorting":"jsonfield1,jsonfield2.asc,jsonfield3.desc"
     }
    "result":[{xxx},{yyy},{zzzz}]
  }
  
