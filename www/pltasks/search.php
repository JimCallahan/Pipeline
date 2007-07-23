<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<HTML>

<?php $temerity_root = "../"; ?>

<HEAD>
  <META http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <TITLE> Task Search </TITLE>
  <LINK rel="stylesheet" type="text/css" href="<?php echo($temerity_root);?>stylesheet.css">
  <LINK rel="SHORTCUT ICON" href="<?php echo($temerity_root);?>favicon.ico">
</HEAD>
<BODY>


<?php
{
  include($temerity_root . "common.php");
  include($temerity_root . "pltasks/auth.php");

  //------------------------------------------------------------------------------------------
  // SQL QUERIES
  //------------------------------------------------------------------------------------------
  
  /* open SQL connection */ 
  include($temerity_root . "pltasks/db-config.php");
  include($temerity_root . "dbopen.php");
  
  /* authenticate the user */
  $authenicate_body = authenticate();

  /* get search tables */ 
  $users  = array();
  $groups = array();
  $assigned_select = array();
  $supervised_select = array();
  {
    $users[0] = array('ident_id'   => "0", 
                      'ident_name' => "*ANY*"); 
    $users[10000] = array('ident_id'   => "10000", 
                          'ident_name' => "*NONE*"); 

    $assigned_select[0]   = '';
    $supervised_select[0] = '';

    $sql = ("SELECT ident_id, ident_name FROM idents WHERE is_group = 0"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);

    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
      $assigned_select[$row['ident_id']]   = '';
      $supervised_select[$row['ident_id']] = '';
      $users[$row['ident_id']] = $row;
    }
  }
  {
    $sql = ("SELECT ident_id, ident_name FROM idents WHERE is_group = 1"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);
    
    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
      $assigned_select[$row['ident_id']]   = '';
      $supervised_select[$row['ident_id']] = '';
      $groups[$row['ident_id']] = $row;
    }
  }

  $task_status = array();
  {
    $task_status[0] = array('status_id'   => "0", 
                            'status_name' => "*ANY*"); 

    $sql = ("SELECT status_id, status_name FROM task_status"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);
    
    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
      $task_status[$row['status_id']] = $row;
  }
  
  $task_activity = array();
  {
    $task_activity[0] = array('active_id'   => "0", 
                              'active_name' => "*ANY*"); 

    $sql = ("SELECT active_id, active_name FROM task_activity"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);
    
    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
      $task_activity[$row['active_id']] = $row;
  }

  $task_titles = array();
  {
    $task_titles[0] = array('title_id'   => "0", 
                            'title_name' => "*ANY*"); 

    $sql = ("SELECT title_id, title_name FROM task_titles"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);

    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
      $task_titles[$row['title_id']] = $row;
  }
  
  $task_types = array();
  {
    $task_types[0] = array('type_id'   => "0", 
                           'type_name' => "*ANY*"); 

    $sql = ("SELECT type_id, type_name FROM task_types"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);
    
    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
      $task_types[$row['type_id']] = $row;
  }
  
  /* search results */ 
  $tasks = array();
  $task_owners = array();
  if(isset($_REQUEST["mode"])) {
    switch($_REQUEST["mode"]) { 
    case 'results':
      {
        /* search for matching task info (except supervised_by) */ 
        $task_owners = array();
        {
          $sql = ("SELECT tasks.task_id AS `tid`, " . 
                         "task_titles.title_name AS `title`, " .
                         "task_types.type_name AS `type`, " . 
                         "task_activity.active_name as `activity`, " .
                         "task_status.status_name AS `status`, " .
                         "tasks.assigned_to as `assigned_to`, " .
                         "tasks.last_modified as `last_modified` " .
                   "FROM tasks, task_titles, task_types, task_activity, task_status " .
                   "WHERE tasks.title_id = task_titles.title_id  " .
                   "AND tasks.type_id = task_types.type_id  " .
                   "AND tasks.status_id = task_status.status_id " .
                   "AND tasks.active_id = task_activity.active_id " .
                   "AND tasks.task_id = ANY ( SELECT tasks.task_id " . 
                                             "FROM tasks, supervisors "); 

          $first = true;

          {
            $first_sub = true;
            $has_any = false;
            foreach($_REQUEST['task_titles'] as $e) {
              switch($e) {
              case 0: 
                $has_any = true;
                break;
                
              default:
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' title_id = ' . $e . ' ');
                $first = false;
                $first_sub = false;
              }
              
              if(($e == 0) || !$has_any) 
                $task_titles[$e]['selected'] = 'selected';
            }

            if(!$first_sub) 
              $sql .= ")";
          }

          {
            $first_sub = true;
            $has_any = false;
            foreach($_REQUEST['task_types'] as $e) {
              switch($e) {
              case 0: 
                $has_any = true;
                break;
                
              default:
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' type_id = ' . $e . ' ');
                $first = false;
                $first_sub = false;
              }
              
              if(($e == 0) || !$has_any) 
                $task_types[$e]['selected'] = 'selected';
            }

            if(!$first_sub) 
              $sql .= ")";
          }

          {
            $first_sub = true;
            $has_any = false;
            foreach($_REQUEST['task_activity'] as $e) {
              switch($e) {
              case 0: 
                $has_any = true;
                break;

              default:
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' active_id = ' . $e . ' ');
                $first = false;
                $first_sub = false;
              }

              if(($e == 0) || !$has_any) 
                $task_activity[$e]['selected'] = 'selected';
            }

            if(!$first_sub) 
              $sql .= ")";
          }

          {
            $first_sub = true;
            $has_any = false;
            foreach($_REQUEST['task_status'] as $e) {
              switch($e) {
              case 0: 
                $has_any = true;
                break;

              default:
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' status_id = ' . $e . ' ');
                $first = false;
                $first_sub = false;
              }

              if(($e == 0) || !$has_any) 
                $task_status[$e]['selected'] = 'selected';
            }

            if(!$first_sub) 
              $sql .= ")";
          }

          {
            $first_sub = true;
            $has_any  = false;
            foreach($_REQUEST['assigned_to'] as $e) {
              switch($e) {
              case 0: 
                $has_any = true;
                break;

              case 10000: 
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' assigned_to IS NULL ');
                $first = false;
                $first_sub = false;
                break;

              default:
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' assigned_to = ' . $e . ' ');
                $first = false;
                $first_sub = false;
              }

              if(($e == 0) || !$has_any) 
                $assigned_select[$e] = 'selected';
            }

            if(!$first_sub) 
              $sql .= ")";
          }

          {
            $first_sub = true;
            $has_any = false;
            foreach($_REQUEST['supervised_by'] as $e) {
              switch($e) {
              case 0: 
                $has_any = true;
                break;
            
              case 10000: 
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' ( tasks.task_id = supervisors.task_id AND ' . 
                            'supervisors.ident_id IS NULL ) ');
                $first = false;
                $first_sub = false;            
                break;
            
              default:
                $sql .= (($first ? "WHERE (" : ($first_sub ? "AND (" : "OR")) . 
                         ' ( tasks.task_id = supervisors.task_id AND ' . 
                            'supervisors.ident_id = ' . $e . ' ) ');
                $first = false;
                $first_sub = false;
              }
          
              if(($e == 0) || !$has_any) 
                $supervised_select[$e] = 'selected';
            }

            if(!$first_sub) 
              $sql .= ")";
          }

          $sql .= " )";


          $result = mysql_query($sql)
            or show_sql_error($sql);

          while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
            $tasks[$row['tid']] = $row;
        }

        /* add names of assigned_to and supervised_by users/groups */ 
        {
          $has_any = false;
          foreach($_REQUEST['supervised_by'] as $e) {
            switch($e) {
            case 0: 
              $has_any = true;
              break;
            }

            if(($e == 0) || !$has_any) 
              $supervised_select[$e] = 'selected';
          }

        }

        foreach($tasks as $tid => $t) {
          if($t['assigned_to'] != NULL) {
            $sql = ("SELECT idents.ident_name AS `name`, " .  
                           "idents.is_group AS `is_group` " . 
                    "FROM idents " . 
                    "WHERE ident_id = " . $t['assigned_to']);
              
            $result = mysql_query($sql)
              or show_sql_error($sql);
          
            if($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
              $name = $row['name'];
              if($row['is_group']) 
                $name = '[' . $name . ']';
              $task_owners[$tid] = array('assigned_to' => $name); 
            }
          }

          {
            $sql = ("SELECT idents.ident_name AS `name`, " .  
                           "idents.is_group AS `is_group` " . 
                    "FROM supervisors, idents " . 
                    "WHERE supervisors.task_id = " . $tid . " " .
                    "AND supervisors.ident_id = idents.ident_id");

            $result = mysql_query($sql)
              or show_sql_error($sql);
          
            $supers = array();
            while($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
              $name = $row['name'];
              if($row['is_group']) 
                $name = '[' . $name . ']';
              $supers[] = $name;
            }

            $task_owners[$tid]['supervised_by'] = $supers; 
          }
        }
      }
    }
  }

  /* close SQL connection */ 
  include($temerity_root . "dbclose.php");
}


?> 


<TABLE class="bg" width="100%" align="center" cellpadding="0" cellspacing="0" border="0"> 
  <TR><TD class="bg" colspan="3"><DIV style="height: 15px;"></DIV></TD><TR>	

  <TR><TD class="bg" width="15"></TD>
      <TD class="bg">

<?php
{
  print($authenicate_body); 
}
?>

<DIV style="height: 15px;"></DIV>

<FORM action="search.php" method="POST">
<INPUT name="mode" value="results" type="hidden">
<INPUT name="auth_id" value="<?php echo($auth_id);?>" type="hidden">
<INPUT name="auth_name" value="<?php echo($auth_name);?>" type="hidden">
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0"> 
  <TR>	
    <TD align="center" class="spaceRow" colspan="6" height="1"><DIV style="height: 1px;"><IMG src="<?php echo($temerity_root);?>images/spacer.gif" alt="" height="1" width="1"></DIV></TD>
  </TR>

  <TR>
    <TD class="row2" align="center" width="17%"><SPAN class="genbig">
      Task&nbsp;Name:&nbsp;
    </SPAN></TD>

    <TD class="row2" align="center" width="17%"><SPAN class="genbig">
      Task&nbsp;Type:&nbsp;
    </SPAN></TD>  

    <TD class="row2" align="center" width="17%"><SPAN class="genbig">
      Task&nbsp;Activity:&nbsp;
    </SPAN></TD> 

    <TD class="row2" align="center" width="17%"><SPAN class="genbig">
      Task&nbsp;Status:&nbsp;
    </SPAN></TD>

    <TD class="row2" align="center" width="17%"><SPAN class="genbig">
      Assigned&nbsp;To:&nbsp;
    </SPAN></TD>

    <TD class="row2" align="center" width="17%"><SPAN class="genbig">
      Supervised&nbsp;By:&nbsp;
    </SPAN></TD> 
  </TR>

  <TR>
    <TD class="row1"><SPAN class="genmed">
      <SELECT multiple size="7" name="task_titles[]" style="width:100%">
        <?php
        {
          foreach($task_titles as $e) {
            print('<OPTION ' . $e['selected'] . ' value="' . $e['title_id'] . '">' . 
                  $e['title_name'] . '&nbsp;</OPTION>' . "\n");
          }
        }
        ?> 

      </SELECT>
    </SPAN></TD>

    <TD class="row1" height="120px"><SPAN class="genmed">
      <SELECT multiple size="7" name="task_types[]" style="width:100%">
        <?php
        {
          foreach($task_types as $e) 
            print('<OPTION ' . $e['selected'] . ' value="' . $e['type_id'] . '">' . 
                  $e['type_name'] . '&nbsp;</OPTION>' . "\n");
        }
        ?> 

      </SELECT>
    </SPAN></TD>      

    <TD class="row1"><SPAN class="genmed">
      <SELECT multiple size="7" name="task_activity[]" style="width:100%">
        <?php
        {
          foreach($task_activity as $e) 
            print('<OPTION ' . $e['selected'] . ' value="' . $e['active_id'] . '">' . 
                  $e['active_name'] . '&nbsp;</OPTION>' . "\n");
        }
        ?> 

      </SELECT>
    </SPAN></TD>        

    <TD class="row1"><SPAN class="genmed">
      <SELECT multiple size="7" name="task_status[]" style="width:100%">
        <?php
        {
          foreach($task_status as $e) 
            print('<OPTION ' . $e['selected'] . ' value="' . $e['status_id'] . '">' . 
                  $e['status_name'] . '&nbsp;</OPTION>' . "\n");
        }
        ?> 

      </SELECT>
    </SPAN></TD>

    <TD class="row1"><SPAN class="genmed">
      <SELECT multiple size="7" name="assigned_to[]" style="width:100%">
        <?php
        {
          foreach($users as $e) {
            print('<OPTION ' . $assigned_select[$e['ident_id']] . 
                  ' value="' . $e['ident_id'] . '">' . 
                  $e['ident_name'] . '&nbsp;</OPTION>' . "\n");
          }
          
          foreach($groups as $e) {
            print('<OPTION ' . $assigned_select[$e['ident_id']] . 
                  ' value="' . $e['ident_id'] . '">[' . 
                  $e['ident_name'] . ']&nbsp;</OPTION>' . "\n");
          }
        }
        ?> 
      </SELECT>
    </SPAN></TD>

    <TD class="row1"><SPAN class="genmed">
      <SELECT multiple size="7" name="supervised_by[]" style="width:100%">
        <?php
        {
          foreach($users as $e) {
            print('<OPTION ' . $supervised_select[$e['ident_id']] . 
                  ' value="' . $e['ident_id'] . '">' . 
                  $e['ident_name'] . '&nbsp;</OPTION>' . "\n");
          }
          
          foreach($groups as $e) {
            print('<OPTION ' . $supervised_select[$e['ident_id']] . 
                  ' value="' . $e['ident_id'] . '">[' . 
                  $e['ident_name'] . ']&nbsp;</OPTION>' . "\n");
          }
        }
        ?> 
      </SELECT>
    </SPAN></TD>
  </TR>

  <TR>	
    <TH class="theader" colspan="6" align="center" height="15">
      <INPUT class="liteoption" value="Search" type="submit">
    </TH>
  </TR>
</TABLE>
</FORM>

<DIV style="height: 15px;"></DIV>

<FORM action="details.php" method="POST">
<INPUT name="auth_id" value="<?php echo($auth_id);?>" type="hidden">
<INPUT name="auth_name" value="<?php echo($auth_name);?>" type="hidden">
<TABLE class="frame" width="100%" align="center" cellpadding="4" cellspacing="1" border="0"> 
  <TR>
    <TH align="center" class="theader" colspan="2" nowrap="nowrap">
      <SPAN class="bold">
        &nbsp;Task&nbsp&nbsp;
      </SPAN>
    </TH>

    <TH align="center" width="100" class="theader" nowrap="nowrap">
      <SPAN class="bold">
        &nbsp;Task&nbsp;Activity&nbsp;
      </SPAN>
    </TH>

    <TH align="center" width="100" class="theader" nowrap="nowrap">
      <SPAN class="bold">
        &nbsp;Task&nbsp;Status&nbsp;
      </SPAN>
    </TH>

    <TH align="center" width="100" class="theader" nowrap="nowrap">
      <SPAN class="bold">
        &nbsp;Assigned&nbsp;To&nbsp;
      </SPAN>
    </TH>

    <TH align="center" width="100" class="theader" nowrap="nowrap">
      <SPAN class="bold">
        &nbsp;Supervised&nbsp;By&nbsp;
      </SPAN>
    </TH>

    <TH align="center" width="70" class="theader" nowrap="nowrap">
      <SPAN class="bold">
        &nbsp;Last&nbsp;Modified&nbsp;
      </SPAN>
    </TH>
  </TR>

  <?php
  {
    foreach($tasks as $tid => $t) {
      $assigned = "-";
      if(strlen($t['assigned_to']) > 0)
        $assigned = $task_owners[$tid]['assigned_to'];       
      
      $supervised = "-";
      if($task_owners[$tid]['supervised_by'] != NULL) {
        $first = true;
        foreach($task_owners[$tid]['supervised_by'] as $s) {
          if($first) {
            $supervised = '';
            $first = false;
          }
          else {
            $supervised .= ', ';
          }
         
          $supervised .= $s;
        }
      }


      print('  <TR>' . "\n" . 
            '    <TD class="row1" width="30" align="center"><SPAN class="redbold">' . "\n" . 
            '      <INPUT name="task_ids[]" value="' . $tid . '" type="checkbox">' . "\n".
            '    </SPAN></TD>' . "\n" . 
            '    <TD class="row1" align="left"><SPAN class="redbold">' . "\n" . 
            '      &nbsp;&nbsp;' . $t['title'] . '&nbsp;:&nbsp;' . $t['type'] . "\n" .
            '    </SPAN></TD>' . "\n" . 
            '    <TD class="row2" align="center">' . $t['activity'] . '</TD>' . "\n" .
            '    <TD class="row2" align="center">' . $t['status'] . '</TD>' . "\n" . 
            '    <TD class="row3" align="center">' . $assigned . '</TD>' . "\n" . 
            '    <TD class="row3" align="center">' . $supervised . '</TD>' . "\n" . 
            '    <TD class="row2" align="center">' . $t['last_modified'] . '</TD>' . "\n" . 
            '  </TR>' . "\n");
    }
  }
  ?>
    
  <TR>	
    <TH class="theader" colspan="7" align="center" height="15">
      <INPUT class="liteoption" value="Details" type="submit">
    </TH>
  </TR>
</TABLE>
</FORM>






    </TD>
    <TD class="bg" width="15"></TD>
  </TR>	
  <TR><TD class="bg" colspan="3"><DIV style="height: 15px;"></DIV></TD><TR>
</TABLE>	


<PRE>
<?php 
// print("<P>_REQUEST<BR>\n");
// var_dump($_REQUEST);
// print("<P>supervised_select<BR>\n");
// var_dump($supervised_select);
// print("<P>task_titles<BR>\n");
// var_dump($task_titles);
// print("<P>task_sql<BR>\n");
// var_dump($task_sql);
// print("<P>tasks<BR>\n");
// var_dump($tasks);
// print("<P>tasks_owners<BR>\n");
// var_dump($task_owners);
?>
</PRE>



</BODY>
