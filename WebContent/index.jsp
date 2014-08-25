<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="//code.jquery.com/ui/1.11.0/themes/smoothness/jquery-ui.css">
<script src="//code.jquery.com/jquery-1.10.2.js"></script>
<script src="//code.jquery.com/ui/1.11.0/jquery-ui.js"></script>
<title>Insert title here</title>
<script>
  $(function() {
	  $( "#from" ).datepicker({
	      defaultDate: "+1w",
	      changeMonth: true,
	      numberOfMonths: 1,
	      onClose: function( selectedDate ) {
	        $( "#to" ).datepicker( "option", "minDate", selectedDate );
	      }
	    });
	    $( "#to" ).datepicker({
	      defaultDate: "+1w",
	      changeMonth: true,
	      numberOfMonths: 1,
	      onClose: function( selectedDate ) {
	        $( "#from" ).datepicker( "option", "maxDate", selectedDate );
	      }
	    });
  });
  
  function validate(){
	  if(document.getElementById("from").value.length==0 || document.getElementById("from").value.length==0){
		  alert("Please select beginning and end dates")
		  return false;
	  }
  }
</script>
</head>
<body>
<div id="container">
	<form id="graph_selections" name="graph_selections" action="RetweetNetworkExample.html" onSubmit="return validate()">
		Hashtags: <input type="text" name="hashtags"/>
		Top nodes: 	<select name="top_nodes">
						<option value="50">50</option>
						<option value="100">100</option>
						<option value="200">200</option>
					</select>
		From: <input type="text" name="from" id="from" readonly="readonly" style="background:white;"/>
		To: <input type="text" name="to" id="to" readonly="readonly" style="background:white;"/>
		<br/><input type="submit" name="submit" value="Create tweets graph"/>
	</form>
	<h2>Other options</h2>
	<ul>
		<!--<li><a href="WordCloudExample.html">See top keywords</a></li>-->
		<li><a href="DiscussionGraphs.html">See all discussions</a></li>
		<li><a href="WordCloud.html">See top keywords beta</a></li>
	</ul>
</div>
</body>
</html>