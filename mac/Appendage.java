import java.io.*;


public class Appendage {
    static public void main(String args[]) {
	try {
	    if (args.length != 3) {
		System.err.println("usage: java Appendage " +
				   "<source.bin> <append.zip> <target.bin>");
		System.exit(0);
	    }
	    new Appendage(args[0], args[1], args[2]);
	    //Appendage.test();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public Appendage(String sourceFile, String appendFile, 
		     String outputFile) throws Exception {
	StructuredFile mbinSource = 
	    new StructuredFile(new File(sourceFile));

	StructuredFile appendSource = 
	    new StructuredFile(new File(appendFile));
	int dataForkLength = appendSource.length();

	byte header[] = new byte[128];
	mbinSource.readBytes(header);
	StructuredByteArray sba = new StructuredByteArray(header);
	sba.writeInt32(dataForkLength, 83);
	sba.writeInt32(MacBinaryFile.calculateCRC(124, header), 124);

	// write the macbinary header to the output file
	StructuredFile output = 
	    new StructuredFile(new File(outputFile));
	output.writeBytes(header);
	
	// write the data fork (the appendage) to the output file
	output.writeChunk(appendSource, dataForkLength);
	int padding = (128 - (dataForkLength % 128)) & 0x7f;
	output.writeZeros(padding);

	// write the resource fork to the output file
	mbinSource.setPosition(0);
	MacBinaryFile mbf = new MacBinaryFile(mbinSource);
	StructuredData resourceFork = mbf.getResourceFork();
	resourceFork.setPosition(0);
	output.writeChunk(resourceFork, mbf.resourceForkLength);
	
	// close it all up and quit
	mbinSource.close();
	appendSource.close();
	output.close();
    }

    static void test() throws Exception {
	StructuredData sdata = new StructuredFile(new File("dbn.bin"));
	MacBinaryFile mbf = new MacBinaryFile(sdata);
	System.out.println(mbf);
    }
}

