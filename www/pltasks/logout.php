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



<TABLE class="bg" width="100%" align="center" cellpadding="0" cellspacing="0" border="0"> 
  <TR><TD class="bg" colspan="3"><DIV style="height: 100px;"></DIV></TD></TR>	

  <TR><TD class="bg" width="200"></TD>
      <TD class="bg">


<FORM action="login.php" method="POST">
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0"> 
  <TR>	
    <TD align="center" class="spaceRow"><SPAN class="bold">
      User Logged Out.
    </SPAN></TD>
  </TR>

  <TR>
    <TD class="row2">
      <TABLE border="0" cellpadding="3" cellspacing="1" width="100%" class="row2">
        <TR>
          <TD colspan="7" align="center"><DIV style="height: 50px;"></DIV></TD>       
        </TR>

        <TR>
          <TD align="center"><DIV style="width: 50px;"></DIV></TD>  

          <TD align="center">
            <FORM action="login.php" method="POST">
              <INPUT class="mainoption" value="Login" type="submit"> 
            </FORM> 
          </TD>

          <TD align="center"><DIV style="width: 8px;"></DIV></TD>  

          <TD align="center">
            <FORM action="register.php" method="POST">
              <INPUT class="mainoption" value="Register" type="submit">   
            </FORM> 
          </TD>

          <TD align="center"><DIV style="width: 8px;"></DIV></TD>  

          <TD align="center">
            <FORM action="search.php" method="POST">
              <INPUT class="mainoption" value="Search" type="submit">   
            </FORM> 
          </TD>

          <TD align="center"><DIV style="width: 50px;"></DIV></TD>  
        </TR>

        <TR>
          <TD colspan="7" align="center"><DIV style="height: 50px;"></DIV></TD>          
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
