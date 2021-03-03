import React from 'react';
import { useD3 } from '../hooks/useD3';

const Container = ({canvas,zoom,noZoom,collect_data,force,parentRef})=>{
    parentRef = useD3((container)=>{
        force.on('tick',tick);
        var link = container.selectAll(".link")
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
        
        var node = container.selectAll(".node")
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
        
        node
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
              });
    return(
        <g className="force-container" ref={parentRef}></g>
    );
}
export default Container;
