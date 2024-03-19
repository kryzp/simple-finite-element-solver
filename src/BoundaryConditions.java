import java.util.ArrayList;

public class BoundaryConditions {
	public record BoundaryCondition(int id, double value, int variable) { }
	private ArrayList<BoundaryCondition> conditions;

	public BoundaryConditions() {
		conditions = new ArrayList<>();
	}

	public ArrayList<BoundaryCondition> getConditions() {
		return conditions;
	}

	public void setX(int id, double value) {
		conditions.add(new BoundaryCondition(id, value, Node.VAR_X));
	}

	public void setY(int id, double value) {
		conditions.add(new BoundaryCondition(id, value, Node.VAR_Y));
	}

	public void setTheta(int id, double value) {
		conditions.add(new BoundaryCondition(id, value, Node.VAR_THETA));
	}

	public void setFx(int id, double value) {
		conditions.add(new BoundaryCondition(id, value, Node.VAR_X));
	}

	public void setFy(int id, double value) {
		conditions.add(new BoundaryCondition(id, value, Node.VAR_Y));
	}

	public void setMoment(int id, double value) {
		conditions.add(new BoundaryCondition(id, value, Node.VAR_THETA));
	}

	public void fix(int id) {
		setX(id, 0.0);
		setY(id, 0.0);
		setTheta(id, 0.0);
	}
}
