package bd.master.rh.documentSegmentation.structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;

public class Block implements Comparable {

	private static final long serialVersionUID = -2526867723386901146L;
	private List<TextBlock> fragments;
	private Box BoundingBox;
	private SortedSet<Block> subBlocks;
	private List<Block> lineBlocks;
	private Page page;
	private String text;

	@Override
	public int hashCode() {
		final int prime = getText().length();
		int result = 1;
		result = prime * result + ((getText() == null) ? 0 : getText().hashCode());
		return result;
	}

	public void setText(String text) {
		this.text = text;
	}

	public static final Comparator<Block> VERTICAL_FRAGMENTS = new Comparator<Block>() {

		public int compare(Block b1, Block b2) {
			if (b1.getBoundingBox().getyBottomRightCorner() < b2.getBoundingBox().getyBottomRightCorner()) {
				return -1;
			} else if (b2.getBoundingBox().getyBottomRightCorner() < b1.getBoundingBox().getyBottomRightCorner()) {
				return 1;
			}
			return 0;
		};
	};

	public static final Comparator<Block> ID_FRAGMENTS = new Comparator<Block>() {

		public int compare(Block b1, Block b2) {
			return b1.getFragments().get(b1.getFragments().size() - 1).getSequence()
					- b2.getFragments().get(b2.getFragments().size() - 1).getSequence();
		};
	};

	public static final Comparator<Block> HORIZONTAL_FRAGMENTS = new Comparator<Block>() {

		public int compare(Block b1, Block b2) {

			if ((b1.getBoundingBox().getxTopLeftCorner() < b2.getBoundingBox().getxTopLeftCorner())
					&& isAtSameHorizontalLevel(b1, b2)) {
				return -1;
			}
			if ((b1.getBoundingBox().getxTopLeftCorner() < b2.getBoundingBox().getxTopLeftCorner())
					&& !isAtSameHorizontalLevel(b1, b2)) {
				if (b1.getBoundingBox().getyBottomRightCorner() < b2.getBoundingBox().getyBottomRightCorner())
					return -1;
				else
					return 1;
			}
			if ((b1.getBoundingBox().getxTopLeftCorner() > b2.getBoundingBox().getxTopLeftCorner())
					&& isAtSameHorizontalLevel(b1, b2)) {
				return 1;
			}
			if ((b1.getBoundingBox().getxTopLeftCorner() > b2.getBoundingBox().getxTopLeftCorner())
					&& !isAtSameHorizontalLevel(b1, b2)) {
				if (b1.getBoundingBox().getyBottomRightCorner() < b2.getBoundingBox().getyBottomRightCorner())
					return 1;
				else
					return -1;
			}
			if((b1.getBoundingBox().getxTopLeftCorner() == b2.getBoundingBox().getxTopLeftCorner())&& !isAtSameHorizontalLevel(b1, b2)) {
				if(b1.getBoundingBox().getyBottomRightCorner() < b2.getBoundingBox().getyBottomRightCorner())
				{
					return -1;
				}
				if(b1.getBoundingBox().getyBottomRightCorner() > b2.getBoundingBox().getyBottomRightCorner())
				{
					return 1;
				}
				if(b1.getBoundingBox().getyBottomRightCorner() < b2.getBoundingBox().getyBottomRightCorner())
				{
					return 0;
				}
			}

			return 0;
		};
	};

	public List<Block> getLineBlocks() {
		lineBlocks = new ArrayList<Block>();
		for (Block b : subBlocks) {
			lineBlocks.add(b);
		}
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
		this.page = page;
		setSubBlocks(currentBlock);
		fillFragmentsIfNull();
		float xTopLeftCorner = subBlocks.stream().map(b -> b.BoundingBox.getxTopLeftCorner()).min(Float::compareTo)
				.get();
		float xBottomRightCorner = subBlocks.stream().map(b -> b.BoundingBox.getxBottomRightCorner())
				.max(Float::compareTo).get();
		float yTopLeftCorner = subBlocks.stream().map(b -> b.BoundingBox.getyTopLeftCorner()).min(Float::compareTo)
				.get();
		float yBottomRightCorner = subBlocks.stream().map(b -> b.BoundingBox.getyBottomRightCorner())
				.max(Float::compareTo).get();
		this.setBoundingBox(new Box(xTopLeftCorner, xBottomRightCorner, yTopLeftCorner, yBottomRightCorner));
	}

	public void fillFragmentsIfNull() {
		List<TextBlock> frags = new ArrayList<TextBlock>();
		for (Block block : subBlocks) {
			if (block.getFragments() != null) {
				frags.addAll(block.getFragments());

			}
		}
		this.setFragments(frags);
	}

	public Block(Page page, ArrayList<TextBlock> arrayList) {
		this.page = page;
		setFragments(arrayList);
		float xTopLeftCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getxTopLeftCorner())
				.min(Float::compareTo).get();
		float xBottomRightCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getxBottomRightCorner())
				.max(Float::compareTo).get();
		float yTopLeftCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getyTopLeftCorner())
				.min(Float::compareTo).get();
		float yBottomRightCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getyBottomRightCorner())
				.max(Float::compareTo).get();
		this.setBoundingBox(new Box(xTopLeftCorner, xBottomRightCorner, yTopLeftCorner, yBottomRightCorner));
	}

	public Block(Page page2, List<TextBlock> fragments2) {
		this.page = page2;
		setFragments(fragments2);
		float xTopLeftCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getxTopLeftCorner())
				.min(Float::compareTo).get();
		float xBottomRightCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getxBottomRightCorner())
				.max(Float::compareTo).get();
		float yTopLeftCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getyTopLeftCorner())
				.min(Float::compareTo).get();
		float yBottomRightCorner = fragments.stream().map(f -> f.getPosition().getBoxCoord().getyBottomRightCorner())
				.max(Float::compareTo).get();
		this.setBoundingBox(new Box(xTopLeftCorner, xBottomRightCorner, yTopLeftCorner, yBottomRightCorner));
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
		String text = "";
		if (fragments != null) {
			for (TextBlock f : fragments) {
				text = text + f.getCharacters();
			}
		}

		return text;
	}

	@Override
	public String toString() {
		return getText();
	}

	public Page getPage() {

		return this.page;
	}

	public int compareTo(Object o) {
		Block other = (Block) o;
		return this.getText().hashCode() - other.getText().hashCode();
	}

	public int compare(Block o1, Block o2) {

		return o1.getText().hashCode() - o2.getText().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Block other = (Block) obj;
		if (getText() == null) {
			if (other.getText() != null)
				return false;
		} else if (!getText().equals(other.getText()))
			return false;
		return true;
	}

	public static Boolean isAtSameHorizontalLevel(Block b1, Block b2) {
		return b1.getBoundingBox().getyBottomRightCorner() == b2.getBoundingBox().getyBottomRightCorner();
	}

}
