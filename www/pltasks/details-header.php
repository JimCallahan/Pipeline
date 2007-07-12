<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0">
  <TR> 
    <TH align="left" class="theader" colspan="4" nowrap="nowrap">
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

    <TD class="row3" rowspan="7" colspan="2" align="left">

      thumbnails go here...  

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
    <TD align="center" class="spaceRow" colspan="4" height="1"><DIV style="height: 1px;"><IMG src="<?php echo($temerity_root);?>images/spacer.gif" alt="" height="1" width="1"></DIV></TD>
  </TR>
