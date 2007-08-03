
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
      <SPAN class="genbig">&nbsp;&nbsp;New Task Status:&nbsp;&nbsp;</SPAN> 

      <SELECT class="post" name="new_status">
      <?php
      {
        /* display different review options based on the current state of the task */ 
        switch($current_task_active) {
        case 1:  // Inactive
          foreach($task_status as $status) {
            switch($status['status_id']) {
            case 0:
              print('<OPTION selected value="0">None Selected</OPTION>' . "\n");
              break;

            case 4: // On Hold
              print('<OPTION value="' . $status['status_id'] . '">' . 
                    $status['status_name'] . '</OPTION>' . "\n"); 
            }
          }
          break;

        case 2:  // Active
          foreach($task_status as $status) {
            switch($status['status_id']) {
            case 0:
              print('<OPTION selected value="0">None Selected</OPTION>' . "\n");
              break;

            case 2: // Changes Required
            case 4: // On Hold
              print('<OPTION value="' . $status['status_id'] . '">' . 
                    $status['status_name'] . '</OPTION>' . "\n"); 
            }
          }
          break;
          
        case 3:  // Submitted
          foreach($task_status as $status) {
            switch($status['status_id']) {
            case 0:
              print('<OPTION selected value="0">None Selected</OPTION>' . "\n");
              break;

            case 2: // Changes Required
            case 3: // Approved
            case 4: // On Hold
            case 5: // Could Be Better
            case 6: // Finalled
              print('<OPTION value="' . $status['status_id'] . '">' . 
                    $status['status_name'] . '</OPTION>' . "\n"); 
            }
          }
        }
      }
      ?>
      </SELECT>

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
      <INPUT name="mode" value="post_review" type="hidden">
      <INPUT class="liteoption" value="Post Review" type="submit">
    </TH>
  </TR>
</TABLE>
</FORM>

<DIV style="height: 5px;"></DIV>

