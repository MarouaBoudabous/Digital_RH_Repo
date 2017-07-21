package bd.master.rh.documentSegmentation.structure;

import java.util.List;

public class ReadingOrder {
	
	private List<List<Integer>> readingOrder;

	public ReadingOrder(List<List<Integer>> list) {
	   this.readingOrder=list;
	}

	public List<Integer> getReadingOrder(int id) {
		
		return this.readingOrder.get(id);
	}

}
