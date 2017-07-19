package bd.master.rh.documentSegmentation.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

public class Block {

	private List<TextBlock> fragments;
	private Box BoundingBox;
	private SortedSet<Block> subBlocks;
	private List<Block> lineBlocks;
	private Page page;

	public static final Comparator<Block> VERTICAL_FRAGMENTS = new Comparator<Block>() {

		public int compare(Block b1, Block b2) {
			return 0;
		};
	};

	
	public static final Comparator<Block> ID_FRAGMENTS = new Comparator<Block>() {

		public int compare(Block b1, Block b2) {
			return 0;
		};
	};
	
	public static final Comparator<Block> HORIZONTAL_FRAGMENTS = new Comparator<Block>() {

		public int compare(Block b1, Block b2) {
			return 0;
		};
	};
	
	public List<Block> getLineBlocks() {
		return lineBlocks;
	}

	public void setLineBlocks(List<Block> lineBlocks) {
		this.lineBlocks = lineBlocks;
	}

	public SortedSet<Block> getSubBlocks() {
		return subBlocks;
	}

	public void setSubBlocks(SortedSet<Block> subBlocks) {
		this.subBlocks = subBlocks;
	}

	public Box getBoundingBox() {
		return BoundingBox;
	}

	public void setBoundingBox(Box boundingBox) {
		BoundingBox = boundingBox;
	}

	public Block(Page page, SortedSet<Block> currentBlock) {
		this.page=page;
		setSubBlocks(currentBlock);
	}

	public Block(Page page, ArrayList<TextBlock> arrayList) {
		this.page=page;
		setFragments(arrayList);
	}

	

	public Block(Page page2, List<TextBlock> fragments2) {
		this.page=page2;
		setFragments(fragments2);
	}

	public List<TextBlock> getFragments() {
		return fragments;
	}

	public void setFragments(List<TextBlock> fragments) {
		this.fragments = fragments;
	}

	public Boolean hasSubBlocks() {
		return !subBlocks.isEmpty();
	}

	public float getMeanFontSize() {
		return 0;
	}

	public String getText() {
		// TODO Auto-generated method stub
		return null;
	}

	public Page getPage() {
		
		return this.page;
	}

}
