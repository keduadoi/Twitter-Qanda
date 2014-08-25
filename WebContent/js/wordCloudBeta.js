    window.onload = function()
{
	$.ajax('jsp/WordCloud.jsp',
            {
                data: {filename:'tweets_single_lines.dat',k:60},
                dataType: 'json',
                success: function (data, statusText, jqXHR) {
				var canvas = document.getElementById('canvas');
				var options = {};
				options.gridSize = 18;
				options.weightFactor = 3;
				options.fondFamily = 'Finger Paint, cursive, sans-serif';
				options.color = '#f0f0c0';
				/*options.hover = 'window.drawBox';
				options.click = 'function(item){\n' +
								'alert(item[0] + \': \' + item[1]);\n' +
								'}';*/
				options.backgroundColor = '#001f00';
				var top_key_words = [];
				console.log('OK: '+JSON.stringify(data));
				for(var i=0;i<data.length;++i){
					console.log(data[i].text);
					var hashtag_array = new Array(data[i].text, data[i].size);
					top_key_words.push(hashtag_array);
				}
				options.list = top_key_words;
				WordCloud(canvas, options);
                }
            });
}
  