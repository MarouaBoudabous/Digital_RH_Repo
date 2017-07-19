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
import java.util.SortedSet;
import java.util.TreeSet;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.TextBlock;


/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public class LineSplitter extends AbstractSplitter {

    private final Block lines;
    private final Page page;

    /**
     * Creates a new instance of this class.
     * @param merge
     */
    public LineSplitter(Page page, Block lineCollection) {
        this.lines = lineCollection;
        this.page = page;
    }

    /**
     * @return
     */
    public Block split() {
        SortedSet<Block> result = new TreeSet<Block>(Block.VERTICAL_FRAGMENTS);
        boolean debug = true;

        for (Block line : lines.getSubBlocks()) {
            SortedSet<Block> words = line.getSubBlocks();
//            if (line.getFragments().get(0).seq == 1446) {
//                System.out.println();
//            }
            
            if (words.size() < 16) {
                result.add(new Block(page, words));
            } else {
//                if (words.get(0).getFragments().get(0).seq == 21) {
//                    System.out.println();
//                }
                double maxDistance = getCutpoint(words, false);
                 if (!Double.isNaN(maxDistance)) {
                    double lastPos = Double.NaN;
                    SortedSet<Block> currentLine = new TreeSet<Block>(Block.HORIZONTAL_FRAGMENTS);
                    if (debug) { System.out.print("Splitting line [" + line + "] into: "); };
                    for (Block word : words) {
                        List<TextBlock> frags = word.getFragments();
                        if (!Double.isNaN(lastPos)) {
                        	TextBlock first = frags.get(0);
                            double d = first.getX()-lastPos;
                            if ((!Double.isNaN(maxDistance) && d >= maxDistance) || d < -10) {
                                Block c = new Block(page, currentLine);
                                result.add(c);
                                if (debug) { System.out.print("["+c+"]"); };
                                currentLine = new TreeSet<Block>(Block.HORIZONTAL_FRAGMENTS);
                            }
                        }
                        currentLine.add(word);
                        TextBlock last = frags.get(frags.size()-1);
                        lastPos = last.getX()+last.getWidth();
                    }
                    if (!currentLine.isEmpty()) {
                        Block c = new Block(page, currentLine);
                        result.add(c);
                        if (debug) { System.out.print("["+c+"]"); };
                    }
                    if (debug) { System.out.println(); };
                } else {
                    result.add(new Block(page, words));
                }
            }
        }
        
        return new Block(page, result);
    }

       
}
