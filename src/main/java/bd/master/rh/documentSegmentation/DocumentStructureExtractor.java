
package bd.master.rh.documentSegmentation;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.BlockLabel;
import bd.master.rh.documentSegmentation.structure.DocumentStructure;
import bd.master.rh.documentSegmentation.structure.Font;
import bd.master.rh.documentSegmentation.structure.ReadingOrder;
import bd.master.rh.documentSegmentation.structure.TextBlock;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;


public class DocumentStructureExtractor {
	public static final Pattern headingSegmentPattern = Pattern.compile("(\\d+)|([iIxXvV]+)|([abcdefABCDEF])");
	public static final Pattern headingPattern = Pattern.compile("((\\d+)|([iIxXvV]+)|([abcdefABCDEF]).?)+");

	/**
	 * @param prefix
	 * @return
	 */
	public static int getSegmentCounter(String prefix) {
		String[] segments = prefix.split("\\.");
		int counter = 0;
		for (String s : segments) {
			String st = s.trim();
			if (!st.isEmpty()) {
				if (headingSegmentPattern.matcher(st).matches()) {
					counter++;
				} else {
					counter = 0;
					break;
				}
			}
		}
		return counter;
	}

	public static final String[] headingsToExclude = new String[] { 
	};

	private static class DocumentStructureModel implements Serializable {
		private static final long serialVersionUID = 1L;

		private final List<Block> pageBlocks;
		private final BlockLabeling labeling;
		private final ReadingOrder readingOrder;
		private final List<Font> fonts;

		/**
		 * Creates a new instance of this class.
		 * 
		 * @param pageBlocks
		 * @param labeling
		 * @param readingOrder
		 * @param fonts
		 */
		public DocumentStructureModel(List<Block> pageBlocks, BlockLabeling labeling, ReadingOrder readingOrder,
				List<Font> fonts) {
			this.pageBlocks = pageBlocks;
			this.labeling = labeling;
			this.readingOrder = readingOrder;
			this.fonts = fonts;
		}

		
	}
	
	

	/**
	 * Extracts the document structure
	 * 
	 * @param pageBlocks
	 *            the blocks from all pages
	 * @param readingOrder
	 *            the reading sequence
	 * @param fonts
	 * @return a map of blocks to their respective structure level, where 0 is the
	 *         title
	 */
	public DocumentStructure detectDocumentStructure(List<Block> pageBlocks, BlockLabeling labeling,
			ReadingOrder readingOrder, List<Font> fonts) {
		Block titleBlock = null;

		List<HeadingBlock> headingBlocks = new ArrayList<HeadingBlock>();
		TIntHashSet segmentCounterSet = new TIntHashSet();
		int blockIndex = 0;
		for (int i = 0; i < pageBlocks.size(); i++) {
			Block pageBlock = pageBlocks.get(i);
			List<Integer> ro = readingOrder.getReadingOrder(i);
			List<Block> blocks = new ArrayList<Block>(pageBlock.getSubBlocks());
			for (int j = 0; j < ro.size(); j++) {
				Block currentBlock = blocks.get(ro.get(j));
				BlockLabel label = labeling.getLabel(currentBlock);

				if (label == BlockLabel.Title) {
					titleBlock = currentBlock;
				} else if (label == BlockLabel.Heading) {
					boolean ignoreBlock = false;

					String blockText = currentBlock.toString();
					for (String eh : headingsToExclude) {
						if (eh.equalsIgnoreCase(blockText)) {
							ignoreBlock = true;
							break;
						}
					}

					if (!ignoreBlock) {
						HeadingBlock headingBlock = new HeadingBlock(currentBlock, blockIndex);
						if (headingBlock.segmentCount > 0) {
							segmentCounterSet.add(headingBlock.segmentCount);
						}
						headingBlocks.add(headingBlock);
					}
				}
				blockIndex++;
			}
		}

		Map<Integer, Font> fontMap = new HashMap<Integer, Font>();
		if (fonts != null) {
			for (Font font : fonts) {
				fontMap.put(font.getId(), font);
			}
		}
		List<Cluster> clusters = initializeClusters(headingBlocks, fontMap);

		mergeClusters(clusters);

		

		SortedSet<Cluster> sortedClusters = sortClusters(clusters);

		ParagraphInformation paragraphInformation = new ParagraphExtractor().extract(pageBlocks, labeling,
				readingOrder);

		return createStrucutureMap(titleBlock, sortedClusters, paragraphInformation);
	}

	protected DocumentStructure createStrucutureMap(Block titleBlock, SortedSet<Cluster> sortedClusters,
			ParagraphInformation paragraphInformation) {
		Map<Block, Integer> result = new LinkedHashMap<Block, Integer>();
		int headingLevel = 0;
		result.put(titleBlock, headingLevel);
		headingLevel++;

		int clusterId = 0;
		int lastNumBlocks = 0;
		for (Cluster c : sortedClusters) {
			
			if (c.blocks.size() <= 1) {
				continue;
			}
			int numBlocks = c.blocks.size();
			System.out.println(String.format("Cluster of size: %.3f, %.3f, %s, %s, %s, %s", c.ucMeanHeight,
					c.ucStdevHeight, c.isUpperCase, c.isBold, c.isItalic, c.id));
			for (HeadingBlock b : c.blocks) {
				System.out.println(String.format("* %s (%.3f, %.3f, %.2f, %s, %s)", b.block.getText(), b.ucMeanHeight,
						b.ucStdevHeight, b.ucRatio, b.blockIndex, Arrays.toString(b.fontIds.keys())));
				result.put(b.block, headingLevel);
			}
			System.out.println();
			headingLevel++;
			clusterId++;
			lastNumBlocks = numBlocks;
		}
		return new DocumentStructure(result, paragraphInformation);
	}

	protected SortedSet<Cluster> sortClusters(List<Cluster> clusters) {
		SortedSet<Cluster> sortedClusters = new TreeSet<Cluster>(new Comparator<Cluster>() {
			public int compare(Cluster arg0, Cluster arg1) {
				if (arg0.id == arg1.id) {
					return 0;
				}

				int d = 0;
				int minDistance = arg0.getMinDistance(arg1);

				if (minDistance == -1) {
					d = -1;
				} else if (minDistance == 1) {
					d = +1;
				} else if (arg0.segmentCount > 0 && arg1.segmentCount > 0) {
					d = arg0.segmentCount - arg1.segmentCount > 0.1 ? +1 : -1;
				} else if (Math.abs(arg0.ucMeanHeight - arg1.ucMeanHeight) > 0.01) {
					d = -Double.compare(arg0.ucMeanHeight, arg1.ucMeanHeight);
				}
				if (d == 0) {
					if (arg0.isUpperCase != arg1.isUpperCase) {
						d = arg0.isUpperCase ? -1 : +1;
					}
				}
				if (d == 0) {
					d = arg0.id < arg1.id ? -1 : +1;
				}
				return d;
			}
		});

		sortedClusters.addAll(clusters);
		return sortedClusters;
	}

	protected boolean isMergeCandidate(List<Cluster> clusters, Cluster a, Cluster b, double d) {
		boolean upperCaseClusterIsOutlier = isUpperCaseOutlier(clusters);
		boolean isMergeCandidate = true;
		if (a.segmentCount == b.segmentCount && a.segmentCount > 0 && b.segmentCount > 0) {
			
		} else {
			if (d > Math.max(a.ucStdevHeight, b.ucStdevHeight) + 0.01) {
				isMergeCandidate = false;
			} else if (a.isUpperCase != b.isUpperCase && !upperCaseClusterIsOutlier) {
				isMergeCandidate = false;
			} else if (a.segmentCount != b.segmentCount && a.segmentCount > 0 && b.segmentCount > 0) {
				isMergeCandidate = false;
			} else if (a.isAdjacentTo(b)) {
				isMergeCandidate = false;
			} else if (a.hasDifferentFontTypes(b)) {
				isMergeCandidate = false;
			} else if (a.hasDifferentFragemtsSizes(b)) {
				isMergeCandidate = false;
			
			}
		}
		return isMergeCandidate;
	}

	protected void mergeClusters(List<Cluster> clusters) {
		int counter = 0;
		boolean isFinished = false;
		do {
			SortedSet<MergeCandidate> candidates = new TreeSet<MergeCandidate>();
			for (int i = 0; i < clusters.size(); i++) {
				for (int j = i + 1; j < clusters.size(); j++) {
					Cluster a = clusters.get(i);
					Cluster b = clusters.get(j);
					if (a.ucMeanHeight < b.ucMeanHeight) {
						Cluster s = a;
						a = b;
						b = s;
					}
					double d = (a.ucMeanHeight - b.ucMeanHeight);
					boolean isMergeCandidate = isMergeCandidate(clusters, a, b, d);

					if (isMergeCandidate) {
						d /= Math.min(a.ucMeanHeight, b.ucMeanHeight);
						d += Math.abs(a.fragmentCount - b.fragmentCount)
								/ (Math.max(a.fragmentCount, b.fragmentCount) * 10.0);
						candidates.add(new MergeCandidate(a, b, d, counter++));
					}
				}
			}

			Set<Cluster> mergedClusters = new HashSet<Cluster>();
			for (MergeCandidate c : candidates) {
				if (!mergedClusters.contains(c.a) && !mergedClusters.contains(c.b)) {
					c.a.merge(c.b);
					clusters.remove(c.b);
				}
				mergedClusters.add(c.a);
				mergedClusters.add(c.b);
			}
			if (candidates.isEmpty()) {
				isFinished = true;
			}
		} while (!isFinished);
	}

	protected boolean isUpperCaseOutlier(List<Cluster> clusters) {
		int allUpperCaseCounter = 0;
		for (int i = 0; i < clusters.size(); i++) {
			if (clusters.get(i).isUpperCase) {
				allUpperCaseCounter++;
			}
		}
		boolean upperCaseClusterIsOutlier = allUpperCaseCounter < 2;
		return upperCaseClusterIsOutlier;
	}

	protected List<Cluster> initializeClusters(List<HeadingBlock> headingBlocks, Map<Integer, Font> fonts) {
		List<Cluster> clusters = new ArrayList<Cluster>();

		Map<InitialClustering, Cluster> segmentCountToCluster = new HashMap<InitialClustering, DocumentStructureExtractor.Cluster>();
		for (int i = 0; i < headingBlocks.size(); i++) {
			HeadingBlock block = headingBlocks.get(i);
			Cluster cluster = new Cluster(block, i, fonts);

			if (block.segmentCount > 0) {
				InitialClustering key = new InitialClustering(block.segmentCount, block.segmentSeparator);
				Cluster segmentCountCluster = segmentCountToCluster.get(block.segmentCount);
				if (segmentCountCluster == null) {
					clusters.add(cluster);
					segmentCountToCluster.put(key, cluster);
				} else {
					segmentCountCluster.merge(cluster);
				}
			} else {
				clusters.add(cluster);
			}
		}

		return clusters;
	}

	protected static class InitialClustering {
		final int segmentCounter;
		final char segmentSeparator;

		/**
		 * Creates a new instance of this class.
		 * 
		 * @param segmentCounter
		 * @param segmentSeparator
		 */
		public InitialClustering(int segmentCounter, char segmentSeparator) {
			super();
			this.segmentCounter = segmentCounter;
			this.segmentSeparator = segmentSeparator;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + segmentCounter;
			result = prime * result + segmentSeparator;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InitialClustering other = (InitialClustering) obj;
			if (segmentCounter != other.segmentCounter)
				return false;
			if (segmentSeparator != other.segmentSeparator)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "InitialClustering [segmentCounter=" + segmentCounter + ", segmentSeparator=" + segmentSeparator
					+ "]";
		}
	}

	protected static class Cluster {
		public final int id;
		public double ucStdevHeight;
		public double ucMeanHeight;
		public boolean isUpperCase;
		public float segmentCount;
		public float fragmentCount;
		public Map<String, MeanDim> fragmentToMeanDim;

		public final Set<HeadingBlock> blocks;
		private Map<Integer, Font> fonts;
		public boolean isBold;
		public boolean isItalic;

		/**
		 * Creates a new instance of this class.
		 * 
		 * @param block
		 * @param id
		 */
		public Cluster(HeadingBlock block, int id, Map<Integer, Font> fonts) {
			this.id = id;
			this.fonts = fonts;
			this.blocks = new TreeSet<HeadingBlock>(new Comparator<HeadingBlock>() {
				public int compare(HeadingBlock o1, HeadingBlock o2) {
					return o1.blockIndex > o2.blockIndex ? +1 : o1.blockIndex < o2.blockIndex ? -1 : 0;
				}
			});
			this.blocks.add(block);
			updateStats();
		}

		public boolean hasBlockId(int blockIndex) {
			for (HeadingBlock block : blocks) {
				if (block.blockIndex == blockIndex) {
					return true;
				}
			}
			return false;
		}

		/**
		 * @param b
		 * @return
		 */
		public boolean hasDifferentFontTypes(Cluster b) {
			if (isBold != b.isBold) {
				return true;
			}
			if (isItalic != b.isItalic) {
				return true;
			}
			return false;
		}

		/**
		 * @param b
		 * @return
		 */
		public boolean hasDifferentFragemtsSizes(Cluster b) {
			boolean result = false;
			for (String f : fragmentToMeanDim.keySet()) {
				MeanDim meanDim1 = fragmentToMeanDim.get(f);
				MeanDim meanDim2 = b.fragmentToMeanDim.get(f);
				if (meanDim2 != null) {
					double h1 = meanDim1.heightSum / meanDim1.count;
					double h2 = meanDim2.heightSum / meanDim2.count;

					double hd = Math.abs(h1 - h2) / Math.max(h1, h2);

					if (hd > 0.1) {
						result = true;
						break;
					}

					
				}
			}
			return result;
		}

		public boolean isAdjacentTo(Cluster o) {
			boolean result = false;
			for (HeadingBlock a : blocks) {
				for (HeadingBlock b : o.blocks) {
					int d = Math.abs(a.blockIndex - b.blockIndex);
					if (d <= 1) {
						result = true;
						break;
					}
				}
			}
			return result;
		}

		public boolean hasCommonFonts(Cluster o) {
			boolean result = false;
			for (HeadingBlock a : blocks) {
				for (HeadingBlock b : o.blocks) {
					for (int fontId : a.fontIds.keys()) {
						if (b.fontIds.containsKey(fontId)) {
							return true;
						}
					}
				}
			}
			return result;
		}

		public int getMinDistance(Cluster o) {
			int result = Integer.MAX_VALUE;
			for (HeadingBlock a : blocks) {
				for (HeadingBlock b : o.blocks) {
					int d = a.blockIndex - b.blockIndex;
					if (Math.abs(d) <= Math.abs(result)) {
						result = d;
					}
				}
			}
			return result;
		}

		public void merge(Cluster o) {
			this.blocks.addAll(o.blocks);
			updateStats();
		}

		protected void updateStats() {
			this.isBold = false;
			this.isItalic = false;
			{
				float m = 0;
				int c = 0;
				for (HeadingBlock b : blocks) {
					m += b.ucMeanHeight;
					c++;
				}
				this.ucMeanHeight = m / c;
			}
			{
				float m = 0;
				int c = 0;
				for (HeadingBlock b : blocks) {
					m += b.ucStdevHeight;
					c++;
				}
				this.ucStdevHeight = m / c;
			}
			{
				float m = 0;
				int c = 0;
				for (HeadingBlock b : blocks) {
					m += b.ucRatio;
					c++;
				}
				this.isUpperCase = m / c >= 0.5;
			}
			{
				float m = 0;
				int c = 0;
				for (HeadingBlock b : blocks) {
					m += b.ucStdevHeight;
					c++;
				}
				this.ucStdevHeight = m / c;
			}
			{
				float m = 0;
				int c = 0;
				for (HeadingBlock b : blocks) {
					m += b.fragmentCounter;
					c++;
				}
				this.fragmentCount = m / c;
			}
			{
				float m = 0;
				int c = 0;
				for (HeadingBlock b : blocks) {
					if (b.segmentCount >= 0) { 
						m += b.segmentCount;
						c++;
					}
				}
				this.segmentCount = c == 0 ? -1 : m / c;
			}
			{
				int boldCounter = 0, italicsCounter = 0, fragmentCounter = 0;
				fragmentToMeanDim = new HashMap<String, DocumentStructureExtractor.MeanDim>();
				TIntIntHashMap fontToCount = new TIntIntHashMap();
				for (HeadingBlock b : blocks) {
					List<TextBlock> fragments = b.block.getFragments();
					for (TextBlock f : fragments) {
						String text = f.getText();
						MeanDim meanDim = fragmentToMeanDim.get(text);
						if (meanDim == null) {
							meanDim = new MeanDim();
							fragmentToMeanDim.put(text, meanDim);
						}
						meanDim.add(f.getHeight(), f.getWidth());
						Font font = fonts.get(f.getFontId());
						if (font != null) {
							Boolean isBold = font.getIsBold();
							if (isBold != null && isBold) {
								boldCounter++;
							}
							Boolean isItalic = font.getIsItalic();
							if (isItalic != null && isItalic) {
								italicsCounter++;
							}
							fragmentCounter++;
						}
						fontToCount.adjustOrPutValue(f.getFontId(), 1, 1);
					}
				}

				if (boldCounter > fragmentCounter / 2) {
					this.isBold = true;
				}
				if (italicsCounter > fragmentCounter / 2) {
					this.isItalic = true;
				}

			}
		}

		@Override
		public String toString() {
			return String.format(
					"Cluster [id=%s, ucStdevHeight=%s, ucMeanHeight=%s, isUpperCase=%s, isBold=%s, isItalic=%s, segmentCount=%s, blocks=%s]",
					id, ucStdevHeight, ucMeanHeight, isUpperCase, isBold, isItalic, segmentCount, blocks);
		}

	}

	protected static class MergeCandidate implements Comparable<MergeCandidate> {
		private final double d;
		private final int id;
		final Cluster a;
		final Cluster b;

		public MergeCandidate(Cluster a, Cluster b, double d, int id) {
			this.a = a;
			this.b = b;
			this.d = d;
			this.id = id;
		}

		public int compareTo(MergeCandidate o) {
			int r = Double.compare(this.d, o.d);
			if (r == 0) {
				r = id > o.id ? 1 : -1;
			}
			return r;
		}
	}

	protected static class HeadingBlock {
		public final Block block;
		public final int blockIndex;
		public final double ucRatio;
		public final double ucMeanHeight;
		public final double ucStdevHeight;
		public final double digitRatio;
		public final int segmentCount;
		public final char segmentSeparator;
		public final int fragmentCounter;
		public final TIntIntHashMap fontIds;

		/**
		 * Creates a new instance of this class.
		 * 
		 * @param block
		 * @param blockIndex
		 */
		public HeadingBlock(Block block, int blockIndex) {
			this.block = block;
			this.blockIndex = blockIndex;

			int upperCaseCounter = 0;
			int fragmentCounter = 0;
			int digitCounter = 0;
			Mean ucm = new Mean(), lcm = new Mean();
			StandardDeviation ucstdev = new StandardDeviation(), lcstdev = new StandardDeviation();
			List<Block> lineBlocks = block.getLineBlocks();
			Block firstLine = lineBlocks.get(0);
			List<TextBlock> fragments = firstLine.getFragments();
			fontIds = new TIntIntHashMap();
			for (TextBlock fragment : fragments) {
				boolean upperCase = isUpperCase(fragment.getText());
				if (upperCase) {
					upperCaseCounter++;
				
				}
				ucm.increment(fragment.getHeight());
				ucstdev.increment(fragment.getHeight());
				boolean isDigit = isDigit(fragment.getText());
				if (isDigit) {
					digitCounter++;
				}

				int fontId = fragment.getFontId();
				fontIds.adjustOrPutValue(fontId, 1, 1);
				fragmentCounter++;
			}
			this.fragmentCounter = fragmentCounter;
			this.ucRatio = (double) upperCaseCounter / fragmentCounter;
			this.digitRatio = (double) digitCounter / fragmentCounter;
			this.ucMeanHeight = ucm.getResult();
			this.ucStdevHeight = ucstdev.getResult();
			
			String text = block.getText().trim();
			int i = text.indexOf(' ');
			if (i > 0) {
				String prefix = text.substring(0, i);
				int counter = getSegmentCounter(prefix);
				this.segmentCount = counter > 0 ? counter : -1;
				this.segmentSeparator = prefix.indexOf('.') > 0 ? '.' : 0;
			} else {
				this.segmentCount = -1;
				this.segmentSeparator = 0;
			}
		}

		private boolean isUpperCase(String text) {
			boolean result = false;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (Character.isUpperCase(c)) {
					result = true;
				}
			}
			return result;
		}

		private boolean isDigit(String text) {
			boolean result = false;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (Character.isDigit(c)) {
					result = true;
				}
			}
			return result;
		}

		@Override
		public String toString() {
			return String.format("HeadingBlock [block=%s]", block);
		}
	}

	private final static class MeanDim {
		double heightSum;
		double widthSum;
		int count;

		public void add(double height, double width) {
			heightSum += height;
			widthSum += width;
			count++;
		}
	}

	public static void main(String[] args) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
