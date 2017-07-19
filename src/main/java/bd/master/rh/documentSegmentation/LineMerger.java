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

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Page;




/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class LineMerger extends HorizontalFragmentMerger {

    /**
     * Creates a new instance of this class.
     * @param wordFragments
     */
    public LineMerger(Page page, Block wordFragments) {
        super(page, wordFragments, 7, 0.3);
    }
    
    @Override
    protected double getDistance(Cluster a, Cluster b) {
        int idDistance = a.getDistanceId(b); 
        if (idDistance > 10) { 
            return Double.NaN; 
        }
        if (b.minId > a.minId && b.maxId < a.maxId) {
            return 1;
        }

        if (a.fragments.size() > 2 && b.fragments.size() > 2) {
        }
        

        double threshold = mergeThresholdX;
        double dx = a.getDistanceX(b, true) / threshold;
        double overlapY = a.getOverlapY(b);
        
        double cd1;
        if (a.centroidY > b.centroidY) {
            cd1 = a.centroidY - (b.centroidY+b.meanHeight/2f);
        } else {
            cd1 = (b.centroidY-b.meanHeight/2f) - a.centroidY;
        }
        double cd2;
        if (b.centroidY > a.centroidY) {
            cd2 = b.centroidY - (a.centroidY+a.meanHeight/2f);
        } else {
            cd2 = (a.centroidY-a.meanHeight/2f) - b.centroidY;
        }
        overlapY = Math.min(cd1, cd2);
            
        
//        double dy = 1 - (overlapY-mergeThresholdY); //Math.abs(a.centroidY - b.centroidY) / Math.max(a.maxHeight, b.maxHeight);
        double dy = 1 + overlapY;
        if (dx < 1 && dy < 1) {
            double distance = Math.max(dx, dy);
            distance += overlapY*0.001;
            return distance;
        }
        return Double.NaN;
    }

 
}
