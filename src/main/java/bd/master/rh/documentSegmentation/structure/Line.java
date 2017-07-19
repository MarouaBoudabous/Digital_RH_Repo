package bd.master.rh.documentSegmentation.structure;

import java.awt.geom.Point2D;

public class Line {
	
	private Point2D start;
	
	private Point2D end;

	public Line(Point2D start, Point2D end) {
		super();
		this.start = start;
		this.end = end;
	}

	public Point2D getStart() {
		return start;
	}

	public void setStart(Point2D start) {
		this.start = start;
	}

	public Point2D getEnd() {
		return end;
	}

	public void setEnd(Point2D end) {
		this.end = end;
	}
	
	


}
