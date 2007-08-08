<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd"> 
<HTML>

<?php $temerity_root = "../"; ?>

<HEAD>
  <META http-equiv="content-type" content="text/html; charset=ISO-8859-1">
  <TITLE> Post Comment </TITLE>
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
  include($temerity_root . "pltasks/pipeline-config.php");
  include($temerity_root . "dbopen.php");

  /* authenticate the user */
  $authenicate_body = authenticate();

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

  /* lookup activity and status of current task */ 
  $current_task_active = NULL;
  $current_task_status = NULL;
  {
    $sql = ("SELECT active_id, status_id FROM tasks " . 
            "WHERE tasks.task_id = " . $_REQUEST["task_id"]);
    
    $result = mysql_query($sql)
      or show_sql_error($sql);

    if($row = mysql_fetch_array($result, MYSQL_ASSOC)) {
      $current_task_active = $row['active_id']; 
      $current_task_status = $row['status_id'];             
    }
  }

  /* save original list of task IDs */ 
  $tids = NULL;
  if(isset($_REQUEST["task_list"])) {
    $tids = array(); 
    foreach(explode(" ", $_REQUEST["task_list"]) as $ltid) {
      if($ltid != "") 
        $tids[] = $ltid;
    }
  }

  /* lookup task ID */ 
  $tid = $_REQUEST["task_id"]; 

  /* process post changes required before task/event lookups... (if any) */ 
  $warning_msg = NULL;
  $approval_info = NULL;
  switch($_REQUEST["mode"]) {
  case 'post_review':
    {      
      /* validate */ 
      if(($_REQUEST["message"] == NULL) || ($_REQUEST["message"] == "")) 
        $warning_msg = "No review text was supplied!";

      $new_task_status = NULL;
      if($warning_msg == NULL) {
        if(($_REQUEST["new_status"] == NULL) || ($_REQUEST["new_status"] == 0)) 
          $warning_msg = "No change in Task Status was specified!";
        
        if($warning_msg == NULL) {
          $found = false;
          foreach($task_status as $status) {
            if($status['status_id'] == $_REQUEST["new_status"]) {
              $found = true;
              break;
            }
          }
          
          if(!$found) 
            $warning_msg = 
              ("Somehow the new Task Status specified (" . $_REQUEST["new_status"] . ") " . 
               "was not legal!");
        }

        $new_task_status = $_REQUEST["new_status"];
      }
      
      /* insert message */ 
      $note_id = NULL;
      if($warning_msg == NULL) {
        $sql = ("INSERT INTO notes (note_text) VALUES " . 
                "('" . mysql_real_escape_string($_REQUEST["message"]) . "')");
        if(mysql_query($sql)) 
          $note_id = mysql_insert_id();
        else 
          $warning_msg = get_sql_error($sql); 
      }

      switch($new_task_status) {
      case 2: // Changes Required
      case 4: // On Hold
        {
          $new_task_active = ($new_task_status == 2) ? 2 : 1;

          /* insert event */ 
          $stamp = time();
          if($warning_msg == NULL) {
            $sql = 
              ("INSERT INTO events (task_id, ident_id, stamp, note_id, " . 
                                   "new_active_id, new_status_id) " .
               "VALUES (" . $tid . ", " . $auth_id . ", FROM_UNIXTIME(" . $stamp . "), " . 
                       $note_id . ", " . $new_task_active . ", " . $new_task_status . ")");
            if(!mysql_query($sql)) 
              $warning_msg = get_sql_error($sql); 
          }
          
          /* update task */ 
          if($warning_msg == NULL) {
            $sql = 
              ("UPDATE tasks " . 
               "SET last_modified = FROM_UNIXTIME(" . $stamp . "), " . 
               "active_id = " . $new_task_active . ", " . 
               "status_id = " . $new_task_status . " " .
               "WHERE task_id = " . $tid);
            if(!mysql_query($sql)) 
              $warning_msg = get_sql_error($sql); 
          }
        }
        break;

      case 3: // Approved
      case 5: // Could Be Better
      case 6: // Finalled
        {
          $approval_info = array('new_status_id' => $new_task_status, 
                                 'new_note_id'   => $note_id, 
                                 'new_note_text' => $_REQUEST["message"]);
        }
      }
    }
    break;

  case 'retry_review':
    {
      /* validate */ 
      if(($_REQUEST["new_status_id"] == NULL) || ($_REQUEST["new_status_id"] == "")) 
        $warning_msg = "No new status ID was supplied!";

      if($warning_msg == NULL) {
        if(($_REQUEST["new_note_id"] == NULL) || ($_REQUEST["new_note_id"] == "")) 
          $warning_msg = "No new note ID was supplied!";
        else {
          $sql = ("SELECT node_text from notes WHERE note_id = " . $_REQUEST["new_note_id"]);

          $result = mysql_query($sql)
            or show_sql_error($sql);

          if($row = mysql_fetch_array($result, MYSQL_ASSOC)) 
            $approval_info = array('new_status_id' => $_REQUEST["new_status_id"],
                                   'new_note_id'   => $_REQUEST["new_note_id"], 
                                   'new_note_text' => $row['note_text']);
        }
      }
    }
    break;

  case 'post_comment':
    {
      /* validate */ 
      if(($_REQUEST["message"] == NULL) || ($_REQUEST["message"] == "")) 
        $warning_msg = "No comment text was supplied!";
      
      /* insert message */ 
      $note_id = NULL;
      if($warning_msg == NULL) {
        $sql = ("INSERT INTO notes (note_text) VALUES " . 
                "('" . mysql_real_escape_string($_REQUEST["message"]) . "')");
        if(mysql_query($sql)) 
          $note_id = mysql_insert_id();
        else 
          $warning_msg = get_sql_error($sql); 
      }

      /* insert event */ 
      $stamp = time();
      if($warning_msg == NULL) {
        $sql = 
          ("INSERT INTO events (task_id, ident_id, stamp, note_id) " .
           "VALUES (" . $tid . ", " . $auth_id . ", " . 
           "FROM_UNIXTIME(" . $stamp . "), " . $note_id . ")");
        if(!mysql_query($sql)) 
          $warning_msg = get_sql_error($sql); 
      }

      /* update task */ 
      if($warning_msg == NULL) {
        $sql = 
          ("UPDATE tasks " . 
           "SET last_modified = FROM_UNIXTIME(" . $stamp . ") " . 
           "WHERE task_id = " . $tid);
        if(!mysql_query($sql)) 
          $warning_msg = get_sql_error($sql); 
      }
    }
    break;

  case 'post_assign':
    if($_REQUEST['auth_name'] == 'pipeline') {
      /* update assigned to */ 
      {
        $sql = NULL;
        if($_REQUEST['new_assigned_to'] == 0) {
          $sql = 
            ("UPDATE tasks " . 
             "SET assigned_to = NULL " . 
             "WHERE task_id = " . $tid);
        }
        else if($_REQUEST['new_assigned_to'] > 0) {
          $sql = 
            ("UPDATE tasks " . 
             "SET assigned_to = " . $_REQUEST['new_assigned_to'] . " " .
             "WHERE task_id = " . $tid);
        }
        
        if($sql != NULL) {
          if(!mysql_query($sql)) 
            $warning_msg = get_sql_error($sql); 
        }
      }

      /* remove existing supervised by */ 
      if($warning_msg == NULL) {
        $sql = ('DELETE FROM supervisors WHERE task_id = ' . $tid);
        if(!mysql_query($sql)) 
          $warning_msg = get_sql_error($sql); 
      }

      /* insert new supervised by */ 
      foreach($_REQUEST['new_supervised_by'] as $nsid) {
        if($warning_msg == NULL) {
          if($nsid > 0) {
            $sql = ('INSERT INTO supervisors (task_id, ident_id) ' . 
                    'VALUES (' . $tid . ', ' . $nsid . ')');
            if(!mysql_query($sql)) 
              $warning_msg = get_sql_error($sql); 
          }
        }
      }
    }    
  }

  /* lookup task information */ 
  $tasks = array(); 
  {
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
                       "node_info.is_focus as `is_focus`, " .  
                       "node_info.is_submit as `is_submit`, " . 
                       "node_info.is_approve as `is_approve` " .    
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

  /* if the task was approved, run the approval builder */ 
  $approval_builder_cmdline = NULL;
  $approval_builder_output  = NULL;
  $approval_builder_errors  = NULL;
  $approval_builder_result  = NULL;
  if(($warning_msg == NULL) && ($approval_info != NULL)) {
    /* lookup the submit/approve nodes */ 
    $builder_approve_node = NULL;
    $builder_submit_node  = NULL;
    foreach($tasks[$tid]['events'] as $eid => $e) {
      if($e['nodes'] != NULL) {
        foreach($e['nodes'] as $node) {
          if($node['is_submit']) {
            if($builder_submit_node == NULL)
              $builder_submit_node = $node['name'];
          }
          else if($node['is_approve']) {
            if($builder_approve_node == NULL)
              $builder_approve_node = $node['name'];
          }
        }
      }
      
      if(($builder_submit_node != NULL) && ($builder_approve_node != NULL))
        break;
    }
    if(($builder_submit_node == NULL) || ($builder_approve_node == NULL))
      $warning_msg = "Unable to find both Submit and Approve nodes associated with the task!";
      
    /* run the builder... */ 
    if($warning_msg == NULL) {
      $filedesc = array(1 => array("pipe", "w"),  
                        2 => array("pipe", "w"));

      $tmpdir = '/tmp';
      
      $approval_builder_cmdline = 
        ($pipeline_latest . 
         '/builders/us/temerity/pipeline/builder/approval/v2_3_2/Unix/plApprovalBuilder ' .
         '--builder=ApprovalBuilder ' . 
         '--ApproveNode=' . $builder_approve_node . ' ' . 
         '--SubmitNode=' . $builder_submit_node . ' ' .
         '--ApprovedBy=' . $auth_name . ' ' . 
         '--ApprovalMessage=' . escapeshellarg($approval_info['new_note_text'])); 

      $approval_process = proc_open($approval_builder_cmdline, $filedesc, $pipes, $tmpdir);
      if(is_resource($approval_process)) {
        $approval_builder_output = stream_get_contents($pipes[1]);
        fclose($pipes[1]);
        
        $approval_builder_errors = stream_get_contents($pipes[2]);
        fclose($pipes[2]);
        
        $approval_builder_result = proc_close($process);
      }
      else {
        $warning_msg = "Unable to create process!";
      }

      if(!$approval_builder_result) {
        $new_task_active = NULL;
        switch($approval_info['new_status_id']) {
        case 6:  // Finalled
          $new_task_active = 1; // Inactive;
          break;

        default:
          $new_task_active = 2; // Active;          
        }

        /* insert event */ 
        $stamp = time();
        if($warning_msg == NULL) {
          $sql = 
            ("INSERT INTO events (task_id, ident_id, stamp, note_id, " . 
             "new_active_id, new_status_id) " .
             "VALUES (" . $tid . ", " . $auth_id . ", FROM_UNIXTIME(" . $stamp . "), " . 
             $approval_info['new_note_id'] . ", " . $new_task_active . ", " . 
              $approval_info['new_status_id'] . ")");
          if(!mysql_query($sql)) 
            $warning_msg = get_sql_error($sql); 
        }
        
         /* update task */ 
        if($warning_msg == NULL) {
          $sql = 
            ("UPDATE tasks " . 
             "SET last_modified = FROM_UNIXTIME(" . $stamp . "), " . 
              "active_id = " . $new_task_active . ", " . 
             "status_id = " . $approval_info['new_status_id'] . " " .
             "WHERE task_id = " . $tid);
          if(!mysql_query($sql)) 
            $warning_msg = get_sql_error($sql); 
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


<?php
{
  /* process tasks... */ 
  $first_task = true;
  $t = $tasks[$tid];
  {
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
      
      $show_details_buttons = false;
      include ($temerity_root . "pltasks/details-header.php");
      
      $header_contents = ob_get_contents();
      ob_end_clean();
    }
    print($header_contents);

    /* warning box */ 
    if($warning_msg != NULL) {
      {
        ob_start();
     
        include ($temerity_root . "pltasks/post-warning.php");
     
        $warning_contents = ob_get_contents();
        ob_end_clean();
      }
      print($warning_contents);
    }
    
    /* approval builder output */ 
    if($approval_builder_cmdline != NULL) {
      {
        ob_start();
     
        include ($temerity_root . "pltasks/approval-builder.php");
     
        $approval_builder_contents = ob_get_contents();
        ob_end_clean();
      }
      print($approval_builder_contents);
    }
    
    /* post form */ 
    switch($_REQUEST["mode"]) {
    case 'post_review':
    case 'review':
      {
        if(($_REQUEST["mode"] == 'post_review') && ($warning_msg == NULL))  
          break;

        /* message text input */ 
        {
          ob_start();
          
          include ($temerity_root . "pltasks/post-review.php");
          
          $review_contents = ob_get_contents();
          ob_end_clean();
        }
        print($review_contents);
      }
      break;

    case 'post_comment':
    case 'comment':
      {
        if(($_REQUEST["mode"] == 'post_comment') && ($warning_msg == NULL))  
          break;

        /* message text input */ 
        {
          ob_start();
          
          include ($temerity_root . "pltasks/post-comment.php");
          
          $comment_contents = ob_get_contents();
          ob_end_clean();
        }
        print($comment_contents);
      }
      break;

    case 'assign':
      {
        /* message text input */ 
        {
          ob_start();
          
          include ($temerity_root . "pltasks/post-assign.php");
          
          $assign_contents = ob_get_contents();
          ob_end_clean();
        }
        print($assign_contents);
      }          
    }

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
//   print("<P>_REQUEST<BR>\n");
//   var_dump($_REQUEST);
// print("<P>approval_process<BR>\n");
// var_dump($approval_process);
// print("<P>approval_builder_output<BR>\n");
// var_dump($approval_builder_output);
// print("<P>approval_builder_errors<BR>\n");
// var_dump($approval_builder_errors);
// print("<P>approval_builder_result<BR>\n");
// var_dump($approval_builder_result);
// print("<P>tids<BR>\n");
// var_dump($tids);
// print("<P>current_task_active<BR>\n");
// var_dump($current_task_active);
// print("<P>current_task_status<BR>\n");
// var_dump($current_task_status);
// print("<P>new_task_status<BR>\n");
// var_dump($new_task_status);
// print("<P>task_status<BR>\n");
// var_dump($task_status);
// print("<P>debug_sql<BR>\n");
// var_dump($debug_sql);
// print("<P>approval_builder_result<BR>\n");
// var_dump($approval_builder_result);
// print("<P>tasks<BR>\n");
// var_dump($tasks);
// print("<P>task_owners<BR>\n");
// var_dump($task_owners);
?>
</PRE>



</BODY>
