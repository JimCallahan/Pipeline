
  <TR>
    <TD class="<?php echo($row_color);?>" align="center" rowspan="2" height="80">
      <SPAN class="bold">
      <?php 
      {
        if($e['new_active'] != NULL) 
          print('&nbsp;' . $e['new_active'] . '&nbsp;<BR>');
        
        if($e['new_status'] != NULL) 
          print('&nbsp;' . $e['new_status'] . '&nbsp;');
      }
      ?>
      </SPAN>
      by <SPAN class="bold"><?php echo($e['name']);?></SPAN>
    </TD>  

    <TD class="<?php echo($row_color);?>" align="left" valign="top" colspan="3" height="15">
      <SPAN class="gensmall">Posted on <?php echo($e['stamp']);?></SPAN>
    </TD>  
  </TR>

  <TR>
    <TD class="<?php echo($row_color);?>" align="left" valign="top" colspan="3" 
        height="40"><PRE style="font-family: Verdana, Arial, Helvetica, sans-serif; "><?php echo($e['message']);?></PRE>

  <?php 
  {
    if($e['nodes'] != NULL) {
      print('<SPAN class="gensmall">' . "\n" . 
            '    <TABLE class="bg" width="100%" align="center" ' . 
                       'cellpadding="2" cellspacing="1" border="0"> ' . "\n"); 
    
      print('      <TR><TD class="' . $row_color . '" align="center" colspan="3">' . 
            'Pipeline Nodes</TD></TR>' . "\n");

      foreach($e['nodes'] as $node) {
        if($node['is_edit'] || $node['is_focus']) {
          print('<TR>' . "\n" . 
                '  <TD class="' . $row_color . '" align="center" width="50">' . "\n");
          
          if($node['is_edit']) {
            print("Edit"); 
          }
          
          if($node['is_focus']) {
            if($node['is_edit']) 
              print(", "); 
            print("Focus"); 
          }
          
          print('  </TD>' . "\n" . 
                '  <TD class="' . $row_color . '" align="left">&nbsp;' . "\n" . 
                $node['name'] . '</TD>' . "\n" . 
                '  <TD class="' . $row_color . '" align="center" width="50">' . "\n" . 
                $node['vid'] . '</TD>' . "\n" . 
                '</TR>' . "\n\n");   
        }
      }
    

      print('    </TABLE></SPAN>' . "\n");
    }
  }
  ?>

    </TD>  
  </TR>
