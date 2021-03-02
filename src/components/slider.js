import React from 'react';
import * as d3 from'd3';
import { useD3 } from '../hooks/useD3';
const Slider = ({canvas,zoom,noZoom,container,force})=>{
    const ref = useD3((sliderContainer)=>{
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
            console.log(container);
            var node = container.selectAll(".node")
              .attr("r", 6 + 6 * value/3.0);
        
            var text = container.selectAll(".node-container text")
              .style("font-size",  7 + 7 * value/3.0 + "px");
        
            console.dir(text);
        
            force.linkDistance(50 + 50 * value);
            force.charge(-100 + -100 * value);
            force.start();
          }
    })
    return(
        <g className="slider-container" ref={ref}></g>
    );
}
export default Slider;