package bd.master.rh.documentSegmentation;

import java.util.List;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Document;


public interface BlockProcessor {
		public  static final long serialVersionUID = -100671252958700723L;
		
		List<Block> extractBlocks(Document pdfDocument, String id);
}
