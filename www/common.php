<?php

/* show an error page */ 
function show_error($error_title, $error_message) 
{
  global $temerity_root;

  /* page body */ 
  {
    ob_start();

    include ($temerity_root . "error.php");

    $body_contents = ob_get_contents();
    ob_end_clean();
  }

  print($body_contents);

  /* abort early from calling context! */ 
  exit();
}


/* show an SQL error page */
function show_sql_error($query) 
{
  show_error("Database Query Failed:", 
	     "Could complete query because: " . 
	     "<UL><B>" . mysql_error() . "</B><BR><BR>" . 
	     "SQL = <CODE>" . $query . "</CODE></UL>");
}


?>
