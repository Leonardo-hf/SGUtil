import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class SimpleSGUtil implements SGUtil {

    private byte[] getImageBinary(String url) throws SteganographyException {
        try {
            DataInputStream in = new DataInputStream(
                    new BufferedInputStream(
                            new FileInputStream(url)));
            return in.readAllBytes();
        } catch (IOException e) {
            throw new SteganographyException("隐写的图片不存在");
        }
    }

    private void writeImageBinary(String url, byte[] bytes, boolean behind) throws SteganographyException {
        try {
            DataOutputStream out = new DataOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(url, behind)));
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            throw new SteganographyException("追加写入出错");
        }
    }

    private void encodeWithBytesArray(String filePath, byte[] array) throws SteganographyException {
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
            writeImageBinary(saveFilePath, array, true);
            System.out.println("隐写的图片被生成到：" + saveFilePath);
        } catch (IOException e) {
            throw new SteganographyException("载体图片不存在");
        }
    }

    private byte[] decodeWithBytesArray(String filePath) throws SteganographyException {
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
        return newFileBytes;
    }

    @Override
    public void encodeWithText(String filePath, String text) {
        try {
            encodeWithBytesArray(filePath, text.getBytes());
        } catch (SteganographyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String decodeWithText(String filePath) {
        try {
            byte[] newFileBytes = decodeWithBytesArray(filePath);
            StringBuilder s = new StringBuilder();
            for (byte b : newFileBytes) {
                s.append((char) b);
            }
            System.out.println(s);
            return s.toString();
        } catch (SteganographyException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void encodeWithImage(String filePath, String hideFilePath) {
        try {
            encodeWithBytesArray(filePath, getImageBinary(hideFilePath));
        } catch (SteganographyException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void decodeWithImage(String filePath) {
        try {
            byte[] newFileBytes = decodeWithBytesArray(filePath);
            String url = filePath.substring(0, filePath.lastIndexOf("/") + 1) + getRandomName(8) + ".jpeg";
            writeImageBinary(url, newFileBytes, false);
            System.out.println("解密的图片被生成到：" + url);
        } catch (SteganographyException e) {
            e.printStackTrace();
        }
    }
}
