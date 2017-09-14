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

3. Pagination support
------------------------
- Input (query parameters)
  - d.meta=true/false 
  - d.start=0
  - d.fetchsize=10
  - d.orderby=jsonfield1,jsonfield2
  - d.orderdesc=true/false
  
  http://127.0.0.1:8080/jsoncrudrest/samples/v1/sample_users?d.meta=true&d.fetchsize=10
  
  
- Output (response with 'd.meta=true' in request query params)
  {
    "d.meta":{
    	"d.start":0,
    	"d.total":100,
    	"d.fetchsize":10,
    	"d.orderby":"jsonfield1,jsonfield2",
    	"d.orderdesc"=false}
    "d.result":[{},{}]
  }
  
  
4. Retrieve/GET query parameter filters keyword

   4.1 Case Sensitive
   		4.1.1 <jsonname>.from=<value>
   		4.1.2 <jsonname>.to=<value>
   		4.1.3 <jsonname>.contain=<value>
   		4.1.4 <jsonname>.startwith=<value>
   		4.1.5 <jsonname>.endwith=<value>
   		
   4.2 Case Insensitive
   		4.2.1 <jsonname>.from.ci=<value>
   		4.2.2 <jsonname>.to.ci=<value>
   		4.2.3 <jsonname>.contain.ci=<value>
   		4.2.4 <jsonname>.startwith.ci=<value>
   		4.2.5 <jsonname>.endwith.ci=<value>
   		4.2.6 <jsonname>.ci=<value>

```
