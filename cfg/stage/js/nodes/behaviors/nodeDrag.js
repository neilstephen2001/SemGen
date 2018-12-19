/**
 *
 */

function NodeDrag() {
    // When the node visualization is created add a dropzone element
    // and listen for dragging.
    // When a model is dragging all other models will display dropzones. If the model
    // is released on a dropzone the merger will open with those models

    var virtualnodes = null;
    var cntrlIsPressedBefore;
    var nodeDragged = false;
    var nodeDrag = d3.drag()
        .on("start", function (_node) {
            cntrlIsPressedBefore = _node.graph.cntrlIsPressed;
            if (cntrlIsPressedBefore) {
                main.task.selectNode(_node);
                return;
            }

            main.task.selectNodeOnDrag(_node);
            _node.graph.pause();

            //Ensure the node has been added to selections
            var selections = _node.multiDrag();

            if (_node.graph.shiftIsPressed) {
                virtualnodes = _node.graph.createGhostNodes(selections);
                selections = virtualnodes;
            }
            selections.forEach(function(n) {
                if (n.nodeType == NodeType.FUNCTION) {
                    n.rootElement.selectAll("rect").attr("width", _node.r*4);
                    n.rootElement.selectAll("rect").attr("height", _node.r*4);
                }
                else {
                    n.rootElement.selectAll("circle").attr("r", _node.r * 2);
                }
                n.fx = _node.xpos();
                n.fy = _node.ypos();
            });

            if (!_node.graph.shiftIsPressed) {
                //Execute any drag behaviors unique to the node type
                _node.dragstart.forEach(function(behavior) {
                    behavior(_node);
                });
            }

            _node.graph.tick();
        })
        .on("drag", function (_node) {
            _node.graph.pause();

            if (cntrlIsPressedBefore) return;
            var dx = d3.event.x - _node.xpos(),
                dy = d3.event.y - _node.ypos();
            var selections = _node.multiDrag();

            if(dx!=0 || dy!=0) {
                nodeDragged = true;
            }

            if (_node.graph.shiftIsPressed) {
                selections = virtualnodes;
                dx = d3.event.x - selections[0].xpos(),
                    dy = d3.event.y - selections[0].ypos();
            }
            else if (!_node.selected) {
                selections = [_node];
            }

            if (!_node.graph.shiftIsPressed && virtualnodes) {
                _node.graph.clearTemporaryObjects();
                virtualnodes = null;
                return;
            }

            selections.forEach(function(n) {
                var posx = n.xpos()+dx,
                    posy = n.ypos()+dy;

                n.setLocation(posx, posy);
            });
            if (!_node.graph.shiftIsPressed) {
                //Execute any drag behaviors unique to the node type
                _node.drag.forEach(function(behavior) {
                    behavior(_node);
                });
            }
            else {
                virtualnodes[0].drag.forEach(function(behavior){
                    behavior(virtualnodes);
                });
            }
            _node.graph.tick();

        })
        .on("end", function (_node) {
            if (cntrlIsPressedBefore) {
                if (_node.nodeType == NodeType.FUNCTION) {
                    _node.rootElement.selectAll("rect")
                        .attr("width", _node.r*2)
                        .attr("height", +node.r*2);
                }
                else {
                    _node.rootElement.selectAll("circle").attr("r", _node.r);
                }

                return;
            }
            var selections = _node.multiDrag();

            if (!_node.graph.shiftIsPressed) {
                if (!_node.selected) {
                    selections = [_node];
                }

                selections.forEach(function(n) {
                    if (n.nodeType == NodeType.FUNCTION) {
                        n.rootElement.selectAll("rect")
                            .attr("width", n.r*2)
                            .attr("height", n.r*2);
                    }
                    else {
                        n.rootElement.selectAll("circle").attr("r", n.r);
                    }
                    n.setLocation(n.fx, n.fy);
                });
                //Execute any drag behaviors unique to the node type
                _node.dragend.forEach(function(behavior) {
                    behavior(_node);
                });

                selections.forEach(function(n) {
                    if (!n.fixed) {
                        n.fx = null;
                        n.fy = null;
                    }
                });
            }
            else {
                virtualnodes[0].dragEnd.forEach(function(behavior){
                    behavior(virtualnodes);
                });
            }
            _node.graph.clearTemporaryObjects();
            virtualnodes = null;

            if (!_node.graph.fixedMode && nodeDragged) {
                _node.graph.resume();
                nodeDragged = false;
            }
        });

    // Add the dragging functionality to the node
    return nodeDrag;
};