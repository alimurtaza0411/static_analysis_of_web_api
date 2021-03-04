import React from 'react';
import { useD3 } from '../hooks/useD3';
import * as d3 from'd3';
const Container = ({canvas,zoom,noZoom,collect_data,force,parentRef})=>{
    var node,link,text;
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
    return(
        <>
        <g className="force-container" ref={contain}></g>
        <g className="slider-container" ref={slid}></g>
        </>
    );
}
export default Container;