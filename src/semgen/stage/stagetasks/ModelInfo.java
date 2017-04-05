package semgen.stage.stagetasks;

import semgen.stage.serialization.ModelNode;
import semsim.model.collection.SemSimModel;
import semsim.reading.ModelAccessor;

public class ModelInfo extends StageRootInfo<ModelNode> {

	
	public ModelInfo(SemSimModel model, ModelAccessor path, Integer modindex) {
		super(model, path, modindex);
		modelnode = new ModelNode(model, modindex);
	}
	
	public ModelInfo(ModelInfo info, Integer modindex) {
		super(info, modindex);

		modelnode = new ModelNode(Model, modindex);
	}
	
	public String getModelName() {
		return Model.getName();
	}
}
