<script type="text/JavaScript">
<!--
versions = new Array();
components = new Array();
versions[0] = new Array(new Array(0,'All Versions'));
components[0] = new Array(new Array(0,'All Components'));
versions[1] = new Array(new Array(0,'All Versions'),new Array(102,'2.3.4'),new Array(100,'2.3.3'),new Array(98,'2.3.2'),new Array(97,'2.3.1'),new Array(95,'2.2.5'),new Array(93,'2.2.4'),new Array(92,'2.2.3'),new Array(90,'2.2.2'),new Array(89,'2.2.1'),new Array(87,'2.1.9'),new Array(85,'2.1.8'),new Array(84,'2.1.7'),new Array(83,'2.1.6'),new Array(82,'2.1.5'),new Array(81,'2.1.4'),new Array(80,'2.1.3'),new Array(79,'2.1.2'),new Array(78,'2.1.1'),new Array(77,'2.0.18'),new Array(75,'2.0.17'),new Array(74,'2.0.16'),new Array(73,'2.0.15'),new Array(72,'2.0.14'),new Array(71,'2.0.13'),new Array(70,'2.0.12'),new Array(69,'2.0.11'),new Array(68,'2.0.10'),new Array(65,'2.0.9'),new Array(64,'2.0.8'),new Array(63,'2.0.7'),new Array(62,'2.0.6'),new Array(61,'2.0.5'),new Array(59,'2.0.4'),new Array(58,'2.0.3'),new Array(57,'2.0.2'),new Array(56,'2.0.1'),new Array(54,'2.0.0'),new Array(53,'1.9.23'),new Array(48,'1.9.22'),new Array(47,'1.9.21'),new Array(44,'1.9.20'),new Array(42,'1.9.19'),new Array(41,'1.9.18'),new Array(30,'1.9.17'),new Array(29,'1.9.16'),new Array(28,'1.9.15'),new Array(27,'1.9.14'),new Array(26,'1.9.13'),new Array(25,'1.9.12'),new Array(24,'1.9.11'),new Array(23,'1.9.10'),new Array(22,'1.9.9'),new Array(21,'1.9.8'),new Array(20,'1.9.7'),new Array(19,'1.9.6'),new Array(18,'1.9.5'),new Array(17,'1.9.4'),new Array(16,'1.9.3'),new Array(15,'1.9.2'),new Array(14,'1.9.1'),new Array(13,'1.9.0'),new Array(12,'1.8.13'),new Array(11,'1.8.12'),new Array(10,'1.8.11'),new Array(9,'1.8.10'),new Array(8,'1.8.9'),new Array(7,'1.8.8'),new Array(6,'1.8.7'),new Array(5,'1.8.6'),new Array(4,'1.8.5'),new Array(3,'1.8.4'),new Array(2,'1.8.3'),new Array(1,'1.8.2'));
components[1] = new Array(new Array(0,'All Components'),new Array(1,'Graphical Client'),new Array(2,'Scripting Client'),new Array(3,'Server Daemons'),new Array(4,'Plugins'),new Array(5,'Java API'),new Array(6,'Docs'),new Array(7,'General'));
versions[2] = new Array(new Array(0,'All Versions'),new Array(99,'3.2.1'),new Array(96,'3.2.0'),new Array(88,'3.1.0'),new Array(86,'3.0.3'),new Array(76,'3.0.2'),new Array(67,'3.0.1'),new Array(66,'3.0.0'),new Array(55,'2.9.0'),new Array(52,'2.8.2'),new Array(51,'2.8.1'),new Array(50,'2.8.0'),new Array(49,'2.7.1'),new Array(43,'2.7.0'),new Array(37,'2.6.0'),new Array(36,'2.5.1'),new Array(35,'2.5.0'),new Array(34,'2.4.0'),new Array(33,'2.3.3'),new Array(32,'2.3.2'),new Array(31,'2.3.0'));
components[2] = new Array(new Array(0,'All Components'),new Array(8,'General'));
versions[3] = new Array(new Array(0,'All Versions'),new Array(60,'2.0.4'),new Array(46,'1.5.0'),new Array(45,'1.4.0'),new Array(40,'1.3.0'),new Array(39,'1.2.0'),new Array(38,'1.1.0'));
components[3] = new Array(new Array(0,'All Components'),new Array(9,'Web Site'),new Array(10,'Tutorials'),new Array(11,'User Manual'),new Array(12,'General'));
versions[4] = new Array(new Array(0,'All Versions'),new Array(101,'1.1.0'),new Array(94,'1.0.0'));
components[4] = new Array(new Array(0,'All Components'),new Array(13,'General'));
function updateMenus(f) {
  idx = f.bug_project.selectedIndex;

  f.bug_version.length = versions[idx].length;
  for(var x = 0; x < versions[idx].length; x++) {
    f.bug_version.options[x].value = versions[idx][x][0];
    f.bug_version.options[x].text  = versions[idx][x][1];
  }

  comps = document.getElementById('bug_comps');
  comps.length = components[idx].length;
  for(var x = 0; x < components[idx].length; x++) {
    comps.options[x].value = components[idx][x][0];
    comps.options[x].text  = components[idx][x][1];
  }
}
// -->

</script>


<form action="search.php?mode=results" method="POST">

<table class="forumline" width="100%" cellpadding="4" cellspacing="1" border="0" class="row1">
	<tr>
		<th class="thHead" colspan="4" height="25">New Search Query</th>
	</tr>
	<tr>
		<td class="row1" colspan="2" width="50%"><span class="gen">Search for Keywords:</span><br /><span class="gensmall">You can use <u>AND</u> to define words which must be in the results, <u>OR</u> to define words which may be in the result and <u>NOT</u> to define words which should not be in the result. Use * as a wildcard for partial matches</span></td>

		<td class="row2" colspan="2" valign="top"><span class="genmed"><input type="text" style="width: 300px" class="post" name="search_keywords" value="*" size="30" /><br /><input type="radio" name="search_terms" value="any" checked="checked" /> Search for any terms or use query as entered<br /><input type="radio" name="search_terms" value="all" /> Search for all terms</span></td>
	</tr>
	<tr>
		<td class="row1" colspan="2"><span class="gen">Search for Author:</span><br /><span class="gensmall">Use * as a wildcard for partial matches</span></td>
		<td class="row2" colspan="2" valign="middle"><span class="genmed"><input type="text" style="width: 300px" class="post" name="search_author" size="30" /></span></td>
	</tr>

	<tr>
		<td class="spaceRow" colspan="4" height="1"><img src="templates/temerity/images/spacer.gif" alt="" width="1" height="1"></td>
	</tr>

	<tr style="height: 50px;">
	  <td class="row1" align="right"><span class="gen">Forum:&nbsp;</span></td>
	  <td class="row2"><span class="genmed">
            <select class="post" name="search_forum"><option value="-1">All Forums</option><option value="2">Using Pipeline</option><option value="8">Development</option><option value="12">Bugs & Features</option><option value="10">News</option></select>

          </span></td>


	  <td class="row1" align="right"><span class="gen">Category:&nbsp;</span></td>
	  <td class="row2"><span class="genmed">
            <select class="post" name="search_cat"><option value="-1">All Catagories</option><option value="2">Pipeline Support</option><option value="4">General</option>
	  </select></span></td>
	</tr>

	<tr>
		<td class="spaceRow" colspan="4" height="1"><img src="templates/temerity/images/spacer.gif" alt="" width="1" height="1"></td>
	</tr>

	<TR>
	  <TD class="row1" align="right">
	    <SPAN class="gen">Project:&nbsp;</SPAN>
          </TD>

	  <TD class="row2"><SPAN class="genmed">
	   <SELECT class="post" name="bug_project" onChange="updateMenus(this.form)">
              <OPTION selected label="0" value="0">All Projects &nbsp;</OPTION>
<OPTION label="1" value="1">Pipeline&nbsp;</OPTION>
<OPTION label="2" value="2">Pipeline Config&nbsp;</OPTION>
<OPTION label="3" value="3">Documentation&nbsp;</OPTION>
<OPTION label="4" value="4">WinJobManager&nbsp;</OPTION>

	   </SELECT>
	  </SPAN></TD>

	  <TD class="row1" align="right" rowspan="2">
	    <SPAN class="gen">Report&nbsp;Type:&nbsp;</SPAN>
          </TD>

          <TD class="row2" rowspan="2"><SPAN class="genmed">
	    <SELECT multiple size="7" name="bug_type[]">

              <OPTION selected label="0" value="0">All Types &nbsp;</OPTION><OPTION label="1" value="1">Improvement&nbsp;</OPTION>
<OPTION label="2" value="2">Feature&nbsp;Request&nbsp;</OPTION>
<OPTION label="3" value="3">Annoyance&nbsp;</OPTION>
<OPTION label="4" value="4">Defect&nbsp;</OPTION>
<OPTION label="5" value="5">Severe&nbsp;Defect&nbsp;</OPTION>
<OPTION label="6" value="6">Show&nbsp;Stopper&nbsp;</OPTION>

	    </SELECT>
	  </SPAN></TD>
        </TR>

	<TR>
	  <TD class="row1" align="right">
	    <SPAN class="gen">Version:&nbsp;</SPAN>
          </TD>

	  <TD class="row2"><SPAN class="genmed">
	   <SELECT name="bug_version">
	       <OPTION selected label="0" value="0">All Versions &nbsp;</OPTION>
	   </SELECT>
	  </SPAN></TD>
        </TR>

	<TR>

	  <TD class="row1" align="right">
	    <SPAN class="gen">Component:&nbsp;</SPAN>
          </TD>

	  <TD class="row2"><SPAN class="genmed">
	   <SELECT multiple size="7" name="bug_component[]" id="bug_comps">
	       <OPTION selected label="0" value="0">All Components &nbsp;</OPTION>
	   </SELECT>

	  </SPAN></TD>

	  <TD class="row1" align="right">
	    <SPAN class="gen">Report&nbsp;Status:&nbsp;</SPAN>
          </TD>

          <TD class="row2"><SPAN class="genmed">
	    <SELECT multiple size="7" name="bug_status[]">
              <OPTION selected label="0" value="0">Any Status &nbsp;</OPTION><OPTION label="1" value="1">New&nbsp;</OPTION>

<OPTION label="2" value="2">Active&nbsp;</OPTION>
<OPTION label="3" value="3">Inactive&nbsp;</OPTION>
<OPTION label="4" value="4">Fixed&nbsp;</OPTION>
<OPTION label="5" value="5">Implemented&nbsp;</OPTION>
<OPTION label="6" value="6">Unresolved&nbsp;</OPTION>
<OPTION label="7" value="7">Not&nbsp;a&nbsp;Bug&nbsp;</OPTION>
<OPTION label="8" value="8">Duplicate&nbsp;</OPTION>
<OPTION label="9" value="9">Rejected&nbsp;</OPTION>

	    </SELECT>
	  </SPAN></TD>
        </TR>

	<tr>
		<td class="spaceRow" colspan="4" height="1"><img src="templates/temerity/images/spacer.gif" alt="" width="1" height="1"></td>
	</tr>

	<tr>

	  <td class="row1" align="right" nowrap="nowrap" rowspan="2">
            <span class="gen">Results As:&nbsp;</span>
          </td>
	  <td class="row2" nowrap="nowrap" rowspan="2">
            <input type="radio" name="show_results" value="topics" checked="checked" />
              <span class="genmed">Topics</span><BR>
            <input type="radio" name="show_results" value="posts" />
              <span class="genmed">Posts</span><BR>

            <input type="radio" name="show_results" value="custom" />
              <span class="genmed">Custom</span>
          </td>

          <td class="row1" align="right" nowrap="nowrap">
            <span class="gen">Previous:&nbsp;</span>
          </td>
	  <td class="row2" valign="middle">

            <span class="genmed">
              <select class="post" name="search_time"><option value="0" selected="selected">All Posts</option><option value="1">1 Day</option><option value="7">7 Days</option><option value="14">2 Weeks</option><option value="30">1 Month</option><option value="90">3 Months</option><option value="180">6 Months</option><option value="364">1 Year</option></select><br />
            </span>
          </td>
	</tr>

	<tr>
          <td class="row1" align="right" nowrap="nowrap">
            <span class="gen">Content:&nbsp;</span>
          </td>
	  <td class="row2" valign="middle">
            <span class="genmed">
	      <input type="radio" name="search_fields" value="all" checked="checked" />
	         Topic title and message text<BR>

              <input type="radio" name="search_fields" value="msgonly" />
                 Message text only
            </span>
          </td>
	</tr>

	<tr>
	  <td class="row1" align="right" rowspan="2">
            <span class="gen">Custom&nbsp;&nbsp;<BR>Columns:&nbsp;</span>

          </td>
	  <td class="row2" rowspan="2"><span class="genmed">
	    <select multiple size="9" class="post" name="bug_column[]">
              <OPTION label="0" value="0">All Columns</OPTION>
<OPTION  label="1" value="1">ID</OPTION>
<OPTION selected label="2" value="2">Topic</OPTION>
<OPTION selected label="3" value="3">Project</OPTION>
<OPTION selected label="4" value="4">Component</OPTION>

<OPTION selected label="5" value="5">Version</OPTION>
<OPTION selected label="6" value="6">Type</OPTION>
<OPTION selected label="7" value="7">Status</OPTION>
<OPTION  label="8" value="8">Created</OPTION>
<OPTION selected label="9" value="9">Created&nbsp;On</OPTION>
<OPTION  label="10" value="10">Created&nbsp;By</OPTION>
<OPTION  label="11" value="11">Modified</OPTION>
<OPTION  label="12" value="12">Last Modified&nbsp;On</OPTION>

<OPTION  label="13" value="13">Last Modified&nbsp;By</OPTION>

            </select>
          </span></td>

          <td class="row1" align="right">
	    <span class="gen">Topic/Post&nbsp;&nbsp;<BR>Sort&nbsp;By:&nbsp;</span>
          </td>

	  <td class="row2" valign="middle" nowrap="nowrap"><span class="genmed">
            <select class="post" name="sort_by"><option value="0">Post Time</option><option value="1">Post Subject</option><option value="2">Topic Title</option><option value="3">Author</option><option value="4">Forum</option></select><br />
            <input type="radio" name="sort_dir" value="ASC" /> Ascending<br />
            <input type="radio" name="sort_dir" value="DESC" checked /> Descending
          </span></td>

	</tr>

	<tr>
	  <td class="row1" align="right">
            <span class="gen">Return First:&nbsp;</span>
          </td>
	  <td class="row2"><span class="genmed">
            <select class="post" name="return_chars"><option value="-1">All Available</option><option value="0">0</option><option value="25">25</option><option value="50">50</option><option value="100">100</option><option value="200" selected="selected">200</option><option value="300">300</option><option value="400">400</option><option value="500">500</option><option value="600">600</option><option value="700">700</option><option value="800">800</option><option value="900">900</option><option value="1000">1000</option></select>

            characters
          </span></td>
	</tr>

	<tr>
		<td class="spaceRow" colspan="4" height="1"><img src="templates/temerity/images/spacer.gif" alt="" width="1" height="1" /></td>
	</tr>
	<tr>
		<td class="row1" align="right" nowrap="nowrap"><span class="gen">Save Search:&nbsp;</span></td>

		<td class="row2" nowrap="nowrap"><span class="genmed">
                  <input type="radio" name="save_search" value="yes" />Yes<BR>
	          <input type="radio" name="save_search" value="no" checked="checked" />No
                </span></td>
		<td class="row1" align="right"><span class="gen">Search Name:&nbsp;</span></td>
		<td class="row2"><span class="genmed"><input type="text" style="width: 192px" class="post" name="saved_search_name" size="30" /></span></td>
	</tr>

	<tr>

		<td class="catBottom" colspan="4" align="center" height="28"><input class="liteoption" type="submit" value="Search" /></td>
	</tr>
</table></form>
