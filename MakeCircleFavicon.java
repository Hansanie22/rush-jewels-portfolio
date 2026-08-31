import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class MakeCircleFavicon {
    public static void main(String[] args) {
        try {
            File inputFile = new File("src/main/resources/static/favicon.png");
            if (!inputFile.exists()) {
                System.out.println("File not found!");
                return;
            }
            BufferedImage master = ImageIO.read(inputFile);

            int diameter = Math.min(master.getWidth(), master.getHeight());
            BufferedImage mask = new BufferedImage(master.getWidth(), master.getHeight(), BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = mask.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.fill(new Ellipse2D.Float(0, 0, diameter, diameter));
            g2d.dispose();

            BufferedImage masked = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
            g2d = masked.createGraphics();
            // Optional: apply rendering hints for smoother edges
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw transparent background
            g2d.setComposite(AlphaComposite.Clear);
            g2d.fillRect(0, 0, diameter, diameter);
            g2d.setComposite(AlphaComposite.Src);

            // Draw the original image but masked
            int x = (diameter - master.getWidth()) / 2;
            int y = (diameter - master.getHeight()) / 2;
            
            // Draw the mask first
            g2d.drawImage(master, x, y, null);
            g2d.setComposite(AlphaComposite.DstIn);
            g2d.drawImage(mask, 0, 0, null);
            g2d.dispose();

            ImageIO.write(masked, "png", new File("src/main/resources/static/favicon.png"));
            System.out.println("Success!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
