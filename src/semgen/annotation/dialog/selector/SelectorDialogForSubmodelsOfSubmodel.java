package semgen.annotation.dialog.selector;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JCheckBox;

import semgen.annotation.annotatorpane.AnnotationPanel;
import semgen.annotation.workbench.AnnotatorWorkbench;
import semsim.model.physical.Submodel;

public class SelectorDialogForSubmodelsOfSubmodel extends SemSimComponentSelectorDialog implements  PropertyChangeListener{
	private static final long serialVersionUID = 1L;
	public Submodel submodel;

	public SelectorDialogForSubmodelsOfSubmodel(
			AnnotatorWorkbench wb,
			AnnotationPanel anndia,
			Set<Submodel> setofsubs, 
			Submodel sub2ignore,
			Submodel sub,
			Set<Submodel> preselected,
			Set<Submodel> substodisable,
			Boolean withdescriptions,
			String title){
		super(wb, setofsubs, sub2ignore, preselected, substodisable, withdescriptions, title);
		this.anndia = anndia;
		this.submodel = sub;
		setUpUI(this);
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent e) {
		String propertyfired = e.getPropertyName();
		if (propertyfired.equals("value")) {
				String value = optionPane.getValue().toString();
			
			if (value == "OK") {
				Component[] complist = panel.getComponents();
				// Remove previous submodel nesting data
				submodel.setSubmodels(new HashSet<Submodel>());
				// Set the new data
				for (int r = 0; r < complist.length; r++) {
					if (complist[r] instanceof JCheckBox) {
						JCheckBox box = (JCheckBox) complist[r];
						if (box.isSelected()) {
							submodel.addSubmodel(workbench.getSemSimModel().getSubmodel(box.getText()));
						}
					}
				}
				anndia.refreshSubmodelData();
				workbench.setModelSaved(false);
			}
			dispose();
		}
	}
}