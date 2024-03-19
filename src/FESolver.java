import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class FESolver {

	private ArrayList<Node> nodes;
	private ArrayList<Element> elements;

	private BoundaryConditions boundaryConditions;
	private BoundaryConditions forces;

	private Matrix stiffnessMatrix;

	private String currentlyLoadedFile;

	public FESolver() {
		this.stiffnessMatrix = null;
	}

	public void buildMatrix() {
		this.stiffnessMatrix = new Matrix(nodes.size() * Node.DEGREES_OF_FREEDOM);

		// set up the stiffness matrix
		System.out.println("Assembling stiffness matrix...");

		for (var elem : elements) {
			Matrix localMatrix = elem.getLocalMatrix();
			Matrix localTransform = elem.getTransformationMatrix();
			Matrix globalMatrix = localTransform.mul(localMatrix).mul(localTransform.transpose());
			for (int i = 0; i < globalMatrix.getSize(); i++) {
				for (int j = 0; j < globalMatrix.getSize(); j++) {
					int row = elem.getGlobalIndex(j);
					int col = elem.getGlobalIndex(i);
					stiffnessMatrix.add(col, row, globalMatrix.get(j, i));
				}
			}
		}

		// apply the boundary conditions
		System.out.println("Applying boundary conditions...");
		for (int i = 0; i < stiffnessMatrix.getSize(); i++) {
			for (var bc : boundaryConditions.getConditions()) {
				int offset = bc.variable();
				int node = bc.id();
				int idx = (node * Node.DEGREES_OF_FREEDOM) + offset;
				if (i == idx) {
					stiffnessMatrix.clearRow(i);
					stiffnessMatrix.set(i, i, 1.0);
					break;
				}
			}
		}
	}

	public ArrayList<Node> solveDisplacements() {

		// build the force vector
		System.out.println("Building force vector...");
		MyVector forceVector = new MyVector(stiffnessMatrix.getSize());
		for (var force : forces.getConditions()) {
			int node = force.id();
			double magnitude = force.value();
			int variable = force.variable();
			int idx = node*Node.DEGREES_OF_FREEDOM + variable;
			forceVector.set(idx, magnitude);
		}

		// solve
		System.out.println("Solving...");
		MyVector displacementVector = stiffnessMatrix.solve(forceVector);

		// results
		System.out.println("Accumulating results...");
		ArrayList<Node> result = new ArrayList<>();
		for (int i = 0; i < displacementVector.getSize(); i += 3) {
			double x = displacementVector.get(i + 0);
			double y = displacementVector.get(i + 1);
			double t = displacementVector.get(i + 2);
			result.add(new Node(i / Node.DEGREES_OF_FREEDOM, x, y, t));
		}

		return result;
	}

	private static final int LOAD_NODE 			= 0;
	private static final int LOAD_ELEMENT 		= 1;
	private static final int LOAD_BOUNDARY 		= 2;
	private static final int LOAD_FORCES 		= 3;
	private static final int LOAD_SUBDIVISIONS 	= 4;

	public void loadFromFile(String filename) {
		System.out.println("Loading \"" + filename + "\"...");
		this.currentlyLoadedFile = filename;

		this.nodes = new ArrayList<>();
		this.elements = new ArrayList<>();

		this.boundaryConditions = new BoundaryConditions();
		this.forces = new BoundaryConditions();

		int subdivisions = 0;

		BufferedReader reader = null;
		int state = -1;
		try {
			reader = new BufferedReader(new FileReader(filename));

			String line = reader.readLine();
			while (line != null) {

				// comments
				if (line.startsWith("//")) {
					line = reader.readLine();
					continue;
				}

				int prev = state;
				state = switch (line) {
					case "#Node" 			-> LOAD_NODE;
					case "#Element" 		-> LOAD_ELEMENT;
					case "#Boundary" 		-> LOAD_BOUNDARY;
					case "#Forces" 			-> LOAD_FORCES;
					case "#Subdivisions" 	-> LOAD_SUBDIVISIONS;
					default -> state;
				};

				if (state != prev) {
					line = reader.readLine();
					continue;
				}

				if (state == LOAD_NODE) {
					String[] split = line.split(", ");
					nodes.add(new Node(
						Integer.parseInt(split[0]),
						Double.parseDouble(split[1]),
						Double.parseDouble(split[2]),
						Double.parseDouble(split[3])
					));
				} else if (state == LOAD_ELEMENT) {
					String[] split = line.split(", ");
					elements.add(getElementFromID(
						Integer.parseInt(split[0]),
						split
					));
				} else if (state == LOAD_BOUNDARY) {
					String[] split = line.split(", ");
					int id = Integer.parseInt(split[0]);
					boundaryConditions.setX(id, Double.parseDouble(split[1]));
					boundaryConditions.setY(id, Double.parseDouble(split[2]));
					boundaryConditions.setTheta(id, Double.parseDouble(split[3]));
				} else if (state == LOAD_FORCES) {
					String[] split = line.split(", ");
					int id = Integer.parseInt(split[0]);
					forces.setFx(id, Double.parseDouble(split[1]));
					forces.setFy(id, Double.parseDouble(split[2]));
					forces.setMoment(id, Double.parseDouble(split[3]));
				} else if (state == LOAD_SUBDIVISIONS) {
					subdivisions = Integer.parseInt(line);
				}

				line = reader.readLine();
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < subdivisions; i++) {
			subdivide();
		}
	}

	private static final int ELEMENT_ID_BEAM = 0;
	private static final int ELEMENT_ID_SPRING = 1;

	private Element getElementFromID(int id, String[] data) {

		// beam element
		if (id == ELEMENT_ID_BEAM) {
			return new BeamElement(
				this,
				Integer.parseInt(data[1]),
				Integer.parseInt(data[2]),
				Double.parseDouble(data[3]),
				Double.parseDouble(data[4]),
				Double.parseDouble(data[5])
			);
		}

		// spring element
		else if (id == ELEMENT_ID_SPRING) {
			return new SpringElement(
				this,
				Integer.parseInt(data[1]),
				Integer.parseInt(data[2]),
				Double.parseDouble(data[3]),
				Double.parseDouble(data[4]),
				Double.parseDouble(data[5])
			);
		}

		return null;
	}

	public String getCurrentlyLoadedFile() {
		return this.currentlyLoadedFile;
	}

	public void subdivide() {
		ArrayList<Element> newElements = new ArrayList<>();

		int maxID = -1;
		for (Node node : nodes) {
			maxID = Math.max(maxID, node.getID() + 1);
		}

		for (var elem : elements) {
			if (elem instanceof BeamElement beam) {
				Node n1 = nodes.get(beam.getNode1());
				Node n2 = nodes.get(beam.getNode2());
				Node n3 = new Node(maxID, (n1.getX() + n2.getX()) * 0.5, (n1.getY() + n2.getY()) * 0.5, 0.0);
				nodes.add(n3);
				newElements.add(new BeamElement(this, n1.getID(), n3.getID(), beam.getYoungsModulus(), beam.getCrossSectionalArea(), beam.getAreaMomentOfInertia()));
				newElements.add(new BeamElement(this, n3.getID(), n2.getID(), beam.getYoungsModulus(), beam.getCrossSectionalArea(), beam.getAreaMomentOfInertia()));
				maxID += 1;
			} else if (elem instanceof SpringElement spring) {
				newElements.add(elem);
			}

			/* springs dont get subdivided */
		}

		elements = newElements;
	}

	public Node getNode(int id) {
		for (Node node : nodes) {
			if (node.getID() == id) {
				return node;
			}
		}
		return null;
	}

	public ArrayList<Node> getNodes() {
		return this.nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	public ArrayList<Element> getElements() {
		return this.elements;
	}

	public void setElements(ArrayList<Element> elements) {
		this.elements = elements;
	}

	public void setBoundaryConditions(BoundaryConditions boundaryConditions) {
		this.boundaryConditions = boundaryConditions;
	}

	public void setForces(BoundaryConditions forces) {
		this.forces = forces;
	}
}
