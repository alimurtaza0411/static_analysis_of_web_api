import React from 'react';
import { useD3 } from '../hooks/useD3';
const Legend = ({legendInfo})=>{
    const ref = useD3((legendContainer)=>{
        legendContainer.selectAll("line.legend-line")
            .data(legendInfo)
            .enter()
            .append("line")
            .attr("class", "legend-line")
            .attr("x1", 10)
            .attr("x2", 150)
            .attr("y1", function(d, i) { return d.y; })
            .attr("y2", function(d, i) { return d.y; })
            .attr("stroke-dasharray", function(d) { return d.lineStyle; })
            .attr("stroke-width", 2)
            .attr("stroke", "black");

        legendContainer.selectAll("text.legend-text")
            .data(legendInfo)
            .enter()
            .append("text")
            .attr("class", "legend-text")
            .attr("x", 10)
            .attr("y", function(d, i) { return d.y-5; })
            .text(function(d) { return d.name; })
            .style("font", "10px sans-serif");
            },legendInfo);
  
    
    return(
        <g className="legend-container" ref={ref}></g>
    );
}
export default Legend;