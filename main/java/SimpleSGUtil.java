import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

public class SimpleSGUtil implements SGUtil {

    @Override
    public void decodeWithText(String filePath) {
        byte[] origin = getImageBinary(filePath);
        byte sectionSymbol = (byte) 0xff;
        byte sectionType = (byte) 0xd9;
        boolean sectionFlag = false;
        int startIndex = 0;
        for (int i = 0; i < origin.length; i++) {
            if (sectionFlag) {
                if (origin[i] != sectionType) {
                    sectionFlag = false;
                    continue;
                }
                startIndex = i + 1;
                break;
            }
            if (origin[i] == sectionSymbol) {
                sectionFlag = true;
            }
        }
        byte[] newFileBytes = new byte[origin.length - startIndex];
        System.arraycopy(origin, startIndex, newFileBytes, 0, origin.length - startIndex);
        for (byte b : newFileBytes) {
            System.out.print((char) b);
        }
    }

    @Override
    public void encodeWithText(String filePath, String text) {
        try {
            String saveFilePath = filePath.substring(0, filePath.lastIndexOf("/") + 1) + getRandomName(8) + ".png";
            FileOutputStream fos = new FileOutputStream(saveFilePath);
            BufferedImage bufferedImage = ImageIO.read(new File(filePath));
            //转成jpeg
            BufferedImage bufferedImageJPEG = new BufferedImage(bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            bufferedImageJPEG.createGraphics().drawImage(bufferedImage, 0, 0, Color.white, null);
            ImageIO.write(bufferedImageJPEG, "jpg", fos);
            fos.flush();
            //System.err.println(getImageBinary(saveFilePath));
            writeImageBinary(saveFilePath, text.getBytes(), true);
            System.out.println("隐写的图片被生成到：" + saveFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static byte[] getImageBinary(String url) {
        try {
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(url)));
            return in.readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void writeImageBinary(String url, byte[] bytes, boolean behind) {
        try {
            DataOutputStream out = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(url, behind)));
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void encodeWithImage(String filePath, String hideFilePath) {
        try {
            String saveFilePath = filePath.substring(0, filePath.lastIndexOf("/") + 1) + getRandomName(8) + ".jpeg";
            FileOutputStream fos = new FileOutputStream(saveFilePath);
            BufferedImage bufferedImage = ImageIO.read(new File(filePath));
            //转成jpeg
            BufferedImage bufferedImageJPEG = new BufferedImage(bufferedImage.getWidth(),
                    bufferedImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB);
            bufferedImageJPEG.createGraphics().drawImage(bufferedImage, 0, 0, Color.white, null);
            ImageIO.write(bufferedImageJPEG, "jpg", fos);
            fos.flush();
            //System.err.println(getImageBinary(saveFilePath));
            writeImageBinary(saveFilePath, getImageBinary(hideFilePath), true);
            System.out.println("隐写的图片被生成到：" + saveFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeWithImage(String filePath) {
        byte[] origin = getImageBinary(filePath);
        assert origin != null;
        byte sectionSymbol = (byte) 0xff;
        byte sectionType = (byte) 0xd9;
        boolean sectionFlag = false;
        int startIndex = 0;
        for (int i = 0; i < origin.length; i++) {
            if (sectionFlag) {
                if (origin[i] != sectionType) {
                    sectionFlag = false;
                    continue;
                }
                startIndex = i + 1;
                break;
            }
            if (origin[i] == sectionSymbol) {
                sectionFlag = true;
            }
        }
        byte[] newFileBytes = new byte[origin.length - startIndex];
        System.arraycopy(origin, startIndex, newFileBytes, 0, origin.length - startIndex);
        writeImageBinary(filePath.substring(0, filePath.lastIndexOf("/") + 1) + getRandomName(8) + ".jpeg", newFileBytes, false);
    }

    public static void main(String[] args) {
        SGUtil sgUtil = new SimpleSGUtil();
        //sgUtil.encodeWithText("src/main/resources/1.jpg", "234ewq");
        //sgUtil.decodeWithText("src/main/resources/YFKVcrJY.png");
        //sgUtil.encodeWithImage("src/main/resources/1.jpg", "21.png");
        sgUtil.decodeWithImage("src/main/resources/mEnygWKq.jpeg");
    }
}
