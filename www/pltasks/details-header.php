
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0">
  <TR> 
    <TH align="left" class="theader" colspan="3" nowrap="nowrap">
      <SPAN class="genbig" style="color:#b8112d;">
        &nbsp;&nbsp;<?php echo($t['title']);?>&nbsp;:&nbsp;<?php echo($t['type']);?> 
      </SPAN> 
    </TH> 
  </TR>
    
  <TR>
    <TD class="row1" align="right" width="100">
      Task&nbsp;Activity:&nbsp;
    </TD>

    <TD class="row2" align="center" width="150">
      <?php echo($t['activity']);?>
    </TD>

    <TD class="row3" rowspan="7" align="left" valign="top">
      <SPAN class="gensmall"><SPAN class="redbold">
      <TABLE class="row3" cellpadding="0" cellspacing="0" border="0">
      <TR><TD><DIV style="height: 8px;"></DIV></TD></TR>
      <?php
      {
        $events = $t['events'];

        $e = current($events);
        foreach($events as $et) {
          if($et['thumbnails'] && $et['nodes']) {
            $e = $et; 
            break;
          }
        }
        
        $thumb_rows = array();
        {
          $max_width = 600; 
          $total_width = 0; 

          $thumbs = array();
          foreach($e['thumbnails'] as $thumb) {
            $image = 'prod/repository' . $thumb['thumb_path'];
            list($width, $height, $type, $attr) = getimagesize($image);
            
            $tname = end(explode("/", $thumb['focus_name'])); 
            $tvid  = $thumb['focus_version']; 

            $script = viewFocusNodeScript($thumb['focus_name'], $tvid, $tname); 

            $thumbs[] = array('name'   => $tname, 
                              'vid'    => $tvid, 
                              'script' => $script, 
                              'image'  => $image, 
                              'width'  => $width, 
                              'height' => $height); 

            $total_width += $width; 
            if($total_width > $max_width) {
              $thumb_rows[] = $thumbs;
              $thumbs = array();
              $total_width = 0;
            }
          }
          if(count($thumbs) > 0) 
            $thumb_rows[] = $thumbs;
        }

        foreach($thumb_rows as $thumbs) {
          print('<TR>' . "\n" . 
                '<TD><DIV style="width: 8px;"></DIV></TD>' . "\n"); 
          
          foreach($thumbs as $thumb) {
            print('<TD align="center" valign="middle">' . 
                  '<A href="' . $thumb['script'] . '">' .
                  '<IMG alt="" src="' . $temerity_root . 'loadpng.php?' . 
                  'File=pltasks/' . $thumb['image'] . '&Bg=d3ceb8"></A>' . 
                  '</TD>' . "\n" . 
                  '<TD><DIV style="width: 12px;"></DIV></TD>' . "\n"); 
          }
          print('</TR>' . "\n\n");

          print('<TR>' . "\n" . 
                '<TD><DIV style="width: 8px;"></DIV></TD>' . "\n"); 

          foreach($thumbs as $thumb) {
            print('<TD align="center" valign="middle">' . $thumb['name'] . '</TD>' . "\n" .
                  '<TD><DIV style="width: 12px;"></DIV></TD>' . "\n"); 
          }
          print('</TR>' . "\n\n");
        }
      }
      ?>
      <TR><TD><DIV style="height: 8px;"></DIV></TD></TR>
      </TABLE>
      </SPAN></SPAN>
    </TD>
  </TR>
    
  <TR>
    <TD class="row1" align="right" width="100">
      Task&nbsp;Status:&nbsp;
    </TD>

    <TD class="row2" align="center" width="150">
      <?php echo($t['status']);?>
    </TD>
  </TR>

  <TR>
    <TD align="center" class="spaceRow" colspan="2" height="1"><DIV style="height: 1px;"><IMG src="<?php echo($temerity_root);?>images/spacer.gif" alt="" height="1" width="1"></DIV></TD>
  </TR>

  <TR>
    <TD class="row1" align="right" width="100">
      Assigned&nbsp;To:&nbsp;
    </TD>

    <TD class="row2" align="center" width="150">
      <?php 
      {
        if(($auth_name == "pipeline") && ($_REQUEST["mode"] != 'post_assign')) {
          print('<SPAN class="genmed">' . "\n" .  
                '<FORM action="post.php" method="POST">' . "\n" . 
                '<SELECT name="new_assigned_to" style="width:100%">' . "\n");
       
          foreach($users as $u) {
            print('<OPTION ');
         
            if($u['ident_name'] == $assigned) 
              print('selected');
            
            print(' value="' . $u['ident_id'] . '">' . 
                  $u['ident_name'] . '&nbsp;</OPTION>' . "\n");
          }
          
          foreach($groups as $g) {
            print('<OPTION ');
            
            if(('[' . $g['ident_name'] . ']') == $assigned) 
              print('selected');
            
            print(' value="' . $g['ident_id'] . '">[' . 
                  $g['ident_name'] . ']&nbsp;</OPTION>' . "\n");
          }
          
          print('</SELECT>' . "\n" .
                '</SPAN>' . "\n");
        }
        else {
          print($assigned);
        }
      }
      ?>
    </TD>
  </TR>

  <TR>
    <TD class="row1" align="right" width="100">
      Supervised&nbsp;By:&nbsp;
    </TD>

    <TD class="row2" align="center" width="150">
      <?php 
      { 
        if(($auth_name == "pipeline") && ($_REQUEST["mode"] != 'post_assign')) {
          print('<SPAN class="genmed">' . "\n" .  
                '<SELECT multiple size="7" name="new_supervised_by[]" ' . 
                'style="width:100%">' . "\n");
          
          foreach($users as $u) {
            print('<OPTION ');
         
            foreach($task_owners[$tid]['supervised_by_ids'] as $sid) {
              if($u['ident_id'] == $sid) 
                print('selected');
            }
         
            print(' value="' . $u['ident_id'] . '">' . 
                  $u['ident_name'] . '&nbsp;</OPTION>' . "\n");
          }
       
          foreach($groups as $g) {
            print('<OPTION ');
         
            foreach($task_owners[$tid]['supervised_by_ids'] as $sid) {
              if($g['ident_id'] == $sid) 
                print('selected');
            }            
         
            print(' value="' . $g['ident_id'] . '">[' . 
                  $g['ident_name'] . ']&nbsp;</OPTION>' . "\n");
          }
       
          print('</SELECT>' . "\n" .
                '</SPAN>' . "\n");
        }
        else {
          print($supervised);
        }
      }
      ?>
    </TD>
  </TR>

  <TR>
    <TD align="center" class="spaceRow" colspan="2" height="1"><DIV style="height: 1px;"><IMG src="<?php echo($temerity_root);?>images/spacer.gif" alt="" height="1" width="1"></DIV></TD>
  </TR>

  <TR>
    <TD class="row1" align="right" width="100">
      Last&nbsp;Modified:&nbsp;
    </TD>

    <TD class="row2" align="center" width="150">
      <?php echo($t['last_modified']);?>
    </TD>
  </TR>
    
  <TR>	
    <TH class="theader" colspan="3" align="center" height="15">

    <TABLE class="theader" width="100%" align="center" 
           cellpadding="2" cellspacing="1" border="0">
      <TR>
        
        <TH class="theader">&nbsp;</TH>

    <?php
    {
      $auth_html = 
        ('  <INPUT name="auth_id" value="' . $auth_id . '" type="hidden">' . "\n" .
         '  <INPUT name="auth_name" value="' . $auth_name . '" type="hidden">' . "\n" . 
         '  <INPUT name="task_id" value="' . $tid . '" type="hidden">' . "\n");

      $auth_html .= '  <INPUT name="task_list" value="'; 
      foreach($tids as $ntid)
        $auth_html .= ($ntid . " "); 
      $auth_html .= '" type="hidden">'; 
        
      if($show_details_buttons) {
        if($auth_name != NULL) {
          if($auth_name == 'pipeline')
            print('<TH class="theader" width="100">' . "\n" . 
                  $auth_html . "\n" . 
                  '  <INPUT name="mode" value="post_assign" type="hidden">' . "\n" .
                  '  <INPUT class="liteoption" value="Assign" type="submit">' . "\n" . 
                  '</FORM>' . "\n" . 
                  '</TH>' . "\n");
          
          $supervised_by = $task_owners[$tid]['supervised_by']; 
          if(($supervised_by != NULL) && in_array($auth_name, $supervised_by)) 
            print('<TH class="theader" width="100">' . "\n" . 
                  '<FORM action="post.php" method="POST">' . "\n" . 
                  $auth_html . "\n" . 
                  '  <INPUT name="mode" value="review" type="hidden">' . "\n" .
                  '  <INPUT class="liteoption" value="Review" type="submit">' . "\n" . 
                  '</FORM>' . "\n" . 
                  '</TH>' . "\n");
          
          print('<TH class="theader" width="100">' . "\n" . 
                '<FORM action="post.php" method="POST">' . "\n" . 
                $auth_html . "\n" . 
                '  <INPUT name="mode" value="comment" type="hidden">' . "\n" .
                '  <INPUT class="liteoption" value="Comment" type="submit">' . "\n" . 
                '</FORM>' . "\n" . 
                '</TH>' . "\n");
        }
      }
      else {      
        print('<TH class="theader" width="100">' . "\n" . 
              '<FORM action="details.php" method="POST">' . "\n" . 
              $auth_html . "\n" .         
              '  <INPUT class="liteoption" value="Back to Details" type="submit">' . "\n" . 
              '</FORM>' . "\n" . 
              '</TH>' . "\n");
      }
      
      print('<TH class="theader" width="100">' . "\n" . 
            '<FORM action="search.php" method="POST">' . "\n" . 
            $auth_html . "\n" .   
            '  <INPUT name="mode" value="repeat" type="hidden">' . "\n" . 
            '  <INPUT class="liteoption" value="Back to Search" type="submit">' . "\n" . 
            '</FORM>' . "\n" . 
            '</TH>' . "\n");
    }
    ?>
      
        <TH class="theader">&nbsp;</TH>
      </TR>
    </TABLE>

    </TH>
  </TR>

</TABLE>
</FORM>

<DIV style="height: 5px;"></DIV>
