import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import java.awt.image.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class LSBSGUtil implements SGUtil {

    private final Set<String> lossyFormat = new HashSet<>(List.of("jpg", "jpeg"));
    private String imgName, imgExt, originExt;
    private String imgPath;
    private int defaultNameLength = 8;
    private String key = "NJU_Text";
    private int DESLength = 64, EXTLength = 0;
    private int[] pixels;
    private ColorModel imgColorType;
    private int width, height, bands;
    private int ctPoint = 0;
    private int ctMaskOne, ctMaskZero;
    private Queue<Integer> maskOne = new ArrayDeque<>(List.of(1, 2, 4, 8, 16, 32, 64, 128));
    private Queue<Integer> maskZero = new ArrayDeque<>(List.of(254, 253, 251, 247, 239, 223, 191, 127));

    private void init(String path) throws SteganographyException, IOException {
        File f = new File(path);
        if (!f.exists()) {
            throw new SteganographyException("隐写对象不存在");
        }
        this.imgName = getRandomName(this.defaultNameLength);
        this.imgPath = f.getPath().substring(0, f.getPath().lastIndexOf("/") + 1);
        this.originExt = f.getName().split("\\.")[1];
        //如果图片为有损压缩格式，将之保存为无损压缩的png格式输出
        if (this.lossyFormat.contains(this.originExt))
            this.imgExt = "png";
        else
            this.imgExt = this.originExt;
        this.maskOne = new ArrayDeque<>(List.of(1, 2, 4, 8, 16, 32, 64, 128));
        this.maskZero = new ArrayDeque<>(List.of(254, 253, 251, 247, 239, 223, 191, 127));
        this.ctMaskOne = this.maskOne.poll();
        this.ctMaskZero = this.maskZero.poll();
        BufferedImage img = ImageIO.read(f);
        this.imgColorType = img.getColorModel();
        Raster raster = img.getData();
        //获得图片的长，宽，段数，并且将每个像素的RGB等值填入pixels
        this.width = raster.getWidth();
        this.height = raster.getHeight();
        this.bands = raster.getNumBands();
        this.pixels = new int[this.width * this.height * this.bands];
        this.ctPoint = 0;
        raster.getPixels(0, 0, this.width, this.height, this.pixels);
    }

    private String string2Binary(String text) {
        StringBuilder transform = new StringBuilder();
        for (byte b : text.getBytes()) {
            transform.append(byte2Binary(b));
        }
        return transform.toString();
    }

    private String byte2Binary(byte c) {
        StringBuilder s = new StringBuilder(Integer.toBinaryString(Byte.toUnsignedInt(c)));
        while (s.length() < 8) {
            s.insert(0, "0");
        }
        return s.toString();
    }

    private void nextSlot() throws SteganographyException {
        if (this.ctPoint == this.width * this.height * this.bands - 1) {
            this.ctPoint = 0;
            if (this.maskOne.isEmpty()) {
                throw new SteganographyException("No available slot to substitute");
            } else {
                this.ctMaskOne = this.maskOne.poll();
                this.ctMaskZero = this.maskZero.poll();
            }
        } else {
            this.ctPoint++;
        }
    }

    private void writeIn(String content) throws SteganographyException {
        for (char c : content.toCharArray()) {
            if (c - 48 == 0) {
                pixels[ctPoint] &= ctMaskZero;
            } else if (c - 48 == 1) {
                pixels[ctPoint] |= ctMaskOne;
            } else {
                throw new SteganographyException("字符串未转换为二进制");
            }
            nextSlot();
        }
    }

    private void writeInFlag(String flag) throws SteganographyException {
        try {
            if (this.DESLength / this.key.getBytes().length != 8 || this.DESLength % this.key.getBytes().length != 0) {
                throw new SteganographyException("秘钥的字节长度与加密长度不匹配");
            }
            SecretKeySpec sKey = new SecretKeySpec(this.key.getBytes(), "DES");
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, sKey);
            byte[] secretFlag = cipher.doFinal(flag.getBytes());
            StringBuilder s = new StringBuilder();
            for (byte b : secretFlag) {
                s.append(byte2Binary(b));
            }
            writeIn(s.toString());
        } catch (NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException e) {
            throw new SteganographyException("加密出错（程序BUG）:" + e.getLocalizedMessage());
        }
    }

    @Override
    public void encodeWithText(String path, String text) {
        try {
            init(path);
            if (this.width * this.height * this.bands < text.length() * 8 + this.DESLength + this.EXTLength) {
                System.err.println("WARN: 文本较长，可能影响图片质量");
                if (this.width * this.height * this.bands * 2 < text.length() * 8 + this.DESLength + this.EXTLength) {
                    System.err.println("文本过长，请缩短文本或更换更大的图片");
                    return;
                }
            }
            //在图片头64个slot中写入加密的长度
            StringBuilder length = new StringBuilder(String.valueOf(text.length()));
            while (length.length() < 8)
                length.insert(0, '0');
            writeInFlag(length.toString());
            //在图片中继续写入需要隐写的数据
            String binaryText = string2Binary(text);
            writeIn(binaryText);
            //生成新的图片
            make();
        } catch (SteganographyException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String decodeWithText(String path) {
        try {
            init(path);
            int length = verify();
            String content = readOut(length);
            System.out.println(content);
            return content;
        } catch (SteganographyException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void encodeWithImage(String path, String hideImgPath) {
        try {
            File file = new File(hideImgPath);
            StringBuilder hideExt = new StringBuilder(file.getName().split("\\.")[1]);
            while (hideExt.length() < 8) {
                hideExt.insert(0, "!");
            }
            StringBuilder s = new StringBuilder();
            try {
                DataInputStream d = new DataInputStream(
                        new BufferedInputStream(
                                new FileInputStream(file)));
                for (byte b : d.readAllBytes()) {
                    s.append(byte2Binary(b));
                }
            } catch (IOException e) {
                throw new SteganographyException("隐写的图片不存在");
            }
            this.EXTLength = 64;
            init(path);
            if (this.width * this.height * this.bands < s.length() + this.DESLength + this.EXTLength) {
                System.err.println("WARN: 隐写图片较大，可能影响图片质量");
                if (this.width * this.height * this.bands * 2 < s.length() + this.DESLength + this.EXTLength) {
                    System.err.println("隐写图片过大，请压缩隐写图片或更换更大的载体图片");
                    return;
                }
            }
            //在图片头64个slot中写入加密的长度
            StringBuilder length = new StringBuilder(String.valueOf(s.length() / 8));
            while (length.length() < 8)
                length.insert(0, '0');
            writeInFlag(length.toString());
            //在图片中继续写入需要隐写的数据
            writeIn(s.toString());
            //写入后缀名
            writeIn(string2Binary(hideExt.toString()));
            //生成新的图片
            make();
        } catch (SteganographyException | IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void decodeWithImage(String path) {
        try {
            init(path);
            int length = verify();
            String content = readOut(length);
            byte[] bytes = new byte[length];
            int index = 0;
            for (char x : content.toCharArray()) {
                bytes[index++] = (byte) x;
            }
            StringBuilder ext = new StringBuilder(readOut(8));
            while (ext.charAt(0) == '!')
                ext.deleteCharAt(0);
            File f = new File(this.imgPath + this.imgName + "." + ext);
            if (f.exists()) {
                f.delete();
                f.createNewFile();
            }
            OutputStream os = new FileOutputStream(f);
            os.write(bytes);
            System.out.println("解密的图片被生成到：" + this.imgPath + this.imgName + "." + ext);
        } catch (SteganographyException | IOException e) {
            e.printStackTrace();
        }
    }

    private String readOut(int length) throws SteganographyException {
        StringBuilder s = new StringBuilder();
        StringBuilder result = new StringBuilder();
        for (int i = 0, m = 1; i < length * 8; i++, m++) {
            if ((pixels[ctPoint] & this.ctMaskOne) > 0)
                s.append(1);
            else
                s.append(0);
            if (m % 8 == 0) {
                m = 0;
                result.append((char) Integer.parseUnsignedInt(s.toString(), 2));
                s.setLength(0);
            }
            nextSlot();
        }
        return result.toString();
    }


    private int verify() throws SteganographyException {
        try {
            //后缀名检验
            if (lossyFormat.contains(this.originExt))
                throw new SteganographyException("不支持的文件格式(jpg,jpeg)");
            if (this.DESLength / this.key.getBytes().length != 8 || this.DESLength % this.key.getBytes().length != 0)
                throw new SteganographyException("秘钥的字节长度与加密长度不匹配");
            StringBuilder s = new StringBuilder();
            byte[] bytes = new byte[this.DESLength / 8];
            for (int i = 0, m = 1, n = 0; i < this.DESLength; i++, m++) {
                if ((pixels[ctPoint] & this.ctMaskOne) > 0)
                    s.append(1);
                else
                    s.append(0);
                if (m % 8 == 0) {
                    m = 0;
                    bytes[n++] = (byte) Integer.parseInt(s.toString(), 2);
                    s.setLength(0);
                }
                nextSlot();
            }
            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            SecretKeySpec sKey = new SecretKeySpec(this.key.getBytes(), "DES");
            cipher.init(Cipher.DECRYPT_MODE, sKey);
            byte[] origin = cipher.doFinal(bytes);
            StringBuilder length = new StringBuilder();
            for (byte b : origin) {
                length.append((char) b);
            }
            return Integer.parseInt(length.toString());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            throw new SteganographyException("解密出错（程序BUG）:" + e.getLocalizedMessage());
        } catch (NumberFormatException e) {
            throw new SteganographyException("文件未隐写入文档");
        }
    }

    private void make() {
        try {
            DataBuffer db = new DataBufferInt(pixels, this.width * this.height * this.bands);
            SampleModel sm = this.imgColorType.createCompatibleSampleModel(this.width, this.height);
            WritableRaster raster = Raster.createWritableRaster(sm, db, null);
            BufferedImage image = new BufferedImage(this.imgColorType, raster, false, null);
            File newImg = new File(this.imgPath + this.imgName + "." + this.imgExt);
            if (newImg.exists()) {
                newImg.delete();
                newImg.createNewFile();
            }
            ImageIO.write(image, this.imgExt, new FileOutputStream(newImg));
            System.out.println("隐写的图片被生成到：" + this.imgPath + this.imgName + "." + this.imgExt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setKey(String key) {
        try {
            if (key.getBytes().length != 8) {
                this.key = key;
                this.DESLength = 8 * key.getBytes().length;
            } else {
                throw new SteganographyException("秘钥的字节长度不为8的倍数");
            }
        } catch (SteganographyException e) {
            e.printStackTrace();
        }
    }

    public void setDefaultNameLength(int defaultNameLength) {
        this.defaultNameLength = defaultNameLength;
    }


}
