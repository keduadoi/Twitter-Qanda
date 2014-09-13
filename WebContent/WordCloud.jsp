<!DOCTYPE html>
<html>
<head>
<link type="text/css" href="styles/graph.css" rel="stylesheet" />
<%@ include file="template/header.html" %>
<title>See top keywords</title>
<link type="text/css" href="styles/tagcloud.css" rel="stylesheet" />		
<script src="lib/jquery/jquery-1.7.1.js"></script>
<script src="js/wordCloudBeta.js"></script>
<script src="lib/wordcloud2/wordcloud2.js"></script>
</head>

<body onload="checkDbCookie()">
<%@ include file="template/navbar.html" %>

<div class="container">
    <div class="clearfix">
    	<div class="page-header">
        	<h1>Top mentioned keywords</h1>
        </div>
      	<div class="row placeholders">
        	<div class="loader">
        	<h4>Processing time can be up to 2 minutes...</h4>
       			<img class="loading-image" src="loader.GIF" alt="loading.."/>
			</div>
			
			<div id="center-container">
				<canvas id="canvas" class="canvas" width="800" height="540"></canvas>
				<br/><br/>
			</div>
		</div>
	</div>
</div>
</body>
</html>