package semgen.utilities.uicomponent;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import semgen.utilities.BrowserLauncher;
import semgen.utilities.SemGenIcon;
import semsim.SemSimConstants;
import semsim.owl.SemSimOWLFactory;
import semsim.utilities.webservices.BioPortalConstants;

public class ExternalURLButton extends JLabel implements MouseListener{

	private static final long serialVersionUID = 1L;

	public ExternalURLButton(){
		addMouseListener(this);
		setIcon(SemGenIcon.externalURLicon);
		setEnabled(true);
		setToolTipText("Show term details in browser");
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	}

	public void openTerminBrowser(URI termuri) {
		if(termuri!=null){
			System.out.println(termuri);
			String namespace = SemSimOWLFactory.getNamespaceFromIRI(termuri.toString());
			String fullontname = SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(namespace);
			String abbrev = SemSimConstants.ONTOLOGY_FULL_NAMES_AND_NICKNAMES_MAP.get(fullontname);
			
			// If an identifiers.org URI is used, just treat the identifier as the URL
			if(termuri.toString().startsWith("http://identifiers.org")) BrowserLauncher.openURL(termuri.toString());

			// ...else, if it's a UNIPROT term...
			else if(SemSimConstants.ONTOLOGY_NAMESPACES_AND_FULL_NAMES_MAP.get(
					SemSimOWLFactory.getNamespaceFromIRI(termuri.toString()))==SemSimConstants.UNIPROT_FULLNAME){
				String id = SemSimOWLFactory.getIRIfragment(termuri.toString());
				String urlstring = "http://www.uniprot.org/uniprot/" + id;
				BrowserLauncher.openURL(urlstring);				
			}
			
			// ...else if we have identified the ontology and it is available through BioPortal, open the BioPortal URL
			else if(abbrev!=null && BioPortalConstants.ONTOLOGY_FULL_NAMES_AND_BIOPORTAL_IDS.containsKey(fullontname)){
				// Special case for BRENDA
				if(abbrev.equals("BRENDA")) abbrev = "BTO";
				String urlstring = "http://bioportal.bioontology.org/ontologies/" + abbrev + "/?p=classes&conceptid=" + SemSimOWLFactory.URIencoding(termuri.toString());
				BrowserLauncher.openURL(urlstring);
			}
			// ...otherwise the knowledge resource is not known or not available online
			else{
				JOptionPane.showMessageDialog(getParent(), "Sorry, could not determine where to find more information about that resource.");
			}
		}
	}
	
	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {
		setCursor(new Cursor(Cursor.HAND_CURSOR));
	}

	public void mouseExited(MouseEvent arg0) {
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}

	public void mousePressed(MouseEvent arg0) {
		setBorder(BorderFactory.createLineBorder(Color.blue,1));
	}
	public void mouseReleased(MouseEvent arg0) {
		setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
	}

}
