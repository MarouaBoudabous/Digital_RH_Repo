package bd.master.rh.documentSegmentation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import bd.master.rh.documentSegmentation.structure.TextBlock;

public class MainSegmenter {

	public static void main(String[] args) {
		// segmentDocument();
	}

	public List<Section> processDocument(String path) throws FileNotFoundException {
		// String path = "pdf" + File.separator + "CV_BOUDABOUS_MAROUA.pdf";
		// Initialize structural object to hold pertinent data for the segmentation
		// process...
		List<Section> finalSections = new ArrayList<Section>();
		// Load file to process...
		InputStream input = new FileInputStream(
				path.replace("\\", "/").substring(1, path.replace("\\", "/").length() - 1));
		File file = new File(path);
		try {
			// Use PdfBox library to load PDF Stream with their geometric descriptors...
			PDDocument doc = PDDocument.load(input);
			Document resultDoc = new Document();
			// Create maps to hold fonts used in the processed document .
			final Map<Object, Integer> fonts = new HashMap<Object, Integer>();
			final Map<Integer, Font> idToFont = new HashMap<Integer, Font>();
			// If the document have multiple pages, manages the segmentation page by page...
			for (int i = 0; i < doc.getNumberOfPages(); i++) {
				PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(i);
				// Set up pdf page object.
				Page pdfPage = new Page();
				pdfPage.setPageRotation(page.findRotation());
				pdfPage.setPageWidth(page.findMediaBox().getWidth());
				pdfPage.setPageHeight(page.findMediaBox().getHeight());
				pdfPage.setOffsetUpperRightX(
						page.findMediaBox().getUpperRightX() - page.findCropBox().getUpperRightX());
				pdfPage.setOffsetUpperRightY(
						page.findMediaBox().getUpperRightY() - page.findCropBox().getUpperRightY());
				pdfPage.setNumber(i);
				// Extracting textual content of each page.
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
			generateFinalSegmentationResults(finalSections, extractedBlocks);

			doc.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return finalSections;
	}

	/**
	 * @param finalSections
	 * @param extractedBlocks
	 */
	private void generateFinalSegmentationResults(List<Section> finalSections, List<Block> extractedBlocks) {
		Boolean personalInfoDetected = Boolean.FALSE;
		Section firstSection = new Section();
		firstSection.setTitle("Titre");
		firstSection.setContent(extractedBlocks.get(0).toString());
		finalSections.add(firstSection);
		int index= 0;
		int j= 0;
		int pertinentIndex=0;
		for (int i = 1; i < extractedBlocks.size(); i++) {
			System.out.println(extractedBlocks.get(i).toString());
			System.out.println("");
			System.out.println("");
			if (extractedBlocks.get(i).toString().contains("@")) {
				Section sect = new Section();
				sect.setTitle("Info-perso");
				sect.setContent(
						finalSections.get(finalSections.size() - 1).getContent() + extractedBlocks.get(i).toString());
				finalSections.add(sect);
				personalInfoDetected = Boolean.TRUE;
				index ++;

			} else if(!personalInfoDetected) {
				Section previousSection= finalSections.get(finalSections.size() - 1);
				previousSection.setContent(previousSection.getContent()+" \n"+ extractedBlocks.get(i).toString());
				j++;
			}else {
				
				if (discardPunctuationMarks(extractedBlocks.get(i)).split(" ").length < 3
						&& !extractedBlocks.get(i).toString().matches(".*\\d+.*") ) {
					if(i<=index+j+1) {
						pertinentIndex = i;
						Section sect = new Section();
						sect.setTitle(extractedBlocks.get(i).toString());
						finalSections.add(sect);
					}else {
						if(hasTitleFont(extractedBlocks, i,pertinentIndex)) {
							Section sect = new Section();
							sect.setTitle(extractedBlocks.get(i).toString());
							finalSections.add(sect);
						}
					}
					
				} else {

					String content = "";
					
					if (finalSections.size() > 1
							&& !finalSections.get(finalSections.size() - 1).getTitle().equalsIgnoreCase("Info-perso")) {
						content = finalSections.get(finalSections.size() - 1).getContent();

						if (content != null) {
							finalSections.get(finalSections.size() - 1)
									.setContent(content + "\n" + extractedBlocks.get(i).toString());
							j++;
						} else {
							finalSections.get(finalSections.size() - 1).setContent(extractedBlocks.get(i).toString());
							j++;
						}

					} else {
						Section sect = new Section();
						sect.setTitle("");
						j++;
						sect.setContent(extractedBlocks.get(i).toString());
						finalSections.add(sect);
					}

				}
			
				
			}
		}

		// for (Block block : extractedBlocks) {
		// Section section = new Section();
		// section.setTitle("");
		// section.setContent(block.toString());
		// finalSections.add(section);
		// }
	}

	/**
	 * @param extractedBlocks
	 * @param i
	 */
	private Boolean hasTitleFont(List<Block> extractedBlocks, int i, int pertinentIndex) {
		Boolean hasTitleFont = Boolean.FALSE;
		for (TextBlock tbox : extractedBlocks.get(i).getFragments()) {
			if (tbox.getFontId()== extractedBlocks.get(pertinentIndex).getFragments().get(0).getFontId()) {
				hasTitleFont = Boolean.TRUE;
			}
		}
		return hasTitleFont;
	}

	private String discardPunctuationMarks(Block block) {
		String cleanedContent = block.toString().replaceAll(":", "");

		return cleanedContent;
	}

}
