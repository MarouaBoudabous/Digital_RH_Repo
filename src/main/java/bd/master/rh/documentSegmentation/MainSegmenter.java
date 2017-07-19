package bd.master.rh.documentSegmentation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.pdfbox.examples.util.PrintTextLocations;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDStream;

import bd.master.rh.documentSegmentation.structure.Document;
import bd.master.rh.documentSegmentation.structure.Font;
import bd.master.rh.documentSegmentation.structure.Page;


public class MainSegmenter {
	public static void main(String[] args) {
		String path = "pdf" + File.separator + "CV_BOUDABOUS_MAROUA.pdf";
		File file = new File(path);
	//	PDDocument document = new PdfBoxDocumentParser().parse(file);
		try {
			PDDocument doc = PDDocument.load(file);
			Document resultDoc= new Document();
			final Map<Object, Integer> fonts = new HashMap<Object, Integer>();
			final Map<Integer, Font> idToFont = new HashMap<Integer, Font>();
			System.out.println("Number of pages   :" + doc.getNumberOfPages());
			System.out.println("Title            :" + doc.getDocumentInformation().getTitle());
			PrintTextLocations printer = new PrintTextLocations();
			for (int i = 0; i < doc.getNumberOfPages(); i++) {
				System.out.println("Processing page number " + i);
				PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages().get(i);
				Page pdfPage= new Page();
				pdfPage.setPageRotation(page.findRotation());
				pdfPage.setPageWidth(page.findMediaBox().getWidth());
				pdfPage.setPageHeight(page.findMediaBox().getHeight());
				pdfPage.setOffsetUpperRightX(page.findMediaBox().getUpperRightX() - page.findCropBox().getUpperRightX());
				pdfPage.setOffsetUpperRightY(page.findMediaBox().getUpperRightY() - page.findCropBox().getUpperRightY());
				pdfPage.setNumber(i);
				PDStream contents = page.getContents();
				if ( contents!= null) {
					if (true) {
						TextExtractor extractor = new TextExtractor(pdfPage, idToFont, fonts);
						extractor.processStream(page, page.findResources(), contents.getStream());
						pdfPage.setLines(extractor.getLines());
						extractor.getTextMatrix();
						} 
				System.out.println("total of lines" + pdfPage.getLines().size());	
				
				}
				resultDoc.getPages().add(pdfPage);
				for (Entry<Integer, Font> e : idToFont.entrySet()) {
					resultDoc.getFonts().add(e.getValue());
		        }
		        
				PdfBlockExtractor pdfExtractor = new PdfBlockExtractor();
				pdfExtractor.extractBlocks(resultDoc, path.split(File.separator+File.separator)[1]);

			}
			doc.close();
			
			System.out.println(resultDoc);
			// PDFTextStripper pdfTextStripper = new PDFTextStripper();
			//
			// pdfTextStripper.setStartPage(1);
			// pdfTextStripper.setEndPage( doc.getNumberOfPages());
			// pdfTextStripper.getSeparateByBeads();
			// String parsedText = pdfTextStripper.getText(doc);
			// System.out.println(parsedText);

			// PDPageTree pageTree = doc.getPages();
			// for (PDPage pdPage : pageTree) {
			// InputStream in =pdPage.getContents();
			// String theString = IOUtils.toString(in, Encoding.utf8Encoding);
			// System.out.println(theString);
			//
			// }
			// //pageTree.getCOSObject().g;
			// System.out.println(doc.getDocumentCatalog().toString());
			// System.out.println("Your file is loaded with success....!!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



}
