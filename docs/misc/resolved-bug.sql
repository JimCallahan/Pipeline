

Generates a table of closed bugs and the Pipeline version they where closed in.


SELECT bug_id, title, version_name, status_name FROM 
  phpbt_bug,phpbt_version,phpbt_status 
  WHERE closed_in_version_id = phpbt_version.version_id 
  AND phpbt_status.status_id = phpbt_bug.status_id 
  AND status_name = "Closed"
