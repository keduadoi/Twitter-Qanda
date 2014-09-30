<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="template/header.html" %>
<title>Top influenced users</title>
<script>

$( document ).ready(function() {
	
    $(".loader").hide();
    
    $("#influenceSubmit").click(function(){
    	$("#info").empty();
    	var numOfNodes = parseInt($("#numOfNodes").val());
    	
    	$.ajax({
		    url: 'jsp/InfluenceCalculation.jsp',
		    type: 'POST',
		    dataType: 'json',
		    data: { influenceType:$('input[name=influenceType]:checked','#influenceForm').val()},
		    success: function (data, statusText, jqXHR) {
				console.log(JSON.stringify(data));
				if(numOfNodes==0){
					numOfNodes = data.nodes.length;
				}
				switch(parseInt($('input[name=influenceType]:checked','#influenceForm').val())){
				case 0:
					$("#info").append("<h3>Top "+numOfNodes+" who were retweeted most</h3>");
					$("#info").append("<table cellpadding=5 id='nodesTable'>");
					$("#nodesTable").append("<tr><th>Username</th><th>Number of retweeted</th></tr>")
					for(var i=0;i<numOfNodes;++i){
						$("#nodesTable").append("<tr><td>"+data.nodes[i].name
								+"</td><td>"+JSON.stringify(data.nodes[i].value)+"</td></tr>");
					}
					$("#info").append("</table>");
					break;
				case 1:
					$("#info").append("<h3>Top "+numOfNodes+" who have most influence</h3>");
					$("#info").append("<table cellpadding=5 id='nodesTable'>");
					$("#nodesTable").append("<tr><th>Username</th><th>Eigen score</th></tr>")
					for(var i=0;i<numOfNodes;++i){
						$("#nodesTable").append("<tr><td>"+data.nodes[i].name
								+"</td><td>"+JSON.stringify(data.nodes[i].value)+"</td></tr>");
					}
					$("#info").append("</table>");
					break;
				case 2:
					$("#info").append("<h3>Top "+numOfNodes+" who control the flow of information</h3>");
					$("#info").append("<table cellpadding=5 id='nodesTable'>");
					$("#nodesTable").append("<tr><th>Username</th><th>Betweeness score</th></tr>")
					for(var i=0;i<numOfNodes;++i){
						$("#nodesTable").append("<tr><td>"+data.nodes[i].name
								+"</td><td>"+data.nodes[i].value+"</td></tr>");
					}
					$("#info").append("</table>");
					break;
				default:
					break;
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

});
</script>
</head>
<body onload="checkDbCookie()">
<!-- navigation bar -->
<%@ include file="template/navbar.html" %>
<div class="container">
    <div class="clearfix">
    	<div class="page-header">
        	<h1>Evaluate influence, centrality of users</h1>
        </div>
        
        <div class="loader" style="padding-left:48%">
       		<img class="loading-image" src="loader.GIF" alt="loading.."/>
		</div>
                        
        <div class="row placeholders" style="float:left">
            <br/>
      		<form id="influenceForm" name="influenceForm" action="" method="post">
      			Select type of influence<br/>
		    	<input type="radio" name="influenceType" value="0" checked="checked" /> In degree<br/>
		    	<input type="radio" name="influenceType" value="1" /> Eigen vector<br/>
		    	<input type="radio" name="influenceType" value="2" /> Betweenness
				<br/><br/>
				Select number of users to be showed
				<select name="numOfNodes" id="numOfNodes">
					<option value="10">Top 10</option>
					<option value="20">Top 20</option>
					<option value="30">Top 30</option>
					<option value="0">Display all users</option>
				</select>
				<br/><br/>
				<button type="button" class="btn btn-primary start" id="influenceSubmit" name="influenceSubmit">
		        	<i class="icon-upload icon-white"></i>
		            <span>See top users</span>
		        </button>
		      	<br/><br/>
		    </form>
      
			<div id="info"></div>
			<br/><br/><br/>
		</div>
		
	</div>
</div>
</body>
</html>