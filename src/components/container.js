import React from 'react';
import { useD3 } from '../hooks/useD3';
import * as d3 from 'd3';
import _ from 'underscore';
var modal_open=false;
const Container = ({force,canvas})=>{
    //console.log(force);
    const ref = useD3((container)=>{
        force.on('tick',tick);
       // console.log(force.links())
        var link = container.selectAll(".link")
        .data(force.links())
        .enter()
        .append("line")
        .attr("class", "link")
        .attr("stroke", "black")
        .style("stroke-width", 1)
        .attr("stroke-dasharray", function(d) {
        	//console.log(d.linkType)
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
            function collect_data(d) {
                var umlData = {children: [], parents: [], rootNode: null, links: []};

                umlData.rootNode = d;
                force.links().map((i)=> {
                if(i.source.name === d.name) {
                // console.log(i);
                umlData.parents.push(i.target);
                // link data
                umlData.links.push({source: d.name, target: i.target.name, linkType: i.linkType});
                }
                else if (i.target.name === d.name) {
                umlData.children.push(i.source);
                umlData.links.push({source: i.source.name, target: d.name, linkType: i.linkType});
                    }
                });
                // console.log(umlData);

                create_modal();

                render_uml(umlData, 900, 750);

            }
            function getNodeIndex(nodes,nodeName){
                var index =  null;
                var instances = 0;
                nodes.map((node, i) => {
                    if(node.name === nodeName) {
                        instances++;
                        index = i;
                    }
                });
                return index;
            }
            function render_uml(data, width, height) {
                if(modal_open) {

                var linkData = [],
                nodeData = [],
                modal = canvas.select("g.modal-container");

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

                //console.log(nodes_attr_display);
                data.links.map((d) =>{
                // console.log(d);
                // console.log(d.source);
                // console.log(d.target);
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
                    } else if (nodes_attr_display === 1) {
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
        } 
        function create_modal() {
            var x = 40,
                y = 50,
                width = 900,
                height = 750;

            canvas
                .append("g")
                .attr("class", "modal-container")
                .attr("transform", "translate(" + x + "," + y + ")")
                .append("rect")
                .attr("width", width)
                .attr("height", height)
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
            }
            function close_modal() {
                canvas.selectAll("g.modal-container").remove();
                modal_open = false;
            }
    });
    return(
        <g className="force-container" ref={ref}></g>
    );
}
export default Container;
