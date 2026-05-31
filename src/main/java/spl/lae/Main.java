package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws IOException {
      if (args.length != 3)
        System.err.print("Invalid arguments\n");

      else{
        int numOfThreads;

        try{
          numOfThreads = Integer.parseInt(args[0]);
          if (numOfThreads <= 0)
            throw new NumberFormatException("Invalid thread number: must be positive\n");

          InputParser parser = new InputParser();
          ComputationNode computeTree = parser.parse(args[1]);

          computeTree.associativeNesting();

          OutputWriter.write(new double[0][0], args[2]);// create empty output file in case of exception

          LinearAlgebraEngine LAE = new LinearAlgebraEngine(numOfThreads);
          ComputationNode resultNode = LAE.run(computeTree);

          System.out.println(LAE.getWorkerReport());
          OutputWriter.write(resultNode.getMatrix(),args[2]);
        }

        catch(NumberFormatException e ){
          System.err.print("Invalid thread number argument\n");
          writeError(e, args[2]);
        }

        catch(ParseException e){
          System.err.print(e.getMessage());
          writeError(e, args[2]);
        }

        catch(IOException e){
          System.err.print("Invalid output file argument\n");
          writeError(e, args[2]);
        }

        catch (Exception e){
          writeError(e, args[2]);
        }
      }
    }

    private static void writeError(Exception e, String output){
      try{
            OutputWriter.write(e.getMessage(), output);
          }
          
          catch(IOException excp){
            System.err.print("Invalid output file argument\n");
          }
    }
}