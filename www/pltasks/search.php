<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<HTML>

<?php $temerity_root = "../"; ?>

<HEAD>
  <META http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <TITLE> Pipeline Task Search </TITLE>
  <LINK rel="stylesheet" type="text/css" href="<?php echo($temerity_root);?>stylesheet.css">
  <LINK rel="SHORTCUT ICON" href="<?php echo($temerity_root);?>favicon.ico">
</HEAD>
<BODY>


<FORM action="etc/phpinfo.php" method="POST">

<TABLE class="frame" width="100%" cellpadding="4" cellspacing="1" border="0">
  <TR>	
    <TD class="spaceRow" colspan="8" height="1">
      <IMG src="search.php_files/spacer.gif" alt="" height="1" width="1">
    </TD>
  </TR>

  <TR>
    <TD class="row1" align="right"><SPAN class="gen">
      Supervised By:&nbsp;
    </SPAN></TD> 

    <TD class="row2"><SPAN class="genmed">
      <SELECT multiple size="7" name="supervised_by[]">
        <OPTION selected label="0" value="0">All Types&nbsp;</OPTION>
        <OPTION label="1" value="1">bob&nbsp;</OPTION>
        <OPTION label="2" value="2">fred&nbsp;</OPTION>
        <OPTION label="3" value="3">julie&nbsp;</OPTION>
        <OPTION label="4" value="4">kevin&nbsp;</OPTION>
        <OPTION label="5" value="5">todd&nbsp;</OPTION>
        <OPTION label="6" value="6">[Modeling]&nbsp;</OPTION>
        <OPTION label="7" value="7">[Effects]&nbsp;</OPTION>
        <OPTION label="8" value="8">[Editorial]&nbsp;</OPTION>
      </SELECT>
    </SPAN></TD>

    <TD class="row1" align="right"><SPAN class="gen">
      Task Status:&nbsp;
    </SPAN></TD>

    <TD class="row2"><SPAN class="genmed">
      <SELECT multiple size="7" name="task_status[]">
        <OPTION selected label="0" value="0">All Types&nbsp;</OPTION>
        <OPTION label="1" value="1">Unapproved&nbsp;</OPTION>
        <OPTION label="2" value="2">Changes Required&nbsp;</OPTION>
        <OPTION label="3" value="3">Approved&nbsp;</OPTION>
        <OPTION label="4" value="4">On Hold&nbsp;</OPTION>
        <OPTION label="5" value="5">Could Be Better&nbsp;</OPTION>
        <OPTION label="6" value="6">Finalled&nbsp;</OPTION>
      </SELECT>
    </SPAN></TD>

    <TD class="row1" align="right"><SPAN class="gen">
      Assigned To:&nbsp;
    </SPAN></TD>

    <TD class="row2"><SPAN class="genmed">
      <SELECT multiple size="7" name="assigned_to[]">
        <OPTION selected label="0" value="0">All Types&nbsp;</OPTION>
        <OPTION label="1" value="1">bob&nbsp;</OPTION>
        <OPTION label="2" value="2">fred&nbsp;</OPTION>
        <OPTION label="3" value="3">julie&nbsp;</OPTION>
        <OPTION label="4" value="4">kevin&nbsp;</OPTION>
        <OPTION label="5" value="5">todd&nbsp;</OPTION>
        <OPTION label="6" value="6">[Modeling]&nbsp;</OPTION>
        <OPTION label="7" value="7">[Effects]&nbsp;</OPTION>
        <OPTION label="8" value="8">[Editorial]&nbsp;</OPTION>
      </SELECT>
    </SPAN></TD>

    <TD class="row1" align="right"><SPAN class="gen">
      Task Activity:&nbsp;
    </SPAN></TD> 

    <TD class="row2"><SPAN class="genmed">
      <SELECT multiple size="7" name="task_activity[]">
        <OPTION selected label="0" value="0">All Types&nbsp;</OPTION>
        <OPTION label="1" value="1">Inactive&nbsp;</OPTION>
        <OPTION label="2" value="2">Active&nbsp;</OPTION>
        <OPTION label="3" value="3">Submitted&nbsp;</OPTION>
      </SELECT>
    </SPAN></TD>        
  </TR>

  <TR>	
    <TD class="spaceRow" colspan="8" height="1">
      <IMG src="search.php_files/spacer.gif" alt="" height="1" width="1">
    </TD>
  </TR>

  <TR>
    <TD class="row2" align="center"><SPAN class="gen">
      <INPUT class="liteoption" value="Search" type="submit">
    </TD> 

    <TD class="bg" colspan="8" rowspan="7">

    


    </TD>      	
  </TR>

  <TR>	
    <TD class="spaceRow" colspan="1" height="1">
      <IMG src="search.php_files/spacer.gif" alt="" height="1" width="1">
    </TD>
  </TR>

  <TR>
    <TD class="row1" align="center"><SPAN class="gen">
      Task Type:&nbsp;
    </SPAN></TD>  
  </TR>

  <TR>
    <TD class="row2" align="right"><SPAN class="genmed">
      <SELECT multiple size="9" name="task_type[]">
        <OPTION selected label="0" value="0">All Types&nbsp;</OPTION>
        <OPTION label="1" value="1">Modeling&nbsp;</OPTION>
        <OPTION label="2" value="2">Rigging&nbsp;</OPTION>
        <OPTION label="3" value="3">LookDev&nbsp;</OPTION>
        <OPTION label="4" value="4">Layout&nbsp;</OPTION>
        <OPTION label="5" value="5">Animation&nbsp;</OPTION>
        <OPTION label="6" value="6">Effects&nbsp;</OPTION>
        <OPTION label="7" value="7">Lighting&nbsp;</OPTION>
        <OPTION label="8" value="8">Compositing&nbsp;</OPTION>
      </SELECT>
    </SPAN></TD>       
  </TR>

  <TR>	
    <TD class="spaceRow" colspan="1" height="1">
      <IMG src="search.php_files/spacer.gif" alt="" height="1" width="1">
    </TD>
  </TR>

  <TR>
    <TD class="row1" align="center"><SPAN class="gen">
      Task Name:&nbsp;
    </SPAN></TD>
  </TR>

  <TR>
    <TD class="row2" align="right"><SPAN class="genmed">
      <SELECT multiple size="50" name="task_title[]">
        <OPTION selected label="0" value="0">All Tasks&nbsp;</OPTION>
        <OPTION label="1" value="1">Bob&nbsp;</OPTION>
        <OPTION label="2" value="2">Fred&nbsp;</OPTION>
        <OPTION label="3" value="3">Shot2_12&nbsp;</OPTION>
        <OPTION label="4" value="4">Shot2_13&nbsp;</OPTION>
        <OPTION label="5" value="5">Shot3_1&nbsp;</OPTION>
        <OPTION label="6" value="6">Shot4_1&nbsp;</OPTION>
        <OPTION label="7" value="7">Shot4_1&nbsp;</OPTION>
      </SELECT>
    </SPAN></TD>
  </TR>

  <TR>	
    <TD class="spaceRow" colspan="8" height="1">
      <IMG src="search.php_files/spacer.gif" alt="" height="1" width="1">
    </TD>
  </TR>
</TABLE>

</FORM>





























</BODY>
