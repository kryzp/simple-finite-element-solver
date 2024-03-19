public class SpringElement extends Element {
	private final int n1;
	private final int n2;

	private final double E;
	private final double G;
	private final double T;

	public SpringElement(FESolver solver, int n1, int n2, double E, double G, double T) {
		super(solver);

		this.n1 = n1;
		this.n2 = n2;

		this.E = E;
		this.G = G;
		this.T = T;
	}

	@Override
	public Matrix getLocalMatrix() {
		double L = getLength();
		double Kx = E * L * T;
		double Ky = G * L * T;
		return new Matrix(2 * Node.DEGREES_OF_FREEDOM, new double[] {
			Kx, 0.0, 0.0, -Kx, 0.0, 0.0,
			0.0, Ky, 0.0, 0.0, -Ky, 0.0,
			0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
			-Kx, 0.0, 0.0, Kx, 0.0, 0.0,
			0.0, -Ky, 0.0, 0.0, Ky, 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, 1.0
		});
	}

	@Override
	public Matrix getTransformationMatrix() {
		double phi = getAngle();
		return new Matrix(2 * Node.DEGREES_OF_FREEDOM, new double[] {
			Math.cos(phi), -Math.sin(phi), 0.0, 0.0, 0.0, 0.0,
			Math.sin(phi), Math.cos(phi), 0.0, 0.0, 0.0, 0.0,
			0.0, 0.0, 1.0, 0.0, 0.0, 0.0,
			0.0, 0.0, 0.0, Math.cos(phi), -Math.sin(phi), 0.0,
			0.0, 0.0, 0.0, Math.sin(phi), Math.cos(phi), 0.0,
			0.0, 0.0, 0.0, 0.0, 0.0, 1.0
		});
	}

	@Override
	public int getGlobalIndex(int index) {
		boolean isN1 = index < Node.DEGREES_OF_FREEDOM;
		int variable = isN1 ? index : index - 3;
		int node = isN1 ? n1 : n2;
		return node*Node.DEGREES_OF_FREEDOM + variable;
	}

	public double getLength() {
		Node node1 = solver.getNode(n1);
		Node node2 = solver.getNode(n2);

		double dx = node2.getX() - node1.getX();
		double dy = node2.getY() - node1.getY();

		return Math.sqrt(dx*dx + dy*dy);
	}

	public double getAngle() {
		Node node1 = solver.getNode(n1);
		Node node2 = solver.getNode(n2);

		double dx = node2.getX() - node1.getX();
		double dy = node2.getY() - node1.getY();

		return Math.atan2(dy, dx);
	}

	public int getNode1() {
		return this.n1;
	}

	public int getNode2() {
		return this.n2;
	}
}
