<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<HTML>

<?php $temerity_root = "../"; ?>

<HEAD>
  <META http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <TITLE> Task Details </TITLE>
  <LINK rel="stylesheet" type="text/css" href="<?php echo($temerity_root);?>stylesheet.css">
  <LINK rel="SHORTCUT ICON" href="<?php echo($temerity_root);?>favicon.ico">
</HEAD>
<BODY>


<?php
{
  include($temerity_root . "common.php");
  include($temerity_root . "pltasks/auth.php");
  include($temerity_root . "pltasks/plscript.php");

  //------------------------------------------------------------------------------------------
  // SQL QUERIES
  //------------------------------------------------------------------------------------------
  
  /* open SQL connection */ 
  include($temerity_root . "pltasks/db-config.php");
  include($temerity_root . "dbopen.php");

  /* authenticate the user */
  $authenicate_body = authenticate();

  /* lookup users/workgroups */ 
  $users  = array();
  $groups = array();
  {
    $users[0] = array('ident_id'   => "0", 
                      'ident_name' => "*NONE*"); 
    
    $sql = ("SELECT ident_id, ident_name FROM idents WHERE is_group = 0"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);
    
    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
      $users[$row['ident_id']] = $row;
  }
  
  {
    $sql = ("SELECT ident_id, ident_name FROM idents WHERE is_group = 1"); 
    $result = mysql_query($sql)
      or show_sql_error($sql);
    
    while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
      $groups[$row['ident_id']] = $row;
  }
  
  /* lookup status information */ 
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

  /* lookup activity information */ 
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

  /* lookup task IDs */ 
  $tids = NULL;
  if(isset($_REQUEST["task_ids"])) {
    $tids = $_REQUEST["task_ids"]; 
  }
  else if(isset($_REQUEST["task_list"])) {
    $tids = array(); 
    foreach(explode(" ", $_REQUEST["task_list"]) as $ltid)  {
      if($ltid != "") 
        $tids[] = $ltid;
    }
  }

  /* lookup task information */ 
  $tasks = array(); 
  foreach($tids as $tid) {
    /* search for matching task info (except supervised_by) */ 
    {
      $sql = 
        ("SELECT task_titles.title_name AS `title`, " .
                "task_types.type_name AS `type`, " .          
                "task_activity.active_name as `activity`, " . 
                "task_status.status_name AS `status`, " .     
                "tasks.assigned_to as `assigned_id`, " .      
                "tasks.last_modified as `last_modified` " .   
         "FROM tasks, task_titles, task_types, task_activity, task_status " .
         "WHERE tasks.title_id = task_titles.title_id  " .
         "AND tasks.type_id = task_types.type_id  " .
         "AND tasks.status_id = task_status.status_id " .
         "AND tasks.active_id = task_activity.active_id " .
         "AND tasks.task_id = " . $tid); 

      $result = mysql_query($sql)
        or show_sql_error($sql);

      if($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
        $tasks[$tid] = $row;
    }
    
    /* add names of assigned_to and supervised_by users/groups */ 
    if($tasks[$tid]['assigned_id'] != NULL) {
      $sql = ("SELECT idents.ident_name AS `name`, " .  
                     "idents.is_group AS `is_group` " . 
              "FROM idents " . 
              "WHERE ident_id = " . $tasks[$tid]['assigned_id']);
              
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
                     "idents.ident_id AS `id`, " .  
                     "idents.is_group AS `is_group` " . 
              "FROM supervisors, idents " . 
              "WHERE supervisors.task_id = " . $tid . " " .
              "AND supervisors.ident_id = idents.ident_id");
      
      $result = mysql_query($sql)
        or show_sql_error($sql);
      
      $supers = array();
      $super_ids = array();
      while($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
        $name = $row['name'];
        if($row['is_group']) 
          $name = '[' . $name . ']';
        $supers[] = $name;
        $super_ids[] = $row['id'];
      }
      
      $task_owners[$tid]['supervised_by'] = $supers; 
      $task_owners[$tid]['supervised_by_ids'] = $super_ids; 
    }

    /* add events */ 
    {
      $sql = ("SELECT events.event_id as `eid`, " .
                     "events.stamp as `stamp`, " . 
                     "notes.note_text as `message`, " . 
                     "idents.ident_name AS `name`, " .  
                     "idents.is_group AS `is_group`, " . 
                     "events.new_active_id AS `new_active_id`, " .
                     "events.new_status_id AS `new_status_id` " .
              "FROM `events`, idents, notes " . 
              "WHERE events.task_id = " . $tid . " " .
              "AND events.ident_id = idents.ident_id " . 
              "AND events.note_id = notes.note_id " . 
              "ORDER BY stamp DESC");

      $result = mysql_query($sql)
        or show_sql_error($sql);

      while($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
        $tasks[$tid]['events'][$row['eid']] = $row;

        $active_id = $row['new_active_id'];
        if(($active_id != NULL) && ($task_activity[$active_id] != NULL))
          $tasks[$tid]['events'][$row['eid']]['new_active'] = 
            $task_activity[$active_id]['active_name'];

        $status_id = $row['new_status_id'];
        if(($status_id != NULL) && ($task_status[$status_id] != NULL))
          $tasks[$tid]['events'][$row['eid']]['new_status'] = 
            $task_status[$status_id]['status_name'];
      }
    }
  
    foreach($tasks[$tid]['events'] as $eid => $e) {
      {
        $sql = ("SELECT node_names.node_name as `name`, " .
                       "node_info.node_version as `vid`, " .
                       "node_info.is_edit as `is_edit`, " . 
                       "node_info.is_focus as `is_focus` " .    
                "FROM node_info, node_names " . 
                "WHERE node_info.event_id = " . $eid . " " . 
                "AND node_info.node_id = node_names.node_id"); 

        $result = mysql_query($sql)
          or show_sql_error($sql);

        while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
          $tasks[$tid]['events'][$eid]['nodes'][] = $row;
      }

      {
        $sql = ("SELECT node_names.node_name as `focus_name`,  " .
                       "node_info.node_version as `focus_version`,  " .
                       "thumb_info.thumb_path as `thumb_path` " .
                "FROM `thumb_info`, `node_info`, `node_names` " .
                "WHERE node_info.event_id = " . $eid . " " . 
                "AND node_info.info_id = thumb_info.focus_info_id " .
                "AND node_info.node_id = node_names.node_id"); 

        $tasks[$tid]['sql'] = $sql;

        $result = mysql_query($sql)
          or show_sql_error($sql);

        while($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
          $tasks[$tid]['events'][$eid]['thumbnails'][] = $row;
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


<?php
{
  /* process tasks... */ 
  $first_task = true;
  foreach($tasks as $tid => $t) {
    if(!$first_task) 
      print('<DIV style="height: 15px;"></DIV>' . "\n\n"); 
    $first_task = false;

    $assigned = "-";
    if(strlen($task_owners[$tid]['assigned_to']) > 0)
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

    {
      ob_start();

      $show_details_buttons = true;
      include ($temerity_root . "pltasks/details-header.php");
      
      $header_contents = ob_get_contents();
      ob_end_clean();
    }
    print($header_contents);

    /* process events... */ 
    print('<TABLE class="frame" width="100%" align="center" ' . 
          'cellpadding="4" cellspacing="1" border="0">' . "\n");
    $ek = 0;
    foreach($t['events'] as $eid => $e) {
      $row_color = (($ek % 2) == 0) ? "row1" : "row2";

      {
        ob_start();
        
        include ($temerity_root . "pltasks/details-event.php");
        
        $event_contents = ob_get_contents();
        ob_end_clean();
      }
      print($event_contents);

      $ek++;
    }

    {
      ob_start();
      
      include ($temerity_root . "pltasks/details-footer.php");
      
      $footer_contents = ob_get_contents();
      ob_end_clean();
    }
    print($footer_contents);
  }
}
?>



    </TD>
    <TD class="bg" width="15"></TD>
  </TR>	
  <TR><TD class="bg" colspan="3"><DIV style="height: 15px;"></DIV></TD><TR>
</TABLE>	


<PRE>
<?php 
// print("<P>_REQUEST<BR>\n");
// var_dump($_REQUEST);
// print("<P>tids<BR>\n");
// var_dump($tids);
// print("<P>tasks<BR>\n");
// var_dump($tasks);
// print("<P>task_owners<BR>\n");
// var_dump($task_owners);
// print("<P>users<BR>\n");
// var_dump($users);
// print("<P>groups<BR>\n");
// var_dump($groups);
?>
</PRE>



</BODY>
