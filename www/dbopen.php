<?php

  $link = mysql_connect($dbhost, $dbuser, $dbpasswd) 
    or show_error("Database Error:", 
		  "Cannot connect to the database because: " . 
		  "<UL><B>" . mysql_error() . "</B></UL>");
  
  mysql_select_db($dbname) 
    or show_error("Database Error:",
		  "Could not select database because: " . 
		  "<UL><B>" . mysql_error() . "</B></UL>");

?> 
