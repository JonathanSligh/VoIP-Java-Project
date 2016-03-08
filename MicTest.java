import java.io.*;
import java.net.*;
import javax.sound.sampled.*;
//a simple program that records a bit and plays it back
class MicTest
{
	public static void main(String args[]) throws Exception
	{
		AudioFormat format=  new AudioFormat(8000.0f, 16, 1, true, true);
		TargetDataLine line = null;
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, 
		format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
			// Handle the error ... 

		}
		// Obtain and open the line.
		try {
			line = (TargetDataLine) AudioSystem.getLine(info);
			line.open(format);
		} catch (LineUnavailableException ex) {
			// Handle the error ... 
		}
		// Assume that the TargetDataLine, line, has already
		// been obtained and opened.
		ByteArrayOutputStream out  = new ByteArrayOutputStream();
		int numBytesRead;
		byte[] data = null;
		if (line != null)
		{
			data = new byte[line.getBufferSize() / 5];
		}
		else
		{
			data = new byte[1024];
		}

		// Begin audio capture.
		line.start();

		// Here, change variable i to record/capture for longer
		int i = 100;
		while (i != 0) {
			// Read the next chunk of data from the TargetDataLine.
			numBytesRead =  line.read(data, 0, data.length);
			// Save this chunk of data.
			out.write(data, 0, numBytesRead);
			i--;
		}
		System.out.println("Play now");
		//this plays back what was recorded.
		while(true){
			byte audio[] = out.toByteArray();
			InputStream input = new ByteArrayInputStream(audio);
			AudioInputStream ais = new AudioInputStream(input, format, audio.length / format.getFrameSize());
			DataLine.Info newinfo = new DataLine.Info(
			SourceDataLine.class, format);
			SourceDataLine newline = 
			(SourceDataLine)AudioSystem.getLine(newinfo);
			newline.open(format);
			newline.start();	
			int bufferSize = (int) format.getSampleRate() 
			* format.getFrameSize();
			byte buffer[] = new byte[bufferSize];

			int count;
			while ((count = 
			ais.read(buffer, 0, buffer.length)) != -1) {
				if (count > 0) {
					newline.write(buffer, 0, count);
				}
			}
			newline.drain();
			newline.close();
		}
	}
}