var tag_cloud = {

	script_location: 'jsp/WordCloud.jsp',
	layout:null,
	
	DrawCloud:function(words)
	{
		var fill = d3.scale.category10();
		d3.select("#cloudpane").append("svg")
		.append("g")
        .attr("transform", "translate(400,400)")
		.selectAll("text")
        .data(words)
		.enter().append("text")
        .style("font-size", function(d) { return d.size + "px"; })
        .style("font-family", "Impact")
        //.style("fill", function(d, i) { return fill(i); })
        .attr("text-anchor", "middle")
        .attr("transform", function(d) {
          return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")";
        })
        .text(function(d) { return d.text; });
	}
  
};  
window.onload = function()
{
	$.ajax(tag_cloud.script_location,
            {
                data: {filename:'tweets_single_lines.dat',k:60},
                dataType: 'json',
                success: function (data, statusText, jqXHR) {
				tag_cloud.layout = d3.layout.cloud().size([350, 400])
				  .words(data)
				  .rotate(function() { return ~~(Math.random() * 2) * 1; })
				  .font("Times New Roman")
				  .fontSize(function(d) { return d.size; })
				  .padding(200)
				  .on("end", tag_cloud.DrawCloud)
				  .start();
				  
                }
            });
}
  