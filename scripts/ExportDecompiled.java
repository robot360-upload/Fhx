// ExportDecompiled.java
// @category Analysis
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionIterator;
import ghidra.program.model.listing.FunctionManager;
import ghidra.util.task.ConsoleTaskMonitor;
public class ExportDecompiled extends GhidraScript {
    @Override
    public void run() throws Exception {
        String outputPath = getScriptArgs()[0];
        File outputDir = new File(outputPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new Exception("Could not create output directory.");
        }
        File outputFile = new File(outputDir, "decompiled.c");
        DecompInterface decompiler = new DecompInterface();
        decompiler.openProgram(currentProgram);
        FunctionManager functionManager =
            currentProgram.getFunctionManager();
        FunctionIterator functions =
            functionManager.getFunctions(true);
        int count = 0;
        try (PrintWriter writer =
                 new PrintWriter(new FileWriter(outputFile))) {
            writer.println("/*");
            writer.println(" * Ghidra decompilation output");
            writer.println(" * Program: " + currentProgram.getName());
            writer.println(" * This is reconstructed pseudocode,");
            writer.println(" * not the original source code.");
            writer.println(" */");
            writer.println();
            while (functions.hasNext() && !monitor.isCancelled()) {
                Function function = functions.next();
                if (function.isExternal()) {
                    continue;
                }
                DecompileResults result = decompiler.decompileFunction(
                    function, 60, monitor
                );
                writer.println(
                    "\n/* Function: " + function.getName()
                    + " at " + function.getEntryPoint() + " */"
                );
                if (result.decompileCompleted()
                        && result.getDecompiledFunction() != null) {
                    writer.println(
                        result.getDecompiledFunction().getC()
                    );
                    count++;
                } else {
                    writer.println(
                        "/* Decompilation failed: "
                        + result.getErrorMessage() + " */"
                    );
                }
                writer.flush();
            }
        } finally {
            decompiler.dispose();
        }
        println("Exported " + count + " functions to " + outputFile);
    }
}
