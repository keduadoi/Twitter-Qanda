<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%@ include file="template/header.html" %>
<title>Twitter analysis</title>

<script>
function validate(){
	if(document.getElementById("hashtags").value.length==0){
		  alert("Enter at least one hashtag");
		  return false;
	  }
	
	  if($("#from").val().length==0 || $("#to").val().length==0){
		  alert("Please select beginning and end dates")
		  return false;
	  }
	  
	  return true;
}

//call this on load
$(document).ready(function()
{ 
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
  
  $("#dbSubmit").click(function(event){
	  var dbInput = $("#dbInput").val();
	  if(dbInput.length==0){
		alert("Database name must not be empty");
		return;
	  }
	   $.ajax({
	    url: 'DatabaseOperations',
	    type: 'POST',
	    data: { dbInput: dbInput, operation: 'get' },
	    success: function (data, statusText, jqXHR) {
	        if(jQuery.parseJSON(data).isExist=="true"){
	        	if(parseInt(jQuery.parseJSON(data).doc_count)==0){
	        		alert("This database is empty, please upload tweets data before processing")
	        	}else{
	        		alert("\""+dbInput+"\" is selected");
		        	setCookie("currentDb", dbInput, 1);
		        	setCookie("doc_count", jQuery.parseJSON(data).doc_count, 1);
		        	$("#currentDbText").text(getCookie("currentDb"));
		        	$("#db_name").text("Database name: "+dbInput);
		        	$("#doc_count").text("Number of tweets: "+jQuery.parseJSON(data).doc_count);
	        	}
	        } else {
	        	alert("Database does not exist. Please create and upload data before analyzing")
	        }
	    },
	    beforeSend: function(){
		       $('.loader').show();
		   },
		  complete: function(){
		       $('.loader').hide();
		  }
	});	   
  });
  
  $('.loader').hide();
  
});
</script>

</head>
<body onload="checkDbCookie()">
<!-- navigation bar -->
<%@ include file="template/navbar.html" %>

<div class="container">
    <div class="clearfix">
    	<div class="page-header">
        	<h1>Generate retweeted network</h1>
        </div>
        
        <div class="loader" style="padding-left:48%">
       		<img class="loading-image" src="loader.GIF" alt="loading.."/>
		</div>
                        
        <div class="row placeholders" style="float:left">
            <br/>
      		<form id="dbSelect" name="dbSelect" action="GetDatabase" method="post">
		    	Enter database name you want to use (default is <b>qanda</b>):
		      	<input type="text" name="dbInput" id="dbInput"/>
		      	<button type="button" class="btn btn-primary start" id="dbSubmit" name="dbSubmit">
		        	<i class="icon-upload icon-white"></i>
		            <span>Submit</span>
		        </button>
		      	<br/><br/>
		    </form>
      
			<form id="graph_selections" name="graph_selections" action="RetweetNetworkExample.jsp" onsubmit="return validate()">
				Hashtags (separated by semicolon) <input type="text" name="hashtags" id="hashtags" placeholder="e.g. qanda"/><br/><br/>
				Root users amount	<select name="top_nodes">
										<option value="50">50</option>
										<option value="100">100</option>
										<option value="200">200</option>
									</select>
				<br/><br/>
				From <input type="text" name="from" id="from" readonly="readonly" style="background:white;"/>
				To <input type="text" name="to" id="to" readonly="readonly" style="background:white;"/>
				<br/><br/>
				<button type="submit" class="btn btn-primary start" id="createGraph" name="createGraph">
		        	<i class="icon-upload icon-white"></i>
		            <span>Create retweeted graph</span>
		        </button>
			</form>

		</div>

		<div class="row placeholders" style="float:right">
			<p><b>Database summary</b></p>
			<p><span id="db_name"></span></p>
			<p><span id="doc_count"></span></p>
			<p><span id="start_date"></span></p>
			<p><span id="end_date"></span></p>
		</div>
		
	</div>
</div>
</body>
</html>