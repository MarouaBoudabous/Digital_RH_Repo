package bd.master.rh.documentSegmentation.structure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;

public class Document extends PDDocument{
	
	public Document() throws IOException {
		super();
		// TODO Auto-generated constructor stub
	}
	private List<Page> pages = new ArrayList<Page>();
	private List <Font> fonts = new ArrayList<Font>();
	public List<Page> getPages() {
		return pages;
	}
	public void setPages(List<Page> pages) {
		this.pages = pages;
	}
	public List<Font> getFonts() {
		return fonts;
	}
	public void setFonts(List<Font> fonts) {
		this.fonts = fonts;
	}
	

}
