package bd.master.rh.documentSegmentation;

import java.util.List;
import java.util.SortedSet;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.TextBlock;




public class AbstractSplitter {
    /**
     * @param collections
     * @param defaultDebug 
     * @return
     */
    protected double getCutpoint(SortedSet<Block> collections, boolean defaultDebug) {
        boolean debug = defaultDebug;

        
        double min = Double.NaN, max = Double.NaN, mean = 0, meanDistance = 0;
        TextBlock maxFrag = null, minFrag = null;
        {
            double lastPos = Double.NaN;
            double distance = 0;
            for (Block word : collections) {
                List<TextBlock> frags = word.getFragments();
                boolean hasLastPos = !Double.isNaN(lastPos);
                if (hasLastPos) {
                	TextBlock first = frags.get(0);
                    double d = getDelta(frags, lastPos);
                    if (!Double.isNaN(min)) {
                        if (d < min) { min = d; minFrag = first; }
                        else if (d > max) { max = d; maxFrag = first; }
                    } else {
                        min = max = d; minFrag = maxFrag = first;
                    }
                    mean += d;
                    if (debug) { System.out.println(String.format("%.3f - %d - %s", d, frags.get(0).getSequence(), word.toString().replace('\n', ' '))); }
                } else {
                    if (debug) { System.out.println(String.format("%.3f - %d - %s", 0.0, frags.get(0).getSequence(), word.toString().replace('\n', ' '))); }
                }
                double old = lastPos;
                lastPos = getEnd(frags);
                if (hasLastPos) {
                    distance += (lastPos-old);
                }
            }
            mean /= collections.size()-1;
            meanDistance = distance / (collections.size()-2); 
        }
        if (max <= 0) { return Double.NaN; }
        if (max < min+Math.abs(mean)) { return Double.NaN; }

        double c1 = min, c2 = max, s1 = c1, s2 = c2; 
        double maxc2 = min;
        double minc2 = max;
        int n1 = 1, n2 = 1;
       
        {
            double lastPos = Double.NaN;
            for (Block collection : collections) {
                List<TextBlock> frags = collection.getFragments();
                if (!Double.isNaN(lastPos)) {
                	TextBlock first = frags.get(0);
                    if (first != minFrag && first != maxFrag) {
                        double d = getDelta(frags, lastPos);
                        double d1 = Math.abs(c1 - d), d2 = Math.abs(c2 - d);
                        if (debug) { System.out.println(String.format("%.3f - %.3f, %.3f - %.3f (%d), %.3f (%d)", 
                                d, d1, d2, c1, n1, c2, n2)); }
                        if (d1 <= d2) {
                            s1 += d; n1++; c1 = s1 / n1;
                          
                            if (Double.isNaN(maxc2) || d > maxc2) { maxc2 = d; }
                        } else {
                            s2 += d; n2++; c2 = s2 / n2;
                            if (Double.isNaN(minc2) || d < minc2) { minc2 = d; }
                        }
                    }
                }
                lastPos = getEnd(frags);
            }
        }

  
        double cutpoint = Double.NaN; 
        boolean isCutpoint = n1 >= n2 && c2 > (c1+meanDistance*0.01) && n2 <= 2 && c2 > 0;
        if (isCutpoint) {
            cutpoint = minc2;
        } 
        
        return cutpoint;  
    }
    
    protected double getCutpoint(List<TextBlock> fragments) {
        boolean debug = false;
        
        double min = Double.NaN, max = Double.NaN, mean = 0;
        TextBlock maxFrag = null, minFrag = null;
        {
            double lastPos = Double.NaN, lastX = Double.NaN;;
            for (TextBlock f : fragments) {
                if (!Double.isNaN(lastPos)) {
                    double d = f.getPosition().getBoxCoord().getxTopLeftCorner()-lastPos;
                    if (!Double.isNaN(min)) {
                        if (d < min) { min = d; minFrag = f; }
                        else if (d > max) { max = d; maxFrag = f; }
                    } else {
                        min = max = d; minFrag = maxFrag = f;
                    }
                    mean += f.getPosition().getBoxCoord().getxTopLeftCorner()-lastX;
                    if (debug) { System.out.println(String.format("%.3f - %s - %d", d, f.getText(), f.getSequence())); }
                } else {
                    if (debug) { System.out.println(String.format("%.3f - %s - %d", 0.0, f.getText(), f.getSequence())); }
                }
                lastPos = f.getPosition().getBoxCoord().getxTopLeftCorner()+(f.getPosition().getBoxCoord().getxBottomRightCorner()-f.getPosition().getBoxCoord().getxTopLeftCorner());
                lastX = f.getPosition().getBoxCoord().getxTopLeftCorner();
            }
            mean /= fragments.size()-1;
        }
        if (max <= 0) { return Double.NaN; }

        double c1 = min, c2 = max, s1 = c1, s2 = c2;
        double minc2 = Double.NaN;
        int n1 = 1, n2 = 1;
        {
            double lastPos = Double.NaN;
            for (TextBlock f : fragments) {
                if (!Double.isNaN(lastPos)) {
                    if (f != minFrag && f != maxFrag) {
                        double d = f.getPosition().getBoxCoord().getxTopLeftCorner()-lastPos;
                        double d1 = Math.abs(c1 - d), d2 = Math.abs(c2 - d);
                        if (debug) { System.out.println(String.format("%.3f - %.3f, %.3f - %.3f (%d), %.3f (%d)", 
                                d, d1, d2, c1, n1, c2, n2)); }
                        if (d1 <= d2) {
                            s1 += d; n1++; c1 = s1 / n1;
                        } else {
                            s2 += d; n2++; c2 = s2 / n2;
                            if (Double.isNaN(minc2) || d < minc2) { minc2 = d; }
                        }
                    }
                }
                lastPos = f.getPosition().getBoxCoord().getxTopLeftCorner()+(f.getPosition().getBoxCoord().getxBottomRightCorner()-f.getPosition().getBoxCoord().getxTopLeftCorner());;
            }
        }

        double cutpoint = Double.NaN;
        if (n1 > n2*2 && c2 > (mean/4)) {
            cutpoint = minc2;
        }
        
        return cutpoint;
    }

    /**
     * @param current
     * @return
     */
    protected float getEnd(List<TextBlock> current) {
    	TextBlock last = current.get(current.size()-1);
        return last.getPosition().getBoxCoord().getxTopLeftCorner()+ (last.getPosition().getBoxCoord().getxTopLeftCorner()+(last.getPosition().getBoxCoord().getxBottomRightCorner()-last.getPosition().getBoxCoord().getxTopLeftCorner()));
    }

    /**
     * @param frags
     * @param lastPos
     * @return
     */
    protected double getDelta(List<TextBlock> frags, double lastPos) {
        return frags.get(0).getPosition().getBoxCoord().getxTopLeftCorner()-lastPos;
    }
    

}
