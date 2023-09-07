# Getting Started
```
1. Download and setup 
   1.1 OpenJDK 11 : https://adoptium.net/temurin/releases/?version=11 
   1.2 Apache Tomcat 9 : https://tomcat.apache.org/download-90.cgi
   1.3 PostgreSQL (latest) : https://www.postgresql.org/download/
2. Setup JSONCrudRest WebApp 
   2.1 Download https://github.com/huilam/jsoncrudrest/blob/master/releases/jsoncrudrest.war 
   2.2 Deploy to Tomcat's "/webapps" folder 
3. Prepare Postges for Sample App
   3.1 Download PgAdmin (PostgreSQL Admin tool UI) : https://www.pgadmin.org/download/
   3.2 Connect PgAdmin to your PostgreSQL database server
   3.3 Execute "test/01-create-schema.sql" to create Sample application database schema  
4. Web Browser to test the REST API
   4.1 Frameowkr About : http://127.0.0.1:8080/hl-jsoncrudrest/about/framework
   4.2 Sample 'Echo' Endpoint : http://127.0.0.1:8080/hl-jsoncrudrest/echo/HELLO%20WORLD
   4.3 Sample Database Endpoint : http://127.0.0.1:8080/hl-jsoncrudrest/jsoncrud_cfg
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
  
