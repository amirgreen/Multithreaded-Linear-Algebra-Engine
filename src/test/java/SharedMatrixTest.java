import memory.SharedMatrix;
import memory.VectorOrientation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedMatrixTest {

    @Test
    void testLoadAndReadRowMajor() {
        double[][] data = {{1, 2},
                           {3, 4}};
        SharedMatrix m = new SharedMatrix(data);
        
        double[][] result = m.readRowMajor();
        assertArrayEquals(data[0], result[0], 0.0001);
        assertArrayEquals(data[1], result[1], 0.0001);
    }

    @Test
    void testLoadColumnMajorCheckOrientation() {
        double[][] data = {{1, 2},
                           {3, 4}};
        SharedMatrix m = new SharedMatrix();
        m.loadColumnMajor(data);
        
        assertEquals(VectorOrientation.COLUMN_MAJOR, m.getOrientation());
        
        double[][] result = m.readRowMajor();

        assertEquals(1.0, result[0][0]);
        assertEquals(2.0, result[0][1]);
        assertEquals(3.0, result[1][0]);
        assertEquals(4.0, result[1][1]);
    }

    @Test
    void testSingleElementMatrix() {
        double[][] data = {{42.0}};
        SharedMatrix m = new SharedMatrix(data);
        assertEquals(1, m.length());
        assertEquals(42.0, m.readRowMajor()[0][0]);
    }

    @Test
    void testNonSquareMatrix() {
        // 2x3 Matrix
        double[][] data = {{1, 2, 3}, {4, 5, 6}};
        SharedMatrix m = new SharedMatrix(data);
        
        assertEquals(2, m.length()); // 2 rows
        assertEquals(3, m.get(0).length()); // 3 cols
        
        double[][] res = m.readRowMajor();
        assertEquals(1.0, res[0][0]);
        assertEquals(2.0, res[0][1]);
        assertEquals(3.0, res[0][2]);
        assertEquals(4.0, res[1][0]);       
        assertEquals(5.0, res[1][1]);
        assertEquals(6.0, res[1][2]);
        
        
    }

    @Test
    void testEmptyMatrix() {
        SharedMatrix m = new SharedMatrix();
        assertEquals(0, m.length());
        assertEquals(0, m.readRowMajor().length);
    }
}