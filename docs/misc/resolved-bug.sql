

Generates a table of closed bugs and the Pipeline version they where closed in.


SELECT bug_id, title, version_name, resolution_name FROM 
  phpbt_bug,phpbt_version,phpbt_status,phpbt_resolution
  WHERE closed_in_version_id = phpbt_version.version_id 
  AND phpbt_status.status_id = phpbt_bug.status_id 
  AND phpbt_resolution.resolution_id = phpbt_bug.resolution_id
  AND status_name = "Closed" 
  AND version_name = "1.8.11"
  ORDER BY resolution_name,phpbt_bug.created_date
