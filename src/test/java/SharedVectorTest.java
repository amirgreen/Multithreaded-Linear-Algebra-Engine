import memory.SharedVector;
import memory.SharedMatrix;
import memory.VectorOrientation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedVectorTest {

    @Test
    void testAddVectors() {
        double[] v1Data = {1.0, 2.0, 3.0};
        double[] v2Data = {4.0, 5.0, 6.0};
        SharedVector v1 = new SharedVector(v1Data, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(v2Data, VectorOrientation.ROW_MAJOR);

        v1.add(v2);

        // Verify result in v1
        assertEquals(5.0, v1.get(0));
        assertEquals(7.0, v1.get(1));
        assertEquals(9.0, v1.get(2));
    }

    @Test
    void testAddDimensionMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);

        assertThrows(ArithmeticException.class, () -> v1.add(v2));
    }

    @Test
    void testAddOrientationMismatch() {
        SharedVector vRow = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector vCol = new SharedVector(new double[]{1, 2}, VectorOrientation.COLUMN_MAJOR);

        assertThrows(ArithmeticException.class, () -> vRow.add(vCol));
    }

    @Test
    void testTranspose() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());

        v.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, v.getOrientation());

        v.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, v.getOrientation());
    }

    @Test
    void testNegate() {
        SharedVector v = new SharedVector(new double[]{1, -2, 0}, VectorOrientation.ROW_MAJOR);
        v.negate();
        
        assertEquals(-1.0, v.get(0));
        assertEquals(2.0, v.get(1));
        assertEquals(-0.0, v.get(2), 0.0001); // -0.0 == 0.0 in double
    }

    @Test
    void testDotProduct() {
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{3, 4}, VectorOrientation.COLUMN_MAJOR);
        
        double result = v1.dot(v2);
        assertEquals(11.0, result); // 1*3 + 2*4 = 3+8 = 11
    }

     @Test
    void testDotOrientationMismatch() {
        SharedVector vRow = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector vCol = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);

        assertThrows(ArithmeticException.class, () -> vRow.dot(vCol));
    }

    @Test
    void testDotDimensionMismatch() {
        SharedVector v1 = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.COLUMN_MAJOR);

        assertThrows(ArithmeticException.class, () -> v1.dot(v2));
    }

    @Test
    void testVecMatMul() {
        SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        SharedMatrix matrix = new SharedMatrix();

        matrix.loadColumnMajor(new double[][]{
            {4, 7},
            {5, 8},
            {6, 9}
        }); 

        v.vecMatMul(matrix);
        
        assertEquals(2, v.length());
        assertEquals(32.0, v.get(0));
        assertEquals(50.0, v.get(1));
    }

    @Test
    void testVecMatMulEmptyMatrix() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        SharedMatrix emptyMat = new SharedMatrix(); // Empty
        v.vecMatMul(emptyMat);
        assertEquals(1, v.get(0));
        assertEquals(2, v.get(1));
        assertEquals(2, v.length()); 
    }
}