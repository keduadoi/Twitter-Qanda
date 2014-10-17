<!DOCTYPE html>
<html>
<head>
<%@ include file="template/header.html" %>
<title>Retweeted network</title>

<!-- CSS Files -->
<link type="text/css" href="styles/graph.css" rel="stylesheet" />
<!-- jQuery Library File -->
<script language="javascript" type="text/javascript" src="lib/jquery/jquery-1.7.1.js"></script>
<!-- JIT Library File & D3 library file-->
<script language="javascript" type="text/javascript" src="lib/d3/d3.v2.js"></script>
<script language="javascript" type="text/javascript" src="lib/jit/jit.js"></script>

<!-- Example File -->
<script language="javascript" type="text/javascript" src="js/network.js"></script>
<script>
$(document).ready(function(){
	$("li#analysisNav").toggleClass('activate');
});
</script>
</head>
<body onload="checkDbCookie()">
<!-- navigation bar -->
<%@ include file="template/navbar.html" %>

<div class="container">
<div class="clearfix">
                    <!--<div class="container">-->
                        <div class="page-header">
                        	<br/>
                            <h1>User's retweeted network representation</h1>
                        </div>
                        <div class="row placeholders">
<div class="loader">
		<h4>Processing time can be up to 2 minutes...</h4>
       <img class="loading-image" src="loader.GIF" alt="loading.."/>
</div>
<div id="center-container">
    <div id="graph"></div>    
</div>
<!--<p>Only nodes with at least 1 retweet are shown in the network.</p>-->
<div id="right-container">
		<form id="download" name="download" action="download" method="post">
		 	<br/>
		 	<input type="hidden" id="tweetsJson" name="tweetsJson" value=""/>
		 	<button type="button" class="btn btn-primary start" id="downloadTweets" name="downloadTweets">
            	<i class="icon-upload icon-white"></i>
                <span>Download tweets</span>
            </button>
		 	<br/><br/>
		 </form>
         <div id="tweetinfo">
		 </div>
		 <div id="histogram">
		 </div>
		 <div id="status">
		 </div>
		 
</div>
</div>
</div>
</div>
</body>
</html>