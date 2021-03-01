import React from 'react';
import * as d3 from 'd3';

export const useD3 = (renderFn,dependencies) =>{
    const ref = React.useRef();

    React.useEffect(()=>{
        renderFn(d3.select(ref.current));
        return () => {};
    },dependencies);
    return ref;
}