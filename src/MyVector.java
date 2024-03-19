public class MyVector {
	private final int size;
	private double[] data;

	public MyVector(int size) {
		this.size = size;
		this.data = new double[size];
	}

	public int getSize() {
		return size;
	}

	public double get(int i) {
		return data[i];
	}

	public void set(int i, double value) {
		data[i] = value;
	}
}
