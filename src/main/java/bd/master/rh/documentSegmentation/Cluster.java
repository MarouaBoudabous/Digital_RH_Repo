

package bd.master.rh.documentSegmentation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.TextBlock;
import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.hash.TIntIntHashMap;



final class Cluster {
    final int id;
    final List<TextBlock> fragments;
    final SortedSet<Block> collections;
    final TIntIntHashMap fontIds = new TIntIntHashMap();
    
    double centroidX;
    double centroidY;
    double meanHeight;
    double meanWidth;
    double meanSpaceWidth;
    double meanDistanceX;
    double meanDistanceY;
    int minId;
    int maxId;
    double minX;
    double minY;
    double maxX;
    double maxY;
    double maxHeight;
    int majorityFontId;
    double meanFontSize; 
    boolean isUpperCase;
    boolean hasLetters;
    int ucLetterCount;
    int letterCount;
    int fragementCount;
    Object cycleStats;
    
    /**
     * Creates a new instance of this class.
     * @param id 
     * @param collection 
     * @param comparator 
     * @param fragments 
     */
    public Cluster(int id, Block collection, Comparator<Block> comparator, List<TextBlock> fragments) {
        this.id = id;
        if (collection != null) {
            assert comparator != null : "Comparator should never be null!";
            this.collections = new TreeSet<Block>(comparator);
            this.collections.add(collection);
        } else
            this.collections = null;
        if (fragments == null) { throw new IllegalArgumentException("Parameter 'fragments' must not be null!"); }
        if (fragments.size() < 1) { throw new IllegalArgumentException("Parameter 'fragments' must contain at least a single entry!"); }
        
        this.fragments = fragments;
        update();
    }
    
    /**
     * Creates a new instance of this class.
     * @param id 
     * @param clusters 
     */
    public Cluster(int id, Cluster... clusters) {
        this.id = id;
        
        SortedSet<Block> collections = null;
        List<TextBlock> frags = new ArrayList<TextBlock>();
        for (Cluster cluster : clusters) {
            frags.addAll(cluster.fragments);
            
            if (cluster.collections != null) {
                if (collections == null) { collections = new TreeSet<Block>(cluster.collections.comparator()); }
                collections.addAll(cluster.collections);
            }
        }
        this.fragments = frags;
        this.collections = collections;
        update();
    }
    
    private void update() {
        fragementCount = fragments.size();
        
        if (fragementCount == 1) {
        	TextBlock f = fragments.get(0);
            centroidX = f.getX() + f.getWidth()/2;
            centroidY = f.getY() + f.getHeight()/2;
            meanHeight = f.getHeight();
            meanWidth = f.getWidth();
            meanSpaceWidth = f.getWidthOfSpace();
            if (meanSpaceWidth < meanWidth/1.5) { 
                meanSpaceWidth = meanWidth/1.5; 
            }
            minId = f.getSequence();
            maxId = f.getSequence();
            minX = f.getX();
            maxX = f.getX() + f.getWidth();
            minY = f.getY();
            maxY = f.getY() + f.getHeight();
            maxHeight = f.getHeight();
            fontIds.put(f.getFontId(), 1);
            majorityFontId = f.getFontId();
            meanFontSize = f.getFontSize();
            meanDistanceX = f.getWidth(); 
            meanDistanceY = f.getHeight();
            isUpperCase = true;
            hasLetters = false;
            for (int i = 0; i < f.getText().length(); i++) {
                char c = f.getText().charAt(i);
                if (Character.isLetter(c)) {
					hasLetters = true;
					letterCount++;
					if (Character.isUpperCase(c)) {
	                    ucLetterCount++;
	                }
				}
            }
            if (letterCount*0.7 >= ucLetterCount) {
                isUpperCase = false;
            }
        } else {
            double x = 0, y = 0;
            double mx = Double.NaN, nx = Double.NaN, my = Double.NaN, ny = Double.NaN;
            double widthSpace = 0, height = 0, width = 0, mh = 0, fs = 0, distanceX = 0, distanceY = 0;
            int mi = -1, ni = -1;
            isUpperCase = true;
            hasLetters = false;
            TextBlock prev = null;
            letterCount = ucLetterCount = 0;
            for (TextBlock f : fragments) {
                widthSpace += f.getWidthOfSpace();
                height += f.getHeight();
                width += f.getWidth();
                x += f.getX() + f.getWidth()/2;
                y += f.getY() + f.getHeight()/2;
                if (Double.isNaN(mx) || f.getX() < mx) { mx = f.getX(); }
                if (Double.isNaN(nx) || f.getX() + f.getWidth() > nx) { nx = f.getX() + f.getWidth(); }
                if (Double.isNaN(my) || f.getY() < my) { my = f.getY(); }
                if (Double.isNaN(ny) || f.getY() + f.getHeight() > ny) { ny = f.getY() + f.getHeight(); }
                if (f.getHeight() > mh) { mh = f.getHeight(); }
                fontIds.adjustOrPutValue(f.getFontId(), 1, 1);
                fs += f.getFontSize();
                if (mi < 0 || mi > f.getSequence()) { mi = f.getSequence(); }
                if (ni < 0 || ni < f.getSequence()) { ni = f.getSequence(); }
                if (prev != null) {
                    distanceX += f.getX() - (prev.getX()+prev.getWidth());
                    distanceY += f.getY() - prev.getY();
                }
                for (int i = 0; i < f.getText().length(); i++) {
                    char c = f.getText().charAt(i);
                    if (Character.isLetter(c)) {
                    	hasLetters = true;
                    	letterCount++;
                        if (Character.isUpperCase(c)) {
                            ucLetterCount++;
                        }
                    }
                }
                if (!hasLetters) {
                    for (int i = 0; i < f.getText().length(); i++) {
                        char c = f.getText().charAt(i);
                        if (Character.isLetter(c)) {
                            hasLetters = true;
                            break;
                        }
                    }
                }
                prev = f;
            }
            meanHeight = height / fragementCount;
            meanWidth = width / fragementCount;
            meanSpaceWidth = widthSpace / fragementCount;
            if (meanSpaceWidth < meanWidth/1.5) { 
                meanSpaceWidth = meanWidth/1.5; 
            }
            meanDistanceX = fragementCount > 1 ? distanceX / (fragementCount) : Double.NaN;
            meanDistanceY = fragementCount > 1 ? distanceY / (fragementCount) : Double.NaN;
            centroidX = x / fragementCount;
            centroidY = y / fragementCount;
            minId = mi;
            maxId = ni;
            minX = mx;
            minY = my;
            maxX = nx;
            maxY = ny;
            maxHeight = mh;
            meanFontSize = fs / fragementCount;
            if (letterCount*0.7 >= ucLetterCount) {
                isUpperCase = false;
            }
            
            int mf = 0;
            int mfi = -1;
            TIntIntIterator iterator = fontIds.iterator();
            for (int i = fontIds.size(); i-- > 0;) {
                iterator.advance();

                int f = iterator.value();
                if (f > mf) {
                    mf = f;
                    mfi = iterator.key();
                }
            }
            majorityFontId = mfi;
        }
        assert minX <= maxX : "MinX (" + minX + ") should be smaller than maxX (" + maxX + ")";
        assert minY <= maxY : "MinY (" + minY + ") should be smaller than maxY (" + maxY + ")";
        assert maxHeight > 0 : "MaxHeight (" + maxHeight + ") must be positive";
        assert meanHeight > 0 : "MeanHeight (" + meanHeight + ") must be positive";
        assert meanWidth > 0 : "MeanHeight (" + meanWidth + ") must be positive";
        assert meanSpaceWidth > 0 : "MeanHeight (" + meanSpaceWidth + ") must be positive";
    }
    
    double getDistanceX(double x1, double x2, double norm) {
        double x = x2-x1;
        x /= norm;
        return Math.abs(x);
    }
    
    double getDistanceY(double y1, double y2, double norm) {
        double y = y2-y1;
        y /= norm;
        return Math.abs(y);
    }
    
    /**
     * 
     * @param o the other cluster
     * @param s true, if the distance is symmetrical
     * @return the distance
     */
    public double getDistanceX(Cluster o, boolean s) {
        double w1;
        w1 = meanWidth;
        double w2;
        w2 = o.meanWidth; 
        double norm = getNormX(w1, w2);
        
        double d = o.minX - maxX;
        d /= norm;
        return d;
    }
    
    public double getOverlapX(Cluster o) {
        double bx = Math.max(minX, o.minX), cx = Math.min(maxX, o.maxX),
               s = Math.min(maxX - minX, o.maxX - o.minX);
        double i = cx-bx;
        return i >= 0 ? i / s : 0;
    }

    public double getOverlapY(Cluster o) {
        double by = Math.max(minY, o.minY), cy = Math.min(maxY, o.maxY),
               s = Math.min(maxY - minY, o.maxY - o.minY);
        double i = cy-by;
        return i >= 0 ? i / s : 0;
    }
    
    public double getNormX(double w1, double w2) {
        return Math.max(w1, w2);
    }
    
    public double getNormY(double h1, double h2) {
        return Math.min(h1, h2);
    }
    
    public double getCentroidDistance(Cluster o) {
        double x = centroidX-o.centroidX;
        double y = centroidY-o.centroidY;
        return Math.sqrt(x*x + y*y);
    }
    
    public double getDistanceY(Cluster o) {
        double d;
        
        double norm = getNormY(meanFontSize, o.meanFontSize);
        if (o.minY >= minY && o.minY <= maxY) {
            d = o.minY - maxY;
            d /= norm;
            assert d <= 0;
        } else if (o.maxY >= minY && o.maxY <= maxY) {
            d = minY - o.maxY;
            d /= norm;
            assert d <= 0;
        } else if (minY >= o.minY && minY <= o.maxY) {
            d = minY - o.maxY;
            d /= norm;
            assert d <= 0;
        } else if (maxY >= o.minY && maxY <= o.maxY) {
            d = o.minY - maxY;
            d /= norm;
            assert d <= 0;
        } else {
            double d1 = getDistanceY(minY, o.maxY, norm);
            double d2 = getDistanceY(maxY, o.minY, norm);
            d = Math.min(d1, d2);
            assert d >= 0;
        }
        
        return d;
    }
    
    public boolean isMergeCandidate(Cluster o, double maxHeight) {
        double d1 = Math.min(Math.abs(minY-o.maxY), Math.abs(maxY-o.minY));
        double d2 = Math.min(Math.abs(minY-o.minY), Math.abs(maxY-o.maxY));
        double hd = Math.min(d1, d2);
        hd = Math.min(Math.abs(centroidY - o.centroidY), hd);
        return hd <= maxHeight;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Cluster other = (Cluster)obj;
        if (id != other.id) return false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        Set<TextBlock> sortedFragments = new TreeSet<TextBlock>(HORIZONTAL_FRAGEMENTS);
        sortedFragments.addAll(fragments);
        for (TextBlock f : sortedFragments) {
            b.append(f.getText());
        }
        return b.toString();
    }

    static final Comparator<Cluster> VERTICAL_CLUSTERS = new Comparator<Cluster>() {
        public int compare(Cluster o1, Cluster o2) {
            int d = Double.compare(o1.centroidY, o2.centroidY);
            if (d == 0) {
                d = Double.compare(o1.centroidX, o2.centroidX);
                if (d == 0) {
                    d = o1.minId - o2.minId;
                }
            }
            return d;
        }
    };
    
    static final Comparator<Cluster> HORIZONTAL_CLUSTERS = new Comparator<Cluster>() {
        public int compare(Cluster o1, Cluster o2) {
            int d = Double.compare(o1.centroidX, o2.centroidX);
            if (d == 0) {
                d = o1.minId - o2.minId;
            }
            return d;
        }
    };
    
    static final Comparator<Cluster> SEQ_CLUSTERS = new Comparator<Cluster>() {
        public int compare(Cluster o1, Cluster o2) {
            return o1.minId - o2.minId;
        }
    };
    
    static final Comparator<TextBlock> HORIZONTAL_FRAGEMENTS = new Comparator<TextBlock>() {
        public int compare(TextBlock o1, TextBlock o2) {
            return o1.getSequence() - o2.getSequence();
        }
    };

    /**
     * @param b
     * @return
     */
    public int getDistanceId(Cluster b) {
        if (this.minId >= b.minId && this.minId <= b.maxId) {
            return 0;
        } 
        if (b.minId >= this.minId && b.minId <= this.maxId) {
            return 0;
        } 
        return Math.min(Math.abs(this.minId - b.maxId), Math.abs(this.maxId - b.minId));
    }

    /**
     * 
     */
    public void resetCycleStats() {
        cycleStats = null;
    }
}