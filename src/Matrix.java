import org.ejml.simple.SimpleMatrix;

public class Matrix {
	private final int size;
	private double[] data;

	public Matrix(int size) {
		this.size = size;
		this.data = new double[size * size];
	}

	public Matrix(int size, double[] data) {
		this.size = size;
		this.data = data;
	}

	MyVector solve(MyVector x) {
		SimpleMatrix matrix = new SimpleMatrix(size, size);
		for (int col = 0; col < size; col++) {
			for (int row = 0; row < size; row++) {
				matrix.set(row, col, get(col, row));
			}
		}

		SimpleMatrix f = new SimpleMatrix(size, 1);
		for (int i = 0; i < size; i++) {
			f.set(i, 0, x.get(i));
		}

		SimpleMatrix u = matrix.solve(f);

		MyVector result = new MyVector(size);
		for (int i = 0; i < size; i++) {
			result.set(i, u.get(i, 0));
		}
		return result;
	}

	public Matrix mul(Matrix rhs) {
		Matrix result = new Matrix(size);

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < size; k++) {
					double value = this.get(k, i) * rhs.get(j, k);
					result.add(j, i, value);
				}
			}
		}

		return result;
	}

	public Matrix transpose() {
		Matrix result = new Matrix(size);

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				result.set(i, j, get(j, i));
			}
		}

		return result;
	}

	double get(int x, int y) {
		return data[y*size + x];
	}

	void set(int x, int y, double value) {
		data[y*size + x] = value;
	}

	void add(int x, int y, double value) {
		data[y*size + x] += value;
	}

	void clearRow(int row) {
		for (int i = 0; i < size; i++) {
			data[row*size + i] = 0.0;
		}
	}

	int getSize() {
		return size;
	}
}
