package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        // TODO: initialize empty matrix
        vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        // TODO: construct matrix as row-major SharedVectors
        vectors = new SharedVector[matrix.length];

        for(int i = 0; i < matrix.length; i++)
            vectors[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
    }

    public void loadRowMajor(double[][] matrix) {
        // TODO: replace internal data with new row-major matrix
        SharedVector[] newMatrix = new SharedVector[matrix.length];

        for(int i = 0; i < matrix.length; i++)
            newMatrix[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);

        vectors = newMatrix;
    }

    public void loadColumnMajor(double[][] matrix) {
        // TODO: replace internal data with new column-major matrix
        if (matrix.length == 0) {
            vectors = new SharedVector[0];
            return;
        }
        if (matrix[0].length == 0) {
            vectors = new SharedVector[0];
            return;
        }

        SharedVector[] newMatrix = new SharedVector[matrix[0].length];

        for(int i = 0; i < matrix[0].length; i++){
            double[] temp = new double[matrix.length];

            for(int j = 0; j < matrix.length; j++)
                temp[j] = matrix[j][i];

            SharedVector v = new SharedVector(temp, VectorOrientation.COLUMN_MAJOR);
            newMatrix[i] = v;
        }

        vectors = newMatrix;
    }

    public double[][] readRowMajor() {
        // TODO: return matrix contents as a row-major double[][]
        if (length() == 0) return new double[0][0];

        acquireAllVectorReadLocks(vectors);

        try{
            int rows, columns;
            boolean column_base = false;

            if(getOrientation() == VectorOrientation.ROW_MAJOR){
                rows = vectors.length;
                columns = vectors[0].length();
            }

            else{
                rows = vectors[0].length();
                columns = vectors.length;
                column_base = true;
            }

            double[][] result = new double[rows][columns];
                for(int i = 0; i < result.length; i++){

                    for(int j = 0; j < result[0].length; j++){
                        if(column_base)
                            result[i][j] = vectors[j].get(i);

                        else
                            result[i][j] = vectors[i].get(j);
                    }
                }
                
            return result;
        }
        finally {
            releaseAllVectorReadLocks(vectors);
        }
    }

    public SharedVector get(int index) {
        // TODO: return vector at index
        if(index >= 0 && index < length())
            return vectors[index];
        else{
            throw new IndexOutOfBoundsException("Illegal index " + index + "\n");
        }
    }

    public int length() {
        // TODO: return number of stored vectors
        return vectors.length;
    }

    public VectorOrientation getOrientation() {
        // TODO: return orientation
        if(vectors.length != 0){
            return vectors[0].getOrientation();
        }
        else{
            return VectorOrientation.ROW_MAJOR; //default value
        }
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vector : vecs)
            vector.readLock();
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for (SharedVector vector : vecs)
            vector.readUnlock();
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vector : vecs)
            vector.writeLock();
    }
    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for (SharedVector vector : vecs)
            vector.writeUnlock();
    }
}
