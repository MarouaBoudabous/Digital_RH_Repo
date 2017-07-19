/**
 * Copyright (C) 2010
 * "Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH"
 * (Know-Center), Graz, Austria, office@know-center.at.
 *
 * Licensees holding valid Know-Center Commercial licenses may use this file in
 * accordance with the Know-Center Commercial License Agreement provided with
 * the Software or, alternatively, in accordance with the terms contained in
 * a written agreement between Licensees and Know-Center.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package bd.master.rh.documentSegmentation;

import java.util.List;

import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.TextBlock;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class WordMerger extends HorizontalFragmentMerger {

    /** double addWidthToDistance */
    private static final double addWidthToDistance = 0.1; //0.25;
    private static final boolean useWordSplitting = false;
    
    /**
     * Creates a new instance of this class.
     * @param fragments
     */
    public WordMerger(Page page, List<TextBlock> fragments) {
        super(page, fragments, 1.7, 0.35);
    }
    
    @Override
    protected double getDistance(Cluster a, Cluster b) {
        boolean debug = false;
        int idDistance = a.getDistanceId(b); 
        if (idDistance > 1) { 
            return Double.NaN; 
        }
        
        TextBlock leftFrag = a.fragments.get(a.fragments.size()-1);
        TextBlock rightFrag = b.fragments.get(0);
        if (rightFrag != null && rightFrag.isWordBreakHint()) {
            return Double.NaN;
        }
        
        if ((b.maxY-(b.maxY-b.minY)/3) < a.centroidY) {
            return Double.NaN;
        }
        if ((b.minY+(b.maxY-b.minY)/3) > a.centroidY) {
            return Double.NaN;
        }

        if (a.fragementCount >= 2 || b.fragementCount >= 2) {
            double maxExpectedDistanceX = a.fragementCount >= b.fragementCount ? 
                    a.meanDistanceX+/*addWidthToDistance*/0.25*a.meanWidth : b.meanDistanceX+0.25*b.meanWidth;
            double dis = b.minX - a.maxX;
            if (dis > maxExpectedDistanceX) {
                return Double.NaN;
            }
        }  

        if (isSeparatorChar(leftFrag) || isSeparatorChar(rightFrag)) {
            return Double.NaN;
        }
    
        
        double dy = a.getDistanceY(b) / mergeThresholdY;
        if (dy < 1) {
            boolean o = a.minId < b.minId;
            Cluster c1 = o ? a : b, c2 = o ? b : a;
            double distanceX = c1.getDistanceX(c2, false);
            double dx = distanceX / mergeThresholdX;
            if (dx < -1) {
                return Double.NaN;
            }
            if (dx < 1 && dy < 1) {
                double distance = distanceX;
                if (debug) {
                    System.out.println();
                }
                return distance;
            }
        }
        return Double.NaN;
    }


    
    private boolean startsWithDigit(TextBlock frag) {
        boolean isDigit = false;
        char c = frag.getText().charAt(0);
        if (Character.isDigit(c)) {
            isDigit = true;
        }
        return isDigit;
    }
    

    
    private boolean startsWithLetter(TextBlock frag) {
        boolean isDigit = false;
        char c = frag.getText().charAt(0);
        if (Character.isLetter(c)) {
            isDigit = true;
        }
        return isDigit;
    }
    

    private boolean isSeparatorChar(TextBlock frag) {
        boolean isSeparatorChar = false;
        if (useWordSplitting) {
            for (int i = 0; i < frag.getText().length(); i++) {
                char c = frag.getText().charAt(i);
                if (c == '*' || c == ',' || c == ';' || (c >= 0x2000 && c <= 0x206F) || c == '(' || c == ')') {
                    isSeparatorChar = true;
                    break;
                }
            }
        }
        return isSeparatorChar;
    }

}
