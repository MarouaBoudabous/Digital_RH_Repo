
package bd.master.rh.documentSegmentation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.TextBlock;


public abstract class FragmentMerger {
    protected final SortedSet<Cluster> clusters;
    protected double maxMergeHeight;
    protected final Page page;
    

    
    public FragmentMerger(Page page, Iterable<TextBlock> fragments, Comparator<Cluster> clusterComparator) {
        this.clusters = new TreeSet<Cluster>(clusterComparator);
        this.page = page;
        
        int id = 0;
        double maxFragmentHeight = 0;
        for (TextBlock fragment : fragments) {
            Cluster c = new Cluster(id++, null, null, Arrays.asList(fragment));
            if (c.maxHeight > maxFragmentHeight) { maxFragmentHeight = c.maxHeight; }
            clusters.add(c);
        }
        maxMergeHeight = getMaxMergeHeight(maxFragmentHeight);
    }
    
   
    public FragmentMerger(Page page, Block collection, Comparator<Cluster> clusterComparator) {
        this.clusters = new TreeSet<Cluster>(clusterComparator);
        this.page = page;
        
        int id = 0;
        double maxFragmentHeight = 0;
        
        if (collection.hasSubBlocks()) {
            for (Block sc : collection.getSubBlocks()) {
                Cluster c = new Cluster(id++, sc, getResultCollectionOrder(), sc.getFragments());
                if (c.maxHeight > maxFragmentHeight) { maxFragmentHeight = c.maxHeight; }
                clusters.add(c);
            }
        } else {
            for (TextBlock fragment : collection.getFragments()) {
                Cluster c = new Cluster(id++, null, null, Arrays.asList(fragment));
                if (c.maxHeight > maxFragmentHeight) { maxFragmentHeight = c.maxHeight; }
                clusters.add(c);
            }
        }
        maxMergeHeight = getMaxMergeHeight(maxFragmentHeight);
    }

    
    protected abstract double getMaxMergeHeight(double maxFragmentHeight);

   
    public Block merge() {
        boolean isFinished = false;
        do {
            SortedSet<ClusterPair> candidates = new TreeSet<ClusterPair>();
            List<Cluster> clusterList = new ArrayList<Cluster>(clusters);
            
            if (useCycleState()) {
                for (int i = 0; i < clusterList.size(); i++) {
                    Cluster a = clusterList.get(i);
                    resetCycleStats(a);
                }
                for (int i = 0; i < clusterList.size(); i++) {
                    Cluster a = clusterList.get(i);
                    for (int j = i+1; j < clusterList.size(); j++) {
                        Cluster b = clusterList.get(j);
                        if (isMergeCandidate(a, b)) {
                            assert a.minId != b.minId : "Two clusters must never have the same min-id!";
                            if (a.minId <= b.minId)
                                updateCycleStats(a, b);
                            else
                                updateCycleStats(b, a);
                        } else if (!useExhaustiveSearch()) {
                            break;
                        }
                    }
                }
            }
            
            for (int i = 0; i < clusterList.size(); i++) {
                Cluster a = clusterList.get(i);
                for (int j = i+1; j < clusterList.size(); j++) {
                    Cluster b = clusterList.get(j);
                    
                    if (isMergeCandidate(a, b)) {
                        assert a.minId != b.minId : "Two clusters must never have the same min-id!";
                        //System.out.println(a.minId + "+++++++++++++++"+ b.minId);
                        double distance = a.minId <= b.minId ? getDistance(a, b) : getDistance(b, a);
                        if (!Double.isNaN(distance)) {
                            candidates.add(new ClusterPair(a, b, distance));
                        }
                    } else if (!useExhaustiveSearch()) {
                        break;
                    }
                }
            }
            
            int pendingMergeCandidates = 0;
            Set<Cluster> mergedClusters = new HashSet<Cluster>();
            for (ClusterPair candidate : candidates) {
                if (!mergedClusters.contains(candidate.a) && !mergedClusters.contains(candidate.b)) {
                    clusters.remove(candidate.a);
                    clusters.remove(candidate.b);
                    
                    Cluster c;
                    if (candidate.a.id < candidate.b.id) {
                        c = new Cluster(candidate.a.id, candidate.a, candidate.b);
                    } else {
                        c = new Cluster(candidate.b.id, candidate.b, candidate.a);
                    }
                    clusters.add(c);
                    
                } else {
                    pendingMergeCandidates++;
                }
               
                mergedClusters.add(candidate.a);
                mergedClusters.add(candidate.b);
            }
            isFinished = candidates.isEmpty() || pendingMergeCandidates == 0;
        } while (!isFinished);
        
        postProcessClusters();
        
        return getMergeResult();
    }

    /**
     * @return
     */
    protected boolean useCycleState() {
        return false;
    }

    /**
     * @param a
     */
    protected void resetCycleStats(Cluster a) {
    }

    /**
     * @param a
     * @param b
     */
    protected void updateCycleStats(Cluster a, Cluster b) {
    }

    /**
     * @return
     */
    protected boolean useExhaustiveSearch() {
        return false;
    }

    /**
     * Callback for derived classes to add logic to filter out spurious clusters.
     */
    protected void postProcessClusters() {
    }

    protected abstract double getDistance(Cluster a, Cluster b);

    /**
     * @param a
     * @param b
     * @return
     */
    protected boolean isMergeCandidate(Cluster a, Cluster b) {
        return a.isMergeCandidate(b, maxMergeHeight);
    }

    private Block getMergeResult() {
        SortedSet<Block> collections = new TreeSet<Block>(getResultCollectionOrder());
        
        for (Cluster c : clusters) {
            Block entry;
            if (c.collections != null) {
                SortedSet<Block> sorted = new TreeSet<Block>(c.collections.comparator());
                sorted.addAll(c.collections);
                entry = new Block(page, sorted);
            } else {
                Set<TextBlock> sorted = new TreeSet<TextBlock>(getResultFragmentOrder());
                sorted.addAll(c.fragments);
                entry = new Block(page, new ArrayList<TextBlock>(sorted));
            }
            
            collections.add(entry);
        }
        
        return new Block(page, collections);
    }

    /**
     * @return
     */
    protected abstract Comparator<TextBlock> getResultFragmentOrder();

    /**
     * @return
     */
    protected abstract Comparator<Block> getResultCollectionOrder();

    protected boolean contains(Cluster a, Cluster b, double threshold) {
        double xs1 = a.minX;
        double xe1 = a.maxX;
    
        double xs2 = b.minX;
        double xe2 = b.maxX;
        
        double w = Math.min(a.maxX - a.minId, b.maxX - b.minId);
    
        if (xe2 <= xs1 || xs2 >= xe1) {
            return false;
        } else if ((xs2 > xs1) && (xe2 > xe1)) {
            double overlap = xe1 - xs2;
            double overlapPercent = overlap / (w);
            return (overlapPercent > threshold);
        } else if ((xs2 < xs1) && (xe2 < xe1)) {
            double overlap = xe2 - xs1;
            double overlapPercent = overlap / (w);
            return (overlapPercent > threshold);
        }
        return true;
    }
}
