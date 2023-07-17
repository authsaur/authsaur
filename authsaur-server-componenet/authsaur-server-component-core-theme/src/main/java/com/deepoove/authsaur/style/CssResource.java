package com.deepoove.authsaur.style;

import com.deepoove.authsaur.setting.SettingKey;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import com.deepoove.authsaur.setting.SettingsConf;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.Resource;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

@Setter
public class CssResource extends AbstractResource {

    private byte[] byteArray;
    private final String description = "";
    private long lastModified = System.currentTimeMillis();
    private final SettingsConf settingsConf;

    public CssResource(SettingsConf settingsConf, byte[] byteArray) {
        Assert.notNull(byteArray, "Byte array must not be null");
        this.byteArray = byteArray;
        this.settingsConf = settingsConf;
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        Map<String, Object> setting = settingsConf.getSetting(SettingKey.THEME);
        Object color = setting.getOrDefault(SettingKey.THEME_COLOR, "");
        String css = "";
        if (StringUtils.isNotBlank(color.toString())) {
            css = "--cas-theme-primary: " + color + ";\n";
            Color decodeColor = Color.decode(color.toString());
            double FACTOR = 0.9;
            Color decode = new Color(Math.max((int) (decodeColor.getRed() * FACTOR), 0),
                    Math.max((int) (decodeColor.getGreen() * FACTOR), 0),
                    Math.max((int) (decodeColor.getBlue() * FACTOR), 0),
                    decodeColor.getAlpha());
            String red = Integer.toHexString(decode.getRed());
            String green = Integer.toHexString(decode.getGreen());
            String blue = Integer.toHexString(decode.getBlue());
            if (red.length() == 1) red = "0" + red;
            if (green.length() == 1) green = "0" + green;
            if (blue.length() == 1) blue = "0" + blue;
            String hexColor = "#" + red + green + blue;
            css += "--cas-theme-primary-dark: " + hexColor + ";\n";
        }
        String content = ":root {\n" + css + "}";
        CssResource innerByteArrayResource = new CssResource(this.settingsConf,
                content.getBytes(StandardCharsets.UTF_8));
        if (content.equals(new String(this.byteArray))) {
            innerByteArrayResource.setLastModified(this.lastModified);
        } else {
            this.byteArray = content.getBytes(StandardCharsets.UTF_8);
            this.lastModified = System.currentTimeMillis();
            innerByteArrayResource.setLastModified(System.currentTimeMillis());
        }
        return innerByteArrayResource;
    }

    @Override
    public URL getURL() throws IOException {
        return new URL("http://memory.resource");
    }

    @Override
    public String getFilename() {
        return "custom.css";
    }

    @Override
    public File getFile() throws IOException {
        return super.getFile();
    }

    @Override
    public long lastModified() throws IOException {
        return lastModified;
    }

    public final byte[] getByteArray() {
        return this.byteArray;
    }

    public boolean exists() {
        return true;
    }

    public long contentLength() {
        return (long) this.byteArray.length;
    }

    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.byteArray);
    }

    public String getDescription() {
        return "Byte array resource [" + this.description + "]";
    }

    public boolean equals(@Nullable Object other) {
        return this == other || other instanceof CssResource && Arrays.equals(
                ((CssResource) other).byteArray, this.byteArray);
    }

    public int hashCode() {
        return Arrays.hashCode(this.byteArray);
    }


}