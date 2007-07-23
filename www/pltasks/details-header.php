<FORM action="post.php" method="POST">
<INPUT name="task" value="<?php echo($tid);?>" type="hidden">
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0">
  <TR> 
    <TH align="left" class="theader" colspan="3" nowrap="nowrap">
      <SPAN class="genbig" style="color:#b8112d;">&nbsp;&nbsp; 
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
        
        $thumb_rows = array();
        {
          $max_width = 600; 
          $total_width = 0; 

          $thumbs = array();
          foreach($e['thumbnails'] as $thumb) {
            $image = 'prod/repository' . $thumb['thumb_path'];
            list($width, $height, $type, $attr) = getimagesize($image);
            
            $thumbs[] = array('name'    => end(explode("/", $thumb['focus_name'])), 
                              'version' => $thumb['focus_version'],  
                              'image'   => $image, 
                              'width'   => $width, 
                              'height'  => $height); 

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
                  '<A HREF="temp-focus-script.plpython">'. 
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
      <?php echo($assigned);?>
    </TD>
  </TR>

  <TR>
    <TD class="row1" align="right" width="100">
      Supervised&nbsp;By:&nbsp;
    </TD>

    <TD class="row2" align="center" width="150">
      <?php echo($supervised);?>
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
      <INPUT class="liteoption" value="Post" type="submit">
    </TH>
  </TR>
</TABLE>
</FORM>

<DIV style="height: 5px;"></DIV>

<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0">
