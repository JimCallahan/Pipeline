<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<HTML>

<?php $temerity_root = "../"; ?>

<HEAD>
  <META http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <TITLE> Task Search </TITLE>
  <LINK rel="stylesheet" type="text/css" href="<?php echo($temerity_root);?>stylesheet.css">
  <LINK rel="SHORTCUT ICON" href="<?php echo($temerity_root);?>favicon.ico">
</HEAD>
<BODY>


<?php 
{
  include($temerity_root . "common.php");

  //------------------------------------------------------------------------------------------
  // SQL QUERIES
  //------------------------------------------------------------------------------------------
  
  /* open SQL connection */ 
  include($temerity_root . "pltasks/db-config.php");
  include($temerity_root . "dbopen.php");

  
  if(isset($_REQUEST["mode"])) {
    switch($_REQUEST["mode"]) { 
    case 'newuser':
      {
        if(!isset($_REQUEST["username"]) || ($_REQUEST["username"] == ""))
          show_error("Entry Error:", 
                     "No username was provided!"); 
          
        if(!isset($_REQUEST["password"]) || ($_REQUEST["password"] == ""))
          show_error("Entry Error:", 
                     "No password was provided!"); 
          
        if(!isset($_REQUEST["confirm"]) || ($_REQUEST["confirm"] == ""))
          show_error("Entry Error:", 
                     "The password was not confirmed!"); 

        if($_REQUEST["password"] != $_REQUEST["confirm"]) 
          show_error("Entry Error:", 
                     "The original and confirmed password did not match!"); 

        /* make sure the username is not already taken */ 
        {
          $sql = ("SELECT idents.ident_name as `username` " .  
                  "FROM idents, auth " . 
                  "WHERE idents.ident_id = auth.ident_id " . 
                  "AND idents.ident_name = '" . $_REQUEST["username"] . "'"); 
          $result = mysql_query($sql)
            or show_sql_error($sql);
    
          if($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
            $auth_failure_msg = 
              "Username (" . $row["username"] . ") was already taken!";
            
            /* page body */ 
            {
              ob_start();
        
              include ($temerity_root . "pltasks/auth-failure.php");
              
              $body_contents = ob_get_contents();
              ob_end_clean();
            }        
            
            print($body_contents);
            
            /* abort early from calling context! */ 
            exit();
          }
        }

        /* lookup (or create) the ident_id for the user */ 
        $ident_id = NULL;
        {
          {
            $sql = ("SELECT ident_id FROM idents " . 
                    "WHERE ident_name = '" . $_REQUEST["username"] . "'"); 
            $result = mysql_query($sql)
              or show_sql_error($sql);
            
            if($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
              $ident_id = $row['ident_id'];
          }
          
          if($ident_id == NULL) {
            $sql = ("INSERT INTO idents (ident_name, is_group) " . 
                    "VALUES ('" . $_REQUEST["username"] . "', 0)");  
            $result = mysql_query($sql)
              or show_sql_error($sql);
          }
          
          {
            $sql = ("SELECT ident_id FROM idents " . 
                    "WHERE ident_name = '" . $_REQUEST["username"] . "'"); 
            $result = mysql_query($sql)
              or show_sql_error($sql);
            
            if($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
              $ident_id = $row['ident_id'];
          }
        }

        /* store the encrypted password */ 
        { 
          $sql = ("INSERT INTO auth (ident_id, password) " . 
                  "VALUES (" . $ident_id . ", '" . crypt($_REQUEST["password"]) . "')");  
          $result = mysql_query($sql)
            or show_sql_error($sql);
        }

        $auth_name = $_REQUEST["username"];
        $auth_id   = $ident_id; 

        /* page body */ 
        {
          ob_start();
          
          include ($temerity_root . "pltasks/registered.php");
          
          $body_contents = ob_get_contents();
          ob_end_clean();
        }        
        
        print($body_contents);
        
        /* abort early from calling context! */ 
        exit();
      }
    }
  }
  
  /* close SQL connection */ 
  include($temerity_root . "dbclose.php");
}
?> 


<TABLE class="bg" width="100%" align="center" cellpadding="0" cellspacing="0" border="0"> 
  <TR><TD class="bg" colspan="3"><DIV style="height: 100px;"></DIV></TD></TR>	

  <TR><TD class="bg" width="200"></TD>
      <TD class="bg">


<FORM action="register.php" method="POST">
<INPUT name="mode" value="newuser" type="hidden">
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0"> 
  <TR>	
    <TD align="center" class="spaceRow"><SPAN class="bold">
      Please enter a new username and password.
    </SPAN></TD>
  </TR>

  <TR>
    <TD class="row2">
      <TABLE border="0" cellpadding="3" cellspacing="1" width="100%" class="row2">
        <TR>
          <TD colspan="2" align="center"><DIV style="height: 50px;"></DIV></TD>       
        </TR>

        <TR>
          <TD width="45%" align="right"><span class="gen">Username:</span></TD>
          <TD><INPUT type="text" name="username" size="25" maxlength="40" value="" /></TD>
        </TR>

        <TR>
          <TD colspan="2" align="center"><DIV style="height: 20px;"></DIV></TD>       
        </TR>

        <TR>
          <TD align="right"><span class="gen">Password:</span></TD>
          <TD><INPUT type="password" name="password" size="25" maxlength="32" /></TD>
        </TR>

        <TR>
          <TD align="right"><span class="gen">Confirm Password:</span></TD>
          <TD><INPUT type="password" name="confirm" size="25" maxlength="32" /></TD>
        </TR>

        <TR>
          <TD colspan="2" align="center"><DIV style="height: 25px;"></DIV></TD>          
        </TR>

        <TR>
          <TD colspan="2" align="center">
            <INPUT class="mainoption" value="Register" type="submit">  
          </TD>
        </TR>

        <TR>
          <TD colspan="2" align="center"><DIV style="height: 25px;"></DIV></TD>          
        </TR>
      </TABLE>
    </TD>
  </TR>
</TABLE>
</FORM>


    </TD>
    <TD class="bg" width="200"></TD>
  </TR>	
  <TR><TD class="bg" colspan="3"><DIV style="height: 100px;"></DIV></TD></TR>
</TABLE>	


</BODY>
