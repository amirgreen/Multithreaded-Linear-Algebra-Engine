package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        try{
            boolean finished = false;
            while (!finished) {
                ComputationNode currentNode = computationRoot.findResolvable();
                if (currentNode == null)
                    finished = true;

                else {
                    loadAndCompute(currentNode);
                    currentNode.resolve(leftMatrix.readRowMajor());
                }
            }
        }

        catch(Exception e){
            throw e;
        }

        finally{
            try{
                executor.shutdown();
            }

            catch(InterruptedException e){
                Thread.currentThread().interrupt();
                System.err.print("shutdown was interrupted");
            }
        }
        
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        List<Runnable> tasks = new ArrayList<Runnable>();

        switch(node.getNodeType()){
            case ComputationNodeType.ADD: {
                loadChildren(node.getChildren(), false);
                tasks = createAddTasks();
                break;
            }

            case ComputationNodeType.MULTIPLY:{
                loadChildren(node.getChildren(), true);
                tasks = createMultiplyTasks();
                break;
            }

            case ComputationNodeType.NEGATE:{
                loadChildren(node.getChildren(), false);
                tasks = createNegateTasks();
                break;
            }

            case ComputationNodeType.TRANSPOSE:{
                loadChildren(node.getChildren(), false);
                tasks = createTransposeTasks();
                break;
            }

            default:{
                throw new IllegalArgumentException("Unknown operator ");
            }
        }

        executor.submitAll(tasks);
    }

    public List<Runnable> createAddTasks() {
        if (leftMatrix.length() != rightMatrix.length()) {
            throw new IllegalArgumentException("Matrix dimensions mismatch (rows): "
                + leftMatrix.length() + " vs " + rightMatrix.length());
        }

        if (leftMatrix.length() > 0 &&
            leftMatrix.get(0).length() != rightMatrix.get(0).length()) {
            throw new IllegalArgumentException("Matrix dimensions mismatch (colums): "
                + leftMatrix.get(0).length() + " vs " + rightMatrix.get(0).length());
        }

        List<Runnable> addTasks = new ArrayList<>();

        for (int i = 0; i < leftMatrix.length(); i++){
            SharedVector left = leftMatrix.get(i);
            SharedVector right = rightMatrix.get(i);

            Runnable task = ()-> {
                left.add(right);
            };

            addTasks.addLast(task);
        }
        return addTasks;
    }

    public List<Runnable> createMultiplyTasks() {
        if(leftMatrix.length() == 0 || rightMatrix.length() == 0)
            return new ArrayList<>();
            
        if (leftMatrix.get(0).length() != rightMatrix.get(0).length()) 
            throw new IllegalArgumentException("Matrix dimensions mismatch : m1 colums: "
                + leftMatrix.get(0).length() + " vs m2 rows: " + rightMatrix.get(0).length());

        List<Runnable> multTasks = new ArrayList<>();

        for (int i = 0; i < leftMatrix.length(); i++){
            SharedVector left = leftMatrix.get(i);

            Runnable task = ()-> {
                left.vecMatMul(rightMatrix);
            };

            multTasks.addLast(task);
        }   
        return multTasks;
    }

    public List<Runnable> createNegateTasks() {
        List<Runnable> negateTasks = new ArrayList<>();

        for (int i = 0; i < leftMatrix.length(); i++){
            SharedVector left = leftMatrix.get(i);

            Runnable task = ()-> {
                left.negate();
            };

            negateTasks.addLast(task);
        }   
        return negateTasks;
    }

    public List<Runnable> createTransposeTasks() {
        List<Runnable> transposeTasks = new ArrayList<>();

        for (int i = 0; i < leftMatrix.length(); i++){
            SharedVector left = leftMatrix.get(i);

            Runnable task = ()-> {
                left.transpose();
            };

            transposeTasks.addLast(task);
        }   
        return transposeTasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }

    private void loadChildren(List<ComputationNode> operands, boolean colum) //loads operands to m1 m2 based on unrity/binarity
    {
        leftMatrix.loadRowMajor(operands.getFirst().getMatrix());

        if (operands.size() == 2) {
            if (colum)
                rightMatrix.loadColumnMajor(operands.getLast().getMatrix());

            else
                rightMatrix.loadRowMajor(operands.getLast().getMatrix());
        }        
    }
}
