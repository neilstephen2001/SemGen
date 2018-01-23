package semgen.utilities.file;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;

import semsim.fileaccessors.FileAccessorFactory;
import semsim.fileaccessors.ModelAccessor;
import semsim.model.collection.SemSimModel;
import semsim.reading.JSimProjectFileReader;
import semsim.reading.ModelClassifier;
import semsim.reading.ModelClassifier.ModelType;
import semsim.reading.ModelReader;

public class SemGenSaveFileChooser extends SemGenFileChooser implements PropertyChangeListener {
	private static final long serialVersionUID = 1L;
	private String modelInArchiveName;
	private static String[] ALL_ANNOTATABLE_TYPES = new String[]{"owl", "cellml", "sbml", "proj", "omex"};
	public static String[] ALL_WRITABLE_TYPES = new String[]{"owl", "cellml", "sbml", "proj", "mod", "omex"};
	
	public SemGenSaveFileChooser() {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		setPreferredSize(filechooserdims);
	}
	
	public SemGenSaveFileChooser(String[] exts, String selectedtype) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(exts);
		setFileFilter(getFilter(selectedtype));
		setPreferredSize(filechooserdims);
	}
	
	public SemGenSaveFileChooser(String selectedtype, String modelinarchivename) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(ALL_ANNOTATABLE_TYPES);
		setFileFilter(getFilter(selectedtype));
		setPreferredSize(filechooserdims);
		modelInArchiveName = modelinarchivename;
	}
	
	public SemGenSaveFileChooser(String[] exts, String selectedtype, String modelinarchivename) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(exts);
		setFileFilter(getFilter(selectedtype));
		setPreferredSize(filechooserdims);
		modelInArchiveName = modelinarchivename;
	}
	
	public SemGenSaveFileChooser(String selectedtype, String modelinarchivename, String suggestedfilename) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(ALL_ANNOTATABLE_TYPES);
		setFileFilter(getFilter(selectedtype));
		setPreferredSize(filechooserdims);
		modelInArchiveName = modelinarchivename;
		setSelectedFile(new File(suggestedfilename));
	}
	
	public SemGenSaveFileChooser(String[] exts, String selectedtype, String modelinarchivename, String suggestedfilename) {
		super("Choose save location");
		setAcceptAllFileFilterUsed(false);
		addFilters(exts);
		setFileFilter(getFilter(selectedtype));
		setPreferredSize(filechooserdims);
		modelInArchiveName = modelinarchivename;
		setSelectedFile(new File(suggestedfilename));
	}
	
	public void setFileExtension() {
		String type = null;
		File file = getSelectedFile();
		
		if(ModelType.OMEX_ARCHIVE.fileFilterMatches(getFileFilter()) || file.getName().endsWith(".omex")){
			type = "omex";
			modeltype = ModelType.OMEX_ARCHIVE;
		}
		else if(ModelType.SEMSIM_MODEL.fileFilterMatches(getFileFilter())){
			type = "owl";
			modeltype = ModelType.SEMSIM_MODEL;
		}
		else if(ModelType.SBML_MODEL.fileFilterMatches(getFileFilter())){
			type = "xml";
			modeltype = ModelType.SBML_MODEL;
		}
		else if(ModelType.CELLML_MODEL.fileFilterMatches(getFileFilter())){
			type = "cellml";
			modeltype = ModelType.CELLML_MODEL;
		}
		else if(ModelType.MML_MODEL.fileFilterMatches(getFileFilter())){
			type = "mod";
			modeltype = ModelType.MML_MODEL;
		}
		else if(ModelType.MML_MODEL_IN_PROJ.fileFilterMatches(getFileFilter())){
			type = "proj";
			modeltype = ModelType.MML_MODEL_IN_PROJ;
		}
		else if(getFileFilter()==csvfilter){
			type = "csv";
		}
		

		// If there's an extension for the file type, make sure the filename ends in it
		if(type!=null){
			if (! file.getName().toLowerCase().endsWith("." + type.toLowerCase())) {
					setSelectedFile(new File(file.getAbsolutePath() + "." + type));
			} 
		}
	}
	
	
	/**
	 * Pass an existing model and use the model's name
	 **/
	public ModelAccessor SaveAsAction(SemSimModel semsimmodel){
		ModelAccessor ma = null;
		
		while(true) {
			int returnVal = showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				setFileExtension();
				File filetosave = getSelectedFile();
				javax.swing.filechooser.FileFilter savefilter = getFileFilter();
				ModelType modeltype = ModelClassifier.getTypebyFilter(savefilter);
				if (filetosave.getName().endsWith(".omex")) modeltype = ModelType.OMEX_ARCHIVE;
				
				// If we're attempting to write a CellML model with discrete events, show error
				if (modeltype == ModelType.CELLML_MODEL && semsimmodel != null) {
					if( ! semsimmodel.getEvents().isEmpty()){
						JOptionPane.showMessageDialog(this, 
								"Cannot save as CellML because model contains discrete events", 
								"Cannot write to CellML", JOptionPane.WARNING_MESSAGE);
						continue;
					}
				}

				boolean overwriting = false;

				// If we're saving to a JSim project file
				if(modeltype == ModelType.MML_MODEL_IN_PROJ){
					
					String modelname = null;
					ArrayList<String> existingmodelnames = new ArrayList<String>();
					
					// If the output file already exists
					if(filetosave.exists()){
						Document projdoc = ModelReader.getJDOMdocumentFromFile(filetosave);
						existingmodelnames = JSimProjectFileReader.getNamesOfModelsInProject(projdoc);	
					}
					
					JSimModelSelectorDialogForWriting jms = 
							new JSimModelSelectorDialogForWriting(existingmodelnames, modelInArchiveName);
					
					modelname = jms.getSelectedModelName();
					
					if(modelname == null) return null;

					overwriting = existingmodelnames.contains(modelname);
					ma = FileAccessorFactory.getJSIMProjectAccessor(filetosave, modelname);
				}
				
				// Otherwise we're saving to a standalone file
				else if (modeltype==ModelType.OMEX_ARCHIVE) {
					OMEXSaveDialog omexdialog = new OMEXSaveDialog();
					String name = omexdialog.getModelName();
					if (name.isEmpty()) return null;
					
					ModelType archivedmodeltype = ModelClassifier.getTypebyExtension(omexdialog.getFormat());
					File modelfile = new File("model/" + name + archivedmodeltype.getExtension());

					ma = FileAccessorFactory.getOMEXArchive(filetosave, modelfile, archivedmodeltype);
				}
				else{
					ma = FileAccessorFactory.getModelAccessor(filetosave, modeltype);
					overwriting = ma.isLocalFile();
				}
				
				// If we're overwriting a model...
				if (overwriting) {
					String overwritemsg = "Overwrite " + ma.getFileName() + "?";
					
					int overwriteval = JOptionPane.showConfirmDialog(this,
							overwritemsg, "Confirm overwrite",
							JOptionPane.OK_CANCEL_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					
					if (overwriteval == JOptionPane.OK_OPTION) break;
					return null;
				}
				break;
			}
			else if (returnVal == JFileChooser.CANCEL_OPTION) {
				return null;
			}
		}

		currentdirectory = getCurrentDirectory();

		return ma;
	}
	
	/**
	 * Select a save location before the model exists (used for merging)
	 * @return the selected file location or null
	 */
	public ModelAccessor SaveAsAction(){	
		return SaveAsAction(null);
	}
	

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		
	}
	
}
