
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0">
  <TR> 
    <TH class="theader" colspan="2" align="left" nowrap="nowrap">
      <SPAN class="genbig">&nbsp;&nbsp;Approval&nbsp;Builder:&nbsp;&nbsp;
        <SPAN class="redbold">
          <?php print($approval_builder_result ? "Failed" : "Finished"); ?>
        </SPAN>
      </SPAN>
    </TH> 
  </TR>
    
  <TR>
    <TD class="row1" align="right" width="100">
      &nbsp;Command:&nbsp;
    </TD>

    <TD class="row2" align="left" valign="top">
      <SPAN class="gen">
       <?php print("<P>" . $approval_builder_cmdline . "<P>"); ?> 
      </SPAN>
    </TD>
  </TR>

  <?php 
  {
    if($approval_builder_output != NULL) {
      print('<TR>' . "\n" .
            '  <TD class="row1" align="right">' . "\n" .
            '    Builder&nbsp;Output:&nbsp;' . "\n" .
            '  </TD>' . "\n" .
            '  <TD class="row2" align="left" valign="top">' . "\n" .
            '    <SPAN class="gen"><PRE>' . $approval_builder_output . '</PRE></SPAN>' ."\n".
            '  </TD>' . "\n" .
            '</TR>' . "\n");
    }

    if($approval_builder_errors != NULL) {
      print('<TR>' . "\n" .
            '  <TD class="row1" align="right">' . "\n" .
            '    Builder&nbsp;Errors:&nbsp;' . "\n" .
            '  </TD>' . "\n" .
            '  <TD class="row2" align="left" valign="top">' . "\n" .
            '    <SPAN class="gen"><PRE>' . $approval_builder_errors . '</PRE></SPAN>' ."\n".
            '  </TD>' . "\n" .
            '</TR>' . "\n");
    }

    $auth_html = 
      ('  <INPUT name="task_id" value="' . $tid . '" type="hidden">' . "\n" .
       '  <INPUT name="auth_id" value="' . $auth_id . '" type="hidden">' . "\n" .
       '  <INPUT name="auth_name" value="' . $auth_name . '" type="hidden">' . "\n");
    
    $auth_html .= '  <INPUT name="task_list" value="'; 
    foreach($tids as $ntid)
      $auth_html .= ($ntid . " "); 
    $auth_html .= '" type="hidden">'; 
  }
  ?>

  <TR>	
    <TH class="theader" colspan="2" align="center" height="15">
    <?php            
    {
      if($approval_builder_result) {
        print('<FORM action="post.php" method="POST">' . "\n" . 
              $auth_html . "\n" . 
              '  <INPUT name="mode" value="retry_review" type="hidden">' . "\n" .
              '  <INPUT name="new_status_id" ' . 
              'value="' . $approval_info['new_status_id'] . '" type="hidden">' . "\n" .
              '  <INPUT name="new_note_id" ' . 
              'value="' . $approval_info['new_note_id'] . '" type="hidden">' . "\n" .
              '  <INPUT class="liteoption" value="Try Again" type="submit">' . "\n" . 
              '</FORM>' . "\n");
      }
    }
    ?>
    </TH>
  </TR>
</TABLE>

<DIV style="height: 5px;"></DIV>

