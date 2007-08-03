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
        rowspan="2" width="120" height="80">
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
              '  <TABLE class="bg" width="100%" align="center" cellpadding="4" ' . 
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

