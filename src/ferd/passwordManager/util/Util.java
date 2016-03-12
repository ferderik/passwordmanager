package ferd.passwordManager.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Tooltip;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;



public class Util {
    static final Text helper = new Text();

    public static double computeTextWidth(final Font font, final String text, final double help0) {
        helper.setText(text);
        helper.setFont(font);
        helper.setWrappingWidth(0.0D);
        helper.setLineSpacing(0.0D);
        double d = Math.min(helper.prefWidth(-1.0D), help0);
        helper.setWrappingWidth((int) Math.ceil(d));
        d = Math.ceil(helper.getLayoutBounds().getWidth());
        return d;
    }

    public static String decryptFileToString(final File inFile, final String pass) throws IOException, GeneralSecurityException {
        final FileInputStream fis = new FileInputStream(inFile);
        final byte[] salt = new byte[8];
        final byte[] iv = new byte[16];
        fis.read(salt);
        fis.read(iv);

        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final KeySpec keySpec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);
        final SecretKey tmp = factory.generateSecret(keySpec);
        final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        String out = "";

        final byte[] in = new byte[64];
        int read;
        while ((read = fis.read(in)) != -1) {
            final byte[] output = cipher.update(in, 0, read);
            if (output != null) {
                out += new String(output, "UTF-8");
            }
        }
        try {
            final byte[] output = cipher.doFinal();
            if (output != null) {
                out += new String(output, "UTF-8");
            }
        } finally {
            fis.close();
        }

        return out;
    }

    public static void encryptStringToFile(final String data, final File outFile, final String pass) throws IOException, GeneralSecurityException {
        final FileOutputStream outFileStream = new FileOutputStream(outFile);
        final byte[] salt = new byte[8];
        final SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        outFileStream.write(salt);

        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final KeySpec keySpec = new PBEKeySpec(pass.toCharArray(), salt, 65536, 128);
        final SecretKey tmp = factory.generateSecret(keySpec);
        final SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        final AlgorithmParameters params = cipher.getParameters();

        final byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        outFileStream.write(iv);

        final byte[] inData = data.getBytes("UTF-8");
        final byte[] input = new byte[64];
        int bytesRead;
        int ind = 0;

        while (ind < inData.length) {
            bytesRead = inData.length - ind >= input.length ? input.length : inData.length - ind;
            System.arraycopy(inData, ind, input, 0, bytesRead);
            ind += bytesRead;
            final byte[] output = cipher.update(input, 0, bytesRead);
            if (output != null) {
                outFileStream.write(output);
            }
        }
        final byte[] output = cipher.doFinal();
        if (output != null) {
            outFileStream.write(output);
        }
        outFileStream.flush();
        outFileStream.close();
    }

    public static File getWorkingDirectory() throws IOException {
        final String userHome = System.getProperty("user.home", ".");

        final String osName = System.getProperty("os.name").toLowerCase();
        File workingDirectory = new File(userHome, "masterPassword/");

        if (osName.contains("win")) {
            final String applicationData = System.getenv("APPDATA");
            final String folder = applicationData != null ? applicationData : userHome;
            workingDirectory = new File(folder, ".masterPassword/");
        } else if (osName.contains("mac")) {
            workingDirectory = new File(userHome, "Library/Application Support/masterPassword");
        } else if (osName.contains("linux") || osName.contains("unix")) {
            workingDirectory = new File(userHome, ".masterPassword/");
        }

        if (workingDirectory.exists() && !workingDirectory.isDirectory()) {
            throw new IOException("Invalid working directory: " + workingDirectory);
        }
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            throw new IOException("Unable to create directory: " + workingDirectory);
        }

        return workingDirectory;
    }

    public static Tooltip hackTooltipTiming(final Tooltip tooltip, final int delay) {
        try {
            final Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            final Object objBehavior = fieldBehavior.get(tooltip);

            final Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            final Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new Duration(delay)));

            final Field fieldLeftTimer = objBehavior.getClass().getDeclaredField("leftTimer");
            fieldLeftTimer.setAccessible(true);
            final Timeline objLeftTimer = (Timeline) fieldLeftTimer.get(objBehavior);

            objLeftTimer.getKeyFrames().clear();
            objLeftTimer.getKeyFrames().add(new KeyFrame(new Duration(delay)));
            return tooltip;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
