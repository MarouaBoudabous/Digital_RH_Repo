
package bd.master.rh.documentSegmentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Document;
import bd.master.rh.documentSegmentation.structure.Font;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.TextBlock;


public class ClusteringPdfBlockExtractor implements BlockProcessor {
	private static final Logger logger = Logger.getLogger(ClusteringPdfBlockExtractor.class.getName());

	public static boolean debug;


	public List<String> doi = new ArrayList<String>();

	/**
	 * Creates a new instance of this class.
	 */
	public ClusteringPdfBlockExtractor() {
	}

	public List<Block> extractBlocks(Document pdfDocument, String id) {
		List<BlocksEntry> pageBlocks = new ArrayList<BlocksEntry>();

		final Map<Integer, Font> idToFont = new HashMap<Integer, Font>();
		for (Page pdfPage : pdfDocument.getPages()) {
			Block mergedWords = new WordMerger(pdfPage, pdfPage.getFragments()).merge();
			Block splittedWords = new WordSplitter(pdfPage, mergedWords).split();
			Block mergedLines = new LineMerger(pdfPage, splittedWords).merge();
			//Block splitedLines = mergedLines; 
			Block cleanedLines = new LineCleaner(pdfPage, mergedLines).clean();
			//Block cleanedLines2 = cleanedLines; 
			double lineSpacing = new LineSpacingDetector(cleanedLines).getLineSpacing();
			Block blocksFragments = new BlockMerger(pdfPage, cleanedLines, lineSpacing, idToFont).merge();
			Block splitedBlocks = new BlockSplitter(pdfPage, blocksFragments).split();

			for (int i = 0; i < splitedBlocks.getLineBlocks().size(); i++) {
				String line2 = splitedBlocks.getLineBlocks().toString();
				String regEx = "10\\.[0-9]{4,}/[^\\s]*[^\\s\\.,]";
				Pattern pattern = Pattern.compile(regEx);
				Matcher matcher = pattern.matcher(line2);

				if (matcher.find() && (matcher.group() != null && !doi.contains(matcher.group()))) {
					doi.add(matcher.group());
				}

			}

			pageBlocks.add(new BlocksEntry(pdfPage, splitedBlocks));

			if (debug) {
				debugOutput(splitedBlocks, id);
			}
		}
		System.out.println(doi.toString());
		return getPages(pageBlocks);
	}

	/**
	 * @param fragments
	 * @param pageWidth
	 * @param pageHeight
	 * 
	 */
	private void testLayout(List<TextBlock> fragments, float pageWidth, float pageHeight) {
		int dimX = 500, dimY = (int) ((double) dimX * pageHeight / pageWidth);
		byte[][] matrix = new byte[dimX][];
		for (int i = 0; i < dimX; i++) {
			matrix[i] = new byte[dimY];
		}

		for (TextBlock fragment : fragments) {
			int xs = (int) Math.floor(dimX * fragment.getX() / pageWidth),
					xe = (int) Math.ceil(dimX * (fragment.getX() + fragment.getWidth()) / pageWidth);
			int ys = (int) Math.floor(dimY * fragment.getY() / pageHeight),
					ye = (int) Math.ceil(dimY * (fragment.getY() + fragment.getHeight()) / pageHeight);

			for (int y = ys; y <= Math.min(dimY - 1, ye); y++) {
				for (int x = xs; x <= Math.min(dimX - 1, xe); x++) {
					matrix[x][y] = 1;
				}
			}
		}

	}

	/**
	 * @param blockEntries
	 * @return
	 */
	private List<Block> getPages(List<BlocksEntry> blockEntries) {
		List<Block> result = new ArrayList<Block>();
		for (BlocksEntry pageEntry : blockEntries) {
			result.add(pageEntry.blocks);
		}
		return result;

	}

	/**
	 * @param block
	 */
	private void removeHypen(StringBuilder block) {
		if (block.length() > 0) {
			if (block.charAt(block.length() - 1) == '-') {
				block.setLength(block.length() - 1);
			} else {
				block.append(' ');
			}
		}
	}

	/**
	 * @param blocksFragments
	 * 
	 */
	private void debugOutput(Block blocksFragments, String id) {
	
		System.out.println(id);
		for (Block blocks : blocksFragments.getSubBlocks()) {
			System.out.println("--");
			for (Block line : blocks.getSubBlocks()) {
				StringBuilder b = new StringBuilder();

				int oldLen = 0;
				for (Block words : line.getSubBlocks()) {
					if (b.length() != oldLen) {
						b.append(' ');
					}
					oldLen = b.length();

					StringBuilder word = new StringBuilder();
					for (TextBlock f : words.getFragments()) {
						word.append(f.getText());
					}
					b.append(word.toString().trim());

				}
				TextBlock f = line.getFragments().get(0);
				b.append("      |" + f.getSequence() + "|" + line.getBoundingBox());

				System.out.println(b.toString());

			}

		}
	}

	
	private static class BlocksEntry {
		final Page pdfPage;
		final Block blocks;

		/**
		 * Creates a new instance of this class.
		 * 
		 * @param pdfPage
		 * @param blocksFragments
		 */
		public BlocksEntry(Page pdfPage, Block blocksFragments) {
			this.pdfPage = pdfPage;
			this.blocks = blocksFragments;
		}

	}

}
