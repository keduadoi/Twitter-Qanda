<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="template/header.html" %>
<title>View mutual information between users</title>
<script src="lib/d3/d3.v3.min.js"></script>
<style>

.node {
    fill: #ccc;
    stroke: #fff;
    stroke-width: 1px;
}

.link {
    stroke: #ccc;
}

</style>
<script>

$( document ).ready(function() {
	
    $(".loader").hide();
    
    $("#miSubmit").click(function(){
    	$("#graph").empty();
    	
    	$.ajax({
		    url: 'jsp/MutualInfoFetcher.jsp',
		    type: 'POST',
		    dataType: 'json',
		    data: { threshold: $("#thresholdInput").val()},
		    success: function (data, statusText, jqXHR) {
		    	$("#graph").css("background-color","#eeeeee");
		    	var links = [];
		    	for(var i=0;i<data.links.length;++i){
		    		console.log(JSON.stringify(data.links[i]));
		    		links.push(data.links[i]);
		    	}
		    	var nodes = {};

		    	// Compute the distinct nodes from the links.
		    	links.forEach(function(link) {
		    	  link.source = nodes[link.source] || (nodes[link.source] = {name: link.source});
		    	  link.target = nodes[link.target] || (nodes[link.target] = {name: link.target});
		    	});
		    	
		    	var width = 1024,
			    height = 900;

				var color = d3.scale.category20();
	
				var force = d3.layout.force()
				    .charge(-200)
				    .linkDistance(function(d) { return  8/(d.value); }) 
				    .size([width, height]);
	
				var svg = d3.select("#graph").append("svg")
				    .attr("width", width)
				    .attr("height", height);
	

				  force
				      .nodes(d3.values(nodes))
				      .links(links)
				      .start();
	
				  var link = svg.selectAll(".link")
				      .data(links)
				  .enter().append("line")
				      .attr("class", "link")
				      .style("stroke-width", function(d) { return 20*(d.value); });
	
				  var node = svg.selectAll(".node")
				      .data(d3.values(nodes))
				      .enter().append("circle")
				      .attr("class", "node")
				      .attr("r", 6)
				      .style("fill", function(d) { return "#3DF500";})
				      .call(force.drag);
	
				  node.append("title")
				      .text(function(d) { return d.name; });
	
				  force.on("tick", function() {
					  link.attr("x1", function(d) { return d.source.x; })
				        .attr("y1", function(d) { return d.source.y; })
				        .attr("x2", function(d) { return d.target.x; })
				        .attr("y2", function(d) { return d.target.y; });
	
				    node.attr("cx", function(d) { return d.x; })
				        .attr("cy", function(d) { return d.y; });
				  });
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
        	<h1>Generate mutual information graph between users</h1>
        </div>
        
        <div class="loader" style="padding-left:48%">
       		<img class="loading-image" src="loader.GIF" alt="loading.."/>
		</div>
                        
        <div class="row placeholders" style="float:left">
            <br/>
      		<form id="miForm" name="miForm" action="" method="post">
      			Select the threshold of mutual information
		    	<input type="range" name="thresholdSlider" id="thresholdSlider" min="0" max="1" value="0.1" step=0.01
		    		oninput="this.form.thresholdInput.value=this.value" />
				<input type="number" name="thresholdInput" id="thresholdInput" min="0" max="1" value="0.1" step=0.01
					oninput="this.form.thresholdSlider.value=this.value" />
				<br/><br/>
				<button type="button" class="btn btn-primary start" id="miSubmit" name="miSubmit">
		        	<i class="icon-upload icon-white"></i>
		            <span>Create graph</span>
		        </button>
		      	<br/><br/>
		    </form>
      
			<div id="graph" style="width: 1024px;height:900px"></div>
			<br/><br/><br/>
		</div>
		
	</div>
</div>

</body>
</html>