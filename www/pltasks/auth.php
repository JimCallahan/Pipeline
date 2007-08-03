<?php

function authenticate()
{
  global $temerity_root;
  global $auth_name; 
  global $auth_id; 

  if(isset($_REQUEST["auth_name"]) && ($_REQUEST["auth_name"] != "") && 
     isset($_REQUEST["auth_id"]) && ($_REQUEST["auth_id"] != "")) {
    
    $auth_name = $_REQUEST['auth_name'];
    $auth_id   = $_REQUEST['auth_id'];
  }
  
  else if(isset($_REQUEST["username"]) && ($_REQUEST["username"] != "") && 
          isset($_REQUEST["password"]) && ($_REQUEST["password"] != "")) {
    
    $sql = ("SELECT idents.ident_name as `auth_name`, " .  
            "idents.ident_id as `auth_id`, " . 
            "auth.password as `password` " . 
            "FROM idents, auth " . 
            "WHERE idents.ident_id = auth.ident_id " . 
            "AND idents.ident_name = '" . $_REQUEST["username"] . "'"); 

    $result = mysql_query($sql)
      or show_sql_error($sql);
    
    if($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
      if(crypt($_REQUEST["password"], $row['password']) == $row['password']) {
        $auth_name = $row['auth_name'];
        $auth_id   = $row['auth_id'];
      }
    }
  }

  {
    $html = 
      ('<SPAN class="genbig">' . "\n" .   
       '<TABLE class="frame" width="100%" align="center" ' . 
       'cellpadding="0" cellspacing="1" border="0">' . "\n" .   
       '  <TR><TD class="row2"><TABLE class="row2" width="100%" align="center" ' . 
       'cellpadding="4" cellspacing="1" border="0">' . "\n" .   
       '    <TR>' . "\n" .   
       '      <TD class="row2" align="left" width="300">'); 
    
    if(($auth_name != NULL) && ($auth_id > 0))
      $html .= 
        ('&nbsp;&nbsp;Welcome:&nbsp;&nbsp;<SPAN class="bold">' . $auth_name . '</SPAN>'); 
    
    $html .= 
      ('</TD>' . "\n\n" .
       '      <TD class="row2" align="right" width="300"><SPAN class="redbold">'); 
    
    if(($auth_name != NULL) && ($auth_id > 0))
      $html .= ('<A href="' . $temerity_root . 'pltasks/logout.php">Logout</A>'); 
    else 
      $html .= ('<A href="' . $temerity_root . 'pltasks/login.php">Login</A>'); 
    
    $html .= 
      ('</SPAN>&nbsp;|&nbsp;<SPAN class="redbold">' . 
       '<A href="' . $temerity_root . 'pltasks/register.php">Register</A>'); 
    
    $html .= 
      ('&nbsp;&nbsp;</SPAN></TD>' . "\n" .
       '    </TR>' . "\n" .  
       '  </TABLE></TD></TR>' . "\n" .   
       '</TABLE>' . "\n" .   
       '</SPAN>' . "\n\n"); 
    
    return $html;
  }
}

?>
