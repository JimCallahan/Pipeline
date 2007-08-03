
<FORM action="post.php" method="POST">

<?php 
{
  $auth_html = 
    ('  <INPUT name="task_id" value="' . $tid . '" type="hidden">' . "\n" .
     '  <INPUT name="auth_id" value="' . $auth_id . '" type="hidden">' . "\n" .
     '  <INPUT name="auth_name" value="' . $auth_name . '" type="hidden">' . "\n");
  
  $auth_html .= '  <INPUT name="task_list" value="'; 
  foreach($tids as $ntid)
    $auth_html .= ($ntid . " "); 
  $auth_html .= '" type="hidden">'; 

  print($auth_html . "\n");
}
?>

<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0">
  <TR> 
    <TH class="theader" align="left" nowrap="nowrap">
      <SPAN class="genbig">&nbsp;&nbsp;New Comment:&nbsp;</SPAN> 
    </TH> 
  </TR>
    
  <TR>
    <TD class="row1" align="center">
      <SPAN class="gen">
        <TEXTAREA name="message" rows="25" wrap="virtual" style="width: 99%;" 
                  tabindex="3" class="post"><?php print($_REQUEST["message"]); ?></TEXTAREA>
      </SPAN>
    </TD>
  </TR>
    
  <TR>	
    <TH class="theader" align="center" height="15">
      <INPUT name="mode" value="post_comment" type="hidden">
      <INPUT class="liteoption" value="Post Comment" type="submit">
    </TH>
  </TR>
</TABLE>
</FORM>

<DIV style="height: 5px;"></DIV>

