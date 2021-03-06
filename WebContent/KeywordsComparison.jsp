<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<%@ include file="template/header.html" %>
<title>View mutual information between users</title>
<link href="styles/trendcomparison.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="lib/d3/d3.js"></script>
<!-- <script type="text/javascript" src="js/trendComparison.js"></script> -->
<script>
function validate(){
	if($("#keywords").val().length==0){
		alert("Enter at least 1 keyword");
		return false;
	}
	
	  if($("#from").val().length==0 || $("#to").val().length==0){
		  alert("Please select beginning and end dates")
		  return false;
	  }
	  
	  return true;
}

var trendcomp = {
		script_location: 'jsp/TrendComparison.jsp',
		margin: {top: 20, right: 80, bottom: 30, left: 50},
	    width:null,
	    height:null,
		parseDate: d3.time.format("%d %b %Y %H:%M").parse,
		x:null,
		y:null,
		color: d3.scale.category10(),
		xAxis:null,
		yAxis:null,
		line:null,
		svg:null

	};

	function Initialize(){
		trendcomp.width = 1024 - trendcomp.margin.left - trendcomp.margin.right,
	    trendcomp.height = 540 - trendcomp.margin.top - trendcomp.margin.bottom;
		trendcomp.x = d3.time.scale()
	    .range([0, trendcomp.width]);
		trendcomp.y = d3.scale.linear()
	    .range([trendcomp.height, 0]);
		trendcomp.xAxis = d3.svg.axis()
			.scale(trendcomp.x)
			.orient("bottom");
		trendcomp.yAxis = d3.svg.axis()
			.scale(trendcomp.y)
			.orient("left");
		trendcomp.line = d3.svg.line()
			.interpolate("basis")
			.x(function(d) { return trendcomp.x(d.date); })
			.y(function(d) { return trendcomp.y(d.count); });
		trendcomp.svg = d3.select("#vizpanel").append("svg")
			.attr("width", trendcomp.width + trendcomp.margin.left + trendcomp.margin.right)
			.attr("height", trendcomp.height + trendcomp.margin.top + trendcomp.margin.bottom)
			.append("g")
			.attr("transform", "translate(" + trendcomp.margin.left + "," + trendcomp.margin.top + ")");	
	}

	function GenerateGraph(data)
	{
		  trendcomp.color.domain(d3.keys(data[0]).filter(function(key) { return key !== "date"; }));
		  data.forEach(function(d) {
			d.date = trendcomp.parseDate(d.date);
		  });

		  var words = trendcomp.color.domain().map(function(word) {
			return {
			  word: word,
			  values: data.map(function(d) {
				return {date: d.date, count: +d[word]};
			  })
			};
		  });

		  trendcomp.x.domain(d3.extent(data, function(d) { return d.date; }));

		  trendcomp.y.domain([
			d3.min(words, function(c) { return d3.min(c.values, function(v) { return v.count; }); }),
			d3.max(words, function(c) { return d3.max(c.values, function(v) { return v.count; }); })
		  ]);

		  trendcomp.svg.append("g")
			  .attr("class", "x axis")
			  .attr("transform", "translate(0," + trendcomp.height + ")")
			  .call(trendcomp.xAxis);

		  trendcomp.svg.append("g")
			  .attr("class", "y axis")
			  .call(trendcomp.yAxis)
			  .append("text")
			  .attr("transform", "rotate(-90)")
			  .attr("class", "ylabel")
			  .attr("dy", ".71em")
			  .attr("text-anchor", "end")
			  .attr("y", -40)
			  .text("Number of Tweets");

		  var word = trendcomp.svg.selectAll(".word")
			  .data(words)
			  .enter().append("g")
			  .attr("class", "word");

		  word.append("path")
			  .attr("class", "line")
			  .attr("d", function(d) { return trendcomp.line(d.values); })
			  .style("stroke", function(d) { return trendcomp.color(d.word); });

		  word.append("text")
			  .datum(function(d) { return {word: d.word, value: d.values[d.values.length - 1]}; })
			  .attr("transform", function(d) { return "translate(" + trendcomp.x(d.value.date) + "," + trendcomp.y(d.value.count) + ")"; })
			  .attr("x", 3)
			  .attr("dy", ".35em")
			  //.text(function(d) { return d.word; });	//comment this to remove the legend beside the trendline
			  
		  var legend = trendcomp.svg.selectAll(".legend")
			  .data(trendcomp.color.domain().slice().reverse())
			  .enter().append("g")
			  .attr("class", "legend")
			  .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

		  legend.append("rect")
			  .attr("x", trendcomp.width - 18)
			  .attr("width", 18)
			  .attr("height", 18)
			  .style("fill", trendcomp.color);

		  legend.append("text")
			  .attr("x", trendcomp.width - 24)
			  .attr("y", 9)
			  .attr("dy", ".35em")
			  .style("text-anchor", "end")
			  .text(function(d) { return d; });
				

	}

$( document ).ready(function() {
	$("li#analysisNav").toggleClass('activate');
	
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
	
    $("#keywordSubmit").click(function(){
    	if(validate()==false){
    		return false;
    	}
    	
    	//$("#vizpanel").empty();
    	
    	$.ajax({
		    url: 'jsp/TrendComparison.jsp',
		    type: 'POST',
		    dataType: 'json',
		    data: { keywords: $("#keywords").val(),from:$("#from").val(),to:$("#to").val()},
		    success: function (data, statusText, jqXHR) {
		    	$("#vizpanel").empty();
		    	Initialize();
		    	//console.log(JSON.stringify(data));
		    	GenerateGraph(data);
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
        	<h1>Keywords comparison</h1>
        </div>
        
        <div class="loader" style="padding-left:48%">
       		<img class="loading-image" src="loader.GIF" alt="loading.."/>
		</div>
                        
        <div class="row placeholders">
            <br/>
      		<form id="keywordsCompareForm" name="keywordsCompareForm" action="" method="post">
      			Enter keywords (keywords separated by semicolon, hashtag must begin by "#")<br/>
      			<input type="text" name="keywords" id="keywords" value=""/>
				<br/><br/>
				From <input type="text" name="from" id="from" readonly="readonly" style="background:white;"/>
				To <input type="text" name="to" id="to" readonly="readonly" style="background:white;"/>
				<br/><br/>
				<button type="button" class="btn btn-primary start" id="keywordSubmit" name="keywordSubmit">
		        	<i class="icon-upload icon-white"></i>
		            <span>Create comparison graph</span>
		        </button>
		      	<br/><br/>
		    </form>
      
			<div id="vizpanel"></div>
			<br/><br/><br/>
		</div>
		
	</div>
</div>

	
</body>
</html> 