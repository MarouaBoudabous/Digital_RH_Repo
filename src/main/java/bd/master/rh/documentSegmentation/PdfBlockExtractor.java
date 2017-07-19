package bd.master.rh.documentSegmentation;

import java.util.List;

import bd.master.rh.documentSegmentation.structure.Block;
import bd.master.rh.documentSegmentation.structure.Document;

public class PdfBlockExtractor {
	
	private List<String> doi;

	@SuppressWarnings("unused")
	public List<Block> extractBlocks(Document document, String id) {
		ClusteringPdfBlockExtractor clusteringPdfBlockExtractor = new ClusteringPdfBlockExtractor();
		List<Block> blocks = clusteringPdfBlockExtractor.extractBlocks(document, id);
		this.doi = clusteringPdfBlockExtractor.doi;
		return blocks;
	}

}
