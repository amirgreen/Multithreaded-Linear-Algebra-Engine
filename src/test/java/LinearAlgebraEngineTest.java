import spl.lae.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

    private LinearAlgebraEngine lae;

    @BeforeEach
    void setUp() {
        lae = new LinearAlgebraEngine(4);
    }

    @Test
    void testSimpleAddition() {
        // Node A: [[1, 2]]
        // Node B: [[3, 4]]
        // Op: ADD
        ComputationNode nodeA = new ComputationNode(new double[][]{{1, 2}});
        ComputationNode nodeB = new ComputationNode(new double[][]{{3, 4}});
        
        List<ComputationNode> children = new ArrayList<>(Arrays.asList(nodeA, nodeB));
        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, children);

        lae.run(root);

        double[][] result = root.getMatrix();
        assertNotNull(result);
        assertArrayEquals(new double[]{4, 6}, result[0], 0.0001);
    }

    @Test
    void testMultiplication() {
        // A (1x2): [[1, 2]]
        // B (2x2): [[3, 4], [5, 6]]
        // Result (1x2): [1*3+2*5, 1*4+2*6] = [13, 16]
        
        ComputationNode nodeA = new ComputationNode(new double[][]{{1, 2}});
        ComputationNode nodeB = new ComputationNode(new double[][]{{3, 4}, {5, 6}});
        
        List<ComputationNode> children = new ArrayList<>(Arrays.asList(nodeA, nodeB));
        ComputationNode root = new ComputationNode(ComputationNodeType.MULTIPLY, children);

        lae.run(root);

        double[][] result = root.getMatrix();
        assertEquals(13.0, result[0][0], 0.0001);
        assertEquals(16.0, result[0][1], 0.0001);
    }

    @Test
    void testComplexTreeExecution() {
        // (A + B)^T
        // A=[[1, 2]], B=[[3, 4]] -> A+B=[[4, 6]] -> Transpose -> [[4], [6]]
        
        ComputationNode nodeA = new ComputationNode(new double[][]{{1, 2}});
        ComputationNode nodeB = new ComputationNode(new double[][]{{3, 4}});
        
        List<ComputationNode> addChildren = new ArrayList<>(Arrays.asList(nodeA, nodeB));
        ComputationNode addNode = new ComputationNode(ComputationNodeType.ADD, addChildren);

        List<ComputationNode> transChildren = new ArrayList<>(Arrays.asList(addNode));
        ComputationNode root = new ComputationNode(ComputationNodeType.TRANSPOSE, transChildren);

        lae.run(root);

        double[][] result = root.getMatrix();
        assertEquals(2, result.length);
        assertEquals(1, result[0].length);
        assertEquals(4.0, result[0][0], 0.0001);
        assertEquals(6.0, result[1][0], 0.0001);
    }


    @Test
    void testErrorPropagationImmediate() {
        ComputationNode nodeA = new ComputationNode(new double[][]{{1, 2}});
        ComputationNode nodeB = new ComputationNode(new double[][]{{1, 2, 3}}); // Mismatch
        
        List<ComputationNode> children = new ArrayList<>(Arrays.asList(nodeA, nodeB));
        ComputationNode root = new ComputationNode(ComputationNodeType.ADD, children);

        assertThrows(IllegalArgumentException.class, () -> lae.run(root));
    }

    @Test
    void testErrorPropagationFromWorker() {

        ComputationNode nodeA = new ComputationNode(new double[][]{{1, 2}});
        ComputationNode nodeB = new ComputationNode(new double[][]{{1}, {2}, {3}});
        
        List<ComputationNode> children = new ArrayList<>(Arrays.asList(nodeA, nodeB));
        ComputationNode root = new ComputationNode(ComputationNodeType.MULTIPLY, children);

        assertThrows(IllegalArgumentException.class, () -> lae.run(root));
    }
}