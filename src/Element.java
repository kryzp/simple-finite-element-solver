public abstract class Element {
	protected final FESolver solver;

	public Element(FESolver solver) {
		this.solver = solver;
	}

	public abstract Matrix getLocalMatrix();
	public abstract Matrix getTransformationMatrix();

	public abstract int getGlobalIndex(int index);
}
