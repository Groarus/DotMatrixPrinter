import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Project: DotPrinter
 * Name: Graham Burgsma
 * Created on 30 March, 2016
 */

public class ImageProcessor {

    public static final int[] palette = {Color.white.getRGB(), Color.red.getRGB(), Color.green.getRGB(), Color.blue.getRGB(), Color.yellow.getRGB(), Color.black.getRGB()};
    private int MAX_PRINT_WIDTH = 5;
    private BufferedImage originalImage, edgeImage;
    private int[][] printMatrix;

    public ImageProcessor(String imageName) {
        MAX_PRINT_WIDTH = (Globals.MAX_SLIDER_DISTANCE / Globals.PRINT_X_SPACING) - (Globals.SLIDER_START_DISTANCE / Globals.PRINT_X_SPACING);

        try {
            originalImage = ImageIO.read(new File("images/" + imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sobelEdgeDetector() {
        SobelEdgeDetection sobelEdgeDetection = new SobelEdgeDetection(originalImage);
        edgeImage = sobelEdgeDetection.process();

        saveImage(edgeImage, "sobel.jpg");
    }

    public int[][] imageToMatrix() {
        int height = edgeImage.getHeight() / (edgeImage.getWidth() / MAX_PRINT_WIDTH);

        Image imageEdge = edgeImage.getScaledInstance(MAX_PRINT_WIDTH, height, Image.SCALE_AREA_AVERAGING);
        Image imageOriginal = originalImage.getScaledInstance(MAX_PRINT_WIDTH, height, Image.SCALE_AREA_AVERAGING);

        BufferedImage resizedImage = toBufferedImage(imageEdge);
        BufferedImage resizedImageOriginal = toBufferedImage(imageOriginal);

        int[][] imageMatrix = new int[resizedImage.getHeight()][resizedImage.getWidth()];

        for (int y = 0; y < resizedImage.getHeight(); y++) {
            for (int x = 0; x < resizedImage.getWidth(); x++) {
                if (getRed(resizedImage.getRGB(x, y)) + getGreen(resizedImage.getRGB(x, y)) + getBlue(resizedImage.getRGB(x, y)) > Globals.PRINT_THRESHOLD) {
                    int minDistance = Integer.MAX_VALUE;
                    int closestColour = 0;

                    for (int i = 0; i < palette.length; i++) {
                        int distance = getDistance(resizedImageOriginal.getRGB(x, y), palette[i]);
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestColour = i;
                        }
                    }
                    imageMatrix[y][x] = closestColour;
                } else {
                    imageMatrix[y][x] = 0;
                }
            }
        }

        saveImage(resizedImage, "scaledImage.jpg");

        printMatrix = imageMatrix;
        return imageMatrix;
    }

    private int getDistance(int color1, int color2) {
        return ((int) (Math.pow(getRed(color2) - getRed(color1), 2) + Math.pow(getGreen(color2) - getGreen(color1), 2) + Math.pow(getBlue(color2) - getBlue(color1), 2)));
    }

    private int getRed(int rgb) {
        return (rgb >> 16) & 0xFF;
    }

    private int getGreen(int rgb) {
        return (rgb >> 8) & 0xFF;
    }

    private int getBlue(int rgb) {
        return rgb & 0xFF;
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        return bimage;
    }

    public void saveMatrixToFile() {
        System.out.println(printMatrix.length);
        System.out.println(printMatrix[0].length);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("matrix.txt")));

            for (int y = 0; y < printMatrix.length; y++) {
                for (int x = 0; x < printMatrix[0].length; x++) {
                    writer.write(String.valueOf(printMatrix[y][x]));
                    writer.write(',');
                }
                writer.write("\n");
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveImage(BufferedImage image, String fileName) {
        File outputfile = new File("images/" + fileName);
        try {
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}