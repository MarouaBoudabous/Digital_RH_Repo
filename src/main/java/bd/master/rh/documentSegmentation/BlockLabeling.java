package bd.master.rh.documentSegmentation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.BlockLabel;



public class BlockLabeling implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Map<Block, BlockLabel> labels;
	
	/**
	 * creates a new block labeling instance
	 */
	public BlockLabeling() {
		this.labels = new HashMap<Block, BlockLabel>();
	}
	
	/**
	 * sets the label for the given block. This overrides any label previously set.
	 * It is also allowed to set the label to null.
	 * @param block the block for which the label should be set
	 * @param label the label of the block
	 */
	public void setLabel(Block block, BlockLabel label) {
		labels.put(block, label);
	}
	
	/**
	 * gets the label of the given block.
	 * @param block the block for which the label should be returned
	 * @return the label of the block, or null, if no label has been set
	 */
	public BlockLabel getLabel(Block block) {
		return this.getLabel(block, null);
	}
	
	/**
	 * gets the label of the given block, or returns default
	 * @param block the block for which the label should be returned
	 * @param default the BlockLabel that is returned if block has not label ie. is null
	 * @return the label of the block, or default, if no label has been set
	 */
	public BlockLabel getLabel(Block block, BlockLabel defaultbl) {
		BlockLabel bl = labels.get(block);
		if (bl == null) bl = defaultbl;
		return bl;
	}
	
	/**
	 * checks if the given block has one of the given labels.
	 * If the label is null, this method returns false.
	 * @param block the block for which the label should be checked
	 * @param possibleLabels the array of labels for which the method returns true
	 * @return true, if the block has a label which is not null and one of the given labels
	 * @see {@link #hasLabelOrNull(Block, BlockLabel...)}
	 */
	public boolean hasLabel(Block block, BlockLabel ... possibleLabels) {
		BlockLabel actualLabel = labels.get(block);
		if (actualLabel==null) {
			return false;
		}
		for (BlockLabel label : possibleLabels) {
			if (label==actualLabel) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * checks if the given block has one of the given labels, or the label is null
	 * @param block the block for which the label should be checked
	 * @param possibleLabels the array of labels for which the method returns true
	 * @return true, if the block has a label which is null or one of the given labels
	 * @see {@link #hasLabel(Block, BlockLabel...)}
	 */
	public boolean hasLabelOrNull(Block block, BlockLabel ... possibleLabels) {
		BlockLabel actualLabel = labels.get(block);
		if (actualLabel==null) {
			return true;
		}
		for (BlockLabel label : possibleLabels) {
			if (label==actualLabel) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * check if block has no label (i.e., is null)
	 * @param block the block to check
	 * @return true, if the label is null
	 */
	public boolean hasNoLabel(Block block) {
		return labels.get(block)==null;
	}
}
