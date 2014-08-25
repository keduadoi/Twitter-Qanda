//call this on load
$(document).ready(function()
{  
   $.ajax('jsp/TraceDiscussions.jsp',
		{// 1303628399,12:59 1303588799
		//2011-04-23 12:00:00, 2011-04-23 12:59:59
			data: {filename:'tweets_single_lines.dat'},
			dataType: 'json',
			success: function (data, statusText, jqXHR) {
				//console.log(JSON.stringify(data));
				
			}
		});	
});