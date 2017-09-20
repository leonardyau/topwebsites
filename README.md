# topwebsites

<h2>Design</h2>
The web application is developed with Spring boot + Spring MVC + thymeleaf + mongodb

<h3>Features</h3>
Apart from the required function, the application has the following additional features:
<li> List all the imported websites
<li> Allow user to choose how many websites to show
<li> Responsive UI
<li> Exclusion website lists are cached for performance
<li> Show current exclusion lists and cache expiry time
<li> Record of files import history
<li> Settings to control the exclusion and import path

<h2>Hosting</h2>
The application is hosted on Azure cloud running as an AppService. The mongodb is hosted on mLab with Database as a Service.

<h2>How to run the application</h2>
To access the application, go to: http://manulifedemo.azurewebsites.net/ and log in as user/password

To upload file for the application to process, ftp to waws-prod-sn1-143.ftp.azurewebsites.windows.net with 
user id: "manulifedemo\manulifedemo" (both part) and password: manuLifedemo1

Put your CSV file to be imported under D:\home\workfile or alternatively you can set up the import path under
application settings.

<h2>Potential Enhancement</h2>
<li> More robust error handling of database exception (e.g. Possible to start up the application even database is down,
  failover handling of database)
<li> Automated UI test
<li> Use database to store the user credentials, separate different roles for users
