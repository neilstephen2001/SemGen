/**
 * 
 */
//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

MergerTask.prototype = new Task();
MergerTask.prototype.constructor = MergerTask;

function MergerTask(graph, state) {
	Task.prototype.constructor.call(this, graph, state);
	
	var merger = this;
	var semrespane;
	
	var t = document.querySelector('#leftMergerMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);

	this.showResolutionPane = function() {
		semrespane = new SemanticResolutionPane();
		semrespane.initialize(this.nodes);
	}
	$("#resolPanels").click(function() {
		$('#taskModal').modal("show");
	})
	
	// Quit merger
	$("#quitMergerBtn").click(function() {

	})	
	
	// Adds a dependency network to the d3 graph
	receiver.onShowDependencyNetwork(function (modelName, dependencyNodeData) {
		console.log("Showing dependencies for model " + modelName);
		graph.displaymode = DisplayModes.SHOWDEPENDENCIES;
		var modelNode = merger.getModelNode(modelName);
		modelNode.setChildren(dependencyNodeData, function (data) {
			return new DependencyNode(graph, data, modelNode);
		});
	});

	// Adds a submodel network to the d3 graph
	receiver.onShowSubmodelNetwork(function (modelName, submodelData) {
		console.log("Showing submodels for model " + modelName);
		graph.displaymode = DisplayModes.SHOWSUBMODELS;
		var modelNode = merger.getModelNode(modelName);
		modelNode.setChildren(submodelData, function (data) {
			return new SubmodelNode(graph, data, modelNode);
		});
	});

	receiver.onReceiveReply(function (reply) {
		CallWaiting(reply);
	});

	receiver.onReceiveReply(function (reply) {
		CallWaiting(reply);
	});
}

MergerTask.prototype.onInitialize = function() {
	if($("#mergerIcon").length == 0	) {
		$("#activeTaskPanel").append("<a data-toggle='modal' href='#taskModal'><img id='mergerIcon' src='../../src/semgen/icons/mergeicon2020.png' /></a>");
	}
	this.showResolutionPane();
}

MergerTask.prototype.onMinimize = function() {
	$("#activeTaskText").removeClass('blink');
	sender.minimizeTask(this.task);
}

MergerTask.prototype.onModelSelection = function(node) {

}

MergerTask.prototype.onClose = function() {
	$("#activeTaskText").removeClass('blink');
	$("#mergerIcon").remove();
	sender.minimizeTask(this.task);
}