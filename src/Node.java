public class Node {
	public static final int VAR_X = 0;
	public static final int VAR_Y = 1;
	public static final int VAR_THETA = 2;

	public static final int DEGREES_OF_FREEDOM = 3;

	private final int id;

	private double x;
	private double y;
	private double t;

	public Node(int id) {
		this.id = id;

		this.x = 0.0;
		this.y = 0.0;
		this.t = 0.0;
	}

	public Node(int id, double x, double y, double t) {
		this.id = id;

		this.x = x;
		this.y = y;
		this.t = t;
	}

	public void printDisplacements() {
		System.out.println(id + ") u=" + x + "m, v=" + y + "m, theta=" + Math.toDegrees(t) + "°");
	}

	public int getID() {
		return id;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getTheta() {
		return t;
	}

	public void setX(double value) {
		x = value;
	}

	public void setY(double value) {
		y = value;
	}

	public void setTheta(double value) {
		t = value;
	}
}
