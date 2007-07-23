

<TABLE class="bg" width="100%" align="center" cellpadding="0" cellspacing="0" border="0"> 
  <TR><TD class="bg" colspan="3"><DIV style="height: 100px;"></DIV></TD></TR>	

  <TR><TD class="bg" width="200"></TD>
      <TD class="bg">


<FORM action="search.php" method="POST">
<INPUT name="auth_id" value="<?php echo($auth_id);?>" type="hidden">
<INPUT name="auth_name" value="<?php echo($auth_name);?>" type="hidden">
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0"> 
  <TR>	
    <TD align="center" class="spaceRow"><SPAN class="bold">
      User Registered.
    </SPAN></TD>
  </TR>

  <TR>
    <TD class="row2">
      <TABLE border="0" cellpadding="3" cellspacing="1" width="100%" class="row2">
        <TR>
          <TD colspan="2" align="center"><DIV style="height: 50px;"></DIV></TD>       
        </TR>

        <TR>
          <TD align="center" colspan="3"><SPAN class="genbig">
            New user (<?php echo($auth_name);?>) registered successfully.
          </SPAN><TD>
        </TR>

        <TR>
          <TD colspan="3" align="center"><DIV style="height: 25px;"></DIV></TD>       
        </TR>

        <TR>
          <TD colspan="2" align="center">
            <INPUT class="mainoption" value="Continue" type="submit">  
          </TD>
        </TR>

        <TR>
          <TD colspan="2" align="center"><DIV style="height: 50px;"></DIV></TD>          
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
