package bd.master.rh.documentSegmentation.structure;

public class Box {

	private float xTopLeftCorner;
	private float yTopLeftCorner;
	private float xBottomRightCorner;
	private float yBottomRightCorner;
	private float minx;
	private float maxx;
	
	public Box(float xTopLeftCorner, float xBottomRightCorner,float yTopLeftCorner,  float yBottomRightCorner) {
		super();
		this.xTopLeftCorner = xTopLeftCorner;
		this.yTopLeftCorner = yTopLeftCorner;
		this.xBottomRightCorner = xBottomRightCorner;
		this.yBottomRightCorner = yBottomRightCorner;
	}
	
	public float getxTopLeftCorner() {
		return xTopLeftCorner;
	}
	public void setxTopLeftCorner(float xTopLeftCorner) {
		this.xTopLeftCorner = xTopLeftCorner;
	}
	public float getyTopLeftCorner() {
		return yTopLeftCorner;
	}
	public void setyTopLeftCorner(float yTopLeftCorner) {
		this.yTopLeftCorner = yTopLeftCorner;
	}
	public float getxBottomRightCorner() {
		return xBottomRightCorner;
	}
	public void setxBottomRightCorner(float xBottomRightCorner) {
		this.xBottomRightCorner = xBottomRightCorner;
	}
	
	public float getyBottomRightCorner() {
		return yBottomRightCorner;
	}
	public void setyBottomRightCorner(float yBottomRightCorner) {
		this.yBottomRightCorner = yBottomRightCorner;
	}

	public float getMinx() {
		return this.xTopLeftCorner;
	}

	public void setMinx(float minx) {
		this.minx = minx;
	}

	public float getMaxx() {
		return this.xBottomRightCorner;
	}

	public void setMaxx(float maxx) {
		this.maxx = maxx;
	}
	
	public float getX() {
		return this.xTopLeftCorner;
	}
	
	public float getY() {
		return this.yBottomRightCorner;
	}
	 public float getHeight() {
		 return this.yTopLeftCorner-this.yBottomRightCorner;
	 }
}
