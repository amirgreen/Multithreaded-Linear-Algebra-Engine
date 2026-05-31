package memory;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        // TODO: store vector data and its orientation
        if (vector == null || orientation == null) {
            throw new IllegalArgumentException("Vector data cannot be null");
        }

        this.vector = vector;
        this.orientation = orientation;
    }

    public double get(int index) {
        return vector[index];
    }

    public int length() {
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        return orientation;
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    //writelock
    public void transpose() {
        writeLock();

        try {
            if(orientation == VectorOrientation.COLUMN_MAJOR)
                orientation = VectorOrientation.ROW_MAJOR;

            else
                orientation = VectorOrientation.COLUMN_MAJOR;
        }

        finally{
            writeUnlock();
        }
    }

    //writelock
    public void add(SharedVector other) {
        writeLock();
        other.readLock();

        try{
            if (this.orientation != other.orientation)
                throw new ArithmeticException("Cannot add row and column vector");

        if(this.vector.length != other.vector.length)
            throw new ArithmeticException("Cannot add vectors with diffrent length");

            for(int i = 0; i< this.vector.length; i++)
                vector[i] += other.vector[i];
        }

        finally {
            writeUnlock();
            other.readUnlock();
        }
    }

    //writelock
    public void negate() {
        writeLock();
        try{
            for(int i=0; i<vector.length; i++)
                vector[i] *= -1;
        }

        finally {
            writeUnlock();
        }
    }

    //readlock
    public double dot(SharedVector other) {
        readLock();
        other.readLock();

        try{
            if (this.orientation == other.orientation)
                throw new ArithmeticException("Cannot multiply two vectors with the same orientation");

            if(this.vector.length != other.vector.length)
                throw new ArithmeticException("Cannot multiply vectors with diffrent length");

            double product = 0;

                for(int i = 0; i < vector.length; i++)
                    product += this.vector[i] * other.vector[i];

            return product; 
        }

        finally{
            readUnlock();
            other.readUnlock();
        }   
    }


    //write lock
    public void vecMatMul(SharedMatrix matrix) {
        writeLock();
        try{
            if (matrix == null || matrix.length() == 0)
                return;

            for(int i = 0; i < matrix.length(); i++)
                matrix.get(i).readLock();

            try {
                double[] result = new double[matrix.length()];

                for(int i = 0; i < result.length; i++)
                    result[i] = dot(matrix.get(i));

                vector = result;
            }
            finally {
                for(int i = 0; i < matrix.length(); i++)
                    matrix.get(i).readUnlock();
            }
        }

        finally {
            writeUnlock();  
        }
    }
}
