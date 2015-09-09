/** 
 *The main content pane of SemGen and the level where most of the action takes place. 
 *Tasks involving the individual tabs, such as creation and closing are handled here
 */
package semgen;

import org.semanticweb.owlapi.model.OWLException;

import semgen.annotation.AnnotationTabFactory;
import semgen.annotation.workbench.AnnotatorFactory;
import semgen.extraction.ExtractorTabFactory;
import semgen.extraction.workbench.ExtractorFactory;
import semgen.menu.SemGenMenuBar;
import semgen.merging.MergerTabFactory;
import semgen.merging.workbench.MergerWorkbenchFactory;
import semgen.stage.StageTabFactory;
import semgen.stage.StageWorkbenchFactory;
import semgen.utilities.SemGenError;
import semgen.utilities.SemGenTask;
import semgen.utilities.Workbench;
import semgen.utilities.WorkbenchFactory;
import semgen.utilities.uicomponent.SemGenProgressBar;
import semgen.utilities.uicomponent.SemGenTab;
import semgen.utilities.uicomponent.TabFactory;

import java.net.URI;
import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class SemGenGUI extends JTabbedPane implements Observer{
	private static final long serialVersionUID = 3618439495848469800L;

	private SemGenSettings settings;
	protected GlobalActions globalactions;

	private ArrayList<SemGenTab> opentabs = new ArrayList<SemGenTab>(); //Open annotation tabs

	private SemGenMenuBar menu;

	public SemGenGUI(SemGenSettings sets,  SemGenMenuBar menubar, GlobalActions gacts){
		settings = sets;
		menu = menubar;
		globalactions = gacts;
		globalactions.addObserver(this);
		
		setPreferredSize(sets.getAppSize());
		setOpaque(true);
		setBackground(Color.white);
	}
	
	// METHODS
	//*****Tab creation methods****
	//These methods each create two object factories; one is for creating the workbench and the 
	//other is for creating the tab. The two factory objects are then passed to the addTab
	//method.
	//The Annotator and Extractor methods each have an overloaded method which allows the
	//creation of a new workbench and tab using an already specified file.
	
	public void startNewAnnotatorTask(){
		AnnotatorFactory factory = new AnnotatorFactory(settings.doAutoAnnotate());
		if (factory.isValid()) {
			int i = 0;
			for (URI uri : factory.getFileURIs()) {
				if (isOntologyOpenForEditing(uri)) {
					if (factory.removeFilebyIndex(i)) return;
				}
				i++;
			}	
			AnnotationTabFactory tabfactory = new AnnotationTabFactory(settings, globalactions);
			addTab(factory, tabfactory);
		}
	}
	
	public void startNewAnnotatorTask(final File existingfile){
		AnnotatorFactory factory = new AnnotatorFactory(settings.doAutoAnnotate(), existingfile);
		if (isOntologyOpenForEditing(existingfile.toURI())) return;
		
		AnnotationTabFactory tabfactory = new AnnotationTabFactory(settings, globalactions);
		addTab(factory, tabfactory);

	}
	
	public void startNewExtractorTask() {
		ExtractorFactory factory = new ExtractorFactory();
		ExtractorTabFactory tabfactory = new ExtractorTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}
	
	public void startNewExtractorTask(final File existingfile){
		ExtractorFactory factory = new ExtractorFactory(existingfile);
		ExtractorTabFactory tabfactory = new ExtractorTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}
	
	public void startNewMergerTask(){
		MergerWorkbenchFactory factory = new MergerWorkbenchFactory();
		MergerTabFactory tabfactory = new MergerTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}
	
	public void startNewMergerTask(Set<File> existingFiles){
		MergerWorkbenchFactory factory = new MergerWorkbenchFactory();
		MergerTabFactory tabfactory = new MergerTabFactory(settings, globalactions, existingFiles);
		addTab(factory, tabfactory);
	}
	
	public void startNewStageTask(){
		StageWorkbenchFactory factory = new StageWorkbenchFactory();
		StageTabFactory tabfactory = new StageTabFactory(settings, globalactions);
		addTab(factory, tabfactory);
	}

	// Initialize a new addTabTask with the provided factories and execute

	private void addTab(WorkbenchFactory<? extends Workbench> workbenchmaker, TabFactory<? extends Workbench> tabmaker) {
		if (!workbenchmaker.isValid()) return;
		AddTabTask<? extends Workbench> task = new AddTabTask(workbenchmaker, tabmaker);
		task.execute();
	}
	
	//Dynamically creates the appropriate workbench and tab using the factories passed to
	//it on initialization. The workbench is created in a daemon thread. If file loading,
	//model creation, processing, and workbench creation are successful, the tab is created
	//using the newly created workbench and added to the Tab Pane. If the process fails at
	//any point, the thread is aborted and all further steps are skipped.
	private class AddTabTask<T extends Workbench> extends SemGenTask {
		WorkbenchFactory<T> workbenchfactory;
		TabFactory<T> tabfactory;
		SemGenTab tab = null;
		AddTabTask(WorkbenchFactory<T> maker, TabFactory<T> tabmaker) {
			workbenchfactory = maker;
			workbenchfactory.addPropertyChangeListener(this);
			tabfactory = tabmaker;
			progframe = new SemGenProgressBar(maker.getStatus(), true);
		}
		
		@Override
		protected Void doInBackground() throws Exception {
			while (workbenchfactory.isValid()) {
				workbenchfactory.run();
				break;
			}
			if (!workbenchfactory.isValid()) {
				cancel(true);
				return null;
			}
			workbenchfactory.addFileMenuasBenchObserver(menu.filemenu);
			return null;
		}
		
		public void endTask() {	
			for (T workbench : workbenchfactory.getWorkbenches()) {
				tab = tabfactory.makeTab(workbench);
				addTab(tab.getName(), tab);
				opentabs.add(tab);
				int tabcount = opentabs.size();
				globalactions.setCurrentTab(tab);
				tab.loadTab();
				setTabComponentAt(tabcount-1, tab.getTabLabel());
				
				tab.addMouseListenertoTabLabel(new tabClickedListener(tab));
				tab.setClosePolicy(new tabCloseListener(tab));
				setSelectedComponent(getComponentAt(tabcount - 1));
				getComponentAt(tabcount - 1).repaint();
				getComponentAt(tabcount - 1).validate();
			}
		}
		
		public void onError() {
			new NewTaskDialog(globalactions);
		}
	}
	
	/** Runs through the annotator tabs and checks if the requested file is already open for annotation.
	* Only one instance of a file can be annotated at a time. */
	public Boolean isOntologyOpenForEditing(URI uritocheck) {
		for (SemGenTab at : opentabs) {
			if (at.fileURIMatches(uritocheck)) {
				JOptionPane.showMessageDialog(null,"Cannot create or load \n"+ uritocheck.toString()+
					"\n because the file is already open for editing.",null, JOptionPane.PLAIN_MESSAGE);
				return true;
			}
		}
		return false;
	}

	/** Run the specified tab's close method defined by its class. */
	private boolean closeTabAction(SemGenTab component) {
		// If the file has been altered, prompt for a save
		boolean returnval =  component.closeTab();
		if (returnval) {
			removeTab(component);
			opentabs.remove(component);
			if (opentabs.size() != 0) {
				globalactions.setCurrentTab(opentabs.get(getSelectedIndex()));
			}
		}
		return returnval;
	}
	
	private void removeTab(SemGenTab component) {
		remove(indexOfComponent(component));
	}
	
	// When a tab is clicked make it the currently displayed tab
	private class tabClickedListener extends MouseAdapter {
		SemGenTab tab;
		tabClickedListener(SemGenTab tb) {
			tab = tb;
		}
		public void mouseClicked(MouseEvent arg0) {
			int tabindex = opentabs.indexOf(tab);
			setSelectedIndex(tabindex);
			globalactions.setCurrentTab((SemGenTab) getComponentAt(tabindex));
		}
	}
	
	private class tabCloseListener extends MouseAdapter {
		SemGenTab tab;
		tabCloseListener(SemGenTab t) {
			tab = t;
		}
		public void mouseClicked(MouseEvent arg0) {
			closeTabAction(tab);
		}
	}
	
	// This class observes the application's globalaction object; when an event in that class
	// is fired, this method processes it
	@Override
	public void update(Observable o, Object arg) {
		if (arg == GlobalActions.appactions.TABCLOSED) {
			closeTabAction(globalactions.getCurrentTab());
		}
		if (arg == GlobalActions.appactions.ANNOTATE) {
			this.startNewAnnotatorTask();
		}
		if (arg == GlobalActions.appactions.ANNOTATEEXISTING) {
			this.startNewAnnotatorTask(globalactions.getSeed());
		}
		if (arg == GlobalActions.appactions.EXTRACT) {
			this.startNewExtractorTask();
		}
		if (arg == GlobalActions.appactions.EXTRACTEXISTING) {
			this.startNewExtractorTask(globalactions.getSeed());
		}
		if (arg == GlobalActions.appactions.MERGE) {
			startNewMergerTask();
		}
		if (arg == GlobalActions.appactions.MERGEEXISTING) {
			startNewMergerTask(globalactions.getSeeds());
		}
		if (arg == GlobalActions.appactions.STAGE) {
			startNewStageTask();
		}
	}
	
	/** Run through each open tab and run its close method. If the user cancels, leave the
	 * specified tab open and keep the application open */
	public boolean quit() throws HeadlessException, OWLException {
		Iterator<SemGenTab> tabit = opentabs.iterator();
		while (tabit.hasNext()) {		
			SemGenTab tab = tabit.next();
			if (!tab.closeTab()) {
				return false;
			}
			removeTab(tab);
			tabit.remove();
		}
		return true;
	}
}