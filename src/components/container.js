import React from 'react';
import { useD3 } from '../hooks/useD3';
import * as d3 from'd3';
import _ from 'underscore';
const Container = ({canvas,zoom,noZoom,force,getNodeIndex})=>{
    var node,link,text, modal_open=false;
    const contain = useD3((container)=>{
        force.on('tick',tick);
        link = container.selectAll(".link")

        .data(force.links())
        .enter()
        .append("line")
        .attr("class", "link")
        .attr("stroke", "black")
        .style("stroke-width", 1)
        .attr("stroke-dasharray", function(d) {
          if(d.linkType === 'associationPair') {
              return "0.9";
          }
          else if (d.linkType === 'optionalExclusiveContainmentPair') {
              return "5, 1";
          }
          else if (d.linkType === 'weakInclusiveContainmentPair') {
              return "5, 5, 1, 5";
          }
          else if (d.linkType === 'strongInclusiveContainmentPair') {
              return "15, 10, 5, 10";
          }
        })
        .attr("marker-end", "url(#arrow-head)"); 

        node = container.selectAll(".node")
            .data(force.nodes())
            .enter()
            .append("g")
            .attr("class", "node-container")
            .on("mousedown",
            function(d) {
                canvas.call(noZoom);
            })
            .on("mouseup",
            function() {
                canvas.call(zoom);
            })
            .on("dblclick", collect_data)
            .call(force.drag);
        
        node
            .append("circle")
            .attr("r", 6)
            .attr("class", "node")
            .attr("opacity", 0.75);
        
        text = node
            .append("text")
            .attr("x", 12)
            .attr("dy", ".35em")
            .text(function(d) { return d.name; })
            .style("font", "7px sans-serif");
             
           function tick() {
                link
                  .attr("x1", function(d) { return d.source.x; })
                  .attr("y1", function(d) { return d.source.y; })
                  .attr("x2", function(d) { return d.target.x; })
                  .attr("y2", function(d) { return d.target.y; });
            
            node
              .attr("transform",
              function(d) { return "translate(" + d.x + "," +  d.y + ")"; });
        }


    }) ;
  

    const slid =  useD3((sliderContainer)=>{
        var x = d3.scale.linear()
            .domain([0, 3]) // inputs
            .range([0, 200]) // outputs
            .clamp(true); // restrained to inputs


        sliderContainer
            .attr("transform", "translate(15," + 10 + ")")
            .append("g")
            .attr("class", "axis")
            .attr("transform", "translate(0," + 10 + ")")
            .call(d3.svg.axis()
            .scale(x)
            .orient("bottom")
            // .tickFormat(function(d) { return d + "%"; })
            .tickSize(0)
            .tickPadding(8))
            .select(".domain")
            .select(function() { return this.parentNode.appendChild(this.cloneNode(true)); })
            .attr("class", "halo");

        var brush = d3.svg.brush()
            .x(x)
            .extent([0, 0])
            .on("brush", brushed)
            .on("brushend", brushend);
            var slider = d3.select(".slider-container").append("g")
            .attr("class", "slider")
            .call(brush);
        
          var handle = slider.append("circle")
            .attr("class", "handle")
            .attr("transform", "translate(0," + 10 + ")")
            .attr("r", 9);
        
          slider
            .selectAll(".extent,.resize")
            .remove();
        
            // Functions
        
          function brushed() {
            var value = brush.extent()[0];
            canvas.call(noZoom);
            if (d3.event.sourceEvent) { // not a programmatic event
              value = x.invert(d3.mouse(this)[0]);
              brush.extent([value, value]);
            }
            handle.attr("cx", x(value));
          }
        
          function brushend() {
            var value = brush.extent()[0];
            canvas.call(zoom);
            node.attr("r,6+6*value/3.0");
        
            text.style("font-size",  7 + 7 * value/3.0 + "px");
        
            console.dir(text);
        
            force.linkDistance(50 + 50 * value);
            force.charge(-100 + -100 * value);
            force.start();
          }
    });


    function collect_data(d) {
      var umlData = {children: [], parents: [], rootNode: null, links: []};
      umlData.rootNode = d;
      _.each(force.links(), function(i) {
        if(i.source.name === d.name) {
          umlData.parents.push(i.target);
          umlData.links.push({source: d.name, target: i.target.name, linkType: i.linkType});
        }
        else if (i.target.name === d.name) {
          umlData.children.push(i.source);
          umlData.links.push({source: i.source.name, target: d.name, linkType: i.linkType});
        }
      });
      create_modal();
      render_uml(umlData, 900, 750);
    }
    
    
    
    function create_modal() {
        d3.select(".modal")
        .append("g")
        .attr("class", "modal-container")
        .attr("transform", "translate(" + 250 + "," + 50 + ")")
        .append("rect")
        .attr("width", 900)
        .attr("height", 750)
        .attr("fill", "white")
        .attr("stroke", "grey");
    
      d3.select(".modal-container")
        .append("g")
        .attr("class", "close-container")
        .attr("transform", "translate(" + 5 + "," + 15 + ")")
        .style("border", "0.5px solid grey")
        .on("click", function() {
          close_modal();
        })
        .append("text")
        .text("Close");

      modal_open = true;
      canvas.call(noZoom);
    }
    
    function close_modal() {
      d3.select(".modal").selectAll("g.modal-container").remove();
      modal_open = false;
      canvas.call(zoom);
    }
    
    function render_uml(data, width, height) {
      if(modal_open) {
    
        var linkData = [],
          nodeData = [],
          modal = d3.select(".modal").select("g.modal-container");
        
        for(var i = 0; i < data.children.length; i++) {
          var obj = _.omit(data.children[i],["px", "py", "x", "y", "weight", "index"]);
          nodeData.push(obj);
        }
    
        for(var j = 0; j < data.parents.length; j++) {
          var obj1 = _.omit(data.parents[j],["px", "py", "x", "y", "weight", "index"]);
          nodeData.push(obj1);
        }
    
        var main = _.omit(data.rootNode, ["px", "py", "x", "y", "weight", "index"]);
        nodeData.push(main);
    
        var nodes_attr_display = [];
        for(var k = 0; k < nodeData.length; k++) {
          nodes_attr_display.push(0);
        }
    
        _.each(data.links, function(d) {
          var source = getNodeIndex(nodeData, d.source);
          var target = getNodeIndex(nodeData, d.target);
          var linkType = d.linkType;
    
          linkData.push({source: source, target: target, linkType: linkType});
        });
    
        var force = d3.layout.force()
          .size([width, height])
          .linkDistance(400)
          .nodes(nodeData)
          .links(linkData)
          .charge(-800)
          .on("tick", tick);
    
        var nodes = modal.selectAll("g.node")
          .data(nodeData)
          .enter()
          .append("g")
          .attr("class", "modal-node")
          .call(force.drag)
          .on("click", function(d , i) {
    
            var thisContainer = (d3.select(this));
        
            if(nodes_attr_display[i] === 0) {
              nodes_attr_display[i] = 1;
        
              var a = thisContainer.selectAll("text.attributes")
                .data(d.attributes)
                .enter()
                .append("text")
                .attr("class", "attributes")
                .text(function(d) { return d.name + "[" + d.type +"]"; })
                .attr("y", function(d, i) { return -12 + i * -12; })
                .style("font", "10px sans-serif");
              } 
              else if (nodes_attr_display === 1) {
                  nodes_attr_display[i] = 0;
                  thisContainer.selectAll("text.attributes").remove();
              }
          });
    
        var links = modal.selectAll("g.modal-link")
          .data(force.links())
          .enter()
          .append("line")
          .attr("class", "modal-link")
          .attr("stroke", "black")
          .style("stroke-width", 1)
          // .attr("marker-end", "url(#arrow-head)");
          .attr("stroke-dasharray", function(d) {
            if(d.linkType === 'associationPair') {
                return "0.9";
            }
            else if (d.linkType === 'optionalExclusiveContainmentPair') {
                return "5, 1";
            }
            else if (d.linkType === 'weakInclusiveContainmentPair') {
                return "5, 5, 1, 5";
            }
            else if (d.linkType === 'strongInclusiveContainmentPair') {
                return "15, 10, 5, 10";
            }
          })
          .attr("marker-end", "url(#arrow-head)");
    
        nodes
          .append("circle")
          .attr("r", 10)
          .attr("opacity", 0.75);
    
        nodes
          .append("text")
          .attr("x", 12)
          .attr("dy", ".35em")
          .text(function(d) { return d.name; })
          .style("font", "12px sans-serif");
    
        force.start();
      }
    
      function tick() {
        links
          .attr("x1", function(d) { return d.source.x; })
          .attr("y1", function(d) { return d.source.y; })
          .attr("x2", function(d) { return d.target.x; })
          .attr("y2", function(d) { return d.target.y; });
    
        nodes
          .attr("transform",
          function(d) { return "translate(" + d.x + "," +  d.y + ")"; });
      }
    }
    return(
        <>
        <g className="force-container" ref={contain}></g>
        <g className="slider-container" ref={slid}></g>
        <g className="modal"></g>
        </>
    );
}
export default Container;
