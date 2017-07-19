

package bd.master.rh.documentSegmentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.ReadingOrder;
import bd.master.rh.documentSegmentation.structure.TextBlock;



public class BlockSplitter extends AbstractSplitter {

    private final Block blocks;
    private final Page page;

    /**
     * Creates a new instance of this class.
     * @param blockCollection 
     */
    public BlockSplitter(Page page, Block blockCollection) {
    	this.page = page;
        this.blocks = blockCollection;
    }

    /**
     * @return
     */
    public Block split() {
        SortedSet<Block> currentBlocks = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        boolean debug = false;

        for (Block block : blocks.getSubBlocks()) {
            SortedSet<Block> lines = block.getSubBlocks();
            double maxDistance = getCutpoint(lines, false);
            if (!Double.isNaN(maxDistance)) {
                double lastPos = Double.NaN;
                SortedSet<Block> currentBlock = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
                if (debug) { System.out.print("Splitting block [" + block + "] into: "); };
                for (Block line : lines) {
                    List<TextBlock> frags = line.getFragments();
                    if (!Double.isNaN(lastPos)) {
                        if ((getDelta(frags, lastPos)) >= maxDistance) {
                            Block c = new Block(page, currentBlock);
                            currentBlocks.add(c);
                            if (debug) { System.out.print("["+c+"]"); };
                            currentBlock = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
                        }
                    }
                    currentBlock.add(line);
                    lastPos = getEnd(frags);
                }
                if (!currentBlock.isEmpty()) {
                    Block c = new Block(page, currentBlock);
                    currentBlocks.add(c);
                    if (debug) { System.out.print("["+c+"]"); };
                }
                if (debug) { System.out.println(); };
            } else {
                currentBlocks.add(new Block(page, lines));
            }
        }
        
        SortedSet<Block> result = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        for (Block block : currentBlocks) {
            ParagraphExtractor pe = new ParagraphExtractor();
            SortedSet<Block> pageBlocks = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
            pageBlocks.add(block);
            ParagraphInformation pi = pe.extract(Arrays.asList(new Block(page, pageBlocks)), null, new ReadingOrder(Arrays.asList(Arrays.asList(0))));
            int[] linesToSplit = pi.getLinesToSplit(block);
            if (linesToSplit != null && linesToSplit.length > 0) {
                List<Block> lineBlocks = new ArrayList<Block>(block.getLineBlocks());
                int to = lineBlocks.size();
                for (int i = linesToSplit.length-1; i >= -1; i--) {
                    int index = i >= 0 ? linesToSplit[i] : 0;
                    List<Block> subList = lineBlocks.subList(index, to);
                    SortedSet<Block> lines = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
                    lines.addAll(subList);
                    result.add(new Block(page, lines));
                    to = index;
                    if (index == 0) { break; }
                }
            } else { 
                result.add(block);
            }
        }
        
        return new Block(page, result);
    }
    
    @Override
    protected double getDelta(List<TextBlock> current, double lastPos) {
        return doGetDelta(current, lastPos);
    }

    static double doGetDelta(List<TextBlock> current, double lastPos) {
        return getMinY(current)-lastPos;
    }
    
    static double getAvgHeight(List<TextBlock> current) {
        double m = 0;
        for (TextBlock f : current) {
            float d = f.getHeight();
            m += d;
        }
        return (float)(m / current.size());
    }
    
    @Override
    protected float getEnd(List<TextBlock> current) {
        return doGetEnd(current);
    }

    static float doGetEnd(List<TextBlock> current) {
        double m = 0;
        for (TextBlock f : current) {
            float d = f.getY()+(f.getHeight()/2.0f);
            m += d;
        }
        return (float)(m / current.size());

    }

    private static double getMinY(List<TextBlock> current) {
        double m = 0;
        for (TextBlock f : current) {
            float d = f.getY()+(f.getHeight()/2.0f);
            m += d;
        }
        return (float)(m / current.size());

    }

    
}
