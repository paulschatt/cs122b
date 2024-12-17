# Project 5
Throughput 1: roughly 5000/min
Throughput 2: roughly 7000/min

Video Url: https://www.youtube.com/watch?v=J0CVaHQycxw
# Project 4
Url for video demo Project 4: https://youtu.be/AnFAZCgPoz8

## General
## Connection Pooling
  ### Files using connection pooling:
  * WebContent/META_INF/context.xml (Declaration of connections as resources)
  * src/*  (lookup connections from connection pool to query database)

  ### Utilization of connection pooling:
  Everytime the database needs to be queried, instead of calling DriverManager.getConnection(), which creates a new connection everytime, we select an existing connection from the connection pool: **((DataSource) new InitialContext().lookup("java:comp/env/jdbc/readconnect")).getConnection()**, which is much faster;

  ### Connection pooling with two backend instances:
  To add a second backend instance, we just add another **Resource** with a different name and url to **context.xml**. When we want to get a connection in our source code, we still need to decide which instance to query (lookup by name). Alternatively, we can create a DB class, which selects one instance randomly for us or routes all writes to one instance and all reads to another (more on that in the Master/Slave section) Because the instance adresses vary depending on the environment and we don't want to change them the whole time, the connection pool setup depends on the environment too. By setting a **DB_ENV** environment variable, we can specify whether we want to use the local or AWS instances. In **context.xml** the **Environment** tag checks for **DB_ENV** and assigns the connections based on its value ("dev" or "prod")
  
## Master/Slave
  ### Config files for replication:
  * WebContent/META_INF/context.xml (Establish one read connection and one write connection)
  * src/dbconnector.java (Route all writes to the write connection, route reads randomly to both read and write connection)

  ### How does the routing work?
  Created class **dbconnector.java** to abstract. When application code wants to read from a DB it calls dbconnector.getReadConnection() which randomly selects one of the two instances. When application code wants to write, it calls dbconnector.getWriteConnection(), which always returns the write instance (which refers to the Master Instance of our replication configuration). The master instance will replicate the write to the Slave / read-only instance. When running locally, formally there are two different connections but they refer to the same DB. Only on AWS("production") there are two databases which enable replication.
  
Url for video demo Project 3: https://youtu.be/SN-_f_Cwo4M

Contributions

Oz Anisman: 

*  HTTPS
*  Dashboard
*  Prepared Statements

Paul Schatt:

*  reCAPTCHA
*  XML Parser
*  Encrypted Passwords

Optimization Techniques for XML Parser:

1) I mostly found it necessary to read from the stars table, because in the casts.xml there were a lot of stars that already were in the movie database. To speed these reads up, we created an index on stars.name

To make writes faster, which is the bulk of the workload we applied two techniques:

2) Batch Statement Processing, being the single most important contribution to speed up
3) increasing buffer size (global variable innodb_buffer_pool_size)

Overall on a local machine, the program went from running over 20 minutes to about 20 seconds.

Url for video demo Project 2: https://youtu.be/rVM1nGZMrw0

Contributions

Oz Anisman:

* Search

  Search is using "LIKE '%X%'" in src/SearchServlet.java to find matching movie titles, directors, and stars
* Pagination
* Sorting

Paul Schatt:
* Shopping Cart & Payment
* Browsing
* Single Movie / Single Star Page Requirements


Url for video demo Project 1: https://youtu.be/MgTQltA3xCA

Contributions

Oz Anisman:

* Single star and single movie pages and servlets
* api links for movies and stars

Paul Schatt:
* Top movies page and servlet
* Navigation bar
* Data table CSS and dark color scheme design
