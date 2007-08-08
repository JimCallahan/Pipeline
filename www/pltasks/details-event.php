  <?php 
  {
    $enode_cnt = 0;
    if($e['nodes'] != NULL) {
      foreach($e['nodes'] as $node) {
        if($node['is_edit'] || $node['is_focus']) 
          $enode_cnt++;
      }
    }

    $has_enodes = ($enode_cnt > 0);
  }
  ?>

  <TR>
    <TD nowrap class="<?php echo($row_color);?>" align="center" 
        rowspan="2" width="100" height="80">
      <SPAN class="bold">
      <?php 
      {
        if($e['new_active'] != NULL) 
          print('&nbsp;' . $e['new_active'] . '&nbsp;<BR>');
        
        if($e['new_status'] != NULL) 
          print('&nbsp;' . $e['new_status'] . '&nbsp;<BR>');
      }
      ?>
      </SPAN>
      by&nbsp;<SPAN class="bold"><?php echo($e['name']);?></SPAN>
    </TD>  

    <TD class="<?php echo($row_color);?>" colspan="<?php echo($has_enodes ? 1 : 2);?>" 
        align="left" valign="top" width="350" height="15">
      <SPAN class="gensmall">Posted on <?php echo($e['stamp']);?></SPAN>
    </TD>  

    <?php
    {
      if($has_enodes) {
        print("\n" . 
              '<TD class="' . $row_color . '" rowspan="2" align="left" valign="top">' . "\n" .

              '<SPAN class="gensmall"><SPAN class="redbold">' . "\n" .
              '<TABLE class="' . $row_color .'" ' . 
                     'cellpadding="0" cellspacing="0" border="0">' . "\n" .
              '<TR><TD><DIV style="height: 8px;"></DIV></TD></TR>' . "\n");

        $thumb_rows = array();
        {
          $max_width = 450; 
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
            print('<TD class="' . $row_color .'" align="center" valign="middle">' . 
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

        print('<TR><TD><DIV style="height: 8px;"></DIV></TD></TR>' . "\n" .
              '</TABLE>' . "\n" .
              '</SPAN></SPAN>' . "\n");

        print('  <TABLE class="bg" width="100%" align="center" cellpadding="4" ' . 
              'cellspacing="1" border="0">' . "\n"); 

        if($e['nodes'] != NULL) {
          foreach($e['nodes'] as $node) {
            if($node['is_edit'] || $node['is_focus']) {
              print('    <TR>' . "\n" . 
                    '      <TD class="' . $row_color . '" align="center" width="50">' . 
                    '<SPAN class="gensmall">');
          
            if($node['is_focus']) {
              $nname = end(explode("/", $node['name'])); 
              $script = viewFocusNodeScript($node['name'], $node['vid'], $nname); 

              print('<A href="' . $script . '"><SPAN class="redbold">Focus</SPAN></A> ');
            }

            if($node['is_edit']) 
              print('<SPAN class="bold">Edit</SPAN>'); 
            
            print('</SPAN></TD>' . "\n" . 
                  '      <TD class="' . $row_color . '" align="left">' . 
                  '<SPAN class="gensmall">&nbsp;' . $node['name'] . '</SPAN></TD>' . "\n" . 
                  '      <TD class="' . $row_color . '" align="center" width="50">' . 
                  '<SPAN class="gensmall">' . $node['vid'] . '</SPAN></TD>' . "\n" . 
                  '    </TR>' . "\n\n"); 
            }
          }
        }

        print('  </TABLE>' . "\n" .
              '</TD>' . "\n\n"); 
      }
    }
    ?>
  </TR>

  <TR>
    <TD class="<?php echo($row_color);?>" colspan="<?php echo($has_enodes ? 1 : 2);?>" align="left" valign="top"><?php echo($e['message']);?></TD>
  </TR>

