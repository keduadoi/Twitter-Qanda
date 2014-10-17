<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">'
<%@ include file="template/header.html" %>
<title>Download raw tweets data</title>
<script>

$( document ).ready(function() {
	$("li#downloadNav").toggleClass('activate');
	$('#timePeriod').css('display','none');
	
	$( "#period" ).change(function() {
		if($('#period').val()=='specifyTime'){
			$('#timePeriod').css('display','block');
		}else{
			$('#timePeriod').css('display','none');
		}
	});
	
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
	
    $(".loader").hide();
    
    $("#downloadTweets").click(function(event){  	
    	
    	if($("#period").val()=='specifyTime'){
    		if($("#from").val().length==0 || $("#to").val().length==0){
    			  alert("Please select beginning and end dates")
    			  return false;
    		}
    	}
    	
    	$("#currentDb").val(getCookie("currentDb"));
    	$("#download").submit();
    });

});
</script>
</head>
<body onload="checkDbCookie()">
<!-- navigation bar -->
<%@ include file="template/navbar.html" %>

<div class="container">
    <div class="clearfix">
    	<div class="page-header">
        	<h1>Download raw tweets data</h1>
        </div>
        
        <div class="loader" style="padding-left:48%">
       		<img class="loading-image" src="loader.GIF" alt="loading.."/>
		</div>
                        
        <div class="row placeholders" style="float:left">
            <br/>
            <form id="download" name="download" action="download" method="post">
			 	<b>Select time period</b>&nbsp; 
			 	<select id='period' name='period'>
			 		<option value="lastHour">Last hour</option>
			 		<option value="today">Today</option>
			 		<option value="lastWeek">Last week</option>
			 		<option value="lastMonth">Last month</option>
			 		<option value="specifyTime">Specify time period...</option>
			 	</select>
			 	<br/><br/>
			 	<div id='timePeriod'>
			 		<input type="hidden" id="currentDb" name="currentDb" value=""/>
				 	From <input type="text" name="from" id="from" readonly="readonly" style="background:white;"/>
					To <input type="text" name="to" id="to" readonly="readonly" style="background:white;"/>
					<br/><br/>
				</div>
			 	<button type="button" class="btn btn-primary start" id="downloadTweets" name="downloadTweets">
	            	<i class="icon-upload icon-white"></i>
	                <span>Download tweets</span>
	            </button>
			 	<br/><br/>
			 </form>
      		
		</div>
		
	</div>
</div>

</body>
</html>