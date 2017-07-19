

package bd.master.rh.documentSegmentation;


import java.util.Comparator;
//import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Box;
import bd.master.rh.documentSegmentation.structure.Font;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.TextBlock;



public class BlockMerger extends VerticalFragmentMerger {
    /** double MAX_REL_FONT_SIZE_DIFF */
    private static final double MAX_REL_FONT_SIZE_DIFF = 0.12;
    /** double MAX_GLYPH_REL_DIFFERENCE */
    private static final double MAX_GLYPH_REL_DIFFERENCE = 0.17;
    private static final double REL_MEAN_HEIGHT_LINE_GAP = 0.2;
    private static final int EXPECTED_SCALE_LINE_GAP = 2;
    
    private final double lineSpacing;

    /**
     * Creates a new instance of this class.
     * @param lineFragments
     * @param lineSpacing 
     * @param idToFont
     */
    public BlockMerger(Page page, Block lineFragments,
                       double lineSpacing, Map<Integer, Font> idToFont) {
        super(page, lineFragments, idToFont, 0.7, lineSpacing * 0.7);
        this.lineSpacing = lineSpacing;
    }
    
    @Override
    protected boolean useExhaustiveSearch() {
        return true;
    }
    
    @Override
    protected Comparator<Block> getResultCollectionOrder() {
        return Block.VERTICAL_FRAGMENTS;
    }
    
    @Override
    protected void resetCycleStats(Cluster a) {
        double[] minDistanceY = new double[2];
        minDistanceY[0] = minDistanceY[1] = Double.NaN;
        a.cycleStats = minDistanceY;
    }
    
    @Override
    protected void updateCycleStats(Cluster a, Cluster b) {
        double overlapX = a.getOverlapX(b);
        if (overlapX > 0) {
            double distanceY = a.getDistanceY(b);
            {
                double[] minDistanceY = (double[])a.cycleStats;
                if (Double.isNaN(minDistanceY[0]) || distanceY < minDistanceY[0]) {
                    minDistanceY[0] = distanceY;
                }
            }
            {
                double[] minDistanceY = (double[])b.cycleStats;
                if (Double.isNaN(minDistanceY[0]) || distanceY < minDistanceY[0]) {
                    minDistanceY[1] = distanceY;
                }
            }
        }
    }

    @Override
    protected double getDistance(Cluster a, Cluster b) {
        double overlapX = a.getOverlapX(b);
        if (overlapX <= 0) {
            return Double.NaN;
        }
        double distanceY = a.getDistanceY(b);
        
        
        {
            double[] minDistanceY = (double[])a.cycleStats;
            if (minDistanceY != null && distanceY*0.98 > minDistanceY[0] && distanceY > mergeThresholdY/2) {
                return Double.NaN;
            }
        }
        {
            double[] minDistanceY = (double[])b.cycleStats;
            if (minDistanceY != null && distanceY*0.98 > minDistanceY[0] && distanceY > mergeThresholdY/2) {
                return Double.NaN;
            }
        }
        
        double meanHeight = Math.min(a.meanHeight, b.meanHeight);
        double relHeightDiff = Math.abs(a.meanHeight-b.meanHeight) / meanHeight;
        double meanFontSize = Math.min(a.meanFontSize, b.meanFontSize);
        double relFontSizeDiff = Math.abs(a.meanFontSize-b.meanFontSize) / meanFontSize;
        boolean containsFont = containsFontsWhole(a, b);
        if (!containsFont) {
            return Double.NaN;
        }

        if (relFontSizeDiff >= MAX_REL_FONT_SIZE_DIFF) {
            return Double.NaN;
        }
        if (a.isUpperCase != b.isUpperCase && a.letterCount > 4 && b.letterCount > 4) {
            return Double.NaN;
        }
        if (!a.fontIds.contains(b.majorityFontId) && !b.fontIds.contains(a.majorityFontId)) {
            return Double.NaN;
        }
        if (b.majorityFontId != a.majorityFontId) {
            return Double.NaN;
        }
        double lineGap = getLineGap(a, b);
        if (a.collections.size() >= 3) {
            Stats agaps = getMeanLineGaps(a);
            if (agaps.getN() >= 2) {
                double maxExpected = agaps.getMaxExpected(EXPECTED_SCALE_LINE_GAP)+REL_MEAN_HEIGHT_LINE_GAP*meanHeight;
                if (lineGap > maxExpected) {
                    return Double.NaN;
                }
            }
        }
        if (b.collections.size() >= 3) {
            Stats bgaps = getMeanLineGaps(b);
            if (bgaps.getN() >= 2) {
                double maxExpected = bgaps.getMaxExpected(EXPECTED_SCALE_LINE_GAP)+REL_MEAN_HEIGHT_LINE_GAP*meanHeight;
                if (lineGap > maxExpected) {
                    return Double.NaN;
                }
            }
        }

        
        double dx = overlapX;
        dx = 1 - (dx-mergeThresholdX);
        double dy = distanceY / mergeThresholdY;
        if (dx < 1 && dy < 1) {
            double distance = dy;
            distance += 0.2*(relHeightDiff + relFontSizeDiff);
            distance += 1 / getOverlapX(a, b);
            return distance;
        }
        return Double.NaN;
    }

    private double getOverlapX(Cluster a, Cluster b) {
        Block last = a.collections.last();
        Block first = b.collections.first();
        
        Box bb1 = last.getBoundingBox();
        Box bb2 = first.getBoundingBox();
        
        double bx = Math.max(bb1.getMinx(), bb2.getMinx()), cx = Math.min(bb1.getMaxx(), bb2.getMaxx()),
               dx = Math.min(bb1.getMinx(), bb2.getMinx()), ex = Math.max(bb1.getMaxx(), bb2.getMaxx());
        double i = cx-bx;
        return i >= 0 ? i / (ex-dx) : 0;
    }



    private Stats getMeanLineGaps(Cluster c) {
        Stats m = new Stats();
        
        double end = Double.NaN;
        for (Block line : c.collections) {
            List<TextBlock> fragments = line.getFragments();
            if (!Double.isNaN(end)) {
                double delta = BlockSplitter.doGetDelta(fragments, end);
                m.increment(delta);
            }
            end = BlockSplitter.doGetEnd(fragments);
        }
        return m;
    }
    
    private double getLineGap(Cluster a, Cluster b) {
        Block lastLine = a.collections.last();
        Block firstLine = b.collections.first();

        return BlockSplitter.doGetDelta(firstLine.getFragments(), BlockSplitter.doGetEnd(lastLine.getFragments()));
    }

    private boolean containsFontsWhole(Cluster a, Cluster b) {
        boolean containsFont = false;
        for (int id : a.fontIds.keys()) {
            if (b.fontIds.containsKey(id)) { 
                containsFont = true;
                break;
            }
        }
        return containsFont;
    }


}
