import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class Main {
	private static double stressGradientBasis(double x) {
		return Math.exp(-x*x*4.5);
	}

	public static Color stressGradient(double stress, double minStress, double maxStress) {
		double normalizedValue = (stress - minStress) / (maxStress - minStress);

		int red = (int)(255.0 * stressGradientBasis(normalizedValue - 1.0));
		int grn = (int)(255.0 * stressGradientBasis(normalizedValue - 0.5));
		int blu = (int)(255.0 * stressGradientBasis(normalizedValue - 0.0));

		return new Color(red, grn, blu);
	}

	private static final int Y_BORDER_EXTRA = 30; // macos??

	public static void main(String[] args) {

		System.out.println("Current directory is " + new File(".").getAbsolutePath());

		FESolver solver = new FESolver();
		solver.loadFromFile("scenario2.fem");
		solver.buildMatrix();

		ArrayList<Node> displacements = solver.solveDisplacements();
		for (Node displacement : displacements) {
			displacement.printDisplacements();
		}

		JFrame frame = new JFrame("FEA | " + solver.getCurrentlyLoadedFile());
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setFocusable(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setSize(512, 512 + Y_BORDER_EXTRA);
		frame.setBackground(new Color(0, 0, 0));

		ArrayList<Element> elements = solver.getElements();

		var content = new JComponent() {
			@Override
			public void paintComponent(Graphics g) {

				// draw before
				g.setColor(new Color(50, 50, 50));

				for (var elem : elements) {
					int i1 = -1;
					int i2 = -1;

					if (elem instanceof BeamElement beam) {
						i1 = beam.getNode1();
						i2 = beam.getNode2();
					} else if (elem instanceof SpringElement spring) {
						i1 = spring.getNode1();
						i2 = spring.getNode2();
					}

					Node n1 = solver.getNode(i1);
					Node n2 = solver.getNode(i2);

					g.drawLine(
						100+(int)(n1.getX()*100), 100-(int)(n1.getY()*100),
						100+(int)(n2.getX()*100), 100-(int)(n2.getY()*100)
					);
				}

				// draw after
				g.setColor(Color.BLUE);

				double minStress = Double.MAX_VALUE;
				double maxStress = Double.MIN_VALUE;

				for (var elem : elements) {
					if (!(elem instanceof BeamElement)) {
						continue;
					}

					BeamElement beam = (BeamElement)elem;

					int i1 = beam.getNode1();
					int i2 = beam.getNode2();

					Node n1 = solver.getNode(i1);
					Node n2 = solver.getNode(i2);
					Node d1 = displacements.get(i1);
					Node d2 = displacements.get(i2);

					double x1 = n1.getX() + d1.getX();
					double y1 = n1.getY() + d1.getY();
					double x2 = n2.getX() + d2.getX();
					double y2 = n2.getY() + d2.getY();

					double dx = x2 - x1;
					double dy = y2 - y1;
					double newLength = Math.sqrt(dx*dx + dy*dy);
					double stress = beam.calcStress(newLength);

					minStress = Math.min(minStress, stress);
					maxStress = Math.max(maxStress, stress);
				}

				for (var elem : elements) {
					if (!(elem instanceof BeamElement)) {
						continue;
					}

					BeamElement beam = (BeamElement)elem;

					int i1 = beam.getNode1();
					int i2 = beam.getNode2();

					Node n1 = solver.getNode(i1);
					Node n2 = solver.getNode(i2);
					Node d1 = displacements.get(i1);
					Node d2 = displacements.get(i2);

					double x1 = n1.getX() + d1.getX();
					double y1 = n1.getY() + d1.getY();
					double x2 = n2.getX() + d2.getX();
					double y2 = n2.getY() + d2.getY();

					double dx = x2 - x1;
					double dy = y2 - y1;
					double newLength = Math.sqrt(dx*dx + dy*dy);
					double stress = beam.calcStress(newLength);

					g.setColor(stressGradient(stress, minStress, maxStress));

					g.drawLine(
						100+(int)(x1*100.0), 100-(int)(y1*100.0),
						100+(int)(x2*100.0), 100-(int)(y2*100.0)
					);
				}

				for (var elem : elements) {
					if (!(elem instanceof SpringElement)) {
						continue;
					}

					SpringElement spring = (SpringElement)elem;

					int i1 = spring.getNode1();
					int i2 = spring.getNode2();

					Node n1 = solver.getNode(i1);
					Node n2 = solver.getNode(i2);
					Node d1 = displacements.get(i1);
					Node d2 = displacements.get(i2);

					double x1 = n1.getX() + d1.getX();
					double y1 = n1.getY() + d1.getY();
					double x2 = n2.getX() + d2.getX();
					double y2 = n2.getY() + d2.getY();

					g.setColor(Color.WHITE);

					g.drawLine(
						100+(int)(x1*100.0), 100-(int)(y1*100.0),
						100+(int)(x2*100.0), 100-(int)(y2*100.0)
					);
				}

				for (int i = 0; i < 512-30; i++) {
					int xx = i + 10;
					int yy = 10;
					g.setColor(stressGradient(i, 0, 512-30));
					g.fillRect(xx, yy, 10, 10);
				}
			}
		};

		frame.setContentPane(content);
	}
}
