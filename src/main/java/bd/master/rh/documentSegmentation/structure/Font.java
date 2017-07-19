package bd.master.rh.documentSegmentation.structure;



public class Font {
	
	private int id;
	private String fontName;
	private float averageFontWidth;
	private float xHeight;
	private Boolean isBold;
	private Boolean isItalic;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public float getAverageFontWidth() {
		return averageFontWidth;
	}
	public void setAverageFontWidth(float averageFontWidth) {
		this.averageFontWidth = averageFontWidth;
	}
	public float getxHeight() {
		return xHeight;
	}
	public void setxHeight(float xHeight) {
		this.xHeight = xHeight;
	}
	public Boolean getIsBold() {
		return isBold;
	}
	public void setIsBold(Boolean isBold) {
		this.isBold = isBold;
	}
	public Boolean getIsItalic() {
		return isItalic;
	}
	public void setIsItalic(Boolean isItalic) {
		this.isItalic = isItalic;
	}
	
	
	
}
