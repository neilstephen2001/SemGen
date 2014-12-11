package semgen.annotation.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextPane;

import semgen.utilities.SemGenFont;
import semgen.utilities.uicomponent.SemGenDialog;
import semgen.utilities.uicomponent.SemGenScrollPane;
import semsim.SemSimConstants;
import semsim.model.SemSimModel;
import semsim.model.annotation.Annotation;
import semsim.model.physical.CompositePhysicalEntity;
import semsim.model.physical.PhysicalEntity;
import semsim.model.physical.PhysicalProcess;
import semsim.model.physical.PhysicalProperty;

public class SemanticSummaryDialog extends SemGenDialog implements PropertyChangeListener{

	private static final long serialVersionUID = -1850501792672929694L;
	public JOptionPane optionPane;
	
	public JPanel mainpanel = new JPanel();
	public SemSimModel semsimmodel;
	
	public SemanticSummaryDialog(SemSimModel semsimmodel){
		super("Biological summary");
		this.semsimmodel = semsimmodel;
		mainpanel.setLayout(new BoxLayout(mainpanel, BoxLayout.Y_AXIS));
		setDataInUI();
		SemGenScrollPane scroller = new SemGenScrollPane(mainpanel);
		scroller.setPreferredSize(new Dimension(800, 700));
		scroller.scrollToTopLeft();
		
		Object[] array = { scroller };
		
		optionPane = new JOptionPane(array, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_OPTION, null);
		
		Object[] options = new Object[]{"OK"};
		optionPane.setOptions(options);
		optionPane.setInitialValue(options[0]);
		setContentPane(optionPane);
		optionPane.addPropertyChangeListener(this);
		showDialog();
	}
	
	public void setDataInUI(){
		// Display model-level info
		JTextPane modelnamepane = new JTextPane();
		modelnamepane.setText(semsimmodel.getName());
		modelnamepane.setFont(SemGenFont.defaultBold(3));
		mainpanel.add(modelnamepane);
		for(Annotation ann : semsimmodel.getAnnotations()){
			if(ann.getRelation()==SemSimConstants.BQB_OCCURS_IN_RELATION){
				mainpanel.add(createPanelForAnnotation(ann.getRelation().getName(), ann.getValue().toString()));
			}
		}
		mainpanel.add(new JSeparator());
		
		// Display the physical properties
		JTextPane propslabel = new JTextPane();
		propslabel.setText("Physical properties");
		propslabel.setFont(SemGenFont.defaultPlain(2));
		mainpanel.add(propslabel);
		for(PhysicalProperty prop : semsimmodel.getPhysicalProperties()){
			if(prop.getPhysicalPropertyOf()!=null){
				JTextPane proppane = new JTextPane();
				proppane.setText(prop.getAssociatedDataStructure().getDescription() + " (codeword " + prop.getAssociatedDataStructure().getName() + ")");
				mainpanel.add(proppane);
			}
		}
		mainpanel.add(new JSeparator());
		JTextPane entitieslabel = new JTextPane();
		entitieslabel.setText("Physical entities");
		entitieslabel.setFont(SemGenFont.defaultBold(2));
		mainpanel.add(entitieslabel);
		
		// Display the entities
		for(PhysicalEntity ent : semsimmodel.getPhysicalEntities()){
			String suffix = "";
			if(ent.hasRefersToAnnotation())
				suffix = " (" + ent.getFirstRefersToReferenceOntologyAnnotation().getOntologyAbbreviation() + ")";
			else if(ent instanceof CompositePhysicalEntity) suffix = " (composite)";
			else suffix = " (custom)";
			JTextPane entpane = new JTextPane();
			entpane.setText(ent.getName() + suffix);
			mainpanel.add(entpane);
		}
		mainpanel.add(new JSeparator());
		
		// Display the processes
		JTextPane proclabel = new JTextPane();
		proclabel.setText("Physical processes");
		proclabel.setFont(SemGenFont.defaultBold(2));
		mainpanel.add(proclabel);
		for(PhysicalProcess proc : semsimmodel.getPhysicalProcesses()){
			String suffix = "";
			if(proc.hasRefersToAnnotation())
				suffix = " (" + proc.getFirstRefersToReferenceOntologyAnnotation().getOntologyAbbreviation() + ")";
			else suffix = " (custom)";
			JTextPane procpane = new JTextPane();
			procpane.setText(proc.getName() + suffix);
			mainpanel.add(procpane);
		}
	}
	
	public JPanel createPanelForAnnotation(String rel, String val){
		JPanel panel = new JPanel(new BorderLayout());
		JPanel subpanel = new JPanel();
		JTextPane valuepane = new JTextPane();
		valuepane.setText(val);
		subpanel.add(new JLabel(rel));
		subpanel.add(valuepane);
		panel.add(subpanel, BorderLayout.WEST);
		panel.add(Box.createGlue(), BorderLayout.EAST);
		return panel;
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		String val = optionPane.getValue().toString();
		if(val == "OK"){
			dispose();
		}
	}
}