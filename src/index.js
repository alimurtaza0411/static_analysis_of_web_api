import React, {Component,useEffect} from 'react';
import ReactDOM from 'react-dom';
import * as d3 from'd3';
import {entities,associationPair,optionalExclusiveContainmentPair,exclusiveContainmentPair,weakInclusiveContainmentPair,strongInclusiveContainmentPair} from './data/OpenShip1.json';
import Marker from './components/marker.js';
import Container from './components/container.js';
import Legend from './components/legend.js';
import Slider from './components/slider.js';
import Modal from './components/modal.js';
class App extends Component {
    constructor(props){
        super(props);
        this.state = {
            legendInfo:[
                {name: "Association", lineStyle: "0.9", y: 115},
                {name: "Exclusive Containment (Mandatory)", lineStyle: null, y: 165},
                {name: "Inclusive Containment (Optional)", lineStyle: "5, 5, 1, 5", y: 65},
                {name: "Inclusive Containment (Mandatory)", lineStyle: "15, 10, 5, 10", y: 140},
                {name: "Exclusive Containment (Optional)", lineStyle: "5, 1", y: 90}
            ],
            width:1500,
            height:960,
            chargeDist:-100,
            linkDist:50,
            links:[],
            modal_open:false,
            title:"Business Entity Data Model: FedEx OpenShipping Service",
            nodes:entities
        }  

    }
    
    componentWillMount(){
        var a = this.createLinks(optionalExclusiveContainmentPair,this.state.nodes,'optionalExclusiveContainmentPair');
        var b = this.createLinks(associationPair, this.state.nodes, 'associationPair');
        var c = this.createLinks(exclusiveContainmentPair, this.state.nodes, 'exclusiveContainmentPair');
        var d = this.createLinks(weakInclusiveContainmentPair, this.state.nodes, 'weakInclusiveContainmentPair');
        var e = this.createLinks(strongInclusiveContainmentPair, this.state.nodes, 'strongInclusiveContainmentPair');
        var l = [...a,...b,...c,...d,...e];
        this.setState({
            links:l
        },()=>{
            console.log(this.state.links);
        });
        
        this.setState({ 
            canvas:d3.select('svg')
                .attr("class", "canvas")
                .attr("width", this.state.width)
                .attr("height", this.state.height)
                .on("click", function() {
                })
        },()=>{
        });
        
        this.setState({ 
            force:d3.layout.force()
            .size([this.state.width, this.state.height])
            .linkDistance(20)
            .charge(-100)
            .nodes(this.state.nodes)
            .links(this.state.links)
            .start(),
        },()=>{
        });
        
    }
    createLinks(linkData, nodes, linkType){
        if(linkData.length === 0) {
            console.log("warning! - No Link data");
          }
          var l =[];
          linkData.map((link) => {
            var parentIndex = this.getNodeIndex(nodes, link.strMainEntity),
                childIndex = this.getNodeIndex(nodes, link.strSlaveEntity);
            l.push({source: parentIndex, target: childIndex, linkType: linkType})
          });
          return l;
    }
    getNodeIndex(nodes,nodeName){
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
    render() {
        return(
            <svg className="canvas" width="1500" height="960">
                <defs><Marker/></defs>
                <Container force={this.state.force} />
                <Legend legendInfo={this.state.legendInfo} />
                <Slider/>
                {this.state.modal_open}?<Modal/>:{null};
            </svg>
        )
    }
}
ReactDOM.render(<App/>, document.getElementById('root'));