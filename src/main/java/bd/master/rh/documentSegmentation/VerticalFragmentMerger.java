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

import java.util.Comparator;
import java.util.Map;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Font;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.TextBlock;


/**
 * Merger for fragments by using a horizontal layout assumption.
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public abstract class VerticalFragmentMerger extends FragmentMerger {
    private final Map<Integer, Font> idToFont;

    /** VerticalFragmentMerger mergeThresholdX */
    protected final double mergeThresholdX;
    /** VerticalFragmentMerger mergeThresholdY */
    protected final double mergeThresholdY;
    

    /**
     * Creates a new instance of this class.
     * @param collection 
     * @param idToFont 
     * @param mergeThresholdX the threshold for merging horizontally 
     * @param mergeThresholdY the threshold for merging vertically
     */
    public VerticalFragmentMerger(Page page, Block collection, Map<Integer, Font> idToFont, double mergeThresholdX, double mergeThresholdY) {
        super(page, collection, Cluster.VERTICAL_CLUSTERS);
        this.idToFont = idToFont;
        this.mergeThresholdX = mergeThresholdX;
        this.mergeThresholdY = mergeThresholdY;
    }
    
    @Override
    protected double getMaxMergeHeight(double maxFragmentHeight) {
        return 3*maxFragmentHeight;
    }
    
    @Override
    protected Comparator<TextBlock> getResultFragmentOrder() {
        return Cluster.HORIZONTAL_FRAGEMENTS;
    }

    @Override
    protected Comparator<Block> getResultCollectionOrder() {
        return Block.VERTICAL_FRAGMENTS;
    } 
}
