import React from 'react';
import {useD3} from '../hooks/useD3.js';
const Marker = ()=>{
    const ref = useD3((marker)=>{
        marker.attr("id", "arrow-head")
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 22)
        .attr("refY", 0)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M0,-5L10,0L0,5 L10,0 L0, -5")
        .style("stroke", "#000");
    });
    return(
        <marker ref={ref}></marker>
    );
}
export default Marker;