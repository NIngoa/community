package com.nowcoder.community;

import java.io.IOException;

public class WkTest {
    public static void main(String[] args) {
        String cmd="d:/wkhtmltopdf/bin/wkhtmltoimage --quality 75 https://www.nowcoder.com d:/work/data/wk-images/3.png";
        try {
            Runtime.getRuntime().exec(cmd);
            System.out.println(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
