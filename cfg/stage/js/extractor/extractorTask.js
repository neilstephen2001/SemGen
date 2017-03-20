/**
 * 
 */
//TODO: Save the current stage graph, clear it, and load relevant nodes of merge resolution.

ExtractorTask.prototype = new Task();
ExtractorTask.prototype.constructor = ExtractorTask;

function ExtractorTask(graph, stagestate) {
	Task.prototype.constructor.call(this, graph, stagestate);
	var extractor = this;
	
	extractor.graph.depBehaviors = [];
	extractor.graph.ghostBehaviors = [];
	extractor.extractions = [];
	
	this.extractionjs = null; //Handle for calling java functions
	extractor.taskindex = stagestate.taskindex;
	extractor.sourcemodel = null;
	
	var t = document.querySelector('#leftExtractorMenus');
	var clone = document.importNode(t.content, true);
	
	document.querySelector('#leftSidebar').appendChild(clone);

	var trash = new StageDoodad(this.graph, "trash", 0.1, 0.9, 2.0, 2.0, "glyphicon glyphicon-scissors");
	this.graph.doodads.push(trash);
	
	$("#addModelButton, .stageSearch").hide();
	
	var droploc;
	


	$("#stageModel").click(function() {
		var extractstostage = [];
		for (i in extractor.extractions) {
			if (extractor.extractions[i].selected) {
				extractstostage.push(extractor.extractions[i].modelindex);
			}
		}
		sender.sendModeltoStage(extractstostage);
	});
	
	$("#minimize").click(function() {
		sender.changeTask(0);
	});
	
	$("#saveModel").click(function() {
		var extractstosave = [];
		for (i in extractor.extractions) {
			if (!extractor.extractions[i].saved && extractor.extractions[i].selected)
				extractstosave.push(extractor.extractions[i].modelindex);
		}
		sender.save(extractstosave);
	});
	
	// Quit merger
	$("#quitExtractorBtn").click(function(e) {
		if (!extractor.isSaved()) {
			e.preventDefault();
            var r = confirm("Close without saving?");
            if (r) {
            	sender.close();
            }
		}
		else {
			sender.close();
		}
	});
}

ExtractorTask.prototype.setSavedState = function (issaved) {
	Task.prototype.setSavedState.call(issaved);
	this.setSaved(this.isSaved());
	$('#saveModel').prop('disabled', issaved);
}

//Everything that needs to be called after the stage and graph are set up.
ExtractorTask.prototype.onInitialize = function() {
	var extractor = this;
	
	extractor.state.models.forEach(function(model) {	
		extractor.sourcemodel = extractor.addModelNode(model, []);
	});
	sender.requestExtractions();
}

ExtractorTask.prototype.onMinimize = function() {
	$("#activeTaskText").removeClass('blink');
	sender.minimizeTask(this.task);
}

ExtractorTask.prototype.onModelSelection = function(srcmodel, node) {
	
}

ExtractorTask.prototype.onClose = function() {

}

ExtractorTask.prototype.getTaskType = function() { return StageTasks.EXTRACTOR; }