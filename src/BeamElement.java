public class BeamElement extends Element {
	private final int n1;
	private final int n2;

	private final double E;
	private final double A;
	private final double I;

	public BeamElement(FESolver solver, int n1, int n2, double E, double A, double I) {
		super(solver);

		this.n1 = n1;
		this.n2 = n2;

		this.E = E;
		this.A = A;
		this.I = I;
	}

	@Override
	public Matrix getLocalMatrix() {
		double L = getLength();
		return new Matrix(2 * Node.DEGREES_OF_FREEDOM, new double[] {
			A * E / L, 0.0, 0.0, -A * E / L, 0.0, 0.0,
			0.0, 12.0 * E * I / (L * L * L), 6.0 * E * I / (L * L), 0.0, -12.0 * E * I / (L * L * L), 6.0 * E * I / (L * L),
			0.0, 6.0 * E * I / (L * L), 4.0 * E * I / L, 0.0, -6.0 * E * I / (L * L), 2.0 * E * I / L,
			-A * E / L, 0.0, 0.0, A * E / L, 0.0, 0.0,
			0.0, -12.0 * E * I / (L * L * L), -6.0 * E * I / (L * L), 0.0, 12.0 * E * I / (L * L * L), -6.0 * E * I / (L * L),
			0.0, 6.0 * E * I / (L * L), 2.0 * E * I / L, 0.0, -6.0 * E * I / (L * L), 4.0 * E * I / L
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

	public double calcStress(double newLength) {
		return getYoungsModulus() * Math.abs(newLength/getLength() - 1.0);
	}

	public int getNode1() {
		return n1;
	}

	public int getNode2() {
		return n2;
	}

	public double getYoungsModulus() {
		return E;
	}

	public double getCrossSectionalArea() {
		return A;
	}

	public double getAreaMomentOfInertia() {
		return I;
	}
}
