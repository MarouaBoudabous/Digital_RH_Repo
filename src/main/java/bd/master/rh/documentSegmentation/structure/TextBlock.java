package bd.master.rh.documentSegmentation.structure;

public class TextBlock {
	
	private String characters;
	private Position position;
	private int fontId;
	
	private float fontSize;
	private int sequence;
	private float scale;
	
	private float widthOfSpace;
	private String text;
	private Boolean WordBreakHint=Boolean.FALSE;
	
	public float getWidthOfSpace() {
		return widthOfSpace;
	}

	public void setWidthOfSpace(float widthOfSpace) {
		this.widthOfSpace = widthOfSpace;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public TextBlock() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TextBlock(String characters, Position position, int fontId) {
		super();
		this.characters = characters;
		this.position = position;
		this.fontId = fontId;
		this.setText(characters);
	}

	public String getCharacters() {
		return characters;
	}

	public void setCharacters(String characters) {
		this.characters = characters;
	}

	public Position getPosition() {
		return position;
	}

	public void setPosition(Position position) {
		this.position = position;
	}

	public int getFontId() {
		return fontId;
	}

	public void setFontId(int fontId) {
		this.fontId = fontId;
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public Boolean getWordBreakHint() {
		return WordBreakHint;
	}

	public void setWordBreakHint(Boolean wordBreakHint) {
		WordBreakHint = wordBreakHint;
	}
	
	public float getX() {
		return this.getPosition().getBoxCoord().getxTopLeftCorner();
	}
	
	public float getWidth() {
		return this.getPosition().getBoxCoord().getxBottomRightCorner()- this.getPosition().getBoxCoord().getxTopLeftCorner();
	}
	
	public float getY() {
		return this.getPosition().getBoxCoord().getyTopLeftCorner();
	}
	
	public float getHeight() {
		return  this.getPosition().getBoxCoord().getyBottomRightCorner()-this.getPosition().getBoxCoord().getyTopLeftCorner();
	}
	
	public Boolean isWordBreakHint() {
		return this.WordBreakHint;
	}
}
