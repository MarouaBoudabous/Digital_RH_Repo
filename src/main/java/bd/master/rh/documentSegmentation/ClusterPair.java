

package bd.master.rh.documentSegmentation;


final class ClusterPair implements Comparable<ClusterPair> {
    final Cluster a;
    final Cluster b;
    final double d;
    
   
    public ClusterPair(Cluster a, Cluster b, double d) {
        this.a = a;
        this.b = b;
        this.d = d;
    }

    public int compareTo(ClusterPair o) {
        int r = Double.compare(d, o.d);
        if (r == 0) {
            r  = (a.id+b.id) - (o.a.id+o.b.id);
            if (r == 0) {
                r = a.id - o.a.id;
            }
        }
        return r;
    }
}