package bd.master.rh.documentSegmentation.structure;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDPage;
import org.w3c.dom.Text;

public class Page extends PDPage{
	
	private int number;
	private int pageRotation;
	private float pageWidth ;
	private float pageHeight;
	private  float offsetUpperRightX; 
	private float offsetUpperRightY ;
	private List<Line> lines;
	private List<TextBlock> fragments;
	
	
	
	public List<TextBlock> getFragments() {
		return (fragments== null)? new ArrayList<TextBlock>():fragments;
	}
	public void setFragments(List<TextBlock> fragments) {
		this.fragments = fragments;
	}
	public List<Line> getLines() {
		return lines;
	}
	public void setLines(List<Line> lines) {
		this.lines = lines;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getPageRotation() {
		return pageRotation;
	}
	public void setPageRotation(int pageRotation) {
		this.pageRotation = pageRotation;
	}
	public float getPageWidth() {
		return pageWidth;
	}
	public void setPageWidth(float pageWidth) {
		this.pageWidth = pageWidth;
	}
	public float getPageHeight() {
		return pageHeight;
	}
	public void setPageHeight(float pageHeight) {
		this.pageHeight = pageHeight;
	}
	public float getOffsetUpperRightX() {
		return offsetUpperRightX;
	}
	public void setOffsetUpperRightX(float offsetUpperRightX) {
		this.offsetUpperRightX = offsetUpperRightX;
	}
	public float getOffsetUpperRightY() {
		return offsetUpperRightY;
	}
	public void setOffsetUpperRightY(float offsetUpperRightY) {
		this.offsetUpperRightY = offsetUpperRightY;
	}
	
	public float getMeanFontSize() {
		return 0;
	}
	
	

}
