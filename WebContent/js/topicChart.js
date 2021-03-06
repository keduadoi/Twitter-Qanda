var cat_chart = {

	script_location: 'jsp/TopicChart.jsp',
	CreateTopicChart:function(json)
	{
		var r = Raphael("vizpanel");                    
         r.dotchart(10, 10, 1000, 500, json.xcoordinates, json.ycoordinates, json.data, {symbol: "o", max: 20, heat: true, axis: "0 0 1 1", axisxstep: json.axisxstep, axisystep: json.axisystep, axisxlabels: json.axisxlabels, axisxtype: "  ", axisytype: "|", axisylabels: json.axisylabels}).hover(function () {
                    this.marker = this.marker || r.tag(this.x, this.y, this.value, 0, this.r + 2).insertBefore(this);
                    this.marker.show();
                }, function () {
                    this.marker && this.marker.hide();
                });           
	}
};

//call this on load
window.onload = function()
{
        
       //call the initializer for infovis graph panel
       //cat_chart.pre_init();	   
       $.ajax(cat_chart.script_location,
            {
                data: {filename:'ows.json'},
                dataType: 'json',
                success: function (data, statusText, jqXHR) {
                cat_chart.CreateTopicChart(data);
                }
            });
			
	
}