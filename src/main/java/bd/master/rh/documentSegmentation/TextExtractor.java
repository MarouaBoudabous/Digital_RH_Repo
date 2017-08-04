package bd.master.rh.documentSegmentation;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.common.PDMatrix;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.pdfbox.util.ResourceLoader;
import org.apache.pdfbox.util.TextNormalize;
import org.apache.pdfbox.util.TextPosition;

import bd.master.rh.documentSegmentation.structure.Box;
import bd.master.rh.documentSegmentation.structure.Font ;
import bd.master.rh.documentSegmentation.structure.Line;
import bd.master.rh.documentSegmentation.structure.Page;
import bd.master.rh.documentSegmentation.structure.Position;
import bd.master.rh.documentSegmentation.structure.TextBlock;

public class TextExtractor extends PDFTextStripper {

	private static final byte[] CHARACTER_FOR_HEIGHT = new byte[] { 'H' };

	private Page pdfPage;
	
	private Map<Object, Integer> fonts= new HashMap<Object, Integer>();
	
	private Map<Integer, Font>         idToFont= new HashMap<Integer, Font>();
	
	private TextPosition previousTextPosition;
    private TextBlock previousFragment;
    
    private TextNormalize normalize;
    private int fragmentCounter;

	private Boolean wordBreakHint;
	

    private Point2D startPosition;
    private Point2D currentPosition;
    private List<Line> lines = new ArrayList<Line>();

	public TextExtractor() throws IOException {
		super();
	}
	
	public Page getPdfPage() {
		return pdfPage;
	}

	public void setPdfPage(Page pdfPage) {
		this.pdfPage = pdfPage;
	}


	public Map<Integer, Font> getIdToFont() {
		return idToFont;
	}

	public void setIdToFont(Map<Integer, Font> idToFont) {
		this.idToFont = idToFont;
	}

	public TextPosition getPreviousTextPosition() {
		return previousTextPosition;
	}

	public void setPreviousTextPosition(TextPosition previousTextPosition) {
		this.previousTextPosition = previousTextPosition;
	}

	public TextBlock getPreviousFragment() {
		return previousFragment;
	}

	public void setPreviousFragment(TextBlock previousFragment) {
		this.previousFragment = previousFragment;
	}

	public TextNormalize getNormalize() {
		return normalize;
	}

	public void setNormalize(TextNormalize normalize) {
		this.normalize = normalize;
	}

	public int getFragmentCounter() {
		return fragmentCounter;
	}

	public void setFragmentCounter(int fragmentCounter) {
		this.fragmentCounter = fragmentCounter;
	}

	public Boolean getWordBreakHint() {
		return wordBreakHint;
	}

	public void setWordBreakHint(Boolean wordBreakHint) {
		this.wordBreakHint = wordBreakHint;
	}

	public Point2D getStartPosition() {
		return startPosition;
	}

	public void setStartPosition(Point2D startPosition) {
		this.startPosition = startPosition;
	}

	public Point2D getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(Point2D currentPosition) {
		this.currentPosition = currentPosition;
	}

	public static byte[] getCharacterForHeight() {
		return CHARACTER_FOR_HEIGHT;
	}

	public void setLines(List<Line> lines) {
		this.lines = lines;
	}

	public TextExtractor(Page pdfPage, Map<Integer, Font> idToFont, Map<Object, Integer> fonts)throws IOException {
		// TODO Auto-generated constructor stub
		super(ResourceLoader.loadProperties("pdfbox.properties", true ));
		
		
		
		
		this.pdfPage = pdfPage;
        this.idToFont = idToFont;
        this.fonts = fonts;
        this.wordBreakHint = true;
        this.normalize = new TextNormalize(/*this.outputEncoding*/"UTF-8");
	}

	@Override
	    public void processEncodedText(byte[] string) throws IOException {
	        super.processEncodedText(string);
	    }
	 
	@Override
	protected void processTextPosition(TextPosition tp) {
		List<TextBlock> fragments = pdfPage.getFragments();

		if (true) {
			String chars = tp.getCharacter();
			if (chars.trim().isEmpty()) {
				return;
			} 
			float x = tp.getX();
			float y = tp.getY();
			x += pdfPage.getOffsetUpperRightX();
			y -= pdfPage.getOffsetUpperRightY();
			float width = tp.getIndividualWidths()[0];
			float height;

			PDFont font = tp.getFont();
			PDMatrix fontMatrix = font.getFontMatrix();
			try {
				float fontSize = tp.getFontSize();
				height = fontSize * font.getFontHeight(CHARACTER_FOR_HEIGHT, 0, 1) / 1000.0f;
			} catch (IOException e) {
				throw new RuntimeException("Caused by IOException", e);
			}
			height = Math.max(height, tp.getHeightDir());
			width *= fontMatrix.getValue(0, 0) * 1000f;
			height *= fontMatrix.getValue(1, 1) * 1000f;
			height += 3;
			if (height < -1e-3) {
				y -= height;
				height = -height;
			} else if (height <= 1e-3) {
				height = 12;
			}
			if (width < -1e-3) {
				x -= width;
				width -= width;
			} else if (width <= 1e-3) {
				width = height * 0.5f;
			}
			y += 1;

			if (tp.getDir() == 90) {
				float f = width;
				width = height;
				height = f;
				x -= width;
			} else if (tp.getDir() == 180) {
				y -= height;
			} else if (tp.getDir() == 270) {
				float f = width;
				width = height;
				height = f;
				y += width;
			}
			String fontSpec = font.getBaseFont();
			Object fontRef;

			fontRef = font;

			Integer id = fonts.get(fontRef);
			int currentFontId = id != null ? id.intValue() : -1;
			if (currentFontId < 0) {
				currentFontId = fonts.size();
				float averageFontWidth = 0; // font.getAverageFontWidth();
				Font pdfFont;
				PDFontDescriptor fd = font.getFontDescriptor();
				try {
					if (fd != null) {
						String fontName = fontSpec != null ? fontSpec : fd.getFontName();
						boolean isBold = fd.isForceBold();
						boolean isItalic = fd.isItalic();
						pdfFont = new Font();
						pdfFont.setId(currentFontId);
						pdfFont.setFontName(fontName);
						pdfFont.setAverageFontWidth(fd.getAverageWidth());
						pdfFont.setxHeight(fd.getXHeight());
						pdfFont.setIsBold(isBold);
						pdfFont.setIsItalic(isItalic);
					} else {
						pdfFont = new Font();
						pdfFont.setId(currentFontId);
						pdfFont.setFontName(fontSpec);
						pdfFont.setAverageFontWidth(averageFontWidth);
					}
				} catch (IOException e) {
					throw new RuntimeException("Caused by IOException", e);
				}

				fonts.put(font, currentFontId);
				idToFont.put(currentFontId, pdfFont);
			}

			Position position = new Position();
			Box boundingBox = new Box(x, x + width, y - height, y);
			position.setBoxCoord(boundingBox);
			position.setPageNumber(pdfPage.getNumber());
			TextBlock fragment = new TextBlock(chars, position, currentFontId);
			fragment.setFontSize(height);
			//fragment.setFontSizePt(height);
			fragment.setSequence(fragments.size());
			fragment.setScale(1);
			fragments.add(fragment);
		}
		pdfPage.setFragments(fragments);
	}
	
	
	
	 public void addMoveTo(Point2D pos) {
	        startPosition = pos;
	        currentPosition = null;
	    }

	    /**
	     * @param pos
	     */
	    public void addLineTo(Point2D pos) {
	        if (currentPosition != null || startPosition != null) {
	        	Line line = new Line(currentPosition != null ? currentPosition : startPosition, pos);
	            lines.add(line);
	            currentPosition = pos;
	        } else {
	            System.err.println("Got a line end without a start");
	        }
	    }
	    
	    public void finishLine() {
	        if (startPosition != null && currentPosition != null) {
	        	Line line = new Line(currentPosition, startPosition);
	            lines.add(line);
	        }
	        startPosition = currentPosition = null;
	    }
	    
	    /**
	     * Returns the lines.
	     * @return the lines
	     */
	    public List<Line> getLines() {
	        return lines;
	    }

}
