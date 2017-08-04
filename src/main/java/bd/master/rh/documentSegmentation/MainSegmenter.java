package bd.master.rh.documentSegmentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Document;
import bd.master.rh.documentSegmentation.structure.Font;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.Section;

public class MainSegmenter {
	public static void main(String[] args) {
		// segmentDocument();
	}

	public List<Section> processDocument(String path) throws FileNotFoundException {
		// String path = "pdf" + File.separator + "CV_BOUDABOUS_MAROUA.pdf";

		// Load file to process...
		List<Section> finalSections = new ArrayList<Section>();
		System.out.println("PATH" + path);
		String test = path.replace("\\", "/");
		System.out.println("PATH" + test);
		// if(.equalsIgnoreCase("C:/Maroua/CV_BOUDABOUS_MAROUA_EN.pdf")) {
		// System.err.println("Yess, they are equals");
		// }else {
		// System.out.println(test.endsWith("C:/Maroua/CV_BOUDABOUS_MAROUA_EN.pdf"));
		// }
		InputStream input = new FileInputStream(test.substring(1, test.length() - 1));
		File file = new File(path);
		try {
			// Use PdfBox library to load PDF Stream with their geometric descriptors...
			PDDocument doc = PDDocument.load(input);
			// Initialize structural object to hold pertinent data for the segmentation
			// process...
			Document resultDoc = new Document();
			final Map<Object, Integer> fonts = new HashMap<Object, Integer>();
			final Map<Integer, Font> idToFont = new HashMap<Integer, Font>();
			// If the document have multiple pages, manages the segmentation page by page...
			for (int i = 0; i < doc.getNumberOfPages(); i++) {
				PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(i);
				Page pdfPage = new Page();
				pdfPage.setPageRotation(page.findRotation());
				pdfPage.setPageWidth(page.findMediaBox().getWidth());
				pdfPage.setPageHeight(page.findMediaBox().getHeight());
				pdfPage.setOffsetUpperRightX(
						page.findMediaBox().getUpperRightX() - page.findCropBox().getUpperRightX());
				pdfPage.setOffsetUpperRightY(
						page.findMediaBox().getUpperRightY() - page.findCropBox().getUpperRightY());
				pdfPage.setNumber(i);
				PDStream contents = page.getContents();
				if (contents != null) {
					if (true) {
						TextExtractor extractor = new TextExtractor(pdfPage, idToFont, fonts);
						extractor.processStream(page, page.findResources(), contents.getStream());
						pdfPage.setLines(extractor.getLines());
						extractor.getTextMatrix();
					}
				}
				// Add pages decorated by geometric descriptors to the doc to segment ...
				resultDoc.getPages().add(pdfPage);
				for (Entry<Integer, Font> e : idToFont.entrySet()) {
					resultDoc.getFonts().add(e.getValue());
				}
			}
			// Process extracting document Sections...
			PdfBlockExtractor pdfExtractor = new PdfBlockExtractor();
			List<Block> extractedBlocks = pdfExtractor.extractBlocks(resultDoc,
					path.split(File.separator + File.separator)[1]);

			// Generating final result....
			// TODO ...
			for (Block block : extractedBlocks) {
				Section section = new Section();
				section.setTitle("");
				section.setContent(block.toString());
				finalSections.add(section);
			}

			doc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalSections;
	}

}
