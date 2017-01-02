import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

@Parameters(separators = "=")
public class PictureToText {
    Logger logger = Logger.getLogger(PictureToText.class);

    @Parameter(names = "-inputFilePath", description = "The image to be transferred", required = true)
    private String imagePath;

    @Parameter(names = "-outputFileDir", description = "The directory the output file will be put at", required = true)
    private String outputFileDir;

    private static final String asciiChar = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\\\"^`'. ";
    private static final int asciiCharLength = asciiChar.length();
    private static final float redPercentInGray = 0.2126f;
    private static final float greenPercentInGray = 0.7152f;
    private static final float bluePercentInGray = 0.0722f;

    private File createOutputFile(File outputDir, String imageName) {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        String inputFileNameWithoutType = imageName.substring(0, imageName.lastIndexOf("."));
        String outputFileName = inputFileNameWithoutType + ".txt";
        File outputFile = Paths.get(outputDir.getAbsolutePath(), outputFileName).toFile();
        if (outputFile.exists()) {
            outputFile.delete();
        }
        return outputFile;
    }

    private Character getCharFromRgb(Color color) {
        if (color.getAlpha() == 0) {
            return ' ';
        }
        float gray = redPercentInGray * color.getRed() + greenPercentInGray * color.getGreen() + bluePercentInGray * color.getBlue();
        float unit = (256.0f + 1) / asciiCharLength;
        return asciiChar.charAt((int) (gray / unit));
    }

    private Color getColorAtPixel(BufferedImage image, int x, int y) {
        int pixel = image.getRGB(x, y);
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        logger.debug("point(" + x + " ," + y + ") : (a,r,g,b): " + alpha + ", " + red + ", " + green + ", " + blue);
        return new Color(red, green, blue, alpha);
    }

    public void transfer() {
        File inputImage = Paths.get(imagePath).toFile();
        File outputDir = Paths.get(outputFileDir).toFile();
        if (!inputImage.exists()) {
            logger.fatal("Input file does not exist.");
            System.exit(1);
        }
        File outputFile = createOutputFile(outputDir, inputImage.getName());

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile))) {
            BufferedImage image = ImageIO.read(inputImage);
            int width = image.getWidth();
            int height = image.getHeight();
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color color = getColorAtPixel(image, j, i);
                    bw.write(getCharFromRgb(color));
                }
                bw.write("\n");
            }
            bw.flush();
        } catch (IOException e) {
            logger.fatal(e);
            e.printStackTrace();
            System.exit(1);
        }

    }

    public static void main(String[] args) {
        PictureToText pictureToText = new PictureToText();
        new JCommander(pictureToText, args);
        pictureToText.transfer();
    }
}
