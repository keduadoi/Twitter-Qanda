var network_page = {

 animate:null,
 width:null,
 force:null,
 height:null, 
 detachednodes:null,
 detachededges:null,
 nodesize:5,
 position:null,
 name_regex:/@[_a-z0-9]+/gi,
 url_regex:/https?:\/\/[a-zA-Z0-9\.\/]+/gi,
 hashtag_regex:/#[a-z0-9]+/gi,
 node_color: "#FFEDA0",
 node_stroke_color:"#000000",
 num_classes: 7,
 //node_color_scheme: ["#A6BDDB","#74A9CF","#3690C0","#0570B0","#045A8D","#023858"],//removed white from the list
 //node_color_scheme4:["#CCEBC5","#A8DDB5","#7BCCC4","#4EB3D3","#2B8CBE","#0868AC","#084081"],//multi hue without bad colors seq 9 without white//"#E0F3DB",
 script_location:'jsp/NetworkFetcher.jsp',
 jsondata:{}, 
 net:null,
 nm:{}, //node map holds the new ids of the nodes
 linkg:null, 
 selected_user:"",
 link:null, 
 nodeg:null, 
 node:null,
 //curve = d3.svg.line().interpolate("cardinal-closed").tension(.85),
 //http://10.211.16.41:8080/Tweettracker-Dashboard/
 remove_addl_info: function(tweet){
	//removes hashtags, usernames, urls to identify the core text of the tweet
	var tw = tweet.replace(this.name_regex,'');
	tw = tw.replace(this.hashtag_regex,'');
	tw = tw.replace(this.url_regex,'');	
	return tw;
 },

 flattenHTObj: function (obj){
       var str = '';
	   var jsonobj = {};
       for(var candidate in obj){
			jsonobj[candidate] = {};
			jsonobj[candidate].color = obj[candidate].color;
			var tg = [];
           for(var tag = 0; tag < obj[candidate].hts.length; tag++)
		   {
			   tg[tag] = obj[candidate].hts[tag];
               /*var t = obj[candidate].hts[tag];
               if(t.substring(0,1) === '#'){
                   t = t.slice(1);
               }
               if(str !== ''){str += ',';}
               str += candidate + '.' + t;*/
           }
		   jsonobj[candidate].hts = tg;
       }
       return jsonobj;
   },

create_dataset: function(data, prev, index){
if(network_page.nm["totalnodes"]>0)
 {
	network_page.nm = {};
	network_page.nm["totalnodes"] = 0;
 }
  var 
      linksmap = {},    // link map
      nodes = [], // output nodes
      links = [], // output links
	  lm = {}; //link map: holds the new links created
  // store nodes
  for (var k=0; k<data.nodes.length; ++k) {
    var n = data.nodes[k],
        i = index(n)[0]; //first value is the current nodes id, this should never be null	
		network_page.nm[n.id] = nodes.length;
		if(!network_page.nm["totalnodes"])
		{
			network_page.nm["totalnodes"]=1;
		}
		else
		{
			network_page.nm["totalnodes"]++;
		}
		//gm[n.id] = 1;
		nodes.push(n);	
  }
 
 //store links
	for (k=0; k<data.links.length; k++) 
	{
		var e = data.links[k],
		u = e.source,
		v = e.target;
		u = network_page.nm[e.source];
		v = network_page.nm[e.target];
		if((u!==undefined) && (v!==undefined))
		{	
			var l = {source: u,target: v,value:1, data:e.data};
			//linksmap[i] = e;
			links.push(l);
		}		
	}
  return {nodes: nodes, links: links};
 
 },

/**
*	Create a pie chart summarizing the retweeted tweets of the clicked user
*/ 
draw_color_histogram: function(histogram_count){
	// clear the earlier pie chart. jit is not clearing it.
	$jit.id('histogram').innerHTML = '';	
	//create pie/bar chart and set the color scheme	
	var pieChart = new $jit.PieChart({
	//id of the visualization container
      injectInto: 'histogram',
      //whether to add animations
      animate: true,
      orientation: 'vertical',
      //bars separation
      barsOffset: 20,
      //visualization offset
      Margin: {
        top:5,
        left: 5,
        right: 5,
        bottom:5
      },
      //labels offset position
      labelOffset: 5,
	  //bars style
      type: 'stacked',
      //whether to show the labels for the bars
      showLabels:false,
      //labels style
      /*Label: {
        type: none, //Native or HTML
        size: 13,
        family: 'Sans-Serif',
        color: 'white'
      },*/
      //add tooltips
      Tips: {
        enable: true,
        onShow: function(tip, elem) {
          tip.innerHTML = elem.value;
        }
      }
    });
    //get the color codes
	var labels = histogram_count['label'];
	var legend = [];
	for(var i=0;i<labels.length;i++)
	{
		legend.push($jit.util.rgbToHex(labels[i].split(",")));
	}
	pieChart.colors = legend;
    //load JSON data.
	pieChart.loadJSON(histogram_count);
    //end  
	
},

nodeid: function (n) {
  return n.size ? "_g_"+n.group : n.name;
},

linkid: function (l) {
  var u = network_page.nodeid(l.source),
      v = network_page.nodeid(l.target);
  return u<v ? u+"|"+v : v+"|"+u;
},

getGroup: function (n) 
{ 
return n.group; 
},

create_network: function() {
//don't do anything if there is no data. Data can only be loaded through state util updates
if(!network_page.jsondata) return;

if (network_page.force) network_page.force.stop();

$('#graph').html('');
  
  var width = network_page.width,
	  height = network_page.height,
	  r = 6;//buffer for the container edge, if the node positions are beyond this they will be pulled back into the screen
	  
 /**
   * Step 1: Create the nodes and the links in the network
   */
  network_page.net = network_page.create_dataset(network_page.jsondata, network_page.net, network_page.getGroup);
  /**
   * Step 2: Initialize the D3 layout with the data and node settings that define the forces acting on the nodes
   */
  network_page.force = d3.layout.force()
      .nodes(network_page.net.nodes)
      .links(network_page.net.links)
      .size([width,height])
	  .charge(-500)
      .linkDistance(function(d) { return  200.0*(d.value); }) 
	  .theta(0.8)
	  .gravity(0.2)
      .start();
	
 var svg = d3.select("#graph").append("svg")
    .attr("width", width)
    .attr("height", height);

	//marker definition for the arrowhead goes in the defs section of the svg element containing the visualization
  var defs = svg.append("svg:defs");
  defs.append("svg:marker")
				.attr("id", "arrowhead")
				.attr("viewBox","0 0 20 20")
				.attr("refX","30")
				.attr("refY","10")
				.attr("markerUnits","strokeWidth")
				.attr("markerWidth","4")
				.attr("markerHeight","8")
				.attr("orient","auto")
				.append("svg:path")
				.attr("d","M 0 0 L 20 10 L 0 20 z");
  /*var gradient = defs.append("svg:linearGradient")
				.attr("id","gradientedge")				
				.attr("spreadMethod","pad");
	gradient.append("svg:stop")
				.attr("offset","0%")
				.attr("stop-color","#000000")
				.attr("stop-opacity","1");
	gradient.append("svg:stop")
			.attr("offset","100%")
			.attr("stop-color","#00cc00")
			.attr("stop-opacity","1");
  */
  network_page.linkg = svg.append("g");
  network_page.nodeg = svg.append("g");
   /**
    * Step 3: Identify and mark the direction of the links and the node sizes
    */
  link = network_page.linkg.selectAll("line.link").data(network_page.net.links, network_page.linkid);
  link.exit().remove();
  link.enter().append("line")
	.attr("class", function(edge){
			var str = "link";
			if(edge.data!=undefined)
			{
				str += " "+edge.data;
			}
			str += " node-id-"+edge.source.id;
			str += " node-id-"+edge.target.id;
			return str;
	  })
      .attr("x1", function(d) { return d.source.x; })
      .attr("y1", function(d) { return d.source.y; })
      .attr("x2", function(d) { return d.target.x; })
      .attr("y2", function(d) { return d.target.y; })	  
      .style("stroke-width", function(d) { return 2.4; })
	  .attr("marker-end","url(#arrowhead)");
	  
  node = network_page.nodeg.selectAll("circle.node").data(network_page.net.nodes, network_page.nodeid);
  node.exit().remove();
  node.enter().append("circle")
	  .attr("data",function(n){return n.id})
      .attr("r", function(n){return (n.group+1)*network_page.nodesize;}) //network_page.nodesize-0.75)
      .attr("cx", function(d) { return d.x; })
      .attr("cy", function(d) { return d.y; })
	  .attr("stroke", function(d) {	return network_page.node_stroke_color; })
	  .attr("stroke-width", function(d) { return 2})	  
      .style("fill", function(d) { return d.catColor;});
	/**
    * Step 3: Add node event handler for "click" event to generate the information panel.
    */
  node.on("click", function(node) {
			if(!node) return;			
			// Build the right column relations list.
			// Link to the Twitter profile of the user under focus
			var html = "<h6><a href='http://www.twitter.com/#!/"+node.name+"' target='_blank'>" + node.name + "</a></h6><ul>",
				tweetlist = [], 
				tweet_count = {}; //structure of tweet_count {tweet1 : [0:count,1:color],tweet2 : [0:count,1:color]}
			//from color brewer 12 classes, qualitative
			var color_scheme = ['141,211,199','166,219,160','190,186,218','251,128,114','128,177,211','253,180,98','179,222,105','252,205,229','217,217,217','188,128,189','204,235,197','255,237,111'],
			text_color_scheme = ['0,0,0'],
				color_counter = 0;
			
			var fromnode = {},
				connections = [],
				nodeinfo = {};
			fromnode["name"] = node.name;
			nodeinfo["node"] = fromnode;
			for(var i=0;i<node.data.length;i++)
			{
				var tonodeinfo = {};
				tonodeinfo["name"] = node.data[i].nodeto;
				tonodeinfo["tweet"] = node.data[i].tweet;
				if(node.data[i].tweet in tweet_count)
				{
					tweet_count[node.data[i].tweet][0] = tweet_count[node.data[i].tweet][0]+1;
				}
				else
				{
					var valarray = [];
					valarray[0] = 1;
					valarray[1] = color_scheme[color_counter];	//text block background color
					valarray[2] = text_color_scheme[0];	//text font color
					tweet_count[node.data[i].tweet] = valarray;
					color_counter++;
					if(color_counter>color_scheme.length)
					{
						color_counter = 0;
					}
				}	
				connections.push(tonodeinfo);		
			}	
			nodeinfo["connections"] = connections;
			//var r=217,g=119,b=5;  // orange
			//var r=80,g=129,b=27;  //green
			//var r=215,g=48,b=31; //map red
			//stores the colors and their values
			var histogram_vals = {};
			for(var i=0;i<node.data.length;i++)
			{
				var tweet = node.data[i].tweet;
				var nameTo = node.data[i].nodeto;
				//var class_code =  node.data[i].classcode;
				if(tweet_count[tweet][0]==1)
				{
					//only one of its kind, then do nothing
					var connection = "<li style=\"color:#28333A;\">@"+nameTo+": "+tweet+"</li>";
					tweetlist.push(connection);
				}
				else
				{
					//total no of tweets >= no of tweets of this kind >1 					
					var connection = "<li style=\"background-color:rgb("+tweet_count[tweet][1]+");color:rgb("+tweet_count[tweet][2]+")\">@"+nameTo+": "+tweet+"</li>";
					//;\" onclick=\"network_page.filter_graph("+class_code+")
					tweetlist.push(connection);					
					if(histogram_vals[tweet_count[tweet][1]]!=undefined)
					{
						histogram_vals[tweet_count[tweet][1]] = histogram_vals[tweet_count[tweet][1]]+1;
					}
					else
					{
						histogram_vals[tweet_count[tweet][1]] = 1;
					}
				}
			}
			//create the pie chart and assign to it to its div
			//create the dataset
			var labels = [],
				values = [];
			var hist_color = {};
			var noofemptyvals=0;
			for(var color in histogram_vals)
			{
				labels.push(color);
				var label_value = {};
				label_value['label'] = color;
				var val = [];
				//insert empty values for the stacked bar chart to adjust the colors
				for(var i=0;i<noofemptyvals;i++)
				{
					val.push(0);
				}
				val.push(histogram_vals[color]);
				label_value['values'] =  val;				
				values.push(label_value);
				noofemptyvals++;
			}
			hist_color['label'] = labels;
			hist_color['values'] = values;
			//draw the pie chart
			network_page.draw_color_histogram(hist_color);
			//append connections information
			$jit.id('tweetinfo').innerHTML = html + tweetlist.join("") + "</li></ul>";
  });  
  
  //add tooltip  
  node.append("title")
	  .append("svg:text")
	  .attr("class", "nodetext")
      .attr("dx", 12)
      .attr("dy", ".35em")
      .text(function(d) { 
			var count = d.data.length;
			var str = d.name+"\n Retweeted by: "+d.size;
			return str;
			});
	  
  node.call(network_page.force.drag);
/**
   * Step 4: Compute the distance between nodes after each time the forces are computed using the tick event.
   */
  network_page.force.on("tick", function() {

    link.attr("x1", function(d) { return d.source.x; })
        .attr("y1", function(d) { return d.source.y; })
        .attr("x2", function(d) { return d.target.x; })
        .attr("y2", function(d) { return d.target.y; });

    node.attr("cx", function(d) { return d.x; })
    	.attr("cy", function(d) { return d.y; });
    
  });

}
};

function getRandomColor() {
    var letters = '0123456789ABCDEF'.split('');
    var color = '#';
    for (var i = 0; i < 6; i++ ) {
        color += letters[Math.floor(Math.random() * 16)];
    }
    return color;
}

	   
//call this on load
$(document).ready(function()
{  
   var filter = {"Group1": {"color":"#FF0000","hts":["#zuccotti"]}, "Group2": {"color":"#3DF500","hts":["#nypd"]}};
   var hashtags_json = {};
   var numnodes = 5;
   
   //get query string
   var qs = (function(a) {
	    if (a == "") return {};
	    var b = {};
	    for (var i = 0; i < a.length; ++i)
	    {
	        var p=a[i].split('=');
	        if (p.length != 2) continue;
	        b[p[0]] = decodeURIComponent(p[1].replace(/\+/g, " "));
	    }
	    return b;
	})(window.location.search.substr(1).split('&'));
   numnodes = qs['top_nodes'];
   
   //handle time period
   from = qs['from'];
   to = qs['to'];
   
   if(qs['hashtags']!=''){
	   var hashtags = qs['hashtags'].split(';');
	   for(var i=0;i<hashtags.length;++i){
		   var group_index = "Group"+(i+1);
		   var hashtag = "#"+hashtags[i];
		   var hts = [];
		   hts.push(hashtag);
		   var color = getRandomColor();
		   var group_desc = {"color":color,"hts":hts};
		   hashtags_json[group_index] = group_desc;
	   }
   }
   
   var params = {};
   params.filename = "tweets_single_lines.dat";
   params.nclasses = network_page.num_classes;
   params.hashtagjson = hashtags_json;
   params.num_nodes = numnodes;
   params.from = from;
   params.to = to;
   
   $("#downloadTweets").click(function(event){
	   $("#download").submit();
   });
   
   $.ajax(network_page.script_location,
		{// 1303628399,12:59 1303588799
		//2011-04-23 12:00:00, 2011-04-23 12:59:59
			data: {filename:'tweets_single_lines.dat', nclasses: network_page.num_classes, hashtagjson: JSON.stringify(hashtags_json),num_nodes:numnodes,from:from,to:to},
			dataType: 'json',
			success: function (data, statusText, jqXHR) {
				console.log(JSON.stringify(data));
			network_page.jsondata = data;
			network_page.width = $('#center-container').width();//897;//
			network_page.height = $('#center-container').height();//697;
			network_page.create_network();
			$("#tweetsJson").val(JSON.stringify(params));
			},
			beforeSend: function(){
			       $('.loader').show();
			       $("#downloadTweets").hide();
			   },
			  complete: function(){
			       $('.loader').hide();
			       //$("#downloadTweets").show();
			       
			  }
		});	
});