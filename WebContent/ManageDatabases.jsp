<!DOCTYPE HTML>
<!--
/*
 * jQuery File Upload Plugin Demo 6.9.1
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */
-->
<html lang="en">
<head>
<%@ include file="template/header.html" %>
<title>Create new database, upload data file</title>
<style type="text/css">
	#fileOperations{
		display: none;
	}
</style>
<script>
$(document).ready(function()
{	      		
	$("#dbSubmit").click(function(event){
		document.getElementById("fileOperations").style.display="none";
		var dbInput = $("#dbName").val();
		if(dbInput.length==0){
			  alert("Database name must not be empty");
			  return;
		}
		
		if($('select[name=dbSelect]').val()==0){
			$.ajax({
			    url: 'DatabaseOperations',
			    type: 'POST',
			    data: { dbInput: dbInput, operation:'get' },
			    success: function (data, statusText, jqXHR) {
			        if(jQuery.parseJSON(data).isExist=="true"){
			        	alert("\""+dbInput+"\" is selected");
				        setCookie("uploadDb", dbInput, 1);
				        $("#uploadDbText").empty();
				        $("#uploadDbText").append("Database to upload tweets: <b>"+dbInput+"</b>");
				        document.getElementById("fileOperations").style.display="block";
			        } else {
			        	alert("Database does not exist. Choose create new database option");
			        }
			    },
			    beforeSend: function(){
				       $('.loader:not(#dbLoader)').show();
				   },
				  complete: function(){
				       $('.loader:not(#dbLoader)').hide();
				 }
			});	 
		}else{
			$.ajax({
			    url: 'DatabaseOperations',
			    type: 'POST',
			    data: { dbInput: dbInput, operation:'create' },
			    success: function (data, statusText, jqXHR) {
			        if(jQuery.parseJSON(data).isExist=="true"){
			        	alert("Database existed");
			        } else {
			        	alert("\""+dbInput+"\" is created");
				        setCookie("uploadDb", dbInput, 1);
				        $("#uploadDbText").empty();
				        $("#uploadDbText").append("Database to upload tweets: <b>"+dbInput+"</b>");
				        document.getElementById("fileOperations").style.display="block";
			        }
			    },
			    beforeSend: function(){
				       $('.loader:not(#dbLoader)').show();
				   },
				  complete: function(){
				       $('.loader:not(#dbLoader)').hide();
				 }
			});	 
		}
		  
	});
	
	$('.loader').hide();
	
	$("#dbManageSubmit").click(function(event){
		$("#selectedDb").empty();		
		  var dbInput = $("#dbManage").val();
		  if(dbInput.length==0){
			  alert("Database name must not be empty");
			  return;
		  }
		   $.ajax({
		    url: 'DatabaseOperations',
		    type: 'POST',
		    data: { dbInput: dbInput , operation: 'get'},
		    success: function (data, statusText, jqXHR) {
		        if(jQuery.parseJSON(data).isExist=="true"){
		        	$("#dbNameHidden").val(dbInput);
		        	$("#dbDocsHidden").val(jQuery.parseJSON(data).doc_count);
		        	$("#selectedDb").append(
		        		"<table border=0 cellpadding='10'><tr>"
		        		+"<td><strong>"+dbInput+"</strong></td>"
		        		+"<td>#tweets: "+jQuery.parseJSON(data).doc_count+"</td>"
		        		+"<td>"
		        		+"<button type='button' class='btn btn-primary start' id='fetchDb' name='fetchDb'>"
			        	+"<i class=icon-upload icon-white'></i>"
			            +"<span>Fetch</span>"
			        	+"</button>"
		        		+"</td>"
		        		+"<td>"
		        		+"<button type='button' class='btn btn-primary start' id='traceDb' name='traceDb'>"
			        	+"<i class=icon-upload icon-white'></i>"
			            +"<span>Generate discussions</span>"
			        	+"</button>"
		        		+"</td>"
		        		+"</tr></table>"
		        	);
		        } else {
		        	alert("Database does not exist. Please create and upload data before analyzing")
		        }
		    },
		    beforeSend: function(){
			       $('#dbLoader').show();
			   },
			  complete: function(){
			      $('#dbLoader').hide();			       
			  }
		});	   
	  });
        	
	$(document).on('click', '#fetchDb', function(){ 
		var dbName = $("#dbNameHidden").val();
		var docsBeforeFetch = $("#dbDocsHidden").val();
		$.ajax({
		    url: 'DatabaseOperations',
		    type: 'POST',
		    data: { dbInput: dbName, operation: 'fetch' },
		    success: function (data, statusText, jqXHR) {
		    	$("#selectedDb").empty();
		    	$("#selectedDb").append(
		        		"<table border=0 cellpadding='10'><tr>"
		        		+"<td><strong>"+dbName+"</strong></td>"
		        		+"<td>#tweets before fetching: "+docsBeforeFetch+"</td>"
		        		+"<td>#tweets after fetching: "+jQuery.parseJSON(data).doc_count+"</td>"
		        		+"<td>"
		        		+"<button type='button' class='btn btn-primary start' id='fetchDb' name='fetchDb'>"
			        	+"<i class=icon-upload icon-white'></i>"
			            +"<span>Fetch</span>"
			        	+"</button>"
		        		+"</td>"
		        		+"<td>"
		        		+"<button type='button' class='btn btn-primary start' id='traceDb' name='traceDb'>"
			        	+"<i class=icon-upload icon-white'></i>"
			            +"<span>Generate discussions</span>"
			        	+"</button>"
		        		+"</td>"
		        		+"</tr></table>"
		        );
		    	$("#dbDocsHidden").val(jQuery.parseJSON(data).doc_count);
		    },
		    beforeSend: function(){
		    	//$("#dbLoader").append("<h5>Estimation fetching time: "+Math.round(0.03*docsBeforeFetch)+" minutes</h5>");
			    $('#dbLoader').show();
			   },
			  complete: function(){
				  //$("#dbLoader").children("h5").remove();
			       $('#dbLoader').hide();
			  }
		});	   
	 });
	
	
	$(document).on('click', '#traceDb', function(){ 
		var dbName = $("#dbNameHidden").val();
		var docsBeforeFetch = $("#dbDocsHidden").val();
		$.ajax({
		    url: 'DatabaseOperations',
		    type: 'POST',
		    data: { dbInput: dbName, operation: 'trace' },
		    success: function (data, statusText, jqXHR) {
		    	$("#selectedDb").empty();
		    	$("#selectedDb").append(
		        		"<table border=0 cellpadding='10'><tr>"
		        		+"<td><strong>"+dbName+"</strong></td>"
		        		+"<td>#tweets: "+docsBeforeFetch+"</td>"
		        		+"<td>"
		        		+"<button type='button' class='btn btn-primary start' id='fetchDb' name='fetchDb'>"
			        	+"<i class=icon-upload icon-white'></i>"
			            +"<span>Fetch</span>"
			        	+"</button>"
		        		+"</td>"
		        		+"<td>"
		        		+"<button type='button' class='btn btn-primary start' id='traceDb' name='traceDb'>"
			        	+"<i class=icon-upload icon-white'></i>"
			            +"<span>Generate discussions</span>"
			        	+"</button>"
		        		+"</td>"
		        		+"</tr></table>"
		        );
		    },
		    beforeSend: function(){
		    	//$("#dbLoader").append("<h5>Estimation fetching time: "+Math.round(0.03*docsBeforeFetch)+" minutes</h5>");
			    $('#dbLoader').show();
			   },
			  complete: function(){
				  //$("#dbLoader").children("h5").remove();
			       $('#dbLoader').hide();
			  }
		});	   
	 });
	
});
</script>   
     
</head>
<body onload="checkDbCookie();">
<%@ include file="template/navbar.html" %>

<div class="container">
	<div class="clearfix">
    	<div class="page-header">
        	<h2>Upload tweets data file into database</h2>
        </div>        
        
        <div class="loader" style="padding-left:48%">
       		<img class="loading-image" src="loader.GIF" alt="loading.."/>
		</div>            
                    
        <div class="row placeholders">
        	<!-- The file upload form used as target for the file upload widget -->
            <form id="fileupload" action="upload" method="POST" enctype="multipart/form-data">
            	<br><br/>
            	Select database option	<select name="dbSelect">
            								<option value="0">Use an existing database</option>
            								<option value="1">Create new database</option>
            							</select>
            	<br/><br/>
            	Enter database name <input type="text" placeholder="e.g. qanda" name="dbName" id="dbName"/>
            	<button type="button" class="btn btn-primary start" id="dbSubmit" name="dbSubmit">
		        	<i class="icon-upload icon-white"></i>
		            <span>Submit</span>
		        </button>
				<br/><br/>
				<p><span id="uploadDbText"></span></p>			
							
                <!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
                <div class="row fileupload-buttonbar" id="fileOperations">
                	<div class="span7">
                    	<!-- The fileinput-button span is used to style the file input field as button -->
                        <span class="btn btn-success fileinput-button">
                        	<i class="glyphicon glyphicon-plus"></i>
                            <span>Add files...</span>
                            <input type="file" name="files[]" multiple accept=".txt,.json,.dat">
                        </span>
                        <button type="submit" class="btn btn-primary start">
                        	<i class="glyphicon glyphicon-star-empty"></i>
                            <span>Start upload</span>
                        </button>
                        <button type="reset" class="btn btn-warning cancel">
                        	<i class="glyphicon glyphicon-ban-circle"></i>
                            <span>Cancel upload</span>
                        </button>
                        <button type="button" class="btn btn-danger delete">
                        	<i class="glyphicon glyphicon-minus"></i>
                            <span>Delete</span>
                         </button>
                         <input type="checkbox" class="toggle">
                  	</div>
                    <span><i>File types accepted: .txt | .dat | .json</i></span>
                    <!-- The global progress information -->
                    <div class="span5 fileupload-progress fade">
                    	<!-- The global progress bar -->
                        <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100">
                        	<div class="bar" style="width:0%;"></div>
                        </div>
                        <!-- The extended global progress information -->
                        <div class="progress-extended">&nbsp;</div>
                    </div>
      			</div>
                
                <!-- The loading indicator is shown during file processing -->
                <div class="fileupload-loading"></div>
               	<br>
                <!-- The table listing the files available for upload/download -->
                <table role="presentation" class="table table-striped"><tbody class="files" data-toggle="modal-gallery" data-target="#modal-gallery"></tbody></table>
          	</form>
            <br>       
             
    	</div>
 	</div>
 	
 	<div class="clearfix">
    	<div class="page-header">
        	<h2>Database management</h2>
        </div>                    
                    
        <div class="row placeholders">
        	Enter database name <input type="text" placeholder="e.g. qanda" name="dbManage" id="dbManage"/>
            <button type="button" class="btn btn-primary start" id="dbManageSubmit" name="dbManageSubmit">
		        	<i class="icon-upload icon-white"></i>
		            <span>Select this database</span>
		        </button>
			<br/><br/>	       
			<input type="hidden" name="dbNameHidden" id="dbNameHidden" value=""/>
			<input type="hidden" name="dbDocsHidden" id="dbDocsHidden" value=""/>
    	</div>
    	<br/><br/>
    	
    	<div class="panel panel-default">  
	        <div class="panel-heading">
	            <h3 class="panel-title">Database information</h3>
	            <div class="loader" id="dbLoader">
	            	<br/>
	       			<img class="loading-image" src="loader.GIF" alt="loading.."/>
				</div>
				
	        </div>
	        <div class="panel-body" id="selectedDb">
	        	
	        </div>
    	</div>
    	<br/><br/><br/>
 	</div>
</div>
     
<!-- The template to display files available for upload -->
<script id="template-upload" type="text/x-tmpl">
			{% for (var i=0, file; file=o.files[i]; i++) { %}
            <tr class="template-upload fade">
            <td class="preview"><span class="fade"></span></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            {% if (file.error) { %}
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
            {% } else if (o.files.valid && !i) { %}
            <td>
            <div class="progress progress-success progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="bar" style="width:0%;"></div></div>
            </td>
            <td class="start">{% if (!o.options.autoUpload) { %}
            <button class="btn btn-primary">
            <i class="glyphicon glyphicon-upload"></i>
            <span>{%=locale.fileupload.start%}</span>
            </button>
            {% } %}</td>
            {% } else { %}
            <td colspan="2"></td>
            {% } %}
            <td class="cancel">{% if (!i) { %}
            <button class="btn btn-warning">
            <i class="glyphicon glyphicon-ban-circle"></i>
            <span>{%=locale.fileupload.cancel%}</span>
            </button>
            {% } %}</td>
            </tr>
            {% } %}            
</script>
<!-- The template to display files available for download -->
<script id="template-download" type="text/x-tmpl">
            {% for (var i=0, file; file=o.files[i]; i++) { %}
            <tr class="template-download fade">
            {% if (file.error) { %}
            <td></td>
            <td class="name"><span>{%=file.name%}</span></td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td class="error" colspan="2"><span class="label label-important">{%=locale.fileupload.error%}</span> {%=locale.fileupload.errors[file.error] || file.error%}</td>
            {% } else { %}
            <td class="preview">{% if (file.thumbnail_url) { %}
            <a href="{%=file.url%}" title="{%=file.name%}" rel="gallery" download="{%=file.name%}"><img src="{%=file.thumbnail_url%}"></a>
            {% } %}</td>
            <td class="name">
            <a href="{%=file.url%}" title="{%=file.name%}" rel="{%=file.thumbnail_url&&'gallery'%}" download="{%=file.name%}">{%=file.name%}</a>
            </td>
            <td class="size"><span>{%=o.formatFileSize(file.size)%}</span></td>
            <td colspan="2"></td>
            {% } %}
            <td class="delete">
            <button class="btn btn-danger" data-type="{%=file.delete_type%}" data-url="{%=file.delete_url%}">
            <i class="glyphicon glyphicon-minus"></i>
            <span>{%=locale.fileupload.destroy%}</span>
            </button>
            <input type="checkbox" name="delete" value="1">
            </td>
            </tr>
            {% } %}
</script>

<script src="github/js/jquery.min.js"></script>
<!-- The jQuery UI widget factory, can be omitted if jQuery UI is already included -->
<script src="js/vendor/jquery.ui.widget.js"></script>
<!-- The Templates plugin is included to render the upload/download listings -->
<script src="github/js/tmpl.min.js"></script>
<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
<script src="github/js/load-image.min.js"></script>
<!-- The Canvas to Blob plugin is included for image resizing functionality -->
<script src="github/js/canvas-to-blob.min.js"></script>
<!-- Bootstrap JS and Bootstrap Image Gallery are not required, but included for the demo -->
<script src="github/js/bootstrap.min.js"></script>
<script src="github/js/jquery.blueimp-gallery.min.js"></script>
<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
<script src="js/jquery.iframe-transport.js"></script>
<!-- The basic File Upload plugin -->
<script src="js/jquery.fileupload.js"></script>
<!-- The File Upload file processing plugin -->
<script src="js/jquery.fileupload-fp.js"></script>
<!-- The File Upload user interface plugin -->
<script src="js/jquery.fileupload-ui.js"></script>
<!-- The localization script -->
<script src="js/locale.js"></script>
<!-- The main application script -->
<script src="js/main.js"></script>
<!-- The XDomainRequest Transport is included for cross-domain file deletion for IE8+ -->
<!--[if gte IE 8]><script src="js/cors/jquery.xdr-transport.js"></script><![endif]-->
                
</body> 
</html>
